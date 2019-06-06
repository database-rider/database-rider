package com.github.database.rider;

import com.github.database.rider.core.api.dataset.JSONDataSet;
import com.github.database.rider.core.api.dataset.ScriptableDataSet;
import com.github.database.rider.core.api.dataset.YamlDataSet;
import com.github.database.rider.core.dataset.builder.DataSetBuilder;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by rmpestano on 15/05/19.
 *
 * A simple benchmark measuring dataset creation via file and with DataSetBuilder (in-memory)
 *
 * Running: mvn exec:exec -Pperf
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class RiderDataSetBenchmark {

    private static final String PROBLEM_IN_BENCHMARK = "Problem in benchmark ";

    private static AtomicInteger yamlDatasetsCreated;
    private static AtomicInteger xmlDatasetsCreated;
    private static AtomicInteger jsonDatasetsCreated;
    private static AtomicInteger programmaticDatasetsCreated;
    private static final Logger LOG = LoggerFactory.getLogger(RiderDataSetBenchmark.class.getName());

    @State(Scope.Benchmark)
    public static class BenchmarkContext {

        @Setup
        public void init() {
            yamlDatasetsCreated = new AtomicInteger(0);
            jsonDatasetsCreated = new AtomicInteger(0);
            xmlDatasetsCreated = new AtomicInteger(0);
            programmaticDatasetsCreated = new AtomicInteger(0);
        }

        @TearDown
        public void tearDown() {
            if (yamlDatasetsCreated.get() != 0) {
                LOG.info("Number of YML datasets created: " + yamlDatasetsCreated.get());
            }
            if (xmlDatasetsCreated.get() != 0) {
                LOG.info("Number of XML datasets created: " + xmlDatasetsCreated.get());
            }
            if (jsonDatasetsCreated.get() != 0) {
                LOG.info("Number of JSON datasets created: " + jsonDatasetsCreated.get());
            }
            if (programmaticDatasetsCreated.get() != 0) {
                LOG.info("Number of programmatic datasets created: " + programmaticDatasetsCreated.get());
            }
        }
    }

    @Benchmark
    public void createDataSetsFromYMLFiles(BenchmarkContext ctx) {
        try {
            IDataSet iDataSet = new ScriptableDataSet(new YamlDataSet(getDataSetStream("users.yml")));
            assertCreatedDataSet(iDataSet);
            yamlDatasetsCreated.incrementAndGet();
        } catch (Exception e) {
            LOG.error(PROBLEM_IN_BENCHMARK + ctx.toString(), e);
        }
    }

    @Benchmark
    public void createDataSetsFromXMLFiles(BenchmarkContext ctx) {
        try {
            IDataSet iDataSet = new ScriptableDataSet(new FlatXmlDataSetBuilder().build(getDataSetStream("users.xml")));
            assertCreatedDataSet(iDataSet);
            xmlDatasetsCreated.incrementAndGet();
        } catch (Exception e) {
            LOG.error(PROBLEM_IN_BENCHMARK + ctx.toString(), e);
        }
    }

    @Benchmark
    public void createDataSetsFromJSONFiles(BenchmarkContext ctx) {
        try {
            IDataSet iDataSet = new ScriptableDataSet(new JSONDataSet(getDataSetStream("users.json")));
            assertCreatedDataSet(iDataSet);
            jsonDatasetsCreated.incrementAndGet();
        } catch (Exception e) {
            LOG.error(PROBLEM_IN_BENCHMARK + ctx.toString(), e);
        }
    }

    @Benchmark
    public void createDataSetsWithDataSetBuilder(BenchmarkContext ctx) {
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
        /*    File datasetFile = Files.createTempFile("dataset-log", ".yml").toFile();
            try(FileOutputStream fos = new FileOutputStream(datasetFile)) {
                FlatXmlDataSet.write(iDataSet, fos);
            }*/
            assertCreatedDataSet(iDataSet);
            programmaticDatasetsCreated.incrementAndGet();
        } catch (Exception e) {
            LOG.error(PROBLEM_IN_BENCHMARK + ctx.toString(), e);
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


    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().
                forks(3).
                threads(4).
                warmupIterations(1).
                warmupForks(1).
                measurementIterations(5).
                include(RiderDataSetBenchmark.class.getSimpleName()).
                measurementTime(TimeValue.milliseconds(300)).
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
