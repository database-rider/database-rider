package com.github.database.rider.core.bdd;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import static org.junit.Assert.assertNotNull;

/**
 * Created by rafael-pestano on 16/06/2016.
 */
public class GeneralSteps {


    @Given("^Groovy script engine is on test classpath$")
    public void Groovy_script_engine_is_on_test_classpath(String docstring) throws Throwable {
        // Express the Regexp above with the code you wish you had
        assertNotNull(docstring);
    }

    @Then("^Dataset script should be interpreted while seeding the database$")
    public void Dataset_script_should_be_interpreted_when_seeding_the_database() throws Throwable {
        // Express the Regexp above with the code you wish you had
    }

    @Then("^Test must pass because database state is as in expected dataset.$")
    public void Test_must_pass_because_dataBase_state_is_as_expected_in_dataset() throws Throwable {
        // Express the Regexp above with the code you wish you had
    }


    @Then("^Test must fail with following error:$")
    public void Test_must_fail_with_following_error(String docstring) throws Throwable {
        assertNotNull(docstring);
    }

    @Then("^Test must pass because inserted users are commited to database and database state matches expected dataset.$")
    public void Test_must_pass_because_inserted_users_are_commited_to_database_and_database_state_matches_in_expected_dataset() throws Throwable {
    }

	@Then("^Test must use dataset declared in `MetaDataSet` annotation\\.$")
	public void testMustUseDatasetDeclaredInMetaDatasetAnnotation() throws Throwable {
		 
	}

	@Given("^The following metataset annotation$")
	public void theFollowingMetatasetAnnotation(String arg1) throws Throwable {
	}

	@Then("^Test must use dataset declared in `AnotherMetaDataSet` annotation\\.$")
	public void testMustUseDatasetDeclaredInAnotherMetaDatasetAnnotation() throws Throwable {
	}
}
