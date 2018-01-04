package com.github.database.rider.core.configuration;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by pestano on 03/09/16.
 */
@RunWith(JUnit4.class)
public class DataSetConfigTest {

    @Test
    @DataSet(strategy = SeedStrategy.UPDATE,disableConstraints = true,cleanAfter = true,transactional = true)
    public void shouldLoadDataSetConfigFromAnnotation() throws NoSuchMethodException {
        Method method = getClass().getMethod("shouldLoadDataSetConfigFromAnnotation");
        assertThat(method).isNotNull();
        DataSet dataSet = method.getAnnotation(DataSet.class);
        assertThat(dataSet).isNotNull();

        DataSetConfig dataSetConfig = new DataSetConfig().from(dataSet);

        assertThat(dataSetConfig).isNotNull()
                .hasFieldOrPropertyWithValue("strategy", SeedStrategy.UPDATE)
                .hasFieldOrPropertyWithValue("useSequenceFiltering", true)
                .hasFieldOrPropertyWithValue("disableConstraints", true)
                .hasFieldOrPropertyWithValue("cleanBefore", false)
                .hasFieldOrPropertyWithValue("cleanAfter", true)
                .hasFieldOrPropertyWithValue("transactional", true);

    }
}
