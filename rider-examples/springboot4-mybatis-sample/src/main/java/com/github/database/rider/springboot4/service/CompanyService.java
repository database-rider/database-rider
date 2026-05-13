package com.github.database.rider.springboot4.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.database.rider.springboot4.entity.company.Company;
import com.github.database.rider.springboot4.repository.CompanyRepository;

@Service
public class CompanyService {

	private final CompanyRepository companyRepository;

	public CompanyService(CompanyRepository companyRepository) {
		this.companyRepository = companyRepository;
	}

	@Transactional
	public List<Company> findAll() {
		return this.companyRepository.findAll();
	}

	@Transactional
	public int delete(List<Long> ids) {
		return this.companyRepository.delete(ids);
	}

	@Transactional
	public long count() {
		return this.companyRepository.count();
	}

	@Transactional
	public Company findByNameLike(String name) {
		return this.companyRepository.findByNameLike(name);
	}

	@Transactional
	public boolean deleteById(Long id) {
		return this.companyRepository.deleteById(id);
	}

	@Transactional
	public boolean save(Company company) {
		return this.companyRepository.save(company);
	}
}