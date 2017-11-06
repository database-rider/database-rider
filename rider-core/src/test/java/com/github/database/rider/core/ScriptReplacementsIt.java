package com.github.database.rider.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.List;

import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.database.rider.core.model.Tweet;

/**
 * Created by pestano on 15/02/16.
 */
public class ScriptReplacementsIt {

    Calendar now;

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance("rules-it",emProvider.connection());

    @Before
    public void setup(){
        now = Calendar.getInstance();
    }

    @Test
    @DataSet(value = "datasets/yml/js-with-date-replacements.yml",cleanBefore = true ,disableConstraints = true, executorId = "rules-it")
    public void shouldReplaceDateUsingJavaScriptInDataset() {
        Tweet tweet = (Tweet) emProvider.em().createQuery("select t from Tweet t where t.id = '1'").
                getSingleResult();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getDate().get(Calendar.DAY_OF_MONTH)).
                isEqualTo(now.get(Calendar.DAY_OF_MONTH));
        assertThat(tweet.getDate().get(Calendar.HOUR_OF_DAY)).
                isEqualTo(now.get(Calendar.HOUR_OF_DAY));
    }

// tag::javascript-likes[]
    @Test
    @DataSet(value = "datasets/yml/js-with-calc-replacements.yml",cleanBefore = true, disableConstraints = true, executorId = "rules-it")
    public void shouldReplaceLikesUsingJavaScriptInDataset() {
        Tweet tweet = (Tweet) emProvider.em().createQuery("select t from Tweet t where t.id = '1'").getSingleResult();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getLikes()).isEqualTo(50);
    }
// end::javascript-likes[]


// tag::groovy[]
    @Test
    @DataSet(value = "datasets/yml/groovy-with-date-replacements.yml",cleanBefore = true, disableConstraints = true, executorId = "rules-it")
    public void shouldReplaceDateUsingGroovyInDataset() {
        Tweet tweet = (Tweet) emProvider.em().createQuery("select t from Tweet t where t.id = '1'").getSingleResult();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getDate().get(Calendar.DAY_OF_MONTH)).
                isEqualTo(now.get(Calendar.DAY_OF_MONTH));
        assertThat(tweet.getDate(). get(Calendar.HOUR_OF_DAY)).
                isEqualTo(now.get(Calendar.HOUR_OF_DAY));
    }
// end::groovy[]


    @Test
    @DataSet(value = "datasets/yml/random-replacements.yml",cleanBefore = true, disableConstraints = true, executorId = "rules-it")
    public void shouldGenerateDifferentValuesInScriptEvaluation() {
        List<Tweet> tweets = emProvider.em().createQuery("select t from Tweet t").getResultList();
        assertThat(tweets).isNotNull().hasSize(2);
        Tweet tweet1 = tweets.get(0);
        Tweet tweet2 = tweets.get(1);
        assertThat(tweet1).isNotEqualTo(tweet2);

        assertThat(tweet1.getContent()).isNotEqualTo(tweet2.getContent());
    }

// tag::unknown[],i.e. part of value instead of defining a script engine
    @Test
    @DataSet(value = "datasets/yml/colonContent-replacements.yml",cleanBefore = true, disableConstraints = true, executorId = "rules-it")
    public void shouldLogWarningSinceColonIsPartOfValueAndImpliesNullScriptEngine() {
        List<Tweet> tweets = emProvider.em().createQuery("select t from Tweet t").getResultList();
        assertThat(tweets).isNotNull().hasSize(2);
        Tweet tweet1 = tweets.get(0);
        Tweet tweet2 = tweets.get(1);
        assertThat(tweet1).isNotEqualTo(tweet2);

        assertThat(tweet1.getContent()).isEqualTo("FOO:01"); // pure content, no (=null) script engine found, warn log 
        assertThat(tweet2.getContent()).isEqualTo("FOO:02");
        assertThat(tweet1.getDate()).isLessThanOrEqualTo(tweet2.getDate()); // groovy:new Date()'s
    }
// end::unknown[]

}
