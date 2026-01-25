package com.github.database.rider.spring.dataset;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;
import com.github.database.rider.spring.config.TestConfig;
import com.github.database.rider.spring.model.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import jakarta.transaction.Transactional;

/**
 * @author Artemy Osipov
 */
@RunWith(SpringRunner.class)
@DBRider
@ContextConfiguration(classes = TestConfig.class)
@DataSet(value = "test2.yml")
public class DataSetIt {

    @Autowired
    private EntityUtils entityUtils;

    @Test
    @DataSet(cleanBefore = true)
    public void testCleanBefore() {
        entityUtils.assertValues();
    }

    @Test
    @DataSet(value = "test.yml")
    public void testOnMethod() {
        entityUtils.assertValues("value1", "value2");
    }

    @Test
    @DataSet(value = "test.yml")
    @Transactional
    public void testOnMethodWithTransaction() {
        entityUtils.assertValues("value1", "value2");
    }

    @Test
    public void testOnClass() {
        entityUtils.assertValues("value3", "value4");
    }
}
