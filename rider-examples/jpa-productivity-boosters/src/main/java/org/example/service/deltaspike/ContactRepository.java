package org.example.service.deltaspike;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.api.Repository;
import org.example.jpadomain.Company;
import org.example.jpadomain.Contact;

import java.util.List;

@Repository(forEntity = Contact.class)
public interface ContactRepository extends EntityRepository<Contact, Long> {

    public List<Contact> findByCompany(Company company);

    public QueryResult findByCompanyAndNameLikeIgnoreCase(Company company,
                                                          String string);
}
