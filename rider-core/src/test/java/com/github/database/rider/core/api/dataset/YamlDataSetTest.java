package com.github.database.rider.core.api.dataset;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.configuration.LetterCase;
import com.github.database.rider.core.configuration.DBUnitConfig;

@DBUnit
public class YamlDataSetTest {

    @Test
    public void testApplyCase_Default() {
        DBUnitConfig dbUnitConfig = DBUnitConfig.from(YamlDataSetTest.class.getAnnotation(DBUnit.class));
        // uses default config (dbunit.yml plus @DBUnit of this class)
        YamlDataSet dataSet = new YamlDataSet(YamlDataSetTest.class.getResourceAsStream("/datasets/yml/user.yml"),
                dbUnitConfig);

        assertEquals("USER", dataSet.applyCase("uSeR"));
    }

    @Test
    public void testApplyCase_StrategyLowerCase() {
        DBUnitConfig dbUnitConfig = DBUnitConfig.from(YamlDataSetTest.class.getAnnotation(DBUnit.class));
        dbUnitConfig.getProperties().put("caseInsensitiveStrategy", LetterCase.LOWER);
        // uses default config (dbunit.yml plus @DBUnit of this class)
        YamlDataSet dataSet = new YamlDataSet(YamlDataSetTest.class.getResourceAsStream("/datasets/yml/user.yml"),
                dbUnitConfig);

        assertEquals("user", dataSet.applyCase("uSeR"));
    }
}
