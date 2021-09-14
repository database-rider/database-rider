package com.github.database.rider.junit5.util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Property Resolution Util Implementation.
 * <p>
 * Implements a Property Resolution Process with the follow strategy:<br>
 *<br>
 * 1. map that defines overriding persistence properties (if any)<br>
 * 2. runtime arguments<br>
 * 3. environment variables<br>
 * </p>
 *
 * Created by markus meisterernst on 24/11/18.
 */
public class PropertyResolutionUtil {
    private static final String PROP_FILTER = "^javax.persistence.(.*)|^eclipselink.(.*)|^hibernate.(.*)|^openjpa.(.)*";
    
    /**
     * Resolves a Union of System.env and System.getProperties() where the KeyValue-Pairs of the later have precedence.
     *
     * @return Map or null if none of the following Properties exists:
     *              javax.persistence.jdbc.driver,javax.persistence.jdbc.url, javax.persistence.jdbc.user, javax.persistence.jdbc.password
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getSystemJavaxPersistenceOverrides() {
        if(propertyOverridesExist()) {
            // we make use of a type cast hack to convert Properties to a Map
            return mergeFilteredMaps(castMap(System.getenv()), (Map) System.getProperties());
        }
        return null;
    }

    public static Map<String, Object> castMap(Map<String, String> stringStringMap) {
        return stringStringMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    /**
     * Resolves a Union of System.env and System.getProperties() and overridingProperties where the KeyValue-Pairs of the later have the highest precedence.
     * @param overridingProperties overridingProperties
     *
     * @return Map or null if there are no entries that match the Persistence Filter {@link #PROP_FILTER}
     */
    public Map<String, Object> persistencePropertiesOverrides(Map<String, Object> overridingProperties) {
        if(overridingProperties == null) {
            throw new IllegalArgumentException("the property 'overridingProperties' is not allowed to be null.");
        }
        Map<String, Object> overridingProperttiesCopy = new HashMap<>(overridingProperties);
        
        return mergeFilteredMaps(getSystemJavaxPersistenceOverrides(), overridingProperttiesCopy);
    }
    
    boolean propertyOverridesExist() {
        return System.getProperties().containsKey("javax.persistence.jdbc.url")
                || System.getProperties().containsKey("javax.persistence.jdbc.user")
                || System.getProperties().containsKey("javax.persistence.jdbc.password")
                || System.getProperties().containsKey("javax.persistence.jdbc.driver")
                || System.getenv().containsKey("javax.persistence.jdbc.url")
                || System.getenv().containsKey("javax.persistence.jdbc.user")
                || System.getenv().containsKey("javax.persistence.jdbc.password")
                || System.getenv().containsKey("javax.persistence.jdbc.driver");
    }
    
   
    Map<String, Object> mergeFilteredMaps(Map<String,Object> ... enlistedMaps) {
        Map<String, Object> targetMap = new HashMap<>();
        for(Map<String, Object> map: enlistedMaps) {
            if(map != null) {
                for(Map.Entry<String, Object> entry : map.entrySet()) {
                    if(entry.getKey().matches(PROP_FILTER)) {
                        targetMap.put(entry.getKey(), ((Object)entry.getValue()));
                    }
                }
            }
        }
        
        return targetMap.size() > 0  ? targetMap : null;
    }
}
