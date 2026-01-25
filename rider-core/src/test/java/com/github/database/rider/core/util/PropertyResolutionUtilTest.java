package com.github.database.rider.core.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test Cases for PropertyResolutionUtil
 * <p>
 * Created by markus-meisterernst on 25/11/18.
 */
public class PropertyResolutionUtilTest {

    PropertyResolutionUtil systemUnderTest;

    @Before
    public void setup() {
        System.clearProperty("jakarta.persistence.jdbc.driver");
        System.clearProperty("jakarta.persistence.jdbc.url");
        System.clearProperty("jakarta.persistence.jdbc.user");
        System.clearProperty("jakarta.persistence.jdbc.password");
        System.clearProperty("yet.another.prop");
        systemUnderTest = new PropertyResolutionUtil();
    }

    @Test
    public void testPropertyOverridesExistEvaluatesToFalse() {
        Assert.assertFalse(systemUnderTest.propertyOverridesExist());
    }

    @Test
    public void testPropertyOverridesExistEvaluatesToFalseVariant2() {
        System.setProperty("yet.another.prop", "org.hsqldb.jdbcDriver");
        Assert.assertFalse(systemUnderTest.propertyOverridesExist());
    }

    @Test
    public void testPropertyOverridesExistEvaluatesToTrue() {
        System.setProperty("jakarta.persistence.jdbc.driver", "org.hsqldb.jdbcDriver");
        Assert.assertTrue(systemUnderTest.propertyOverridesExist());
    }

    @Test
    public void testPropertyOverridesExistEvaluatesToTrueVariant2() {
        System.setProperty("jakarta.persistence.jdbc.url", "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=-1");
        Assert.assertTrue(systemUnderTest.propertyOverridesExist());
    }

    @Test
    public void testPropertyOverridesExistEvaluatesToTrueVariant3() {
        System.setProperty("jakarta.persistence.jdbc.user", "sa");
        Assert.assertTrue(systemUnderTest.propertyOverridesExist());
    }

    @Test
    public void testPropertyOverridesExistEvaluatesToTrueVariant4() {
        System.setProperty("jakarta.persistence.jdbc.password", "changeit");
        Assert.assertTrue(systemUnderTest.propertyOverridesExist());
    }

    @Test
    public void testMergeFilteredMapsWithNoMapsAtAll() {
        Map<String, Object> filteredMap = systemUnderTest.mergeFilteredMaps(null, null);

        Assert.assertNull(filteredMap);
    }

    @Test
    public void testMergeFilteredMapsWithNoPersistenceProps() {
        Map<String, Object> filteredMap = systemUnderTest.mergeFilteredMaps(null, new HashMap<String, Object>() {{
            put("a", "b");
            put("b", "c");
        }});

        Assert.assertNull(filteredMap);
    }

    @Test
    public void testMergeFilteredMapsWithNoPersistencePropsVariant2() {
        Map<String, Object> filteredMap = systemUnderTest.mergeFilteredMaps(new HashMap<String, Object>() {{
            put("a", "b");
            put("b", "c");
        }}, null);

        Assert.assertNull(filteredMap);
    }

    @Test
    public void testMergeFilteredMapsWithNoPersistencePropsVariant3() {
        Map<String, Object> filteredMap = systemUnderTest.mergeFilteredMaps(new HashMap<String, Object>() {{
            put("a", "b");
            put("b", "c");
        }}, new HashMap<String, Object>() {{
            put("a", "b");
            put("b", "c");
        }});

        Assert.assertNull(filteredMap);
    }

    @Test
    public void testMergeFilteredMapsWithOnePersistenceProp() {
        final String EXPECTED_PROPERTY_KEY = "jakarta.persistence.jdbc.url";
        final String EXPECTED_PROPERTY_VALUE = "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=-1";
        Map<String, Object> filteredMap = systemUnderTest.mergeFilteredMaps(null, new HashMap<String, Object>() {{
            put(EXPECTED_PROPERTY_KEY, EXPECTED_PROPERTY_VALUE);
            put("b", "c");
        }});

        Assert.assertNotNull(filteredMap);
        Assert.assertTrue(filteredMap.size() == 1);
        Assert.assertTrue(filteredMap.containsKey(EXPECTED_PROPERTY_KEY));
        Assert.assertEquals(EXPECTED_PROPERTY_VALUE, filteredMap.get(EXPECTED_PROPERTY_KEY));
    }

    @Test
    public void testMergeFilteredMapsWithOnePersistencePropVariant2() {
        final String EXPECTED_PROPERTY_KEY = "jakarta.persistence.jdbc.url";
        final String EXPECTED_PROPERTY_VALUE = "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=-1";
        Map<String, Object> filteredMap = systemUnderTest.mergeFilteredMaps(new HashMap<String, Object>() {{
            put(EXPECTED_PROPERTY_KEY, EXPECTED_PROPERTY_VALUE);
            put("b", "c");
        }}, null);

        Assert.assertNotNull(filteredMap);
        Assert.assertTrue(filteredMap.size() == 1);
        Assert.assertTrue(filteredMap.containsKey(EXPECTED_PROPERTY_KEY));
        Assert.assertEquals(EXPECTED_PROPERTY_VALUE, filteredMap.get(EXPECTED_PROPERTY_KEY));
    }

    @Test
    public void testMergeFilteredMapsWithOneHibernateProp() {
        final String EXPECTED_PROPERTY_KEY = "hibernate.connection.url";
        final String EXPECTED_PROPERTY_VALUE = "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=-1";
        Map<String, Object> filteredMap = systemUnderTest.mergeFilteredMaps(null, new HashMap<String, Object>() {{
            put(EXPECTED_PROPERTY_KEY, EXPECTED_PROPERTY_VALUE);
            put("b", "c");
        }});

        Assert.assertNotNull(filteredMap);
        Assert.assertTrue(filteredMap.size() == 1);
        Assert.assertTrue(filteredMap.containsKey(EXPECTED_PROPERTY_KEY));
        Assert.assertEquals(EXPECTED_PROPERTY_VALUE, filteredMap.get(EXPECTED_PROPERTY_KEY));
    }

    @Test
    public void testMergeFilteredMapsWithOnePersistencePropOverridden() {
        final String EXPECTED_PROPERTY_KEY = "jakarta.persistence.jdbc.url";
        final String EXPECTED_PROPERTY_VALUE_ENV = "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=-1";
        final String EXPECTED_PROPERTY_VALUE_RUNTIME_ARGS = "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=0";
        Map<String, Object> filteredMap = systemUnderTest.mergeFilteredMaps(new HashMap<String, Object>() {{
                                                                                put(EXPECTED_PROPERTY_KEY, EXPECTED_PROPERTY_VALUE_ENV);
                                                                                put("b", "c");
                                                                            }},
                new HashMap<String, Object>() {{
                    put(EXPECTED_PROPERTY_KEY, EXPECTED_PROPERTY_VALUE_RUNTIME_ARGS);
                    put("b", "c");
                }});

        Assert.assertNotNull(filteredMap);
        Assert.assertTrue(filteredMap.size() == 1);
        Assert.assertTrue(filteredMap.containsKey(EXPECTED_PROPERTY_KEY));
        Assert.assertEquals(EXPECTED_PROPERTY_VALUE_RUNTIME_ARGS, filteredMap.get(EXPECTED_PROPERTY_KEY));
    }

    @Test
    public void testGetSystemjakartaPersistenceOverridesReturnsNull() {
        Assert.assertNull(systemUnderTest.getSystemjakartaPersistenceOverrides());
    }

    @Test
    public void testGetSystemjakartaPersistenceOverridesReturnsMap() {
        System.setProperty("jakarta.persistence.jdbc.driver", "org.hsqldb.jdbcDriver");
        System.setProperty("jakarta.persistence.jdbc.url", "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=0");
        System.setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=-1");
        System.setProperty("eclipselink.connection.url", "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=-1");
        System.setProperty("openjpa.connection.url", "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=-1");
        Map<String, Object> systemjakartaPersistenceOverrides = systemUnderTest.getSystemjakartaPersistenceOverrides();
        Assert.assertNotNull(systemjakartaPersistenceOverrides);
        Assert.assertTrue(systemjakartaPersistenceOverrides.size() == 5);
    }

    @Test
    public void testPersistencePropertiesOverridesReturnsNull() {
        Map<String, Object> systemjakartaPersistenceOverrides = systemUnderTest.persistencePropertiesOverrides(new HashMap<String, Object>() {{
            put("a", "b");
            put("b", "c");
        }});
        Assert.assertNull(systemjakartaPersistenceOverrides);
    }

    @Test
    public void testPersistencePropertiesOverridesReturnsOneEntry() {
        final String EXPECTED_PROPERTY_KEY = "jakarta.persistence.jdbc.url";
        final String EXPECTED_PROPERTY_VALUE = "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=-1";

        Map<String, Object> propertiesOverrides = systemUnderTest.persistencePropertiesOverrides(new HashMap<String, Object>() {{
            put(EXPECTED_PROPERTY_KEY, EXPECTED_PROPERTY_VALUE);
            put("b", "c");
        }});
        Assert.assertNotNull(propertiesOverrides);
        Assert.assertTrue(propertiesOverrides.size() == 1);
        Assert.assertEquals(EXPECTED_PROPERTY_VALUE, propertiesOverrides.get(EXPECTED_PROPERTY_KEY));
    }

    @Test
    public void testShouldCastThePropertyHashMap() {

        Map<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("bar", "bar");
        final Map<String, Object> stringObjectMap = systemUnderTest.castMap(stringStringHashMap);

        try {
            stringObjectMap.put("foo", new Object());
        } catch (Exception e) {
            Assert.fail("should not throw exception");
        }

        assertThat(stringObjectMap.get("bar")).isEqualTo("bar");
        assertThat(stringObjectMap.get("foo")).isNotNull();
    }
}
