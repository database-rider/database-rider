package com.github.database.rider.core.api.dataset;

import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;

import java.util.*;

/**
 * DBUnit DataSet format for java.util.Map based datasets.
 * On the top level, each key is a Table name, and the value is a java.util.List
 * of columns. Each column is again a java.util.Map, mapping column names to values.
 * The implementation is similar to the flat XML layout, but has some improvements
 * (columns are calculated by parsing the entire dataset, not just the first row).
 *
 * @author Lieven DOCLO
 * @author Björn Beskow
 */
public class MapDataSet extends AbstractDataSet {

	// The parser for the dataset Map
	protected MapITableParser mapTableParser = new MapITableParser();

	// The tables after parsing
	protected List<ITable> tables;

	protected MapDataSet() { }

	/**
	 * Creates a dataset based on a java.util.Map.
	 *
	 * @param dataset
	 *            A dataset Map object
	 */
	public MapDataSet(Map<String, Object> dataset) {
		tables = mapTableParser.getTables(dataset);
	}

	@Override
	protected ITableIterator createIterator(boolean reverse) {
		return new DefaultTableIterator(
				tables.toArray(new ITable[tables.size()]));
	}

	protected static class MapITableParser {

		/**
		 * Transforms a dataset object represented as a Map into
		 * a list of DBUnit tables.
		 *
		 * @param dataset
		 *           A dataset Map object
		 * @return A list of DBUnit tables
		 */
		protected List<ITable> getTables(Map<String, Object> dataset) {
			List<ITable> tables = new ArrayList<>();

			// iterate over the tables in the object tree
			for (Map.Entry<String, Object> entry : dataset.entrySet()) {
				// get the rows for the table
				List<Map<String, Object>> rows = (List<Map<String, Object>>) entry.getValue();
				ITableMetaData meta = getMetaData(entry.getKey(), rows);
				// create a table based on the metadata
				DefaultTable table = new DefaultTable(meta);
				int rowIndex = 0;
				// iterate through the rows and fill the table
				for (Map<String, Object> row : rows) {
					fillRow(table, row, rowIndex++);
				}
				// add the table to the list of DBUnit tables
				tables.add(table);
			}

			return tables;
		}

		/**
		 * Gets the table meta data based on the rows for a table
		 * 
		 * @param tableName
		 *            The name of the table
		 * @param rows
		 *            The rows of the table
		 * @return The table metadata for the table
		 */
		private ITableMetaData getMetaData(String tableName,
				List<Map<String, Object>> rows) {
			Set<String> columns = new LinkedHashSet<>();
			// iterate through the dataset and add the column names to a set
			for (Map<String, Object> row : rows) {
				for (Map.Entry<String, Object> column : row.entrySet()) {
					columns.add(column.getKey());
				}
			}
			List<Column> list = new ArrayList<>(columns.size());
			// create a list of DBUnit columns based on the column name set
			for (String s : columns) {
				list.add(new Column(s, DataType.UNKNOWN));
			}
			return new DefaultTableMetaData(tableName,
					list.toArray(new Column[list.size()]));
		}

		/**
		 * Fill a table row
		 * 
		 * @param table
		 *            The table to be filled
		 * @param row
		 *            A map containing the column values
		 * @param rowIndex
		 *            The index of the row to te filled
		 */
		private void fillRow(DefaultTable table, Map<String, Object> row,
				int rowIndex) {
			try {
				if(!row.entrySet().isEmpty()){
					table.addRow();
					// set the column values for the current row
					for (Map.Entry<String, Object> column : row.entrySet()) {
						table.setValue(rowIndex, column.getKey(), column.getValue());
					}
				}

			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	}

}
