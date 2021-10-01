package com.github.database.rider.junit5;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetFormat;
import com.github.database.rider.core.api.exporter.ExportDataSet;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.junit5.incubating.DBUnitExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

/**
 * Created by pestano on 23/07/15.
 */

@ExtendWith(DBUnitExtension.class)
@RunWith(JUnitPlatform.class)
public class ExportDataSetJUnit5It {

    private static final String NEW_LINE = System.getProperty("line.separator");

    private ConnectionHolder connectionHolder = () -> //<3>
            EntityManagerProvider.instance("junit5-pu").connection();//<4>

    @Test
    @DataSet(value = "users.yml", cleanBefore = true)
    @ExportDataSet(format = DataSetFormat.XML, outputName = "target/exported/xml/allTables.xml")
    public void shouldExportAllTablesInXMLFormat() {
    }

    @Test
    @DataSet(value = "users.yml", cleanBefore = true)
    @ExportDataSet(format = DataSetFormat.XML_DTD, outputName = "target/exported/xml_dtd/allTables.xml")
    public void shouldExportAllTablesInXMLAndDTDFormat() {
    }

    @Test
    @DataSet(value = "users.yml", cleanBefore = true)
    @ExportDataSet(format = DataSetFormat.YML, outputName = "target/exported/yml/allTables")
    public void shouldExportAllTablesInYMLFormatOmmitingExtension() {
    }


    @AfterAll
    public static void assertGeneratedDataSets() {
        //XML generator
        assertXMLFileContent("target/exported/xml/allTables.xml");

        //xmlDataSetWithAllTables.delete();

        //YML generator
        File ymlDataSetWithAllTables = new File("target/exported/yml/allTables.yml");
        assertThat(ymlDataSetWithAllTables).exists();
        assertThat(contentOf(ymlDataSetWithAllTables)).
                contains("USER:" + NEW_LINE +
                        "  - ID: 1" + NEW_LINE +
                        "    NAME: \"@realpestano\"" + NEW_LINE +
                        "  - ID: 2" + NEW_LINE +
                        "    NAME: \"@dbunit\"");

        //XML + DTD generator
        assertXMLFileContent("target/exported/xml_dtd/allTables.xml");

        File dtdDataSetWithAllTables = new File("target/exported/xml_dtd/allTables.dtd");
        assertThat(dtdDataSetWithAllTables).exists();
        assertThat(contentOf(dtdDataSetWithAllTables)).
                contains("<!ELEMENT dataset (\n" +
                        "    TWEET*,\n" +
                        "    USER*)>\n" +
                        "\n" +
                        "<!ELEMENT TWEET EMPTY>\n" +
                        "<!ATTLIST TWEET\n" +
                        "    ID CDATA #REQUIRED\n" +
                        "    CONTENT CDATA #IMPLIED\n" +
                        "    DATE CDATA #IMPLIED\n" +
                        "    LIKES CDATA #IMPLIED\n" +
                        "    USER_ID CDATA #IMPLIED\n" +
                        ">\n" +
                        "\n" +
                        "<!ELEMENT USER EMPTY>\n" +
                        "<!ATTLIST USER\n" +
                        "    ID CDATA #REQUIRED\n" +
                        "    NAME CDATA #IMPLIED\n" +
                        ">\n");
    }

    private static void assertXMLFileContent(String filename) {
        File xmlDataSetWithAllTables = new File(filename);

        assertThat(xmlDataSetWithAllTables).exists();
        assertThat(contentOf(xmlDataSetWithAllTables)).contains("<USER ID=\"1\" NAME=\"@realpestano\"/>")
                .contains("<USER ID=\"2\" NAME=\"@dbunit\"/>");
    }
}