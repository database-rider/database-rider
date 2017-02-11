package com.github.database.rider.core.exporter;

import com.github.database.rider.core.DBUnitRule;
import com.github.database.rider.core.api.dataset.DataSetFormat;
import com.github.database.rider.core.api.exporter.ExportDataSet;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by artemy-osipov on 10/02/17.
 */
@RunWith(JUnit4.class)
public class SingleExportDataSetIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection());

    @Test
    @ExportDataSet(format = DataSetFormat.YML, outputName = "target/exported/yml/singleExport.yml")
    public void shouldExportData() {
    }

    @AfterClass
    public static void assertGeneratedDataSets() {
        File export = new File("target/exported/yml/singleExport.yml");
        assertThat(export).exists();
    }
}
