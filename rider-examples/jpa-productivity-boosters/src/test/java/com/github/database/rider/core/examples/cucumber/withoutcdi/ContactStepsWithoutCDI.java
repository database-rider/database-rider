package com.github.database.rider.core.examples.cucumber.withoutcdi;

import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.connection.ConnectionHolderImpl;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.example.jpadomain.Contact;
import org.junit.Assert;

import javax.persistence.Query;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static com.github.database.rider.core.util.EntityManagerProvider.tx;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ContactStepsWithoutCDI {


    EntityManagerProvider entityManagerProvider = EntityManagerProvider.newInstance("customerTestDB");

    DataSetExecutor dbunitExecutor;

    Long count;


    @Before
    public void setUp() {
        dbunitExecutor = DataSetExecutorImpl.instance(new ConnectionHolderImpl(entityManagerProvider.connection()));
        EntityManagerProvider.em().clear();//important to clear JPA first level cache between scenarios
    }


    @Given("^we have a list of contacts2$")
    public void given() {
        dbunitExecutor.createDataSet(new DataSetConfig("contacts.yml"));
        Assert.assertEquals(EntityManagerProvider.em().createQuery("select count(c.id) from Contact c").getSingleResult(), new Long(3));
    }

    @When("^^we search contacts by name \"([^\"]*)\"2$")
    public void we_search_contacts_by_name_(String name) throws Throwable {
        Contact contact = new Contact();
        contact.setName(name);
        Query query = EntityManagerProvider.em().createQuery("select count(c.id) from Contact c where UPPER(c.name) like :name");
        query.setParameter("name", "%" + name.toUpperCase() + "%");
        count = (Long) query.getSingleResult();
    }


    @Then("^we should find (\\d+) contacts2$")
    public void we_should_find_result_contacts(Long result) throws Throwable {
        assertEquals(result, count);
    }


    @When("^we delete contact by id (\\d+) 2$")
    public void we_delete_contact_by_id(long id) throws Throwable {
        EntityManagerProvider.tx().begin();
        EntityManagerProvider.em().remove(EntityManagerProvider.em().find(Contact.class, id));
        EntityManagerProvider.tx().commit();
    }

    @Then("^we should not find contact (\\d+) 2$")
    public void we_should_not_find_contacts_in_database(long id) throws Throwable {
        assertNull(EntityManagerProvider.em().find(Contact.class, id));
    }
}
