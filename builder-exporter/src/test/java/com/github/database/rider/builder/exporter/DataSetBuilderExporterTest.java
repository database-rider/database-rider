package com.github.database.rider.builder.exporter;

import com.github.database.rider.builder.exporter.model.BuilderType;
import com.github.database.rider.core.api.dataset.JSONDataSet;
import com.github.database.rider.core.api.dataset.YamlDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

public class DataSetBuilderExporterTest {
    
    final static String NEW_LINE = System.getProperty("line.separator");

    @Test
    public void shouldExportYamlDataSetUsingDefaultSyntax() {
        IDataSet iDataSet = new YamlDataSet(getDataSetStream("users.yml"),null);
        File outputDir = Paths.get("target/FromYamlDefaultBuilder.java").toAbsolutePath().toFile();
        new DataSetBuilderExporter().export(iDataSet, new BuilderExportConfig(BuilderType.DEFAUTT, outputDir));
        assertThat(contentOf(outputDir)).isEqualTo("DataSetBuilder builder = new DataSetBuilder();" + NEW_LINE +
                "IDataSet dataSet = builder" + NEW_LINE +
                "    .table(\"TWEET\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"DATE\", \"[DAY,NOW]\")" + NEW_LINE +
                "        .column(\"USER_ID\", 1)" + NEW_LINE +
                "        .column(\"ID\", \"abcdef12345\")" + NEW_LINE +
                "        .column(\"CONTENT\", \"dbunit rules!\")" + NEW_LINE +
                "    .table(\"USER\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"ID\", 1)" + NEW_LINE +
                "        .column(\"NAME\", \"@realpestano\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"ID\", 2)" + NEW_LINE +
                "        .column(\"NAME\", \"@dbunit\")" + NEW_LINE +
                "    .table(\"FOLLOWER\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"USER_ID\", 1)" + NEW_LINE +
                "        .column(\"ID\", 1)" + NEW_LINE +
                "        .column(\"FOLLOWER_ID\", 2).build();");
    }

    @Test
    public void shouldExportYamlDataSetUsingColumnValuesSyntax() {
        IDataSet iDataSet = new YamlDataSet(getDataSetStream("users.yml"),null);
        File outputDir = Paths.get("target/FromYamlBuilder.java").toAbsolutePath().toFile();
        new DataSetBuilderExporter().export(iDataSet, new BuilderExportConfig(BuilderType.COLUMNS_VALUES, outputDir));
        assertThat(contentOf(outputDir)).isEqualTo("DataSetBuilder builder = new DataSetBuilder();" + NEW_LINE +
                "IDataSet dataSet = builder" + NEW_LINE +
                "    .table(\"TWEET\")" + NEW_LINE +
                "        .columns(\"DATE\", \"USER_ID\", \"ID\", \"CONTENT\")" + NEW_LINE +
                "        .values(\"[DAY,NOW]\", 1, \"abcdef12345\", \"dbunit rules!\")" + NEW_LINE +
                "    .table(\"USER\")" + NEW_LINE +
                "        .columns(\"ID\", \"NAME\")" + NEW_LINE +
                "        .values(1, \"@realpestano\")" + NEW_LINE +
                "        .values(2, \"@dbunit\")" + NEW_LINE +
                "    .table(\"FOLLOWER\")" + NEW_LINE +
                "        .columns(\"USER_ID\", \"ID\", \"FOLLOWER_ID\")" + NEW_LINE +
                "        .values(1, 1, 2).build();");
    }

    @Test
    public void shouldExportXMLDataSetUsingDefaultSyntax() throws DataSetException {
        IDataSet iDataSet = new FlatXmlDataSetBuilder().build(getDataSetStream("users.xml"));
        File outputDir = Paths.get("target/FromXMlDefaultBuilder.java").toAbsolutePath().toFile();
        new DataSetBuilderExporter().export(iDataSet, new BuilderExportConfig(BuilderType.DEFAUTT, outputDir));
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
        IDataSet iDataSet = new FlatXmlDataSetBuilder().build(getDataSetStream("users.xml"));
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
        IDataSet iDataSet = new JSONDataSet(getDataSetStream("users.json"));
        File outputDir = Paths.get("target/FromJSONDefaultBuilder.java").toAbsolutePath().toFile();
        new DataSetBuilderExporter().export(iDataSet, new BuilderExportConfig(BuilderType.DEFAUTT, outputDir));
        assertThat(contentOf(outputDir)).isEqualTo("DataSetBuilder builder = new DataSetBuilder();" + NEW_LINE +
                "IDataSet dataSet = builder" + NEW_LINE +
                "    .table(\"USER\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"id\", 1)" + NEW_LINE +
                "        .column(\"name\", \"@realpestano\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"id\", 2)" + NEW_LINE +
                "        .column(\"name\", \"@dbunit\")" + NEW_LINE +
                "    .table(\"TWEET\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"id\", \"abcdef12345\")" + NEW_LINE +
                "        .column(\"content\", \"dbunit rules json example\")" + NEW_LINE +
                "        .column(\"date\", \"2013-01-20\")" + NEW_LINE +
                "        .column(\"user_id\", 1)" + NEW_LINE +
                "    .table(\"FOLLOWER\")" + NEW_LINE +
                "    .row()" + NEW_LINE +
                "        .column(\"id\", 1)" + NEW_LINE +
                "        .column(\"user_id\", 1)" + NEW_LINE +
                "        .column(\"follower_id\", 2).build();");
    }

    @Test
    public void shouldExportJSONDataSetUsingColumnValuesSyntax() throws DataSetException {
        IDataSet iDataSet = new JSONDataSet(getDataSetStream("users.json"));
        File outputDir = Paths.get("target/FromJSONBuilder.java").toAbsolutePath().toFile();
        new DataSetBuilderExporter().export(iDataSet, new BuilderExportConfig(BuilderType.COLUMNS_VALUES, outputDir));
        assertThat(contentOf(outputDir)).isEqualTo("DataSetBuilder builder = new DataSetBuilder();" + NEW_LINE +
                "IDataSet dataSet = builder" + NEW_LINE +
                "    .table(\"USER\")" + NEW_LINE +
                "        .columns(\"id\", \"name\")" + NEW_LINE +
                "        .values(1, \"@realpestano\")" + NEW_LINE +
                "        .values(2, \"@dbunit\")" + NEW_LINE +
                "    .table(\"TWEET\")" + NEW_LINE +
                "        .columns(\"id\", \"content\", \"date\", \"user_id\")" + NEW_LINE +
                "        .values(\"abcdef12345\", \"dbunit rules json example\", \"2013-01-20\", 1)" + NEW_LINE +
                "    .table(\"FOLLOWER\")" + NEW_LINE +
                "        .columns(\"id\", \"user_id\", \"follower_id\")" + NEW_LINE +
                "        .values(1, 1, 2).build();");
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
