package com.github.database.rider.springboot4.repository;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Repository;

import com.github.database.rider.springboot4.entity.company.Company;
import com.github.database.rider.springboot4.entity.company.CompanyExample;
import com.github.database.rider.springboot4.mapper.company.CompanyMapper;

@Repository
public class CompanyRepository {

	private final CompanyMapper companyMapper;

	public CompanyRepository(CompanyMapper companyMapper) {
		this.companyMapper = companyMapper;
	}

	public List<Company> findAll() {
        return this.companyMapper.selectByExample(null);
    }

	public int delete(List<Long> ids) {
		CompanyExample companyExample = new CompanyExample();
		companyExample.createCriteria().andIdIn(ids);
		return this.companyMapper.deleteByExample(companyExample);
	}

	public long count() {
		return this.companyMapper.countByExample(null);
	}

	public Company findByNameLike(String name) {
		CompanyExample companyExample = new CompanyExample();
		companyExample.createCriteria().andNameLike(name);
		return this.companyMapper.selectByExample(companyExample).stream().findFirst().orElse(null);
	}
	
	public boolean deleteById(Long id) {
		return this.companyMapper.deleteByPrimaryKey(id) > 0;
	}

	public boolean save(Company company) {
		List<Company> companyList = this.findAll();
		long maxId = 0;
		for (Company c : companyList) {
			if (c.getId() > maxId) {
				maxId = c.getId();
			}
			if (c.getName().equals(company.getName())) {
				company.setId(c.getId());
			}
		}
		if (Objects.nonNull(company.getId())) {
			return this.companyMapper.updateByPrimaryKey(company) > 0;
		} else {
			company.setId(maxId + 1);
			return this.companyMapper.insert(company) > 0;
		}
	}
}
