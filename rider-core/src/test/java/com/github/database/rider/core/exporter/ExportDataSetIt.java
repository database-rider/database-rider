package com.github.database.rider.core.exporter;

import com.github.database.rider.core.DBUnitRule;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetFormat;
import com.github.database.rider.core.api.exporter.BuilderType;
import com.github.database.rider.core.api.exporter.DataSetExportConfig;
import com.github.database.rider.core.api.exporter.ExportDataSet;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;

import static com.github.database.rider.core.util.EntityManagerProvider.tx;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

/**
 * Created by pestano on 11/09/16.
 */
@RunWith(JUnit4.class)
public class ExportDataSetIt {

    private static final String NEW_LINE = System.getProperty("line.separator");

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection());


//tag::export-annotation[]
    @Test
    @DataSet("datasets/yml/users.yml") //<1>
    @ExportDataSet(format = DataSetFormat.XML, outputName = "target/exported/xml/allTables.xml")
    public void shouldExportAllTablesInXMLFormat() {
    }

//end::export-annotation[]

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.XML_DTD, outputName = "target/exported/xml_dtd/allTables.xml")
    public void shouldExportAllTablesInXMLAndDTDFormat() {
    }
    
    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.YML, outputName = "target/exported/yml/allTables")
    public void shouldExportAllTablesInYMLFormatOmmitingExtension() {
    }

    @Test
    @ExportDataSet(outputName = "target/exported/yml/generatedWithoutDataSetAnnotation")
    public void shouldExportAllTablesInYMLFormatWithoutDataSetAnnotation() {
        //seed database
        DataSetExecutorImpl.getExecutorById(DataSetExecutorImpl.DEFAULT_EXECUTOR_ID)
                .createDataSet(new DataSetConfig("datasets/yml/users.yml"));
    }

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.XLS, outputName = "target/exported/xls/allTables.xls")
    public void shouldExportAllTablesInXLSFormat() {
    }

//tag::export-programmatically[]
    @Test
    @DataSet(cleanBefore = true)
    public void shouldExportYMLDataSetProgrammatically() throws SQLException, DatabaseUnitException {
        tx().begin();
        User u1 = new User();
        u1.setName("u1");
        EntityManagerProvider.em().persist(u1);
        tx().commit();
        DataSetExporter.getInstance().export(emProvider.connection(), new DataSetExportConfig().outputFileName("target/user.yml"));
        File ymlDataSet = new File("target/user.yml");
        assertThat(ymlDataSet).exists();
        assertThat(contentOf(ymlDataSet)).
                contains("USER:" + NEW_LINE +
                                "  - ID: 1" + NEW_LINE +
                                "    NAME: \"u1\"" + NEW_LINE
                );
    }

//end::export-programmatically[]

    @Test
    @DataSet(cleanBefore = true)
    public void shouldExportJSONDataSetProgrammatically() throws SQLException, DatabaseUnitException, FileNotFoundException {
        tx().begin();
        User u1 = new User();
        u1.setName("u1");
        EntityManagerProvider.em().persist(u1);
        tx().commit();
        DataSetExporter.getInstance().export(emProvider.connection(), new DataSetExportConfig().
                outputFileName("target/user.json").dataSetFormat(DataSetFormat.JSON));
        File jsonDataSet = new File("target/user.json");
        assertThat(jsonDataSet).exists();
        assertThat(contentOf(jsonDataSet)).contains("\"USER\": [" + NEW_LINE +
                "    {" + NEW_LINE +
                "      \"ID\": " + u1.getId() +
                "," + NEW_LINE +
                "      \"NAME\": \"u1\"" + NEW_LINE +
                "    }" + NEW_LINE +
                "  ]");
    }

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.CSV, outputName = "target/exported/csv/allTables.csv")
    public void shouldExportAllTablesInCSVFormat() {
    }

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.JSON, outputName = "target/exported/json/allTables.json")
    public void shouldExportAllTablesInJSONFormat() {
    }

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.XML, queryList = {"select * from USER u where u.ID = 1"}, outputName = "target/exported/xml/filtered.xml")
    public void shouldExportXMLDataSetUsingQueryToFilterRows() {

    }

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.XML_DTD, queryList = {"select * from USER u where u.ID = 1"}, outputName = "target/exported/xml_dtd/filtered.xml")
    public void shouldExportXMLAndDTDDataSetUsingQueryToFilterRows() {

    }
    
    @Test
    @DataSet("datasets/yml/users.yml")
    public void shouldExportDataSetUsingSubSelectToFilterRows() throws SQLException, DatabaseUnitException {
        DataSetExporter.getInstance().export(new DatabaseConnection(emProvider.connection()),
                new DataSetExportConfig().
                        outputFileName("target/querySubselect.yml").
                        //export only users that have tweets
                        queryList(new String[]{"select * from user u where u.id in" +
                                " (select t.user_id from tweet t)"})
        );
        File ymlDataSet = new File("target/querySubselect.yml");
        assertThat(ymlDataSet).exists();
        assertThat(contentOf(ymlDataSet)).
            isEqualTo("user:"+NEW_LINE +
                    "  - ID: 1"+NEW_LINE +
                    "    NAME: \"@realpestano\""+NEW_LINE+NEW_LINE);
    }


    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.YML, queryList = {"select * from USER u where u.ID = 1"}, outputName = "target/exported/yml/filtered.yml")
    public void shouldExportYMLDataSetUsingQueryToFilterRows() {

    }

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.XML, queryList = {"select * from USER u where u.ID = 1"}, includeTables = {"TWEET"}, outputName = "target/exported/xml/filteredIncludes.xml")
    public void shouldExportXMLDataSetUsingQueryAndIncludesToFilterRows() {

    }

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.XML_DTD, queryList = {"select * from USER u where u.ID = 1"}, includeTables = {"TWEET"}, outputName = "target/exported/xml_dtd/filteredIncludes.xml")
    public void shouldExportXMLAndDTDDataSetUsingQueryAndIncludesToFilterRows() {

    }
    
    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.YML, queryList = {"select * from USER u where u.ID = 1"}, includeTables = "TWEET", outputName = "target/exported/yml/filteredIncludes.yml")
    public void shouldExportYMLDataSetUsingQueryAndIncludesToFilterRows() {

    }

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.XML, includeTables = "USER", outputName = "target/exported/xml/includes.xml")
    public void shouldExportXMLDataSetWithTablesInIncludes() {

    }

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.XML_DTD, includeTables = "USER", outputName = "target/exported/xml_dtd/includes.xml")
    public void shouldExportXMLAndDTDDataSetWithTablesInIncludes() {

    }
    
    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.YML, includeTables = "USER", outputName = "target/exported/yml/includes.yml")
    public void shouldExportYMLDataSetWithTablesInIncludes() {

    }

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.XML, includeTables = "USER", dependentTables = true, outputName = "target/exported/xml/dependentTables.xml")
    public void shouldExportXMLDataSetUsingIncludesWithDependentTables() {

    }

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.XML_DTD, includeTables = "USER", dependentTables = true, outputName = "target/exported/xml_dtd/dependentTables.xml")
    public void shouldExportXMLAndDTDDataSetUsingIncludesWithDependentTables() {

    }
    
    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.YML, includeTables = {"USER", "TWEET"}, dependentTables = true, outputName = "target/exported/yml/dependentTables.yml")
    public void shouldExportYMLDataSetUsingIncludesWithDependentTables() {

    }

    @Test
    @DataSet("datasets/yml/users.yml") //<1>
    @ExportDataSet(format = DataSetFormat.XML, outputName = "target/exported/xml/AllTables.xml", builderType = BuilderType.DEFAULT)
    public void shouldExportDataSetAsBuilderInDefaultSyntax() {
    }

    @Test
    @DataSet("datasets/yml/users.yml") //<1>
    @ExportDataSet(format = DataSetFormat.XML, outputName = "target/exported/xml/AllTables2.xml", builderType = BuilderType.COLUMNS_VALUES)
    public void shouldExportDataSetAsBuilderInColumnValuesSyntax() {
    }

    @AfterClass
    public static void assertGeneratedDataSets() {
    		assertXMLFileContent("target/exported/xml/allTables.xml");
    		assertXMLFileContent("target/exported/xml_dtd/allTables.xml");
    		assertDTDFileContent("target/exported/xml_dtd/allTables.dtd");
    		
        //xmlDataSetWithAllTables.delete();

        File ymlDataSetWithAllTables = new File("target/exported/yml/allTables.yml");
        assertThat(ymlDataSetWithAllTables).exists();
        assertThat(contentOf(ymlDataSetWithAllTables)).
                contains("FOLLOWER:" + NEW_LINE +
                        "  - ID: 1" + NEW_LINE +
                        "    USER_ID: 1" + NEW_LINE +
                        "    FOLLOWER_ID: 2" + NEW_LINE);

        assertThat(contentOf(ymlDataSetWithAllTables)).
                contains("USER:" + NEW_LINE +
                        "  - ID: 1" + NEW_LINE +
                        "    NAME: \"@realpestano\"" + NEW_LINE +
                        "  - ID: 2" + NEW_LINE +
                        "    NAME: \"@dbunit\"");

        //TODO validate generated content
        File xlsDataSetWithAllTables = new File("target/exported/xls/allTables.xls");
        assertThat(xlsDataSetWithAllTables).exists();

        File jsonDataSetWithAllTables = new File("target/exported/json/allTables.json");
        //TODO validate generated content
        assertThat(jsonDataSetWithAllTables).exists();


        File csvDataSetWithAllTables = new File("target/exported/csv/allTables");
        assertThat(csvDataSetWithAllTables).exists();
        //TODO validate generated content
        File userCsvDataSet = new File("target/exported/csv/allTables/USER.csv");
        assertThat(userCsvDataSet).exists();

        File tweetCsvDataSet = new File("target/exported/csv/allTables/TWEET.csv");
        assertThat(tweetCsvDataSet).exists();

        File followerCsvDataSet = new File("target/exported/csv/allTables/FOLLOWER.csv");
        assertThat(followerCsvDataSet).exists();

        assertXMLFileContentFiltered("target/exported/xml/filtered.xml");
        
        assertXMLFileContentFiltered("target/exported/xml_dtd/filtered.xml");
        assertDTDFileContentFiltered("target/exported/xml_dtd/filtered.dtd");
        
        File ymlFilteredDataSet = new File("target/exported/yml/filtered.yml");
        assertThat(ymlFilteredDataSet).exists();
        assertThat(contentOf(ymlFilteredDataSet)).contains("USER:" + NEW_LINE +
                "  - ID: 1" + NEW_LINE +
                "    NAME: \"@realpestano\"");


        assertXMLFileContentFilteredIncludes("target/exported/xml/filteredIncludes.xml");
        
        assertXMLFileContentFilteredIncludes("target/exported/xml_dtd/filteredIncludes.xml");
        assertDTDFileContentFilteredIncludes("target/exported/xml_dtd/filteredIncludes.dtd");
        
        File ymlFilteredIncludesDataSet = new File("target/exported/yml/filteredIncludes.yml");
        assertThat(ymlFilteredIncludesDataSet).exists();
        assertThat(contentOf(ymlFilteredIncludesDataSet)).contains("USER:" + NEW_LINE +
                "  - ID: 1" + NEW_LINE +
                "    NAME: \"@realpestano\"");

        assertThat(contentOf(ymlFilteredIncludesDataSet)).
                contains("TWEET:" + NEW_LINE +
                        "  - ID: \"abcdef12345\"" + NEW_LINE +
                        "    CONTENT: \"dbunit rules!\"");


        assertXMLFileContentDependent("target/exported/xml/dependentTables.xml");
        
        assertXMLFileContentDependent("target/exported/xml_dtd/dependentTables.xml");
        assertDTDFileContentDependent("target/exported/xml_dtd/dependentTables.dtd");

        File ymlDependentTablesDataSet = new File("target/exported/yml/dependentTables.yml");
        assertThat(ymlDependentTablesDataSet).exists();
        assertThat(contentOf(ymlDependentTablesDataSet)).contains("USER:" + NEW_LINE +
                "  - ID: 1" + NEW_LINE +
                "    NAME: \"@realpestano\"" + NEW_LINE +
                "  - ID: 2" + NEW_LINE +
                "    NAME: \"@dbunit\"");

        assertThat(contentOf(ymlDependentTablesDataSet)).
                contains("TWEET:" + NEW_LINE +
                        "  - ID: \"abcdef12345\"" + NEW_LINE +
                        "    CONTENT: \"dbunit rules!\"");

        assertThat(contentOf(ymlDependentTablesDataSet)).
                contains("FOLLOWER:" + NEW_LINE +
                        "  - ID: 1" + NEW_LINE +
                        "    USER_ID: 1" + NEW_LINE +
                        "    FOLLOWER_ID: 2");

        File datasetBuilderInDefaultSyntax = new File("target/exported/xml/AllTables.java");
        assertThat(contentOf(datasetBuilderInDefaultSyntax)).
                contains("DataSetBuilder builder = new DataSetBuilder();" + NEW_LINE +
                        "IDataSet dataSet = builder" + NEW_LINE +
                        "    .table(\"FOLLOWER\")" + NEW_LINE +
                        "    .row()" + NEW_LINE +
                        "        .column(\"ID\", 1)" + NEW_LINE +
                        "        .column(\"USER_ID\", 1)" + NEW_LINE +
                        "        .column(\"FOLLOWER_ID\", 2)" + NEW_LINE +
                        "    .table(\"SEQUENCE\")" + NEW_LINE +
                        "    .row()" + NEW_LINE +
                        "        .column(\"SEQ_NAME\", \"SEQ_GEN\")" + NEW_LINE +
                        "        .column(\"SEQ_COUNT\", 50)" + NEW_LINE +
                        "    .table(\"TWEET\")" + NEW_LINE +
                        "    .row()" + NEW_LINE +
                        "        .column(\"ID\", \"abcdef12345\")" + NEW_LINE +
                        "        .column(\"CONTENT\", \"dbunit rules!\")" + NEW_LINE +
                        "        .column(\"DATE\", \"")
                .contains("        .column(\"LIKES\", null)" + NEW_LINE +
                        "        .column(\"TIMESTAMP\", null)" + NEW_LINE +
                        "        .column(\"USER_ID\", 1)" + NEW_LINE +
                        "    .table(\"USER\")" + NEW_LINE +
                        "    .row()" + NEW_LINE +
                        "        .column(\"ID\", 1)" + NEW_LINE +
                        "        .column(\"NAME\", \"@realpestano\")" + NEW_LINE +
                        "    .row()" + NEW_LINE +
                        "        .column(\"ID\", 2)" + NEW_LINE +
                        "        .column(\"NAME\", \"@dbunit\").build();");
        
        
        

        File datasetBuilderInColumnValuesSyntax = new File("target/exported/xml/AllTables2.java");
        assertThat(contentOf(datasetBuilderInColumnValuesSyntax)).
                contains("DataSetBuilder builder = new DataSetBuilder();" + NEW_LINE +
                        "IDataSet dataSet = builder" + NEW_LINE +
                        "    .table(\"FOLLOWER\")" + NEW_LINE +
                        "        .columns(\"ID\", \"USER_ID\", \"FOLLOWER_ID\")" + NEW_LINE +
                        "        .values(1, 1, 2)" + NEW_LINE +
                        "    .table(\"SEQUENCE\")" + NEW_LINE +
                        "        .columns(\"SEQ_NAME\", \"SEQ_COUNT\")" + NEW_LINE +
                        "        .values(\"SEQ_GEN\", 50)" + NEW_LINE +
                        "    .table(\"TWEET\")" + NEW_LINE +
                        "        .columns(\"ID\", \"CONTENT\", \"DATE\", \"LIKES\", \"TIMESTAMP\", \"USER_ID\")")
                .contains("        .values(\"abcdef12345\", \"dbunit rules!\"")
                .contains("    .table(\"USER\")" + NEW_LINE +
                        "        .columns(\"ID\", \"NAME\")" + NEW_LINE +
                        "        .values(1, \"@realpestano\")" + NEW_LINE +
                        "        .values(2, \"@dbunit\").build();");
    }

    private static void assertXMLFileContentFiltered(String filename) {
      File xmlDataSet = new File(filename);
      
      assertThat(xmlDataSet).exists();
      assertThat(contentOf(xmlDataSet)).contains("<USER ID=\"1\" NAME=\"@realpestano\"/>")
      	.doesNotContain("<USER ID=\"2\" NAME=\"@dbunit\"/>")
      	.doesNotContain("<FOLLOWER ID=\"1\" USER_ID=\"1\" FOLLOWER_ID=\"2\"/>");
    }
    
    private static void assertXMLFileContent(String filename) {
      File xmlDataSetWithAllTables = new File(filename);
      
      assertThat(xmlDataSetWithAllTables).exists();
      assertThat(contentOf(xmlDataSetWithAllTables)).contains("<USER ID=\"1\" NAME=\"@realpestano\"/>")
      	.contains("<USER ID=\"2\" NAME=\"@dbunit\"/>")
      	.contains("<FOLLOWER ID=\"1\" USER_ID=\"1\" FOLLOWER_ID=\"2\"/>");
    }

    private static void assertXMLFileContentFilteredIncludes(String filename) {
      File xmlDataSet = new File(filename);
      
      assertThat(xmlDataSet).exists();
      assertThat(contentOf(xmlDataSet)).contains("<USER ID=\"1\" NAME=\"@realpestano\"/>")
      	.contains("<TWEET ID=\"abcdef12345\" CONTENT=\"dbunit rules!\"")
      	.doesNotContain("<USER ID=\"2\" NAME=\"@dbunit\"/>")
      	.doesNotContain("<FOLLOWER ID=\"1\" USER_ID=\"1\" FOLLOWER_ID=\"2\"/>");
    }
    
    private static void assertXMLFileContentDependent(String filename) {
      File xmlDataSet = new File(filename);
      
      assertThat(xmlDataSet).exists();
      assertThat(contentOf(xmlDataSet)).contains("<USER ID=\"1\" NAME=\"@realpestano\"/>")
      	.contains("<USER ID=\"2\" NAME=\"@dbunit\"/>")
      	.contains("<FOLLOWER ID=\"1\" USER_ID=\"1\" FOLLOWER_ID=\"2\"/>")
      	.contains("<TWEET ID=\"abcdef12345\" CONTENT=\"dbunit rules!\"");
    }

    private static void assertDTDFileContent(String filename) {
      File dtdDataSet = new File(filename);
      
      assertThat(dtdDataSet).exists();
      assertThat(contentOf(dtdDataSet)).
              contains("<!ELEMENT dataset (\n" + 
              		"    FOLLOWER*,\n" + 
              		"    SEQUENCE*,\n" + 
              		"    TWEET*,\n" + 
              		"    USER*)>\n" + 
              		"\n" + 
              		"<!ELEMENT FOLLOWER EMPTY>\n" + 
              		"<!ATTLIST FOLLOWER\n" + 
              		"    ID CDATA #REQUIRED\n" + 
              		"    USER_ID CDATA #IMPLIED\n" + 
              		"    FOLLOWER_ID CDATA #IMPLIED\n" + 
              		">\n" + 
              		"\n" + 
              		"<!ELEMENT SEQUENCE EMPTY>\n" + 
              		"<!ATTLIST SEQUENCE\n" + 
              		"    SEQ_NAME CDATA #REQUIRED\n" + 
              		"    SEQ_COUNT CDATA #IMPLIED\n" + 
              		">\n" + 
              		"\n" + 
              		"<!ELEMENT TWEET EMPTY>\n" + 
              		"<!ATTLIST TWEET\n" + 
              		"    ID CDATA #REQUIRED\n" + 
              		"    CONTENT CDATA #IMPLIED\n" + 
              		"    DATE CDATA #IMPLIED\n" + 
              		"    LIKES CDATA #IMPLIED\n" + 
              		"    TIMESTAMP CDATA #IMPLIED\n" + 
              		"    USER_ID CDATA #IMPLIED\n" + 
              		">\n" + 
              		"\n" + 
              		"<!ELEMENT USER EMPTY>\n" + 
              		"<!ATTLIST USER\n" + 
              		"    ID CDATA #REQUIRED\n" + 
              		"    NAME CDATA #IMPLIED\n" + 
              		">\n" + 
              		"\n");
    }

    private static void assertDTDFileContentDependent(String filename) {
      File dtdDataSet = new File(filename);
      
      assertThat(dtdDataSet).exists();
      assertThat(contentOf(dtdDataSet)).
              contains("<!ELEMENT dataset (\n" + 
              		"    TWEET*,\n" + 
              		"    USER*,\n" + 
              		"    FOLLOWER*)>\n" + 
              		"\n" + 
              		"<!ELEMENT TWEET EMPTY>\n" + 
              		"<!ATTLIST TWEET\n" + 
              		"    ID CDATA #REQUIRED\n" + 
              		"    CONTENT CDATA #IMPLIED\n" + 
              		"    DATE CDATA #IMPLIED\n" + 
              		"    LIKES CDATA #IMPLIED\n" + 
              		"    TIMESTAMP CDATA #IMPLIED\n" + 
              		"    USER_ID CDATA #IMPLIED\n" + 
              		">\n" + 
              		"\n" + 
              		"<!ELEMENT USER EMPTY>\n" + 
              		"<!ATTLIST USER\n" + 
              		"    ID CDATA #REQUIRED\n" + 
              		"    NAME CDATA #IMPLIED\n" + 
              		">\n" + 
              		"\n" + 
              		"<!ELEMENT FOLLOWER EMPTY>\n" + 
              		"<!ATTLIST FOLLOWER\n" + 
              		"    ID CDATA #REQUIRED\n" + 
              		"    USER_ID CDATA #IMPLIED\n" + 
              		"    FOLLOWER_ID CDATA #IMPLIED\n" + 
              		">\n" + 
              		"\n");
    }
    
    private static void assertDTDFileContentFiltered(String filename) {
      File dtdDataSet = new File(filename);
      
      assertThat(dtdDataSet).exists();
      assertThat(contentOf(dtdDataSet)).
              contains("<!ELEMENT dataset (\n" + 
              		"    USER*)>\n" + 
              		"\n" + 
              		"<!ELEMENT USER EMPTY>\n" + 
              		"<!ATTLIST USER\n" + 
              		"    ID CDATA #REQUIRED\n" + 
              		"    NAME CDATA #IMPLIED\n" + 
              		">\n" + 
              		"\n");
    }
    
    private static void assertDTDFileContentFilteredIncludes(String filename) {
      File dtdDataSet = new File(filename);
      
      assertThat(dtdDataSet).exists();
      assertThat(contentOf(dtdDataSet)).
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
              		"    TIMESTAMP CDATA #IMPLIED\n" + 
              		"    USER_ID CDATA #IMPLIED\n" + 
              		">\n" + 
              		"\n" + 
              		"<!ELEMENT USER EMPTY>\n" + 
              		"<!ATTLIST USER\n" + 
              		"    ID CDATA #REQUIRED\n" + 
              		"    NAME CDATA #IMPLIED\n" + 
              		">\n" + 
              		"\n");
    }
}
