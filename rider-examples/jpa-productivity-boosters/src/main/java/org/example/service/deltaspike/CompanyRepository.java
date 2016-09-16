package org.example.service.deltaspike;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import org.example.jpadomain.Company;

import java.util.List;

@Repository(forEntity = Company.class)
public interface CompanyRepository extends EntityRepository<Company, Long> {

    List<Company> findByName(String name);

}
