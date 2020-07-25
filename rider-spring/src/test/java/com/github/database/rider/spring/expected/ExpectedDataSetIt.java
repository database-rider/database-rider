package com.github.database.rider.spring.expected;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.spring.api.DBRider;
import com.github.database.rider.spring.config.TestConfig;
import com.github.database.rider.spring.model.EntityUtils;

/**
 * @author Artemy Osipov
 */
@RunWith(SpringRunner.class)
@DBRider
@ContextConfiguration(classes = TestConfig.class)
@DataSet(cleanBefore = true)
public class ExpectedDataSetIt {

    @Autowired
    private EntityUtils entityUtils;

    @Test
    @ExpectedDataSet("empty.yml")
    public void testEmpty() {
    }

    @Test
    @ExpectedDataSet(value = "test.yml", ignoreCols = "id")
    public void testOnMethod() {
        entityUtils.addValues("value1", "value2");
    }

    @Test
    @ExpectedDataSet(value = "test.yml", ignoreCols = "id")
    @Transactional
    public void testOnMethodWithTransaction() {
        entityUtils.addValues("value1", "value2");
    }

    @Test
    @ExpectedDataSet(value = "test.yml", ignoreCols = "id")
    public void testOnMethodWithManualTransaction() {
        entityUtils.executeInTransaction(
                new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                        entityUtils.addValues("value1", "value2");
                    }
                }
        );
    }
}
