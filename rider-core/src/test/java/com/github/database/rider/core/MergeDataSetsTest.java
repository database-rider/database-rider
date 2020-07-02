/*
 * Copyright 2018 rmpestano.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.database.rider.core;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetImpl;
import com.github.database.rider.core.replacers.CustomReplacer;
import com.github.database.rider.core.replacers.CustomReplacerBar;
import com.github.database.rider.core.replacers.NullReplacer;
import com.github.database.rider.core.util.AnnotationUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author rmpestano
 */
@RunWith(JUnit4.class)
public class MergeDataSetsTest {

    @Test
    public void shouldMergeDataSets() {
        DataSet classLevel = DataSetImpl.instance().withValue("classDataSet.yml", "classDataSet2.json");
        DataSet methodLevel = DataSetImpl.instance().withValue(new String[]{"methodDataSet.yml", "methodDataSet2.json"});
        DataSet mergeDataSets = AnnotationUtils.mergeDataSetAnnotations(classLevel, methodLevel);

        assertThat(mergeDataSets).isNotNull();
        assertThat(mergeDataSets.value())
                .isEqualTo(new String[]{"classDataSet.yml", "classDataSet2.json", "methodDataSet.yml", "methodDataSet2.json"})
                .isNotEqualTo(new String[]{"methodDataSet.yml", "methodDataSet2.json", "classDataSet.yml", "classDataSet2.yml"});
    }

    @Test
    public void shouldMergeScripts() {
        DataSet classLevel = DataSetImpl.instance()
                .withExecuteScriptsBefore("classScriptBefore.sql", "classScriptBefore2.sql")
                .withExecuteScriptsAfter("classScriptAfter.sql", "classScriptAfter2.sql");

        DataSet methodLevel = DataSetImpl.instance()
                .withExecuteScriptsBefore("methodScriptBefore.sql", "methodScriptBefore2.sql")
                .withExecuteScriptsAfter("methodScriptAfter.sql", "methodScriptAfter2.sql");

        DataSet mergeDataSets = AnnotationUtils.mergeDataSetAnnotations(classLevel, methodLevel);

        assertThat(mergeDataSets).isNotNull();
        assertThat(mergeDataSets.executeScriptsBefore())
                .isEqualTo(new String[]{"classScriptBefore.sql", "classScriptBefore2.sql", "methodScriptBefore.sql", "methodScriptBefore2.sql"});

        assertThat(mergeDataSets.executeScriptsAfter())
                .isEqualTo(new String[]{"classScriptAfter.sql", "classScriptAfter2.sql", "methodScriptAfter.sql", "methodScriptAfter2.sql"});
    }

    @Test
    public void shouldMergeStatements() {
        DataSet classLevel = DataSetImpl.instance()
                .withExecuteStatementsBefore("classStatementBefore.sql", "classStatementBefore2.sql")
                .withExecuteStatementsAfter("classStatementAfter.sql", "classStatementAfter2.sql");

        DataSet methodLevel = DataSetImpl.instance()
                .withExecuteStatementsBefore("methodStatementBefore.sql", "methodStatementBefore2.sql")
                .withExecuteStatementsAfter("methodStatementAfter.sql", "methodStatementAfter2.sql");

        DataSet mergeDataSets = AnnotationUtils.mergeDataSetAnnotations(classLevel, methodLevel);

        assertThat(mergeDataSets).isNotNull();
        assertThat(mergeDataSets.executeStatementsBefore())
                .isEqualTo(new String[]{"classStatementBefore.sql", "classStatementBefore2.sql", "methodStatementBefore.sql", "methodStatementBefore2.sql"});

        assertThat(mergeDataSets.executeStatementsAfter())
                .isEqualTo(new String[]{"classStatementAfter.sql", "classStatementAfter2.sql", "methodStatementAfter.sql", "methodStatementAfter2.sql"});
    }

    @Test
    public void shouldMergeTableOrdering() {
        DataSet classLevel = DataSetImpl.instance().withTableOrdering("USER", "FOLLOWER");
        DataSet methodLevel = DataSetImpl.instance().withTableOrdering("TWEET");
        DataSet mergeDataSets = AnnotationUtils.mergeDataSetAnnotations(classLevel, methodLevel);

        assertThat(mergeDataSets).isNotNull();
        assertThat(mergeDataSets.tableOrdering())
                .isEqualTo(new String[]{"USER", "FOLLOWER", "TWEET"});
    }

    @Test
    public void shouldMergeSkipCleaningFor() {
        DataSet classLevel = DataSetImpl.instance().withSkipCleaningFor("USER", "FOLLOWER");
        DataSet methodLevel = DataSetImpl.instance().withSkipCleaningFor("TWEET");
        DataSet mergeDataSets = AnnotationUtils.mergeDataSetAnnotations(classLevel, methodLevel);

        assertThat(mergeDataSets).isNotNull();
        assertThat(mergeDataSets.skipCleaningFor())
                .isEqualTo(new String[]{"USER", "FOLLOWER", "TWEET"});
    }

    @Test
    public void shouldMergeReplacers() {
        DataSet classLevel = DataSetImpl.instance().withReplacers(CustomReplacer.class);
        DataSet methodLevel = DataSetImpl.instance().withReplacers(CustomReplacerBar.class, NullReplacer.class);
        DataSet mergeDataSets = AnnotationUtils.mergeDataSetAnnotations(classLevel, methodLevel);

        assertThat(mergeDataSets).isNotNull();
        assertThat(mergeDataSets.replacers())
                .isEqualTo(new Class[]{CustomReplacer.class, CustomReplacerBar.class, NullReplacer.class});
    }
}
