package com.github.database.rider.core.bdd;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * Created by rmpestano on 4/17/16.
 */
@RunWith(Cucumber.class)
@CucumberOptions(features = {
        "src/test/resources/features/seeding/seeding-database.feature",
        "src/test/resources/features/format/dataset-format.feature",
        "src/test/resources/features/configuration/dataset-configuration.feature",
        "src/test/resources/features/assertion/dataset-assertion.feature",
        "src/test/resources/features/scriptable/scriptable-dataset.feature",
        "src/test/resources/features/leak/leak-hunter.feature",
        "src/test/resources/features/export/dataset-export.feature",
        "src/test/resources/features/metadataset/metadataset.feature"
},
        plugin = "json:target/dbunit-rules.json")
public class DatabaseRiderBdd {
}
