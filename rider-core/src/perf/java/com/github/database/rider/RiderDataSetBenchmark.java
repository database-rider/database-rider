package com.github.database.rider;

import com.github.database.rider.core.api.dataset.ScriptableDataSet;
import com.github.database.rider.core.api.dataset.YamlDataSet;
import com.github.database.rider.core.dataset.builder.DataSetBuilder;
import com.github.database.rider.core.dataset.writer.YMLWriter;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by pestano on 22/02/16.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class RiderDataSetBenchmark {

    static AtomicInteger datasetsCreated;
    static AtomicInteger programmaticDatasetsCreated;
    static final Logger LOG = LoggerFactory.getLogger(RiderDataSetBenchmark.class.getName());

    @State(Scope.Benchmark)
    public static class BenchmarkContext {

        @Setup
        public void init() {
            datasetsCreated = new AtomicInteger(0);
            programmaticDatasetsCreated = new AtomicInteger(0);
        }

        @TearDown
        public void tearDown() {
            if(datasetsCreated.get() != 0) {
                LOG.info("Number of datasets created: " + datasetsCreated.get());
            }
            if(programmaticDatasetsCreated.get() != 0) {
                LOG.info("Number of programmatic datasets created: " + programmaticDatasetsCreated.get());
            }
        }
    }

    //@Benchmark
    public void createDataSetsFromFiles(BenchmarkContext ctx) {
        try {
            IDataSet iDataSet = new ScriptableDataSet(new YamlDataSet(getDataSetStream("users.yml")));
            assertCreatedDataSet(iDataSet);
            datasetsCreated.incrementAndGet();
        }catch (Exception e) {
            LOG.error("Problem in benchmark "+ctx.toString(), e);
        }
    }

    private void assertCreatedDataSet(IDataSet iDataSet) throws DataSetException {
        if (iDataSet.getTableNames().length != 5) {
            throw new RuntimeException("Must create five tables but created " + iDataSet.getTableNames().length);
        }
        if (iDataSet.getTable("TABLE5").getRowCount() != 5) {
            throw new RuntimeException("TABLE5 must have 5 rows but has " + iDataSet.getTable("TABLE5").getRowCount());
        }
        if (iDataSet.getTable("TABLE5").getTableMetaData().getColumns().length != 11) {
            throw new RuntimeException("TABLE5 must have 11 columns per row but has " + iDataSet.getTable("TABLE5").getTableMetaData().getColumns().length);
        }
    }

    @Benchmark
    public void createDataSetsWithDataSetBuilder(BenchmarkContext ctx)  {
        try {
            DataSetBuilder dataSetBuilder = new DataSetBuilder()
                    .defaultValue("COL1", "col1")
                    .defaultValue("COL2", "col2")
                    .defaultValue("COL3", "col3")
                    .defaultValue("COL4", "col4")
                    .defaultValue("COL5", "col5")
                    .defaultValue("COL6", "col6")
                    .defaultValue("COL7", "col7")
                    .defaultValue("COL8", "col8")
                    .defaultValue("COL9", "col9")
                    .defaultValue("COL10", "col10");
            for (int i = 1; i <= 5; i++) {
                dataSetBuilder.table("TABLE" + i)
                        .columns("id")
                        .values(1)
                        .values(2)
                        .values(3)
                        .values(4)
                        .values(5);
            }
            IDataSet iDataSet = dataSetBuilder.build();
            /*File datasetFile = Files.createTempFile("dataset-log", ".yml").toFile();
            FileOutputStream fos = new FileOutputStream(datasetFile);
            new YMLWriter(fos).write(iDataSet)*/;
            assertCreatedDataSet(iDataSet);
            programmaticDatasetsCreated.incrementAndGet();
        }catch (Exception e) {
            LOG.error("Problem in benchmark "+ctx.toString(), e);
        }
    }


    public static void main(String[] args) throws RunnerException, InterruptedException {
            new Runner(new OptionsBuilder().
                    forks(1).
                    threads(8).
                    warmupIterations(1).
                    warmupForks(1).
                    measurementIterations(10).
                    include(RiderDataSetBenchmark.class.getSimpleName()).
                    measurementTime(TimeValue.milliseconds(350)).
                    build()
            ).run();
    }

    private InputStream getDataSetStream(String dataSet) {
        if (!dataSet.startsWith("/")) {
            dataSet = "/" + dataSet;
        }
        InputStream is = getClass().getResourceAsStream(dataSet);
        if (is == null) {
            throw new RuntimeException(
                    String.format("Could not find dataset '%s' under 'resources' or 'resources/datasets' directory.",
                            dataSet.substring(1)));
        }
        return is;
    }


}
