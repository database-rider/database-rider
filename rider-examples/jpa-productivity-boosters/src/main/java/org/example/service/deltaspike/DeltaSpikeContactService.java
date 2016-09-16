package org.example.service.deltaspike;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.example.jpadomain.Company;
import org.example.jpadomain.Contact;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

/**
 * EJB to hide JPA related stuff from the UI layer.
 */
@Stateless
public class DeltaSpikeContactService {

    @Inject
    ContactRepository repository;

    @Inject
    CompanyRepository companyRepository;

    public DeltaSpikeContactService() {
    }

    @Transactional
    public void save(Contact entry) {
        repository.save(entry);
    }

    @Transactional
    public void delete(Contact value) {
        repository.remove(value);
    }

    @Transactional
    public void removeCompany(Company company) {
        companyRepository.remove(company);
    }

    public List<Contact> findByCompanyAndName(Company company, String filter) {
        return repository.findByCompanyAndNameLikeIgnoreCase(company,
                "%" + filter + "%").getResultList();
    }

    public List<Contact> findPageByCompanyAndName(Company company, String filter,
                                                  int firstrow, int maxrows) {
        return repository.findByCompanyAndNameLikeIgnoreCase(company,
                "%" + filter + "%").firstResult(firstrow).maxResults(maxrows).
                getResultList();
    }

    public Long countByCompanyAndName(Company company, String filter) {
        return repository.findByCompanyAndNameLikeIgnoreCase(company,
                "%" + filter + "%").count();
    }

    public List<Company> findCompanies() {
        return companyRepository.findAll();
    }


    public Contact refreshEntry(Contact entry) {
        return repository.findBy(entry.getId());
    }

}
