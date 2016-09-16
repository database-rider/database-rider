package org.example.service.springdata;

import org.example.jpadomain.Company;
import org.example.jpadomain.Contact;
import org.springframework.data.domain.PageRequest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

/**
 * Spring Data can be used with Java EE projects as well. Just need to expose
 * EntityManager to CDI (see CDIConfig class) and tackle transactions (here with
 * stateless EJB).
 */
@Stateless
public class SpringDataContactService {

    @Inject
    ContactRepository repository;

    @Inject
    CompanyRepository companyRepository;

    public SpringDataContactService() {
    }

    public void save(Contact entry) {
        repository.save(entry);
    }

    public void delete(Contact value) {
        repository.delete(value);
    }

    public List<Contact> findByCompanyAndName(Company company, String filter) {
        return repository.findByCompanyAndNameLikeIgnoreCase(company,
                filter + "%");
    }

    public List<Contact> findPageByCompanyAndName(Company company, String filter,
                                                  int firstrow, int maxrows) {
        return repository.findByCompanyAndNameLikeIgnoreCase(company,
                filter + "%",
                new PageRequest(firstrow / maxrows, maxrows))
                .getContent();
    }

    public Long countByCompanyAndName(Company company, String filter) {
        return repository.countByCompanyAndNameLikeIgnoreCase(company,
                filter + "%");
    }

    public List<Company> findCompanies() {
        return companyRepository.findAll();
    }

    public Contact refreshEntry(Contact entry) {
        return repository.findOne(entry.getId());
    }

}
