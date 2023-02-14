package com.github.database.rider.core.exporter;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dbunit.dataset.excel.XlsDataSetWriter;
import org.apache.poi.ss.usermodel.Workbook;

public class XlsxDataSetWriter extends XlsDataSetWriter {

    @Override
    protected Workbook createWorkbook() {
        return new XSSFWorkbook();
    }
}
