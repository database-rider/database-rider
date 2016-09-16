package com.github.database.rider.examples.cucumber.withoutcdi;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;


@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"src/test/resources/features/contacts-without-cdi.feature"},
        plugin = {"json:target/cucumber.json"}
        //glue = "com.github.dbunit.rules.examples.glues"
)
public class ContactFeatureWithoutCDI {
}
