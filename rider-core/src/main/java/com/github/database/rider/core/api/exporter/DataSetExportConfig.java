package com.github.database.rider.core.api.exporter;

import com.github.database.rider.core.api.dataset.DataSetFormat;

/**
 * Created by pestano on 09/09/16.
 */
public class DataSetExportConfig {

    private DataSetFormat dataSetFormat = DataSetFormat.YML;
    private String[] includeTables;
    private String[] queryList;
    private boolean dependentTables = true;
    private String outputFileName;

    public DataSetExportConfig dataSetFormat(DataSetFormat dataSetFormat) {
        this.dataSetFormat = dataSetFormat;
        return this;
    }

    public DataSetExportConfig includeTables(String[] includeTables) {
        this.includeTables = includeTables;
        return this;
    }

    public DataSetExportConfig dependentTables(boolean dependentTables) {
        this.dependentTables = dependentTables;
        return this;
    }

    public DataSetExportConfig queryList(String[] queryList){
        this.queryList = queryList;
        return this;
    }

    public DataSetExportConfig outputFileName(String outputFileName){
        this.outputFileName = outputFileName;
        return this;
    }

    public DataSetFormat getDataSetFormat() {
        return dataSetFormat;
    }

    public String[] getIncludeTables() {
        return includeTables;
    }

    public boolean isDependentTables() {
        return dependentTables;
    }

    public String[] getQueryList() {
        return queryList;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public static DataSetExportConfig from(ExportDataSet dataSetExport){
        return new DataSetExportConfig().
                dataSetFormat(dataSetExport.format()).dependentTables(dataSetExport.dependentTables()).
                outputFileName(dataSetExport.outputName()).
                includeTables(dataSetExport.includeTables()).queryList(dataSetExport.queryList());
    }


}
