package com.github.database.rider.core.assertion;

import com.github.database.rider.core.dataset.builder.DataSetBuilder;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.IDataSet;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class PrologAssertTest {
    private static final Logger log = LoggerFactory.getLogger(PrologAssertTest.class);

    @Test
    public void shouldFindASolution() {
        IDataSet currentDataSet = new DataSetBuilder()
                .table("USER")
                    .row()
                        .column("ID", 1)
                        .column("NAME", "@realpestano")
                .table("USER")
                    .row()
                        .column("ID", 2)
                        .column("NAME", "@dbunit")
                .table("TWEET")
                    .row()
                        .column("ID", "abcdef12345")
                        .column("CONTENT", "dbunit rules!")
                        .column("DATE", "[DAY,NOW]")
                        .column("USER_ID", "1")
                .build();


        IDataSet expectedDataSet = new DataSetBuilder()
                .table("USER")
                    .row()
                        .column("ID", "$$x$$")
                        .column("NAME", "@realpestano")
                .table("TWEET")
                    .row()
                    .column("CONTENT", "dbunit rules!")
                    .column("USER_ID", "$$x$$")
                .build();

        try {
            PrologAssert.compareProlog(currentDataSet, expectedDataSet, new String[]{"USER", "TWEET"}, 1_000L);
        } catch (DatabaseUnitException e) {
            log.error("FAIL", e);
            fail();
        }
    }

    @Test
    public void shouldNotFindASolution() {
        IDataSet currentDataSet = new DataSetBuilder()
                .table("USER")
                    .row()
                        .column("ID", 1)
                        .column("NAME", "@realpestano")
                .table("USER")
                    .row()
                        .column("ID", 2)
                        .column("NAME", "@dbunit")
                .table("TWEET")
                    .row()
                        .column("ID", "abcdef12345")
                        .column("CONTENT", "dbunit rules!")
                        .column("DATE", "[DAY,NOW]")
                        .column("USER_ID", "1")
                .build();


        IDataSet expectedDataSet = new DataSetBuilder()
                .table("USER")
                    .row()
                        .column("ID", "$$x$$")
                        .column("NAME", "@realpestano")
                .table("TWEET")
                    .row()
                        .column("CONTENT", "lets ignore tests!")
                        .column("USER_ID", "$$x$$")
                .build();

        try {
            PrologAssert.compareProlog(currentDataSet, expectedDataSet, new String[]{"USER", "TWEET"}, 1_000L);
            fail();
        } catch (DatabaseUnitException e) {
            assertThat(e.getMessage()).contains("Could not find a solution to theory");
        }
    }

    @Test
    public void shouldSupportSingleQuotes() {
        IDataSet currentDataSet =
                new DataSetBuilder()
                        .table("TWEET")
                            .row()
                                .column("ID", "abcdef12345")
                                .column("CONTENT", "db'unit rules!")
                                .column("DATE", "[DAY,NOW]")
                        .build();


        IDataSet expectedDataSet =
                new DataSetBuilder()
                        .table("TWEET")
                            .row()
                                .column("CONTENT", "db'unit rules!")
                        .build();

        try {
            PrologAssert.compareProlog(currentDataSet, expectedDataSet, new String[]{"TWEET"}, 1_000L);
        } catch (DatabaseUnitException e) {
            log.error("FAIL", e);
            fail();
        }
    }
}
