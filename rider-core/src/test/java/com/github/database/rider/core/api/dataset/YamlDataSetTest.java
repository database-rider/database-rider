package com.github.database.rider.core.api.dataset;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.configuration.Orthography;
import com.github.database.rider.core.configuration.DBUnitConfig;
import org.dbunit.dataset.DataSetException;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@DBUnit
public class YamlDataSetTest {

    @Test
    public void testApplyCase_Default() {
        DBUnitConfig dbUnitConfig = DBUnitConfig.from(YamlDataSetTest.class.getAnnotation(DBUnit.class));
        // uses default config (dbunit.yml plus @DBUnit of this class)
        YamlDataSet dataSet = new YamlDataSet(YamlDataSetTest.class.getResourceAsStream("/datasets/yml/user.yml"),
                dbUnitConfig);

        assertEquals("USER", dataSet.applyCaseInsensitivity("uSeR"));
    }

    @Test
    public void testApplyCase_StrategyLowerCase() {
        DBUnitConfig dbUnitConfig = DBUnitConfig.from(YamlDataSetTest.class.getAnnotation(DBUnit.class));
        dbUnitConfig.caseInsensitiveStrategy(Orthography.LOWERCASE);
        // uses default config (dbunit.yml plus @DBUnit of this class)
        YamlDataSet dataSet = new YamlDataSet(YamlDataSetTest.class.getResourceAsStream("/datasets/yml/user.yml"),
                dbUnitConfig);

        assertEquals("user", dataSet.applyCaseInsensitivity("uSeR"));
    }

    @Test
    public void testPreserveTablesOrder() throws DataSetException {
        DBUnitConfig dbUnitConfig = DBUnitConfig.from(YamlDataSetTest.class.getAnnotation(DBUnit.class));
        // uses default config (dbunit.yml plus @DBUnit of this class)
        YamlDataSet dataSet = new YamlDataSet(YamlDataSetTest.class.getResourceAsStream("/datasets/yml/user-with-datasets-order.yml"),
                dbUnitConfig);

        assertArrayEquals(new String[]{"USER", "USER_DETAILS"}, dataSet.getTableNames());
    }
}
