package com.github.database.rider.core.exporter;

import com.github.database.rider.core.api.dataset.DataSetFormat;
import com.github.database.rider.core.api.exporter.DataSetExportConfig;
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

import static com.github.database.rider.core.util.EntityManagerProvider.newInstance;
import static com.github.database.rider.core.util.EntityManagerProvider.tx;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

/**
 * Created by rafael-pestano on 28/09/2016.
 */

@RunWith(JUnit4.class)
public class ExportNullPropertiesIt {

    private static final String NEW_LINE = System.getProperty("line.separator");
    private static EntityManagerProvider emProvider;


    @BeforeClass
    public static void setup(){
        emProvider = newInstance("rules-it");
        tx().begin();
        User u1 = new User();
        u1.setName(null);
        EntityManagerProvider.em().persist(u1);
        tx().commit();
    }

    @Test
    public void shouldNotExportNullColumnsInYMLDataSet() throws SQLException, DatabaseUnitException {
        DataSetExporter.getInstance().export(new DatabaseConnection(emProvider.connection()), new DataSetExportConfig().outputFileName("target/userWithNullProperty.yml"));
        File ymlDataSet = new File("target/userWithNullProperty.yml");
        assertThat(ymlDataSet).exists();
        assertThat(contentOf(ymlDataSet)).
                contains("FOLLOWER:" + NEW_LINE +
                                "" + NEW_LINE +
                                "SEQUENCE:" + NEW_LINE +
                                "  - SEQ_NAME: \"SEQ_GEN\"" + NEW_LINE +
                                "    SEQ_COUNT: 50" + NEW_LINE +
                                "" + NEW_LINE +
                                "TWEET:" + NEW_LINE +
                                "" + NEW_LINE +
                                "USER:" + NEW_LINE +
                                "  - ID: 1" + NEW_LINE + NEW_LINE
                );
    }

    @Test
    public void shouldNotExportNullColumnsInXMLDataSet() throws SQLException, DatabaseUnitException{
        DataSetExporter.getInstance().export(new DatabaseConnection(emProvider.connection()), new DataSetExportConfig().
                dataSetFormat(DataSetFormat.XML).outputFileName("target/userWithNullProperty.xml"));
        File xmlDataSet = new File("target/userWithNullProperty.xml");
        assertThat(xmlDataSet).exists();
        assertThat(contentOf(xmlDataSet).replaceAll("\r","")).isEqualTo(("<?xml version='1.0' encoding='UTF-8'?>" + NEW_LINE +
                "<dataset>" + NEW_LINE +
                "  <FOLLOWER/>" + NEW_LINE +
                "  <SEQUENCE SEQ_NAME=\"SEQ_GEN\" SEQ_COUNT=\"50\"/>" + NEW_LINE +
                "  <TWEET/>" + NEW_LINE +
                "  <USER ID=\"1\"/>" + NEW_LINE +
                "</dataset>" + NEW_LINE).replaceAll("\r", ""));

    }


    @Test
    public void shouldNotExportNullColumnsInJSONDataSet() throws SQLException, DatabaseUnitException{
        DataSetExporter.getInstance().export(new DatabaseConnection(emProvider.connection()), new DataSetExportConfig().
                dataSetFormat(DataSetFormat.JSON).outputFileName("target/userWithNullProperty.json"));
        File xmlDataSet = new File("target/userWithNullProperty.json");
        assertThat(xmlDataSet).exists();
        assertThat(contentOf(xmlDataSet).replaceAll("\r","")).isEqualTo(("{"+NEW_LINE +
                "  \"FOLLOWER\": ["+NEW_LINE +
                "  ],"+NEW_LINE +
                "  \"SEQUENCE\": ["+NEW_LINE +
                "    {"+NEW_LINE +
                "      \"SEQ_NAME\": \"SEQ_GEN\","+NEW_LINE +
                "      \"SEQ_COUNT\": 50"+NEW_LINE +
                "    }"+NEW_LINE +
                "  ],"+NEW_LINE +
                "  \"TWEET\": ["+NEW_LINE +
                "  ],"+NEW_LINE +
                "  \"USER\": ["+NEW_LINE +
                "    {"+NEW_LINE +
                "      \"ID\": 1,"+NEW_LINE +
                "    }"+NEW_LINE +
                "  ]"+NEW_LINE +
                "}").replaceAll("\r",""));

    }
}
