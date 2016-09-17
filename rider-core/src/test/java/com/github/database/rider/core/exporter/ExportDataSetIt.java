package com.github.database.rider.core.exporter;

import com.github.database.rider.core.DBUnitRule;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.expoter.DataSetExportConfig;
import com.github.database.rider.core.api.expoter.ExportDataSet;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.core.api.dataset.DataSetFormat;
import com.github.database.rider.core.model.User;
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

import static com.github.database.rider.core.util.EntityManagerProvider.em;
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


    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.XML,outputName="target/exported/xml/allTables.xml")
    public void shouldExportAllTablesInXMLFormat() {
    }

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.YML,outputName="target/exported/yml/allTables")
    public void shouldExportAllTablesInYMLFormatOmmitingExtension() {
    }

    @Test
    @ExportDataSet(outputName="target/exported/yml/generatedWithoutDataSetAnnotation")
    public void shouldExportAllTablesInYMLFormatWithoutDataSetAnnotation() {
        //seed database
        DataSetExecutorImpl.getExecutorById(DataSetExecutorImpl.DEFAULT_EXECUTOR_ID)
                .createDataSet(new DataSetConfig("datasets/yml/users.yml"));
    }
    
    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.XLS,outputName="target/exported/xls/allTables.xls")
    public void shouldExportAllTablesInXLSFormat() {
    }
    
    @Test
    @DataSet(cleanBefore=true)
    public void shouldExportYMLDataSetProgrammatically() throws SQLException, DatabaseUnitException{
    	EntityManagerProvider.tx().begin();
    	User u1 = new User();
    	u1.setName("u1");
    	EntityManagerProvider.em().persist(u1);
    	EntityManagerProvider.tx().commit();
    	DataSetExporter.getInstance().export(new DatabaseConnection(emProvider.connection()), new DataSetExportConfig().outputFileName("target/user.yml"));
    	File ymlDataSet = new File("target/user.yml");
          assertThat(ymlDataSet).exists();
          assertThat(contentOf(ymlDataSet)).
                  contains("USER:"+NEW_LINE +
                          "  - ID: 1"+NEW_LINE +
                          "    NAME: \"u1\""+NEW_LINE 
                          );
    }

    @Test
    @DataSet(cleanBefore=true)
    public void shouldExportJSONDataSetProgrammatically() throws SQLException, DatabaseUnitException, FileNotFoundException {
        EntityManagerProvider.tx().begin();
        User u1 = new User();
        u1.setName("u1");
        EntityManagerProvider.em().persist(u1);
        EntityManagerProvider.tx().commit();
        DataSetExporter.getInstance().export(emProvider.connection(), new DataSetExportConfig().
                outputFileName("target/user.json").dataSetFormat(DataSetFormat.JSON));
        File jsonDataSet = new File("target/user.json");
        assertThat(jsonDataSet).exists();
        assertThat(contentOf(jsonDataSet)).contains("\"USER\": ["+NEW_LINE +
                "    {"+NEW_LINE +
                "      \"ID\": " + u1.getId() +
                ","+NEW_LINE +
                "      \"NAME\": \"u1\""+NEW_LINE +
                "    }"+NEW_LINE +
                "  ]");
    }
    
    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.CSV,outputName="target/exported/csv/allTables.csv")
    public void shouldExportAllTablesInCSVFormat() {
    }
    
    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.JSON,outputName="target/exported/json/allTables.json")
    public void shouldExportAllTablesInJSONFormat() {
    }

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.XML, queryList = {"select * from USER u where u.ID = 1"}, outputName="target/exported/xml/filtered.xml")
    public void shouldExportXMLDataSetUsingQueryToFilterRows() {

    }

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.YML, queryList = {"select * from USER u where u.ID = 1"}, outputName="target/exported/yml/filtered.yml")
    public void shouldExportYMLDataSetUsingQueryToFilterRows() {

    }

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.XML, queryList = {"select * from USER u where u.ID = 1"}, includeTables = {"TWEET"}, outputName="target/exported/xml/filteredIncludes.xml")
    public void shouldExportXMLDataSetUsingQueryAndIncludesToFilterRows() {

    }

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.YML, queryList = {"select * from USER u where u.ID = 1"}, includeTables = "TWEET", outputName="target/exported/yml/filteredIncludes.yml")
    public void shouldExportYMLDataSetUsingQueryAndIncludesToFilterRows() {

    }

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.XML, includeTables = "USER", outputName="target/exported/xml/includes.xml")
    public void shouldExportXMLDataSetWithTablesInIncludes() {

    }

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.YML, includeTables = "USER", outputName="target/exported/yml/includes.yml")
    public void shouldExportYMLDataSetWithTablesInIncludes() {

    }

    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.XML, includeTables = "USER", dependentTables = true, outputName="target/exported/xml/dependentTables.xml")
    public void shouldExportXMLDataSetUsingIncludesWithDependentTables() {

    }


    @Test
    @DataSet("datasets/yml/users.yml")
    @ExportDataSet(format = DataSetFormat.YML, includeTables = {"USER","TWEET"}, dependentTables = true, outputName="target/exported/yml/dependentTables.yml")
    public void shouldExportYMLDataSetUsingIncludesWithDependentTables() {

    }


    @AfterClass
    public static void assertGeneratedDataSets(){
        File xmlDataSetWithAllTables = new File("target/exported/xml/allTables.xml");
        assertThat(xmlDataSetWithAllTables).exists();
        assertThat(contentOf(xmlDataSetWithAllTables)).contains("<USER ID=\"1\" NAME=\"@realpestano\"/>");
        assertThat(contentOf(xmlDataSetWithAllTables)).contains("<USER ID=\"2\" NAME=\"@dbunit\"/>");
        assertThat(contentOf(xmlDataSetWithAllTables)).contains("<FOLLOWER ID=\"1\" USER_ID=\"1\" FOLLOWER_ID=\"2\"/>");

        //xmlDataSetWithAllTables.delete();

        File ymlDataSetWithAllTables = new File("target/exported/yml/allTables.yml");
        assertThat(ymlDataSetWithAllTables).exists();
        assertThat(contentOf(ymlDataSetWithAllTables)).
                contains("FOLLOWER:"+NEW_LINE +
                "  - ID: 1"+NEW_LINE +
                "    USER_ID: 1"+NEW_LINE +
                "    FOLLOWER_ID: 2"+NEW_LINE );

        assertThat(contentOf(ymlDataSetWithAllTables)).
                contains("USER:"+NEW_LINE +
                        "  - ID: 1"+NEW_LINE +
                        "    NAME: \"@realpestano\""+NEW_LINE +
                        "  - ID: 2"+NEW_LINE +
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

        File xmlFilteredDataSet = new File("target/exported/xml/filtered.xml");
        assertThat(xmlFilteredDataSet).exists();
        assertThat(contentOf(xmlFilteredDataSet)).contains("<USER ID=\"1\" NAME=\"@realpestano\"/>");
        assertThat(contentOf(xmlFilteredDataSet)).doesNotContain("<USER ID=\"2\" NAME=\"@dbunit\"/>");
        assertThat(contentOf(xmlFilteredDataSet)).doesNotContain("<FOLLOWER ID=\"1\" USER_ID=\"1\" FOLLOWER_ID=\"2\"/>");

        File ymlFilteredDataSet = new File("target/exported/yml/filtered.yml");
        assertThat(ymlFilteredDataSet).exists();
        assertThat(contentOf(ymlFilteredDataSet)).contains("USER:"+NEW_LINE +
                "  - ID: 1"+NEW_LINE +
                "    NAME: \"@realpestano\"");


        
        
        File xmlFilteredWithIncludesDataSet = new File("target/exported/xml/filteredIncludes.xml");
        assertThat(xmlFilteredWithIncludesDataSet).exists();
        assertThat(contentOf(xmlFilteredWithIncludesDataSet)).contains("<USER ID=\"1\" NAME=\"@realpestano\"/>");
        assertThat(contentOf(xmlFilteredWithIncludesDataSet)).contains("<TWEET ID=\"abcdef12345\" CONTENT=\"dbunit rules!\"");
        assertThat(contentOf(xmlFilteredWithIncludesDataSet)).doesNotContain("<USER ID=\"2\" NAME=\"@dbunit\"/>");
        assertThat(contentOf(xmlFilteredWithIncludesDataSet)).doesNotContain("<FOLLOWER ID=\"1\" USER_ID=\"1\" FOLLOWER_ID=\"2\"/>");

        File ymlFilteredIncludesDataSet = new File("target/exported/yml/filteredIncludes.yml");
        assertThat(ymlFilteredIncludesDataSet).exists();
        assertThat(contentOf(ymlFilteredIncludesDataSet)).contains("USER:" + NEW_LINE +
                "  - ID: 1" + NEW_LINE +
                "    NAME: \"@realpestano\"");

        assertThat(contentOf(ymlFilteredIncludesDataSet)).
                contains("TWEET:"+NEW_LINE +
                        "  - ID: \"abcdef12345\""+NEW_LINE +
                        "    CONTENT: \"dbunit rules!\"");


        File xmlDependentTablesDataSet = new File("target/exported/xml/dependentTables.xml");
        assertThat(xmlDependentTablesDataSet).exists();
        assertThat(contentOf(xmlDependentTablesDataSet)).contains("<USER ID=\"1\" NAME=\"@realpestano\"/>");
        assertThat(contentOf(xmlDependentTablesDataSet)).contains("<USER ID=\"2\" NAME=\"@dbunit\"/>");
        assertThat(contentOf(xmlDependentTablesDataSet)).contains("<FOLLOWER ID=\"1\" USER_ID=\"1\" FOLLOWER_ID=\"2\"/>");
        assertThat(contentOf(xmlDependentTablesDataSet)).contains("<TWEET ID=\"abcdef12345\" CONTENT=\"dbunit rules!\"");


        File ymlDependentTablesDataSet = new File("target/exported/yml/dependentTables.yml");
        assertThat(ymlDependentTablesDataSet).exists();
        assertThat(contentOf(ymlDependentTablesDataSet)).contains("USER:"+NEW_LINE +
                "  - ID: 1"+NEW_LINE +
                "    NAME: \"@realpestano\""+NEW_LINE +
                "  - ID: 2"+NEW_LINE +
                "    NAME: \"@dbunit\"");

        assertThat(contentOf(ymlDependentTablesDataSet)).
                contains("TWEET:"+NEW_LINE +
                        "  - ID: \"abcdef12345\""+NEW_LINE +
                        "    CONTENT: \"dbunit rules!\"");

        assertThat(contentOf(ymlDependentTablesDataSet)).
                contains("FOLLOWER:"+NEW_LINE +
                        "  - ID: 1"+NEW_LINE +
                        "    USER_ID: 1"+NEW_LINE +
                        "    FOLLOWER_ID: 2");
    }
}
