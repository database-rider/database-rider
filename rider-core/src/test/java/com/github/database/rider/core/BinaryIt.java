package com.github.database.rider.core;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetProvider;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.dataset.builder.DataSetBuilder;
import com.github.database.rider.core.model.Doc;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import javax.xml.bind.DatatypeConverter;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class BinaryIt {

    public EntityManagerProvider emProvider = EntityManagerProvider.instance("doc-it");

    @Rule
    public TestRule theRule = RuleChain.outerRule(emProvider).
            around(DBUnitRule.instance(emProvider.connection()));


    @Test
    @DataSet("yml/doc.yml")
    public void shouldSeedBinaryData() {
        Doc doc = em().createQuery("select d from Doc d where d.id = 1", Doc.class).getSingleResult();
        assertThat(new String(doc.getContent()))
                .isEqualTo("DBRider!");
    }

    @Test
    @DataSet("json/doc.json")
    public void shouldSeedBinaryDataInJson() {
        Doc doc = em().createQuery("select d from Doc d where d.id = 1", Doc.class).getSingleResult();
        assertThat(new String(doc.getContent()))
                .isEqualTo("DBRider!");
    }

    @Test
    @DataSet("xml/doc.xml")
    public void shouldSeedBinaryDataInXml() {
        Doc doc = em().createQuery("select d from Doc d where d.id = 1", Doc.class).getSingleResult();
        assertThat(new String(doc.getContent()))
                .isEqualTo("DBRider!");
    }

    @Test
    @DataSet(provider = BinaryDataSetProvider.class)
    public void shouldSeedBinaryDataUsingDataSetProvider() {
        Doc doc = em().createQuery("select d from Doc d where d.id = 1", Doc.class).getSingleResult();
        assertThat(new String(doc.getContent()))
                .isEqualTo("DBRider!");
    }

    @Test
    @DataSet(transactional = true, cleanBefore = true)
    @ExpectedDataSet("yml/doc.yml")
    public void shouldExpectBinaryData() {
        em().persist(new Doc().setContent("DBRider!".getBytes()));
    }

    public static class BinaryDataSetProvider implements DataSetProvider {

        @Override
        public IDataSet provide() throws DataSetException {
            DataSetBuilder builder = new DataSetBuilder();
            return builder
                    .table("DOC")
                    .row()
                    .column("ID", 1)
                    .column("CONTENT", DatatypeConverter.printBase64Binary("DBRider!".getBytes()))
                    .build();
        }
    }
}
