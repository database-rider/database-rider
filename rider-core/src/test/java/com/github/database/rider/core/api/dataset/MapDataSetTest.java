package com.github.database.rider.core.api.dataset;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.junit.Test;

public class MapDataSetTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testParseMap() throws IOException, DataSetException {
        InputStream jsonData = MapDataSetTest.class.getResourceAsStream("/datasets/json/users.json");
        Map<String, Object> map = mapper.readValue(jsonData, Map.class);
        MapDataSet dataSet = new MapDataSet(map);
        assertArrayEquals(new String[]{"USER", "TWEET", "FOLLOWER"}, dataSet.getTableNames());
        ITable userTable = dataSet.getTable("USER");
        assertEquals(2, userTable.getRowCount());
        assertEquals(1, userTable.getValue(0, "id"));
        assertEquals("@realpestano", userTable.getValue(0, "name"));
    }
}
