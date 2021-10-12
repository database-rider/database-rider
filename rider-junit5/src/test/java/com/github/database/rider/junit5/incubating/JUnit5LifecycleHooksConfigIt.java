package com.github.database.rider.junit5.incubating;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.junit5.model.Tweet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DBRider
@RunWith(JUnitPlatform.class)
public class JUnit5LifecycleHooksConfigIt {

    private static File customConfigFile = new File("target/test-classes/dbunit.yml");

    @BeforeAll
    public static void setUpConfig() throws IOException {
        // Initialize database
        EntityManagerProvider.instance("junit5-pu");
        // Put global config file into place
        copyResourceToFile("/config/sample-dbunit.yml", customConfigFile);
    }

    @AfterAll
    public static void deleteConfigFile() {
        customConfigFile.delete();
    }

    private static void copyResourceToFile(String resourceName, File to) throws IOException {
        try (InputStream from = JUnit5LifecycleHooksConfigIt.class.getResourceAsStream(resourceName)) {
            Files.copy(from, to.toPath());
        }
    }

    @BeforeEach
    @DataSet(value = "tweetBeforeEach.yml", disableConstraints = true)
    public void shouldLoadDBUnitConfigViaCustomGlobalFile() {
        List<Tweet> tweets = EntityManagerProvider.em().createQuery("select t from Tweet t").getResultList();
        assertThat(tweets).isNotNull()
                .hasSize(1)
                .extracting("content")
                .contains("tweet before each!");
    }

    @Test
    public void dummyTest() {
    }

}
