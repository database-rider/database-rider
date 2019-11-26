package com.github.database.rider.core.exporter;

import com.github.database.rider.core.api.dataset.DataSetFormat;
import com.github.database.rider.core.api.exporter.DataSetExportConfig;
import com.github.database.rider.core.model.Tweet;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.sql.SQLException;

import static com.github.database.rider.core.util.EntityManagerProvider.newInstance;
import static com.github.database.rider.core.util.EntityManagerProvider.tx;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

/**
 * Created by rafael-pestano on 28/09/2016.
 */

@RunWith(JUnit4.class)
public class ExportStringPropertyWithNewLineSymbolIt {

  private static final String NEW_LINE = System.getProperty("line.separator");
  private static EntityManagerProvider emProvider;


  @BeforeClass
  public static void setup() {
    emProvider = newInstance("rules-it");
    tx().begin();
    Tweet tweet = new Tweet();
    tweet.setContent("first_line\nsecond_line\nthird_line");
    tweet.setLikes(3);
    EntityManagerProvider.em().persist(tweet);
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
      "      \"CONTENT\": \"first_line\\nsecond_line\\nthird_line\"," + NEW_LINE +
      "      \"LIKES\": 3" + NEW_LINE +
      "    }" + NEW_LINE +
      "  ]," + NEW_LINE +
      "  \"USER\": [" + NEW_LINE +
      "  ]" + NEW_LINE +
      "}").replaceAll("\r", ""));

  }
}
