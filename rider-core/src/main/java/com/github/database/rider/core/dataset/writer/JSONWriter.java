package com.github.database.rider.core.dataset.writer;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.stream.DataSetProducerAdapter;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by pestano on 11/09/16.
 */
public class JSONWriter implements IDataSetConsumer {

	private static final String NEW_LINE = System.getProperty("line.separator");
	private static final String DOUBLE_SPACES = "  ";
	private static final String FOUR_SPACES = DOUBLE_SPACES+DOUBLE_SPACES;
	
    private static final Logger logger = LoggerFactory.getLogger(JSONWriter.class);

	
	private IDataSet dataSet;

	private OutputStreamWriter out;
	private ITableMetaData metaData;
	private int tableCount;
	private int rowCount;

	public JSONWriter(OutputStream outputStream, IDataSet dataSet) throws IOException {
		out = new OutputStreamWriter(outputStream, "UTF-8");
		this.dataSet = dataSet;
	}

	@Override
	public void startDataSet() throws DataSetException {
		try {
			tableCount = 0;
			out.write("{"+NEW_LINE);
		} catch (IOException e) {
			logger.warn("Could not start dataset.", e);
		}
	}

	@Override
	public void endDataSet() throws DataSetException {
		try {
			out.write("}");
			out.flush();
		} catch (IOException e) {
			logger.warn("Could not end dataset.", e);
		}
	}

	@Override
	public void startTable(ITableMetaData metaData) throws DataSetException {
		this.metaData = metaData;
		rowCount = 0;
		try {
			out.write(DOUBLE_SPACES+"\""+metaData.getTableName() + "\": [" + NEW_LINE);
		} catch (IOException e) {
			logger.warn("Could not start table.", e);
		}
	}

	@Override
	public void endTable() throws DataSetException {
		try {
			tableCount++;
			if(dataSet.getTableNames().length == tableCount){
				out.write(DOUBLE_SPACES+"]"+NEW_LINE);
			}else{
				out.write(DOUBLE_SPACES+"],"+NEW_LINE);
			}
		} catch (IOException e) {
			logger.warn("Could end table.", e);
		}
	}

	@Override
	public void row(Object[] values) throws DataSetException {
		rowCount++;
		try {
			out.write(FOUR_SPACES+"{"+NEW_LINE);
			for (int i = 0; i < values.length; i++) {
				if(values[i] == null){
					continue;
				}

				Column currentColumn = metaData.getColumns()[i];
				out.write(FOUR_SPACES+DOUBLE_SPACES+"\""+metaData.getColumns()[i].getColumnName() + "\": ");
				boolean isNumber = currentColumn.getDataType().isNumber();
				if (!isNumber) {
					out.write("\"");
				}
				
			    out.write(values[i].toString());
			    
				if (!isNumber) {
					out.write("\"");
				}
				if(i != values.length-1){
					out.write(",");
				}
				out.write(NEW_LINE);
				
			}
			if(dataSet.getTable(metaData.getTableName()).getRowCount() != rowCount ){
				out.write(FOUR_SPACES+"},"+NEW_LINE);
			}else {
				out.write(FOUR_SPACES+"}"+NEW_LINE);

			}

			
		} catch (Exception e) {
			logger.warn("Could not write row.", e);
		}
	}

	public synchronized void write() throws DataSetException {
		DataSetProducerAdapter provider = new DataSetProducerAdapter(dataSet);
		provider.setConsumer(this);
		provider.produce();
	}
}
