package com.github.database.rider.core.exporter.builder;

import org.dbunit.dataset.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import static com.github.database.rider.core.api.exporter.BuilderType.DEFAULT;

public class DataSetBuilderExporter {

    private final Logger LOGGER = LoggerFactory.getLogger(DataSetBuilderExporter.class);
    private final String NEW_LINE = System.getProperty("line.separator");
    private final String FOUR_SPACES = "    ";
    /**
     * Generates a dataset builder java snippet based on a dbunit dataset
     *
     * @param dataSet dbunit dataset
     * @param config  exporter configuration
     */
    public void export(IDataSet dataSet, BuilderExportConfig config) {

        try {
            StringBuilder source = new StringBuilder("DataSetBuilder builder = new DataSetBuilder();").append(NEW_LINE)
                    .append("IDataSet dataSet = builder");

            boolean defaultSyntax = DEFAULT == config.getType();
            ITableIterator datasetIterator = dataSet.iterator();
            while (datasetIterator.next()) {
                ITable table = datasetIterator.getTable();
                source.append(NEW_LINE+ FOUR_SPACES +".table(\"").append(table.getTableMetaData().getTableName()).append("\")").append(NEW_LINE);
                if(!defaultSyntax) {
                    generateBuilderUsingColumnsValuesSyntax(table, source);
                } else {
                    generateBuilderUsingDefaultSyntax(table, source);
                }
            }
            source.append(".build();");
            try(FileOutputStream fout = new FileOutputStream(config.getOutputDir())) {
                fout.write(source.toString().getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
           LOGGER.error("Could not generate DataSetBuilder for given dataset.", e);
        }
    }

    protected void generateBuilderUsingDefaultSyntax(ITable table, StringBuilder source) throws DataSetException {
        ITableMetaData tableMetaData = table.getTableMetaData();
        for (int i = 0; i < table.getRowCount(); i++) {
            final boolean isLastRow = (i == table.getRowCount() - 1);
            source.append(FOUR_SPACES).append(".row()").append(NEW_LINE);
            for (int j = 0; j < tableMetaData.getColumns().length; j++) {
                final boolean isLastColumn = (j == tableMetaData.getColumns().length - 1);
                Column column = tableMetaData.getColumns()[j];
                source.append(FOUR_SPACES + FOUR_SPACES).append(".column(\"").append(column.getColumnName()).append("\", ");
                Object value = table.getValue(i,tableMetaData.getColumns()[j].getColumnName());
                appendColumnValue(source, column, value);
                source.append(")");
                if(!isLastColumn || !isLastRow) {
                    source.append(NEW_LINE);
                }
            }
        }
    }

    private void appendColumnValue(StringBuilder source, Column column, Object value) {
        boolean isNumeric = column.getDataType().isNumber();
        if(!isNumeric) {
            source.append("\"");
        }
        source.append(value);
        if(!isNumeric) {
            source.append("\"");
        }
    }

    protected void generateBuilderUsingColumnsValuesSyntax(ITable table, StringBuilder source) throws DataSetException {
        ITableMetaData tableMetaData = table.getTableMetaData();
        source.append(FOUR_SPACES + FOUR_SPACES).append(".columns(");

        for (Column column : tableMetaData.getColumns()) {
            source.append("\""+column.getColumnName()).append("\", ");
        }
        if(source.indexOf(",") != -1) {
            //remove lastcomma and its space
            int indexOfComma = source.lastIndexOf(",");
            source.deleteCharAt(indexOfComma);
            source.deleteCharAt(indexOfComma);
        }
        source.append(")").append(NEW_LINE+ FOUR_SPACES);
        for (int i = 0; i < table.getRowCount(); i++) {
            final boolean isLastRow = (i == table.getRowCount() - 1);
            source.append(FOUR_SPACES +".values(");
            for (int j = 0; j < tableMetaData.getColumns().length; j++) {
                final boolean lastColumn = (j == tableMetaData.getColumns().length - 1);
                Column column = tableMetaData.getColumns()[j];
                Object value = table.getValue(i,tableMetaData.getColumns()[j].getColumnName());
                appendColumnValue(source, column, value);
                if(!lastColumn) {
                    source.append(", ");
                } else {
                    source.append(",");
                }
            }
            source.deleteCharAt(source.lastIndexOf(","));
            source.append(")");
            if(!isLastRow) { //not last row?
                source.append(NEW_LINE+ FOUR_SPACES);
            }
        }
    }
}
