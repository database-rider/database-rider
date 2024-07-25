package com.github.database.rider.core.api.dataset;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dbunit.dataset.ITable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * DBUnit DataSet format for JSON based datasets.
 * It uses Jackson, a fast JSON processor, to parse the JSON into a
 * java.util.Map which is then fed into the MapDataSet parent implementation.
 *
 * @author Lieven DOCLO
 * @author Björn Beskow
 */
public class JSONDataSet extends MapDataSet {

	// The parser for the dataset JSON file
	private final JSONITableParser jsonTableParser = new JSONITableParser();

	private final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Creates a JSON dataset based on a file.
	 * 
	 * @param file
	 *            A JSON dataset file
	 */
	public JSONDataSet(File file) {
		tables = jsonTableParser.getTables(file);
	}

	/**
	 * Creates a JSON dataset based on an inputstream.
	 * 
	 * @param is
	 *            An inputstream pointing to a JSON dataset
	 */
	public JSONDataSet(InputStream is) {
		tables = jsonTableParser.getTables(is);
	}

	private class JSONITableParser {

		/**
		 * Parses a JSON dataset file and returns the list of DBUnit tables
		 * contained in that file.
		 * 
		 * @param jsonFile
		 *            A JSON dataset file
		 * @return A list of DBUnit tables
		 */
		public List<ITable> getTables(File jsonFile) {
			try {
				return getTables(new FileInputStream(jsonFile));
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}

		/**
		 * Parses a JSON dataset input stream and returns the list of DBUnit
		 * tables contained in that input stream.
		 * 
		 * @param jsonStream
		 *            A JSON dataset input stream
		 * @return A list of DBUnit tables
		 */
		@SuppressWarnings("unchecked")
		public List<ITable> getTables(InputStream jsonStream) {
			try {
				// get the base object tree from the JSON stream
				Map<String, Object> dataset = mapper.readValue(jsonStream, Map.class);
                dataset.remove("$schema");
				return mapTableParser.getTables(dataset);

			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	}
}
