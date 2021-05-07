package com.github.database.rider.core.dataset.writer;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.stream.DataSetProducerAdapter;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

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

	public JSONWriter(OutputStream outputStream, IDataSet dataSet) {
		out = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
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
			String sb = createSetFromValues(values);
			out.write(sb);
			if(dataSet.getTable(metaData.getTableName()).getRowCount() != rowCount ){
				out.write(FOUR_SPACES+"},"+NEW_LINE);
			}else {
				out.write(FOUR_SPACES+"}"+NEW_LINE);
			}
		} catch (Exception e) {
			logger.warn("Could not write row.", e);
		}
	}

	private String createSetFromValues(Object[] values) throws DataSetException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			Object currentValue = values[i];
			if(currentValue == null){
					continue;
			}

			Column currentColumn = metaData.getColumns()[i];
			sb.append(FOUR_SPACES + DOUBLE_SPACES + '"').append(metaData.getColumns()[i].getColumnName()).append("\": ");
			boolean isNumber = currentColumn.getDataType().isNumber();
			if (!isNumber) {
				sb.append('"');
			}
            if(values[i] instanceof byte[]){
                sb.append(DatatypeConverter.printBase64Binary((byte[]) values[i]).replace(NEW_LINE, "\\n"));
            } else {
                sb.append(currentValue.toString()
						.replace("\\", "\\\\")
						.replace("\"", "\\\"")
						.replace(NEW_LINE, "\\n")
				);
            }
			if (!isNumber) {
				sb.append('"');
			}
			if(i != values.length-1){
				sb.append(',');
			}
			sb.append(NEW_LINE);

		}
		return replaceExtraCommaInTheEnd(sb);
	}

	private String replaceExtraCommaInTheEnd(StringBuilder sb) {
		int indexOfPenultimateSymbol = sb.length() - 2;
		if(sb.length() > 1 && sb.charAt(indexOfPenultimateSymbol) == ','){
			sb.deleteCharAt(indexOfPenultimateSymbol);
		}
		return sb.toString();
	}

	public synchronized void write() throws DataSetException {
		DataSetProducerAdapter provider = new DataSetProducerAdapter(dataSet);
		provider.setConsumer(this);
		provider.produce();
	}
}
