package com.github.database.rider.core.dataset.builder;

import com.github.database.rider.core.dataset.writer.JSONWriter;
import com.github.database.rider.core.dataset.writer.YMLWriter;
import com.github.database.rider.core.metamodel.Contact_;
import com.github.database.rider.core.util.DateUtils;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

@RunWith(JUnit4.class)
public class DatasetBuilderTest {

    private static final String NEW_LINE = System.getProperty("line.separator");


    @Test
    public void shouldGenerateYamlDataSet() throws DataSetException, IOException {
        DataSetBuilder builder = new DataSetBuilder();
        ColumnSpec id = ColumnSpec.of("ID");
        IDataSet dataSet = builder.table("USER")
                .row()
                    .column("ID", 1)
                    .column("NAME", "@realpestano")
                .table("USER")
                .row()
                    .column(id, 2).column("NAME", "@dbunit")
                .table("TWEET")
                .row()
                    .column("ID", "abcdef12345")
                    .column("CONTENT", "dbunit rules!")
                    .column("DATE", "[DAY,NOW]")
                .table("FOLLOWER")
                .row().column(id, 1)
                    .column("USER_ID", 1)
                    .column("FOLLOWER_ID", 2)
                .build();

        File datasetFile = Files.createTempFile("rider-dataset", ".yml").toFile();
        FileOutputStream fos = new FileOutputStream(datasetFile);
        new YMLWriter(fos).write(dataSet);

        assertThat(contentOf(datasetFile)).
                contains("FOLLOWER:" + NEW_LINE +
                        "  - ID: 1" + NEW_LINE +
                        "    USER_ID: 1" + NEW_LINE +
                        "    FOLLOWER_ID: 2" + NEW_LINE).
                contains("USER:" + NEW_LINE +
                "  - ID: 1" + NEW_LINE +
                "    NAME: \"@realpestano\"" + NEW_LINE +
                "  - ID: 2" + NEW_LINE +
                "    NAME: \"@dbunit\"").
                contains("TWEET:" + NEW_LINE +
                "  - ID: \"abcdef12345\"" + NEW_LINE +
                "    CONTENT: \"dbunit rules!\"" + NEW_LINE +
                "    DATE: \"[DAY,NOW]\""+ NEW_LINE );
    }

    @Test
    public void shouldGenerateJsonDataSet() throws DataSetException, IOException {
        DataSetBuilder builder = new DataSetBuilder();
        ColumnSpec id = ColumnSpec.of("ID");
        builder.table("USER")
                .row()
                    .column("ID", 1)
                    .column("NAME", "@realpestano")
                .table("USER")
                .row()
                    .column(id, 2).column("NAME", "@dbunit")
                .table("TWEET")
                .row()
                    .column("ID", "abcdef12345")
                    .column("CONTENT", "dbunit rules!")
                    .column("DATE", "[DAY,NOW]")
                .table("FOLLOWER")
                .row()
                    .column(id, 1)
                    .column("USER_ID", 1)
                    .column("FOLLOWER_ID", 2)
                .build();

        IDataSet dataSet = builder.build();

        File datasetFile = Files.createTempFile("rider-dataset", ".json").toFile();
        FileOutputStream fos = new FileOutputStream(datasetFile);
        new JSONWriter(fos,dataSet).write();

        assertThat(contentOf(datasetFile)).
                contains("{"+NEW_LINE  +
                        "  \"USER\": ["+NEW_LINE  +
                        "    {"+NEW_LINE  +
                        "      \"ID\": 1,"+NEW_LINE  +
                        "      \"NAME\": \"@realpestano\""+NEW_LINE  +
                        "    },"+NEW_LINE  +
                        "    {"+NEW_LINE  +
                        "      \"ID\": 2,"+NEW_LINE  +
                        "      \"NAME\": \"@dbunit\""+NEW_LINE  +
                        "    }"+NEW_LINE  +
                        "  ],"+NEW_LINE  +
                        "  \"TWEET\": ["+NEW_LINE  +
                        "    {"+NEW_LINE  +
                        "      \"ID\": \"abcdef12345\","+NEW_LINE  +
                        "      \"CONTENT\": \"dbunit rules!\","+NEW_LINE  +
                        "      \"DATE\": \"[DAY,NOW]\""+NEW_LINE  +
                        "    }"+NEW_LINE  +
                        "  ],"+NEW_LINE  +
                        "  \"FOLLOWER\": ["+NEW_LINE  +
                        "    {"+NEW_LINE  +
                        "      \"ID\": 1,"+NEW_LINE  +
                        "      \"USER_ID\": 1,"+NEW_LINE  +
                        "      \"FOLLOWER_ID\": 2"+NEW_LINE  +
                        "    }"+NEW_LINE  +
                        "  ]"+NEW_LINE  +
                        "}");
    }

    @Test
    public void shouldGenerateFlatXmlDataSet() throws DataSetException, IOException {
        DataSetBuilder builder = new DataSetBuilder();
        ColumnSpec id = ColumnSpec.of("ID");
        builder.table("USER")
                .row().column("ID", 1)
                    .column("NAME", "@realpestano")
                .table("USER")
                .row().column(id, 2)
                    .column("NAME", "@dbunit")
                .table("TWEET")
                .row()
                    .column("ID", "abcdef12345")
                    .column("CONTENT", "dbunit rules!")
                    .column("DATE", "[DAY,NOW]")
                .table("FOLLOWER")
                .row()
                    .column(id, 1)
                    .column("USER_ID", 1)
                    .column("FOLLOWER_ID", 2)
                .build();

        IDataSet dataSet = builder.build();

        File datasetFile = Files.createTempFile("rider-dataset", ".xml").toFile();
        FileOutputStream fos = new FileOutputStream(datasetFile);
        FlatXmlDataSet.write(dataSet, fos);
        assertThat(contentOf(datasetFile)).
                contains("<?xml version='1.0' encoding='UTF-8'?>"+NEW_LINE  +
                        "<dataset>"+NEW_LINE  +
                        "  <USER ID=\"1\" NAME=\"@realpestano\"/>"+NEW_LINE  +
                        "  <USER ID=\"2\" NAME=\"@dbunit\"/>"+NEW_LINE  +
                        "  <TWEET ID=\"abcdef12345\" CONTENT=\"dbunit rules!\" DATE=\"[DAY,NOW]\"/>"+NEW_LINE  +
                        "  <FOLLOWER ID=\"1\" USER_ID=\"1\" FOLLOWER_ID=\"2\"/>"+NEW_LINE  +
                        "</dataset>");

    }

    @Test
    public void shouldGenerateDataSetcolumnDateColumn() throws DataSetException, IOException {
        DataSetBuilder builder = new DataSetBuilder();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        Date date = new Date();

        IDataSet dataSet = builder.table("USER")
                .row()
                    .column("ID", 1)
                    .column("DATE", date)
                    .column("CALENDAR", calendar)
                .build();


        File datasetFile = Files.createTempFile("rider-dataset", ".yml").toFile();
        FileOutputStream fos = new FileOutputStream(datasetFile);
        new YMLWriter(fos).write(dataSet);


        assertThat(contentOf(datasetFile)).
                contains("USER:" + NEW_LINE +
                        "  - ID: 1" + NEW_LINE +
                        "    DATE: \"" + DateUtils.format(date) +"\"" + NEW_LINE +
                        "    CALENDAR: \"" + DateUtils.format(calendar.getTime()) +"\"" + NEW_LINE +
                        "");
    }

    @Test
    public void shouldGenerateDataSetUsingDefaultValues() throws DataSetException, IOException {
        DataSetBuilder builder = new DataSetBuilder()
                .defaultValue("NAME", "DEFAULT")
                .defaultValue("date", "[DAY,NOW]");
        ColumnSpec id = ColumnSpec.of("ID");
        IDataSet dataSet = builder.table("USER")
                .row()
                   .column(id, 1)
                   .column("NAME", "@realpestano")
                .table("USER")
                .row()
                   .column(id, 2)
                .table("TWEET")
                .row()
                    .column("ID", "abcdef12345")
                    .column("CONTENT", "dbunit rules!")
                .table("FOLLOWER")
                .row()
                    .column(id, 1)
                    .column("USER_ID", 1)
                    .column("FOLLOWER_ID", 2)
                .build();

        File datasetFile = Files.createTempFile("rider-dataset", ".yml").toFile();
        FileOutputStream fos = new FileOutputStream(datasetFile);
        new YMLWriter(fos).write(dataSet);

        assertThat(contentOf(datasetFile)).
                contains("USER:" + NEW_LINE +
                        "  - ID: 1" + NEW_LINE +
                        "    NAME: \"@realpestano\"" + NEW_LINE +
                        "    DATE: \"[DAY,NOW]\"" + NEW_LINE +
                        "  - ID: 2" + NEW_LINE +
                        "    NAME: \"DEFAULT\"" + NEW_LINE +
                        "    DATE: \"[DAY,NOW]\"" + NEW_LINE +
                        "" + NEW_LINE +
                        "TWEET:" + NEW_LINE +
                        "  - ID: \"abcdef12345\"" + NEW_LINE +
                        "    CONTENT: \"dbunit rules!\"" + NEW_LINE +
                        "    NAME: \"DEFAULT\"" + NEW_LINE +
                        "    DATE: \"[DAY,NOW]\"" + NEW_LINE +
                        "" + NEW_LINE +
                        "FOLLOWER:" + NEW_LINE +
                        "  - ID: 1" + NEW_LINE +
                        "    USER_ID: 1" + NEW_LINE +
                        "    FOLLOWER_ID: 2" + NEW_LINE +
                        "    NAME: \"DEFAULT\"" + NEW_LINE +
                        "    DATE: \"[DAY,NOW]\"" + NEW_LINE+ NEW_LINE );
    }

    @Test
    public void shouldGenerateDataSetUsingMetaModel() throws DataSetException, IOException {

        EntityManagerProvider.instance("contactPU");
        DataSetBuilder builder = new DataSetBuilder();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        Date date = new Date();

        IDataSet dataSet = builder.table("CONTACT")
                .row()
                    .column(Contact_.id, 1)
                    .column(Contact_.name, "dbrider")
                    .column(Contact_.date,date )
                    .column(Contact_.calendar, calendar)
                .build();


        File datasetFile = Files.createTempFile("rider-dataset", ".yml").toFile();
        FileOutputStream fos = new FileOutputStream(datasetFile);
        new YMLWriter(fos).write(dataSet);


        assertThat(contentOf(datasetFile)).
                contains("CONTACT:" + NEW_LINE +
                        "  - ID: 1" + NEW_LINE +
                        "    NAME: \"dbrider\"" + NEW_LINE +
                        "    DATE: \"" + DateUtils.format(date) +"\"" + NEW_LINE +
                        "    CALENDAR: \"" + DateUtils.format(calendar.getTime()) +"\"" + NEW_LINE +
                        "");
    }

    @Test
    public void shouldNotGenerateDataSetWhenColumnsSizeIsDifferentThanValuesSize() {

        try {
            new DataSetBuilder().table("user")
                    .columns("id", "name")
                    .values(1, "@dbunit", "anotherValue")
                    .values(2, "@dbrider").build();
            Assert.fail("Build method must throw an exception");
        }catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Number of columns (2) for table USER is different than the number of provided values (3)");
        }
    }

    @Test
    public void shouldGenerateDataSetUsingMetaModelWithColumnValuesSyntax() throws DataSetException, IOException {

        EntityManagerProvider.instance("contactPU");
        DataSetBuilder builder = new DataSetBuilder();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        Date date = new Date();

        IDataSet dataSet = builder.table("CONTACT")
                .columns(Contact_.id,Contact_.name, Contact_.date, Contact_.calendar)
                .values(1, "dbrider" ,date , calendar)
                .build();


        File datasetFile = Files.createTempFile("rider-dataset", ".yml").toFile();
        FileOutputStream fos = new FileOutputStream(datasetFile);
        new YMLWriter(fos).write(dataSet);


        assertThat(contentOf(datasetFile)).
                contains("CONTACT:" + NEW_LINE +
                        "  - ID: 1" + NEW_LINE +
                        "    NAME: \"dbrider\"" + NEW_LINE +
                        "    DATE: \"" + DateUtils.format(date) +"\"" + NEW_LINE +
                        "    CALENDAR: \"" + DateUtils.format(calendar.getTime()) +"\"" + NEW_LINE +
                        "");
    }

    @Test
    public void shouldGenerateUppercaseYamlDataSet() throws IOException, DataSetException {
        DataSetBuilder builder = new DataSetBuilder();
        ColumnSpec id = ColumnSpec.of("id");
        builder.table("user")
                .row()
                    .column(id, 1)
                    .column("name", "@realpestano")
                .table("user")
                .row()
                    .column(id, 2).column("name", "@dbunit")
                .table("tweet")
                .row()
                    .column("id", "abcdef12345")
                    .column("content", "dbunit rules!")
                    .column("date", "[DAY,NOW]")
                .table("follower")
                .row()
                    .column(id, 1)
                    .column("user_id", 1)
                    .column("follower_id", 2)
                .build();

        IDataSet dataSet = builder.build();

        File datasetFile = Files.createTempFile("rider-dataset", ".yml").toFile();
        FileOutputStream fos = new FileOutputStream(datasetFile);
        new YMLWriter(fos).write(dataSet);

        assertThat(contentOf(datasetFile)).
                contains("FOLLOWER:" + NEW_LINE +
                        "  - ID: 1" + NEW_LINE +
                        "    USER_ID: 1" + NEW_LINE +
                        "    FOLLOWER_ID: 2" + NEW_LINE).
                contains("USER:" + NEW_LINE +
                        "  - ID: 1" + NEW_LINE +
                        "    NAME: \"@realpestano\"" + NEW_LINE +
                        "  - ID: 2" + NEW_LINE +
                        "    NAME: \"@dbunit\"").
                contains("TWEET:" + NEW_LINE +
                        "  - ID: \"abcdef12345\"" + NEW_LINE +
                        "    CONTENT: \"dbunit rules!\"" + NEW_LINE +
                        "    DATE: \"[DAY,NOW]\""+ NEW_LINE );
    }


    @Test
    public void shouldGenerateDataSetUsingTableDefaultValues() throws IOException, DataSetException {
        DataSetBuilder builder = new DataSetBuilder();
        builder.defaultValue("id", -1).//defaultValue for all tables, applied only if not specified
                table("user")
                .row()
                      .column("id", 1)
                      .column("name", "@realpestano")
                .row() //creates a new row for user table
                      .column("id", 2)
                      .column("name", "@dbunit")
                .row()
                     .column("name", "@dbunit3")//third row for user table using default value
                .table("tweet")
                    .defaultValue("likes", 99) //defaultValue only for table tweet
                .row()
                    .column("id", "abcdef12345")
                    .column("content", "dbunit rules!")
                    .column("date", "[DAY,NOW]")
                .table("follower")
                .row()
                    .column("id", 1)
                    .column("user_id", 1).column("follower_id", 2)
                .build();

        IDataSet dataSet = builder.build();

        File datasetFile = Files.createTempFile("rider-dataset", ".yml").toFile();
        FileOutputStream fos = new FileOutputStream(datasetFile);
        new YMLWriter(fos).write(dataSet);

        assertThat(contentOf(datasetFile)).
                contains("FOLLOWER:" + NEW_LINE +
                        "  - ID: 1" + NEW_LINE +
                        "    USER_ID: 1" + NEW_LINE +
                        "    FOLLOWER_ID: 2" + NEW_LINE).
                contains("USER:" + NEW_LINE +
                        "  - ID: 1" + NEW_LINE +
                        "    NAME: \"@realpestano\"" + NEW_LINE +
                        "  - ID: 2" + NEW_LINE +
                        "    NAME: \"@dbunit\"" + NEW_LINE +
                        "  - ID: -1" + NEW_LINE +
                        "    NAME: \"@dbunit3\"").
                contains("TWEET:" + NEW_LINE +
                        "  - ID: \"abcdef12345\"" + NEW_LINE +
                        "    CONTENT: \"dbunit rules!\"" + NEW_LINE +
                        "    DATE: \"[DAY,NOW]\""+ NEW_LINE  +
                        "    LIKES: 99"+ NEW_LINE );

    }


    @Test
    public void shouldGenerateDataSetUsingColumnsSyntax() throws IOException, DataSetException {
        DataSetBuilder builder = new DataSetBuilder();
        builder.defaultValue("id", -1)
                .table("user")
                    .columns("id", "name")
                    .values(1,  "@dbrider")
                    .values(2,  "@dbunit")
                    .values(null,  "@dbunit3")
                .table("tweet")
                    .defaultValue("likes", 99)
                    .columns("id", "content", "date")
                    .values("abcdef12345",  "dbunit rules!", "[DAY,NOW]")
                .table("follower")
                    .columns("id", "user_id", "follower_id")
                    .values(1,  1, 2)
                .build();
        IDataSet dataSet = builder.build();

        File datasetFile = Files.createTempFile("rider-dataset", ".yml").toFile();
        FileOutputStream fos = new FileOutputStream(datasetFile);
        new YMLWriter(fos).write(dataSet);

        assertThat(contentOf(datasetFile)).
                contains("FOLLOWER:" + NEW_LINE +
                        "  - ID: 1" + NEW_LINE +
                        "    USER_ID: 1" + NEW_LINE +
                        "    FOLLOWER_ID: 2" + NEW_LINE).
                contains("USER:" + NEW_LINE +
                        "  - ID: 1" + NEW_LINE +
                        "    NAME: \"@dbrider\"" + NEW_LINE +
                        "  - ID: 2" + NEW_LINE +
                        "    NAME: \"@dbunit\"" + NEW_LINE +
                        "  - ID: -1" + NEW_LINE +
                        "    NAME: \"@dbunit3\"").
                contains("TWEET:" + NEW_LINE +
                        "  - ID: \"abcdef12345\"" + NEW_LINE +
                        "    CONTENT: \"dbunit rules!\"" + NEW_LINE +
                        "    DATE: \"[DAY,NOW]\""+ NEW_LINE  +
                        "    LIKES: 99"+ NEW_LINE );
    }

    @Test
    public void shouldGenerateDataSetUsingMixedSyntax() throws IOException, DataSetException {
        DataSetBuilder builder = new DataSetBuilder();
        builder.defaultValue("id", -1)
                .table("user")
                    .columns("id", "name")
                    .values(1,  "@dbrider")
                    .values(2,  "@dbunit")
                    .values(null,  "@dbunit3")//will use default value
                .table("tweet")
                    .defaultValue("likes", 99)
                .row()
                        .column("id", "abcdef12345")
                        .column("content", "dbunit rules!")
                        .column("date", "[DAY,NOW]")
                .table("follower")
                    .columns("id", "user_id", "follower_id")
                    .values(1,  1, 2)
                .build();
        IDataSet dataSet = builder.build();

        File datasetFile = Files.createTempFile("rider-dataset", ".yml").toFile();
        FileOutputStream fos = new FileOutputStream(datasetFile);
        new YMLWriter(fos).write(dataSet);

        assertThat(contentOf(datasetFile)).
                contains("FOLLOWER:" + NEW_LINE +
                        "  - ID: 1" + NEW_LINE +
                        "    USER_ID: 1" + NEW_LINE +
                        "    FOLLOWER_ID: 2" + NEW_LINE).
                contains("USER:" + NEW_LINE +
                        "  - ID: 1" + NEW_LINE +
                        "    NAME: \"@dbrider\"" + NEW_LINE +
                        "  - ID: 2" + NEW_LINE +
                        "    NAME: \"@dbunit\"" + NEW_LINE +
                        "  - ID: -1" + NEW_LINE +
                        "    NAME: \"@dbunit3\"").
                contains("TWEET:" + NEW_LINE +
                        "  - ID: \"abcdef12345\"" + NEW_LINE +
                        "    CONTENT: \"dbunit rules!\"" + NEW_LINE +
                        "    DATE: \"[DAY,NOW]\""+ NEW_LINE  +
                        "    LIKES: 99"+ NEW_LINE );
    }


}
