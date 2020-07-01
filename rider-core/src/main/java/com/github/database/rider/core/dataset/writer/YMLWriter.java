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

/**
 * Created by pestano on 11/09/16.
 */
public class YMLWriter implements IDataSetConsumer {

    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String DOUBLE_SPACES = "  ";
    private static final Logger logger = LoggerFactory.getLogger(YMLWriter.class);


    private OutputStreamWriter out;
    private ITableMetaData metaData;

    public YMLWriter(OutputStream outputStream) throws IOException {
        out = new OutputStreamWriter(outputStream, "UTF-8");
    }

    @Override
    public void startDataSet() throws DataSetException {

    }

    @Override
    public void endDataSet() throws DataSetException {
        try {
            out.flush();
        } catch (IOException e) {
        	logger.warn("Could not end dataset.", e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                logger.error("Could not close dataset writer.", e);
            }
        }
    }

    @Override
    public void startTable(ITableMetaData metaData) throws DataSetException {
        this.metaData = metaData;
        try {
            out.write(metaData.getTableName()+":"+NEW_LINE);
        } catch (IOException e) {
        	logger.warn("Could not start table.", e);
        }
    }

    @Override
    public void endTable() throws DataSetException {
        try {
            out.write(NEW_LINE);
        } catch (IOException e) {
        	logger.warn("Could end table.", e);
        }
    }

    @Override
    public void row(Object[] values) throws DataSetException {
        try {
            for (int i = 0; i < values.length; i++) {

                if(values[i] == null){
                    continue;
                }

                if (i == 0) {
                    out.write(DOUBLE_SPACES + "- ");
                } else{
                    out.write(DOUBLE_SPACES + DOUBLE_SPACES);
                }
                
                Column currentColumn = metaData.getColumns()[i];
                out.write(metaData.getColumns()[i].getColumnName()+": ");
                boolean isNumber = currentColumn.getDataType().isNumber();
                if(!isNumber){
                    out.write("\"");
                }
                if(values[i] != null){
                    if(values[i] instanceof byte[]){
                        out.write(DatatypeConverter.printBase64Binary((byte[]) values[i]));
                    } else {
                        out.write(values[i].toString());
                    }
                }
                if(!isNumber){
                    out.write("\"");
                }
                out.write(NEW_LINE);
            }
        }catch (Exception e){
        	logger.warn("Could not write row.",e);
        }
    }

    public synchronized void write(IDataSet dataSet) throws DataSetException
    {
        DataSetProducerAdapter provider = new DataSetProducerAdapter(dataSet);
        provider.setConsumer(this);
        provider.produce();
    }
}
