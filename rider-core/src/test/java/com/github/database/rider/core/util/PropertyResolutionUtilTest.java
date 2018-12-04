package com.github.database.rider.core.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test Cases for PropertyResolutionUtil
 *
 * Created by markus-meisterernst on 25/11/18.
 */
public class PropertyResolutionUtilTest {
    
    PropertyResolutionUtil systemUnderTest;
    
    @Before
    public void setup() {
        System.clearProperty("javax.persistence.jdbc.driver");
        System.clearProperty("javax.persistence.jdbc.url");
        System.clearProperty("javax.persistence.jdbc.user");
        System.clearProperty("javax.persistence.jdbc.password");
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
        System.setProperty("javax.persistence.jdbc.driver", "org.hsqldb.jdbcDriver");
        Assert.assertTrue(systemUnderTest.propertyOverridesExist());
    }
    
    @Test
    public void testPropertyOverridesExistEvaluatesToTrueVariant2() {
        System.setProperty("javax.persistence.jdbc.url", "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=-1");
        Assert.assertTrue(systemUnderTest.propertyOverridesExist());
    }
    
    @Test
    public void testPropertyOverridesExistEvaluatesToTrueVariant3() {
        System.setProperty("javax.persistence.jdbc.user", "sa");
        Assert.assertTrue(systemUnderTest.propertyOverridesExist());
    }
    
    @Test
    public void testPropertyOverridesExistEvaluatesToTrueVariant4() {
        System.setProperty("javax.persistence.jdbc.password", "changeit");
        Assert.assertTrue(systemUnderTest.propertyOverridesExist());
    }
    
    @Test
    public void testMergeFilteredMapsWithNoMapsAtAll() {
        Map<String, String> filteredMap = systemUnderTest.mergeFilteredMaps(null, null);
        
        Assert.assertNull(filteredMap);
    }
    
    @Test
    public void testMergeFilteredMapsWithNoPersistenceProps() {
        Map<String, String> filteredMap = systemUnderTest.mergeFilteredMaps(null, new HashMap<String, String>() {{
            put("a", "b");
            put("b", "c");
        }});
        
        Assert.assertNull(filteredMap);
    }
    
    @Test
    public void testMergeFilteredMapsWithNoPersistencePropsVariant2() {
        Map<String, String> filteredMap = systemUnderTest.mergeFilteredMaps( new HashMap<String, String>() {{
            put("a", "b");
            put("b", "c");
        }}, null);
        
        Assert.assertNull(filteredMap);
    }
    
    @Test
    public void testMergeFilteredMapsWithNoPersistencePropsVariant3() {
        Map<String, String> filteredMap = systemUnderTest.mergeFilteredMaps( new HashMap<String, String>() {{
                put("a", "b");
                put("b", "c");
            }}, new HashMap<String, String>() {{
                put("a", "b");
                put("b", "c");
        }});
        
        Assert.assertNull(filteredMap);
    }
    
    @Test
    public void testMergeFilteredMapsWithOnePersistenceProp() {
        final String EXPECTED_PROPERTY_KEY = "javax.persistence.jdbc.url";
        final String EXPECTED_PROPERTY_VALUE = "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=-1";
        Map<String, String> filteredMap = systemUnderTest.mergeFilteredMaps(null, new HashMap<String, String>() {{
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
        final String EXPECTED_PROPERTY_KEY = "javax.persistence.jdbc.url";
        final String EXPECTED_PROPERTY_VALUE = "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=-1";
        Map<String, String> filteredMap = systemUnderTest.mergeFilteredMaps( new HashMap<String, String>() {{
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
        Map<String, String> filteredMap = systemUnderTest.mergeFilteredMaps(null, new HashMap<String, String>() {{
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
        final String EXPECTED_PROPERTY_KEY = "javax.persistence.jdbc.url";
        final String EXPECTED_PROPERTY_VALUE_ENV = "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=-1";
        final String EXPECTED_PROPERTY_VALUE_RUNTIME_ARGS = "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=0";
        Map<String, String> filteredMap = systemUnderTest.mergeFilteredMaps(new HashMap<String, String>() {{
                put(EXPECTED_PROPERTY_KEY, EXPECTED_PROPERTY_VALUE_ENV);
                put("b", "c");
            }},
            new HashMap<String, String>() {{
                put(EXPECTED_PROPERTY_KEY, EXPECTED_PROPERTY_VALUE_RUNTIME_ARGS);
                put("b", "c");
        }});
        
        Assert.assertNotNull(filteredMap);
        Assert.assertTrue(filteredMap.size() == 1);
        Assert.assertTrue(filteredMap.containsKey(EXPECTED_PROPERTY_KEY));
        Assert.assertEquals(EXPECTED_PROPERTY_VALUE_RUNTIME_ARGS, filteredMap.get(EXPECTED_PROPERTY_KEY));
    }
    
    @Test
    public void testGetSystemJavaxPersistenceOverridesReturnsNull() {
        Assert.assertNull(systemUnderTest.getSystemJavaxPersistenceOverrides());
    }
    
    @Test
    public void testGetSystemJavaxPersistenceOverridesReturnsMap() {
        System.setProperty("javax.persistence.jdbc.driver","org.hsqldb.jdbcDriver");
        System.setProperty("javax.persistence.jdbc.url", "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=0");
        System.setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=-1");
        System.setProperty("eclipselink.connection.url", "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=-1");
        System.setProperty("openjpa.connection.url", "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=-1");
        Map<String, String> systemJavaxPersistenceOverrides = systemUnderTest.getSystemJavaxPersistenceOverrides();
        Assert.assertNotNull(systemJavaxPersistenceOverrides);
        Assert.assertTrue(systemJavaxPersistenceOverrides.size() == 5);
    }
    
    @Test
    public void testPersistencePropertiesOverridesReturnsNull() {
        Map<String, String> systemJavaxPersistenceOverrides = systemUnderTest.persistencePropertiesOverrides(new HashMap<String, String>() {{
            put("a", "b");
            put("b", "c");
        }});
        Assert.assertNull(systemJavaxPersistenceOverrides);
    }
    
    @Test
    public void testPersistencePropertiesOverridesReturnsOneEntry() {
        final String EXPECTED_PROPERTY_KEY = "javax.persistence.jdbc.url";
        final String EXPECTED_PROPERTY_VALUE = "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=-1";
        
        Map<String, String> propertiesOverrides = systemUnderTest.persistencePropertiesOverrides(new HashMap<String, String>() {{
            put(EXPECTED_PROPERTY_KEY,EXPECTED_PROPERTY_VALUE);
            put("b", "c");
        }});
        Assert.assertNotNull(propertiesOverrides);
        Assert.assertTrue(propertiesOverrides.size() == 1);
        Assert.assertEquals(EXPECTED_PROPERTY_VALUE, propertiesOverrides.get(EXPECTED_PROPERTY_KEY));
    }
}
