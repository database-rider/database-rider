package com.github.database.rider.bdd.cdi;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by rafael-pestano on 09/10/2015.
 */
public class CDISeedDatabaseStepdefs {


    @Given("^DBUnit interceptor is enabled in your test beans.xml:$")
    public void DBUnit_interceptor_is_enabled_in_your_test_beans_xml(String docstring) throws Throwable {
        assertTrue(true);
    }

    @And("^The following dataset$")
    public void The_following_dataset(String docstring) throws Throwable {
        assertNotNull(docstring);
    }

    @When("^The following test is executed:$")
    public void The_test_below_is_executed(String docstring) throws Throwable {
        assertNotNull(docstring);
    }

    @Then("^The database should be seeded with the dataset content before test execution$")
    public void The_database_should_be_seeded_with_the_dataset_content() throws Throwable {

    }

    /*  Use database seeding in cucumber on cucumber feature

    @Then("^The database should be seeded with the dataset content before test execution$")
    @DataSet("yml/users.yml")
    public void The_database_should_be_seeded_with_the_dataset_content() throws Throwable {
        List<User> users = em.createQuery("select u from User u order by u.id asc").getResultList();
        User user1 = new User(1);
        User user2 = new User(2);
        Tweet tweetUser1 = new Tweet();
        tweetUser1.setId("abcdef12345");
        Tweet tweetUser2 = new Tweet();
        tweetUser2.setId("abcdef12233");
        assertThat(users).isNotNull().hasSize(2).contains(user1, user2);
        List<Tweet> tweetsUser1 = users.get(0).getTweets();
        assertThat(tweetsUser1).isNotNull().hasSize(1).contains(tweetUser1);

        List<Tweet> tweetsUser2 = users.get(1).getTweets();
        assertThat(tweetsUser2).isNotNull().hasSize(2).contains(tweetUser2);

    }

     */


}
