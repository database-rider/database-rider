package com.github.database.rider.core.examples.cucumber; //<1>

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.cdi.api.DBUnitInterceptor;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.example.jpadomain.Contact;
import org.example.jpadomain.Contact_;
import org.example.service.deltaspike.ContactRepository;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@DBUnitInterceptor
public class ContactSteps {

    @Inject
    ContactRepository contactRepository; //<1>

    Long count;

    @When("^^we search contacts by name \"([^\"]*)\"$")
    public void we_search_contacts_by_name_(String name) throws Throwable {
        Contact contact = new Contact();
        contact.setName(name);
        count = contactRepository.countLike(contact, Contact_.name);
    }


    @Then("^we should find (\\d+) contacts$")
    public void we_should_find_result_contacts(Long result) throws Throwable {
        assertEquals(result, count);
    }

    @Given("^we have a list of contacts$")
    @DataSet("datasets/contacts.yml") //<2>
    public void given() {
        assertEquals(contactRepository.count(), new Long(3));
    }

    @When("^we delete contact by id (\\d+)$")
    public void we_delete_contact_by_id(long id) throws Throwable {
        contactRepository.remove(contactRepository.findBy(id));
    }

    @Then("^we should not find contact (\\d+)$")
    public void we_should_not_find_contacts_in_database(long id) throws Throwable {
        assertNull(contactRepository.findBy(id));
    }
}
