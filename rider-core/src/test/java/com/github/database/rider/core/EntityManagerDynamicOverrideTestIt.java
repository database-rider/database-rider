package com.github.database.rider.core;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by markus meisterernst on 26/11/18.
 */
@RunWith(JUnit4.class)
public class EntityManagerDynamicOverrideTestIt {
    private static final String PROP_KEY_URL = "javax.persistence.jdbc.url";
    private static final String PROP_VALUE_URL = "jdbc:hsqldb:mem:susi;DB_CLOSE_DELAY=-1";
    private static final String PROP_KEY_DRIVER = "javax.persistence.jdbc.driver";
    private static final String PROP_KEY_USER = "javax.persistence.jdbc.user";
    private static final String PROP_KEY_PASSWORD = "javax.persistence.jdbc.password";
    private static Map<String,Object> systemConfigOverridingProps= new HashMap<String, Object>() {{
        put(PROP_KEY_URL, PROP_VALUE_URL);
        put(PROP_KEY_DRIVER,  "org.hsqldb.jdbc.JDBCDriver");
        put(PROP_KEY_USER, "sa");
        put(PROP_KEY_PASSWORD, "");
    }};
    
    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it", systemConfigOverridingProps);

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection());

    @DataSet(value = "datasets/yml/user.yml")
    @Test
    public void shouldFindUserForOverridenUrl() throws SQLException {
        assertThat(em().find(User.class, 1L)).isNotNull();
    
        // is it the susi-Database ?
        assertThat(emProvider.connection().getMetaData().getURL()).isEqualTo(PROP_VALUE_URL);
    }
    
}
