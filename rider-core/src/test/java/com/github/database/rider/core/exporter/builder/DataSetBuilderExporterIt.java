package com.github.database.rider.core.exporter.builder;

import com.github.database.rider.core.api.exporter.BuilderType;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.connection.ConnectionHolderImpl;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.ForwardOnlyResultSetTableFactory;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

public class DataSetBuilderExporterIt {

    private static final String NEW_LINE = System.getProperty("line.separator");
    
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("exporter-it");

    private static DataSetExecutorImpl executor;


    @BeforeClass
    public static void setup() {
        executor = DataSetExecutorImpl.instance("executor-name", new ConnectionHolderImpl(EntityManagerProvider.instance("exporter-it").connection()));
    }


    @Test
    public void shouldExportYamlDataSetUsingDefaultSyntax() throws DataSetException {
        executor.createDataSet(new DataSetConfig("yml/users.yml"));
        IDataSet iDataSet = createDataSetFromDatabase();
        File outputDir = Paths.get("target/FromYamlDefaultBuilder.java").toAbsolutePath().toFile();
        new DataSetBuilderExporter().export(iDataSet, new BuilderExportConfig(BuilderType.DEFAULT, outputDir));
        assertThat(contentOf(outputDir)).isEqualTo("DataSetBuilder builder = new DataSetBuilder();" + NEW_LINE +
                "IDataSet dataSet = builder" + NEW_LINE +
                "    .table(\"FOLLOWER\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"ID\", 1)" + NEW_LINE +
                "        .column(\"USER_ID\", 1)" + NEW_LINE +
                "        .column(\"FOLLOWER_ID\", 2)" + NEW_LINE +
                "    .table(\"SEQUENCE\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"SEQ_NAME\", \"SEQ_GEN\")" + NEW_LINE +
                "        .column(\"SEQ_COUNT\", 0)" + NEW_LINE +
                "    .table(\"TWEET\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"ID\", \"abcdef12345\")" + NEW_LINE +
                "        .column(\"CONTENT\", \"dbunit rules!\")" + NEW_LINE +
                "        .column(\"DATE\", \"" +
                "" + iDataSet.getTable("TWEET").getValue(0,"DATE") +
                "\")" + NEW_LINE +
                "        .column(\"LIKES\", null)" + NEW_LINE +
                "        .column(\"TIMESTAMP\", null)" + NEW_LINE +
                "        .column(\"USER_ID\", 1)" + NEW_LINE +
                "    .table(\"USER\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"ID\", 1)" + NEW_LINE +
                "        .column(\"NAME\", \"@realpestano\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"ID\", 2)" + NEW_LINE +
                "        .column(\"NAME\", \"@dbunit\").build();");
    }

    @Test
    public void shouldExportYamlDataSetUsingColumnValuesSyntax() throws DataSetException {
        executor.createDataSet(new DataSetConfig("yml/users.yml"));
        IDataSet iDataSet = createDataSetFromDatabase();
        File outputDir = Paths.get("target/FromYamlBuilder.java").toAbsolutePath().toFile();
        new DataSetBuilderExporter().export(iDataSet, new BuilderExportConfig(BuilderType.COLUMNS_VALUES, outputDir));
        assertThat(contentOf(outputDir)).isEqualTo("DataSetBuilder builder = new DataSetBuilder();" + NEW_LINE +
                "IDataSet dataSet = builder" + NEW_LINE +
                "    .table(\"FOLLOWER\")" + NEW_LINE +
                "        .columns(\"ID\", \"USER_ID\", \"FOLLOWER_ID\")" + NEW_LINE +
                "        .values(1, 1, 2)" + NEW_LINE +
                "    .table(\"SEQUENCE\")" + NEW_LINE +
                "        .columns(\"SEQ_NAME\", \"SEQ_COUNT\")" + NEW_LINE +
                "        .values(\"SEQ_GEN\", 0)" + NEW_LINE +
                "    .table(\"TWEET\")" + NEW_LINE +
                "        .columns(\"ID\", \"CONTENT\", \"DATE\", \"LIKES\", \"TIMESTAMP\", \"USER_ID\")" + NEW_LINE +
                "        .values(\"abcdef12345\", \"dbunit rules!\", \"" +
                "" + iDataSet.getTable("TWEET").getValue(0,"DATE") +
                "\", null, null, 1)" + NEW_LINE +
                "    .table(\"USER\")" + NEW_LINE +
                "        .columns(\"ID\", \"NAME\")" + NEW_LINE +
                "        .values(1, \"@realpestano\")" + NEW_LINE +
                "        .values(2, \"@dbunit\").build();");
    }

    @Test
    public void shouldExportXMLDataSetUsingDefaultSyntax() throws DataSetException {
        IDataSet iDataSet = new FlatXmlDataSetBuilder().build(getDataSetStream("datasets/xml/users.xml"));
        File outputDir = Paths.get("target/FromXMlDefaultBuilder.java").toAbsolutePath().toFile();
        new DataSetBuilderExporter().export(iDataSet, new BuilderExportConfig(BuilderType.DEFAULT, outputDir));
        assertThat(contentOf(outputDir)).isEqualTo("DataSetBuilder builder = new DataSetBuilder();" + NEW_LINE +
                "IDataSet dataSet = builder" + NEW_LINE +
                "    .table(\"USER\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"id\", \"1\")" + NEW_LINE +
                "        .column(\"name\", \"@realpestano\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"id\", \"2\")" + NEW_LINE +
                "        .column(\"name\", \"@dbunit\")" + NEW_LINE +
                "    .table(\"TWEET\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"id\", \"abcdef12345\")" + NEW_LINE +
                "        .column(\"content\", \"dbunit rules flat xml example\")" + NEW_LINE +
                "        .column(\"user_id\", \"1\")" + NEW_LINE +
                "    .table(\"FOLLOWER\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"id\", \"1\")" + NEW_LINE +
                "        .column(\"user_id\", \"1\")" + NEW_LINE +
                "        .column(\"follower_id\", \"2\").build();");
    }

    @Test
    public void shouldExportXMLDataSetUsingColumnValuesSyntax() throws DataSetException {
        IDataSet iDataSet = new FlatXmlDataSetBuilder().build(getDataSetStream("datasets/xml/users.xml"));
        File outputDir = Paths.get("target/FromXMlBuilder.java").toAbsolutePath().toFile();
        new DataSetBuilderExporter().export(iDataSet, new BuilderExportConfig(BuilderType.COLUMNS_VALUES, outputDir));
        assertThat(contentOf(outputDir)).isEqualTo("DataSetBuilder builder = new DataSetBuilder();" + NEW_LINE +
                "IDataSet dataSet = builder" + NEW_LINE +
                "    .table(\"USER\")" + NEW_LINE +
                "        .columns(\"id\", \"name\")" + NEW_LINE +
                "        .values(\"1\", \"@realpestano\")" + NEW_LINE +
                "        .values(\"2\", \"@dbunit\")" + NEW_LINE +
                "    .table(\"TWEET\")" + NEW_LINE +
                "        .columns(\"id\", \"content\", \"user_id\")" + NEW_LINE +
                "        .values(\"abcdef12345\", \"dbunit rules flat xml example\", \"1\")" + NEW_LINE +
                "    .table(\"FOLLOWER\")" + NEW_LINE +
                "        .columns(\"id\", \"user_id\", \"follower_id\")" + NEW_LINE +
                "        .values(\"1\", \"1\", \"2\").build();");
    }

    @Test
    public void shouldExportJSONDataSetUsingDefaultSyntax() throws DataSetException {
        executor.createDataSet(new DataSetConfig("json/users.json"));
        IDataSet iDataSet = createDataSetFromDatabase();
        File outputDir = Paths.get("target/FromJSONDefaultBuilder.java").toAbsolutePath().toFile();
        new DataSetBuilderExporter().export(iDataSet, new BuilderExportConfig(BuilderType.DEFAULT, outputDir));
        assertThat(contentOf(outputDir)).isEqualTo("DataSetBuilder builder = new DataSetBuilder();" + NEW_LINE +
                "IDataSet dataSet = builder" + NEW_LINE +
                "    .table(\"FOLLOWER\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"ID\", 1)" + NEW_LINE +
                "        .column(\"USER_ID\", 1)" + NEW_LINE +
                "        .column(\"FOLLOWER_ID\", 2)" + NEW_LINE +
                "    .table(\"SEQUENCE\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"SEQ_NAME\", \"SEQ_GEN\")" + NEW_LINE +
                "        .column(\"SEQ_COUNT\", 0)" + NEW_LINE +
                "    .table(\"TWEET\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"ID\", \"abcdef12345\")" + NEW_LINE +
                "        .column(\"CONTENT\", \"dbunit rules json example\")" + NEW_LINE +
                "        .column(\"DATE\", \"" + iDataSet.getTable("TWEET").getValue(0,"DATE") + "\")" + NEW_LINE +
                "        .column(\"LIKES\", null)" + NEW_LINE +
                "        .column(\"TIMESTAMP\", null)" + NEW_LINE +
                "        .column(\"USER_ID\", 1)" + NEW_LINE +
                "    .table(\"USER\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"ID\", 1)" + NEW_LINE +
                "        .column(\"NAME\", \"@realpestano\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"ID\", 2)" + NEW_LINE +
                "        .column(\"NAME\", \"@dbunit\").build();");
    }

    @Test
    public void shouldExportJSONDataSetUsingColumnValuesSyntax() throws DataSetException {
        executor.createDataSet(new DataSetConfig("json/users.json"));
        IDataSet iDataSet = createDataSetFromDatabase();
        File outputDir = Paths.get("target/FromJSONBuilder.java").toAbsolutePath().toFile();
        new DataSetBuilderExporter().export(iDataSet, new BuilderExportConfig(BuilderType.COLUMNS_VALUES, outputDir));
        assertThat(contentOf(outputDir)).isEqualTo("DataSetBuilder builder = new DataSetBuilder();" + NEW_LINE +
                "IDataSet dataSet = builder" + NEW_LINE +
                "    .table(\"FOLLOWER\")" + NEW_LINE +
                "        .columns(\"ID\", \"USER_ID\", \"FOLLOWER_ID\")" + NEW_LINE +
                "        .values(1, 1, 2)" + NEW_LINE +
                "    .table(\"SEQUENCE\")" + NEW_LINE +
                "        .columns(\"SEQ_NAME\", \"SEQ_COUNT\")" + NEW_LINE +
                "        .values(\"SEQ_GEN\", 0)" + NEW_LINE +
                "    .table(\"TWEET\")" + NEW_LINE +
                "        .columns(\"ID\", \"CONTENT\", \"DATE\", \"LIKES\", \"TIMESTAMP\", \"USER_ID\")" + NEW_LINE +
                "        .values(\"abcdef12345\", \"dbunit rules json example\", \"" +
                "" + iDataSet.getTable("TWEET").getValue(0,"DATE") +
                "\", null, null, 1)" + NEW_LINE +
                "    .table(\"USER\")" + NEW_LINE +
                "        .columns(\"ID\", \"NAME\")" + NEW_LINE +
                "        .values(1, \"@realpestano\")" + NEW_LINE +
                "        .values(2, \"@dbunit\").build();");
    }

    private IDataSet createDataSetFromDatabase() {
        DatabaseConnection databaseConnection = null;
        try {
            databaseConnection = new DatabaseConnection(emProvider.connection());
            DatabaseConfig config = databaseConnection.getConfig();
            config.setProperty(DatabaseConfig.PROPERTY_RESULTSET_TABLE_FACTORY, new ForwardOnlyResultSetTableFactory());
            return databaseConnection.createDataSet();
        } catch (Exception e) {
            throw new RuntimeException("Could not create dataset from database", e);
        }

    }

    private InputStream getDataSetStream(String dataSet) {
        if (!dataSet.startsWith("/")) {
            dataSet = "/" + dataSet;
        }
        InputStream is = getClass().getResourceAsStream(dataSet);
        if (is == null) {
            throw new RuntimeException(
                    String.format("Could not find dataset '%s' under 'resources' directory.",
                            dataSet.substring(1)));
        }
        return is;
    }
}
