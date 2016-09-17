package com.github.database.rider.core.examples;

import com.github.database.rider.core.DBUnitRule;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.example.jpadomain.Company;
import org.example.jpadomain.Contact;
import org.example.service.deltaspike.CompanyRepository;
import org.example.service.deltaspike.DeltaSpikeContactService;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.sql.Connection;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * Created by pestano on 23/07/15.
 */

@RunWith(CdiTestRunner.class)
public class DeltaspikeIt {

    @Inject
    EntityManager entityManager;

    @Inject
    DeltaSpikeContactService contactService;

    @Inject
    CompanyRepository companyRepository;

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(new ConnectionHolder() {
        @Override
        public Connection getConnection() {
            return createConnection();
        }
    });

    private Connection createConnection() {
        /* eclipselink
       entityManager.getTransaction().begin();
        Connection connection = entityManager.unwrap(java.sql.Connection.class);
        entityManager.getTransaction().commit();*/
        Connection connection = ((SessionImpl) entityManager.unwrap(Session.class)).connection();
        assertNotNull(connection);
        return connection;

    }

    @Test
    @DataSet("datasets/contacts.yml")
    public void shouldQueryAllCompanies() {
        assertNotNull(contactService);
        assertThat(contactService.findCompanies()).hasSize(4);
    }

    @Test
    @DataSet("datasets/contacts.yml")
    public void shouldQueryAllCompaniesUsingInterceptor() {
        assertNotNull(contactService);
        assertThat(contactService.findCompanies()).hasSize(4);
    }


    @Test
    @DataSet("datasets/contacts.json")
    public void shouldQueryAllContactsUsingJsonDataSet() {
        assertThat(companyRepository.count()).isEqualTo(4);
    }

    @Test
    @DataSet("datasets/contacts.yml")
    public void shouldFindCompanyByName() {
        Company expectedCompany = new Company("Google");
        assertNotNull(companyRepository);
        assertThat(companyRepository.findByName("Google")).
                isNotNull().usingElementComparator(new Comparator<Company>() {
            @Override
            public int compare(Company o1, Company o2) {
                return o1.getName().compareTo(o2.getName());
            }
        }).contains(expectedCompany);
    }

    @Test
    @DataSet("contacts.yml")
    public void shouldFindCompanyByNameUsingInterceptor() {
        Company expectedCompany = new Company("Google");
        assertNotNull(companyRepository);
        assertThat(companyRepository.findByName("Google")).
                isNotNull().usingElementComparator(new Comparator<Company>() {
            @Override
            public int compare(Company o1, Company o2) {
                return o1.getName().compareTo(o2.getName());
            }
        }).contains(expectedCompany);
    }


    @Test
    @DataSet(value = "datasets/contacts.yml")
    public void shouldCreateCompany() {
        assertThat(companyRepository.count()).isEqualTo(4);
        Company company = new Company("test company");
        beginTx();
        Company companyCreated = companyRepository.save(company);
        assertThat(companyCreated.id).isNotNull();
        commitTx();
        assertThat(companyRepository.count()).isEqualTo(5);
    }

    @Test
    @DataSet(value = "datasets/contacts.yml")
    public void shouldCreateContact() {
        Company google = companyRepository.findByName("Google").get(0);
        assertThat(contactService.countByCompanyAndName(google, "rmpestano")).isEqualTo(0);
        Contact rmpestano = new Contact("rmpestano", null, "rmpestano@gmail.com", google);
        contactService.save(rmpestano);
        assertThat(rmpestano.id).isNotNull();
        assertThat(contactService.countByCompanyAndName(google, "rmpestano")).isEqualTo(1);
    }

    @Test
    @DataSet(value = "datasets/contacts.yml")
    public void shouldDeleteContact() {
        Company pivotal = companyRepository.findByName("Pivotal").get(0);
        assertThat(contactService.countByCompanyAndName(pivotal, "Spring")).
                isEqualTo(1);
        Contact spring = contactService.findByCompanyAndName(pivotal, "Spring").get(0);
        contactService.delete(spring);
        assertThat(contactService.countByCompanyAndName(pivotal, "Spring")).
                isEqualTo(0);
    }


    private void beginTx() {
        entityManager.getTransaction().begin();
    }

    private void commitTx() {
        entityManager.getTransaction().commit();
    }
}
