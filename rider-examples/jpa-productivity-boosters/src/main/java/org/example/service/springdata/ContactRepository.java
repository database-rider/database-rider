package org.example.service.springdata;

import org.example.jpadomain.Company;
import org.example.jpadomain.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long>,
        QueryDslPredicateExecutor {

    public Page<Contact> findByCompanyAndNameLikeIgnoreCase(Company company,
                                                            String filter, Pageable pageable);

    public Long countByCompanyAndNameLikeIgnoreCase(Company company,
                                                    String string);

    public List<Contact> findByCompanyAndNameLikeIgnoreCase(Company company,
                                                            String string);
}
