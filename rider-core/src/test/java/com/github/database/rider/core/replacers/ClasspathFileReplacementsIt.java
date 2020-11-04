package com.github.database.rider.core.replacers;

import com.github.database.rider.core.DBUnitRule;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.model.Tweet;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DBUnit(cacheConnection = true)
public class ClasspathFileReplacementsIt {
    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance("rules-it", emProvider.connection());

    @Test
    @DataSet(replacers = ClasspathFileReplacer.class,
            value = "datasets/yml/classpath-file-xml-replacements.yml")
    public void shouldReplaceXmlFileContent() {
        Tweet tweet = (Tweet) EntityManagerProvider.em().createQuery("select t from Tweet t where t.id = '1'").getSingleResult();

        assertThat(tweet).isNotNull();
        assertThat(tweet.getContent()).contains("<DOC ID=\"1\" CONTENT=\"REJSaWRlciE=\"/>");
    }

    @Test
    @DataSet(replacers = ClasspathFileReplacer.class,
            value = "datasets/yml/classpath-file-json-replacements.yml")
    public void shouldReplaceJsonFileContent() {
        Tweet tweet = (Tweet) EntityManagerProvider.em().createQuery("select t from Tweet t where t.id = '1'").getSingleResult();

        assertThat(tweet).isNotNull();
        assertThat(tweet.getContent()).startsWith("{");
        assertThat(tweet.getContent()).contains("\"CONTENT\": \"REJSaWRlciE=\"");
        assertThat(tweet.getContent()).endsWith("}");
    }

    @Test
    @DataSet(replacers = ClasspathFileReplacer.class,
            value = "datasets/yml/classpath-file-csv-replacements.yml")
    public void shouldReplaceCsvFileContent() {
        Tweet tweet = (Tweet) EntityManagerProvider.em().createQuery("select t from Tweet t where t.id = '1'").getSingleResult();

        assertThat(tweet).isNotNull();
        assertThat(tweet.getContent()).contains("ID, USER_ID, FOLLOWER_ID");
        assertThat(tweet.getContent()).contains("1,1,2");
    }

    @Test
    @DataSet(replacers = ClasspathFileReplacer.class,
            value = "datasets/yml/classpath-file-yml-replacements.yml")
    public void shouldReplaceYmlFileContent() {
        Tweet tweet = (Tweet) EntityManagerProvider.em().createQuery("select t from Tweet t where t.id = '1'").getSingleResult();

        assertThat(tweet).isNotNull();
        assertThat(tweet.getContent()).contains("CONTENT: \"[CLASSPATH_FILE]datasets/yml/classpath-file-yml-replacements.yml\"");
    }
}
