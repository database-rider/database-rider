package com.github.database.rider.core.exporter;

import com.github.database.rider.core.api.exporter.BuilderType;
import com.github.database.rider.core.api.exporter.DataSetExportConfig;
import com.github.database.rider.core.dataset.writer.JSONWriter;
import com.github.database.rider.core.dataset.writer.YMLWriter;
import com.github.database.rider.core.exporter.builder.BuilderExportConfig;
import com.github.database.rider.core.exporter.builder.DataSetBuilderExporter;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.*;
import org.dbunit.database.search.TablesDependencyHelper;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.dbunit.dataset.excel.XlsDataSetWriter;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pestano on 09/09/16.
 *
 * based on: http://archive.oreilly.com/pub/post/dbunit_made_easy.html
 */
public class DataSetExporter {

    /**
     * A regular expression that is used to get the table name
     * from a SQL 'select' statement.
     * This  pattern matches a string that starts with any characters,
     * followed by the case-insensitive word 'from',
     * followed by a table name of the form 'foo' or 'schema.foo',
     * followed by any number of remaining characters.
     */
    private static final Pattern TABLE_MATCH_PATTERN = Pattern.compile(".*\\s+from\\s+(\\w+(\\.\\w+)?).*",
            Pattern.CASE_INSENSITIVE);

    private static Logger log = LoggerFactory.getLogger(DataSetExporter.class.getName());


    private static DataSetExporter instance;

    private DataSetExporter() {
    }

    public static DataSetExporter getInstance() {
        if (instance == null) {
            instance = new DataSetExporter();
        }
        return instance;
    }

    public OutputStream export(Connection connection, DataSetExportConfig dataSetExportConfig) throws SQLException, DatabaseUnitException {
        return export(connection, dataSetExportConfig, null);
    }

    public OutputStream export(Connection connection, DataSetExportConfig dataSetExportConfig, String schema) throws SQLException, DatabaseUnitException {
        return export(new DatabaseConnection(connection, schema), dataSetExportConfig);
    }

    public OutputStream export(DatabaseConnection databaseConnection, DataSetExportConfig dataSetExportConfig) throws SQLException, DatabaseUnitException {

        if (databaseConnection == null || databaseConnection.getConnection() == null || databaseConnection.getConnection().isClosed()) {
            throw new RuntimeException("Provide a valid connection to export datasets");
        }

        if (dataSetExportConfig == null) {
            dataSetExportConfig = new DataSetExportConfig();
        }

        String outputFile = dataSetExportConfig.getOutputFileName();

        if (outputFile == null || "".equals(outputFile)) {
            throw new RuntimeException("Provide output file name to export dataset.");
        }

        if (!outputFile.contains(".")) {
            outputFile = outputFile + "." + dataSetExportConfig.getDataSetFormat().name().toLowerCase();
        }

        if (outputFile.contains("/") && System.getProperty("os.name").toLowerCase().contains("win")) {
            outputFile = outputFile.replace("/", "\\");
        }

        boolean hasIncludes = dataSetExportConfig.getIncludeTables() != null && dataSetExportConfig.getIncludeTables().length > 0;


        DatabaseConfig config = databaseConnection.getConfig();
        config.setProperty(DatabaseConfig.PROPERTY_RESULTSET_TABLE_FACTORY, new ForwardOnlyResultSetTableFactory());

        Set<String> targetTables = new HashSet<>();

        if (hasIncludes) {
            targetTables.addAll(Arrays.asList(dataSetExportConfig.getIncludeTables()));
            if (dataSetExportConfig.isDependentTables()) {
                String[] dependentTables = TablesDependencyHelper.getAllDependentTables(databaseConnection, dataSetExportConfig.getIncludeTables());
                if (dependentTables != null && dependentTables.length > 0) {
                    targetTables.addAll(Arrays.asList(dependentTables));
                }
            }
        }

        IDataSet dataSet = new QueryDataSet(databaseConnection);
        if ((!targetTables.isEmpty()) || (dataSetExportConfig.getQueryList() != null && dataSetExportConfig.getQueryList().length > 0)) {
            addQueries((QueryDataSet) dataSet, dataSetExportConfig.getQueryList(), targetTables);
        } else {
            dataSet = databaseConnection.createDataSet();
        }

        FileOutputStream fos = null;
        FileOutputStream fosDtd = null;
        try {
            if (outputFile.contains(System.getProperty("file.separator"))) {
                String pathWithoutFileName = outputFile.substring(0, outputFile.lastIndexOf(System.getProperty("file.separator")) + 1);
                new File(pathWithoutFileName).mkdirs();
            }
            fos = new FileOutputStream(outputFile);
            switch (dataSetExportConfig.getDataSetFormat()) {
                case XML_DTD: {
                    FlatXmlDataSet.write(dataSet, fos);
                    //dtd file has the same name but other file extension
                    fosDtd = new FileOutputStream(outputFile.substring(0, outputFile.lastIndexOf('.')) + ".dtd");
                    FlatDtdDataSet.write(dataSet, fosDtd);
                    break;
                }
                case XML: {
                    FlatXmlDataSet.write(dataSet, fos);
                    break;
                }
                case YML: {
                    new YMLWriter(fos).write(dataSet);
                    break;
                }
                case XLS: {
                    config.setProperty(DatabaseConfig.PROPERTY_RESULTSET_TABLE_FACTORY, new CachedResultSetTableFactory());
                    new XlsDataSetWriter().write(dataSet, fos);
                    break;
                }
                case CSV: {
                    //csv needs a directory instead of file
                    outputFile = outputFile.substring(0, outputFile.lastIndexOf('.'));
                    CsvDataSetWriter.write(dataSet, new File(outputFile));
                    break;
                }
                case JSON: {
                    config.setProperty(DatabaseConfig.PROPERTY_RESULTSET_TABLE_FACTORY, new CachedResultSetTableFactory());
                    new JSONWriter(fos, dataSet).write();
                    break;
                }
                default: {
                    throw new RuntimeException("Format not supported.");
                }
            }
            log.info("DataSet exported successfully at " + Paths.get(outputFile).toAbsolutePath().toString());

            boolean generateBuilder = BuilderType.NONE != dataSetExportConfig.getBuilderType();
            if(generateBuilder) {
                String builderName = outputFile.substring(0, outputFile.lastIndexOf('.'))+".java";
                new DataSetBuilderExporter().export(dataSet, new BuilderExportConfig(dataSetExportConfig.getBuilderType(), new File(builderName)));
            }
        } catch (Exception e) {
            log.error("Could not export dataset.", e);
            throw new RuntimeException("Could not export dataset.", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error("Could not close file output stream.", e);
                }
            }
            if (fosDtd != null) {
                try {
                    fosDtd.close();
                } catch (IOException e) {
                    log.error("Could not close file output stream for dtd file.", e);
                }
            }
            //set back default ResultSetTableFactory
            config.setProperty(DatabaseConfig.PROPERTY_RESULTSET_TABLE_FACTORY, new CachedResultSetTableFactory());

        }

        return null;
    }

    private void addQueries(QueryDataSet dataSet, String[] queryList, Set<String> targetTables) {
        try {
            for (String targetTable : targetTables) {
                dataSet.addTable(targetTable);
            }
            for (String query : queryList) {
                //gets the first select to extract table
                Matcher m = TABLE_MATCH_PATTERN.matcher(query.split("(?i)select")[1]);
                if (!m.matches()) {
                    log.warn("Unable to parse query. Ignoring '" + query + "'.");
                } else {
                    String table = m.group(1);
                    if (targetTables.contains(table)) {
                        //already in includes
                        log.warn(String.format("Ignoring query %s because its table is already in includedTables.", query));
                    } else {
                        dataSet.addTable(table, query);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Could not add query due to following error.", e);
        }

    }
}
