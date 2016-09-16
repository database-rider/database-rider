package com.github.database.rider.examples.cucumber;

import com.github.database.rider.cucumber.CdiCucumberTestRunner;
import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;


@RunWith(CdiCucumberTestRunner.class)
@CucumberOptions(
        features = {"src/test/resources/features/contacts.feature"},
        plugin = {"json:target/cucumber.json"}
        //glue = "com.github.dbunit.rules.examples.glues"
)
public class ContactFeature {
}
