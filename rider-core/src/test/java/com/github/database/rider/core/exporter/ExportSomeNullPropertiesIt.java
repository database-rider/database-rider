package com.github.database.rider.core.exporter;

import com.github.database.rider.core.api.dataset.DataSetFormat;
import com.github.database.rider.core.api.exporter.DataSetExportConfig;
import com.github.database.rider.core.model.Tweet;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.github.database.rider.core.util.EntityManagerProvider.newInstance;
import static com.github.database.rider.core.util.EntityManagerProvider.tx;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

/**
 *  Created by TonnTamerlan on 26/11/2019.
 */

@RunWith(JUnit4.class)
public class ExportSomeNullPropertiesIt {

  private static final String NEW_LINE = System.getProperty("line.separator");
  private static EntityManagerProvider emProvider;


  @BeforeClass
  public static void setup() {
    emProvider = newInstance("rules-it");
    tx().begin();
    User u2 = new User();
    u2.setId(1);
    u2.setName(null);
    List<Tweet> tweets = new ArrayList<>();
    Tweet tweet = new Tweet();
    tweet.setContent("tweet_content");
    tweet.setLikes(3);
    EntityManagerProvider.em().persist(tweet);
    tweets.add(tweet);
    u2.setTweets(tweets);
    EntityManagerProvider.em().persist(u2);
    tx().commit();
  }

  @Test
  public void shouldNotExportNullColumnsInJSONDataSet() throws SQLException, DatabaseUnitException {
    DataSetExporter.getInstance().export(new DatabaseConnection(emProvider.connection()), new DataSetExportConfig().
      dataSetFormat(DataSetFormat.JSON).outputFileName("target/userWithNullProperty.json"));
    File jsonDataSet = new File("target/userWithNullProperty.json");
    assertThat(jsonDataSet).exists();
    assertThat(contentOf(jsonDataSet).replaceAll("\r", "")).isEqualTo(("{" + NEW_LINE +
      "  \"FOLLOWER\": [" + NEW_LINE +
      "  ]," + NEW_LINE +
      "  \"SEQUENCE\": [" + NEW_LINE +
      "    {" + NEW_LINE +
      "      \"SEQ_NAME\": \"SEQ_GEN\"," + NEW_LINE +
      "      \"SEQ_COUNT\": 50" + NEW_LINE +
      "    }" + NEW_LINE +
      "  ]," + NEW_LINE +
      "  \"TWEET\": [" + NEW_LINE +
      "    {" + NEW_LINE +
      "      \"ID\": \"1\"," + NEW_LINE +
      "      \"CONTENT\": \"tweet_content\"," + NEW_LINE +
      "      \"LIKES\": 3" + NEW_LINE +
      "    }" + NEW_LINE +
      "  ]," + NEW_LINE +
      "  \"USER\": [" + NEW_LINE +
      "    {" + NEW_LINE +
      "      \"ID\": 1" + NEW_LINE +
      "    }" + NEW_LINE +
      "  ]" + NEW_LINE +
      "}").replaceAll("\r", ""));

  }
}
