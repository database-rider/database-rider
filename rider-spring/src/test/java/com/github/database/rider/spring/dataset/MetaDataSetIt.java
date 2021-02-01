package com.github.database.rider.spring.dataset;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.database.rider.spring.config.TestConfig;
import com.github.database.rider.spring.model.EntityUtils;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@MetaDataSet
public class MetaDataSetIt {

    @Autowired
    private EntityUtils entityUtils;

    @Test
    public void testMetaAnnotationOnClass() {
        entityUtils.assertValues("value1", "value2");
    }

    @Test
    @MetaDataSet("test2.yml")
    public void testMetaAnnotationOnMethod() {
        entityUtils.assertValues("value3", "value4");
    }
}

