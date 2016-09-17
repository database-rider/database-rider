package com.github.database.rider.core;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.core.model.Tweet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by pestano on 15/02/16.
 */
@DBUnit(cacheConnection = true)
public class DateReplacementsIt {

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
    @DataSet(value = "datasets/yml/date-replacements.yml",disableConstraints = true, executorId = "rules-it")
    public void shouldReplaceDateWithNowPlaceHolder() {
        Tweet tweet = (Tweet) emProvider.em().createQuery("select t from Tweet t where t.id = '1'").getSingleResult();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getDate().get(Calendar.DAY_OF_MONTH)).isEqualTo(now.get(Calendar.DAY_OF_MONTH));
        assertThat(tweet.getDate().get(Calendar.HOUR_OF_DAY)).isEqualTo(now.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    @DataSet(value = "datasets/yml/date-replacements.yml",disableConstraints = true, executorId = "rules-it")
    public void shouldReplaceDateWithYesterdayPlaceHolder() {
        Tweet tweet = (Tweet) emProvider.em().createQuery("select t from Tweet t where t.id = '2'").getSingleResult();
        assertThat(tweet).isNotNull();
        Calendar date = (Calendar)tweet.getDate().clone();
        date.add(Calendar.DAY_OF_MONTH, 1);
        assertThat(date.get(Calendar.DAY_OF_MONTH)).isEqualTo(now.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    @DataSet(value = "datasets/yml/date-replacements.yml",disableConstraints = true, executorId = "rules-it")
    public void shouldReplaceDateWithTomorrowPlaceHolder() {
        Tweet tweet = (Tweet) emProvider.em().createQuery("select t from Tweet t where t.id = '3'").getSingleResult();
        assertThat(tweet).isNotNull();
        Calendar date = (Calendar)tweet.getDate().clone();
        date.add(Calendar.DAY_OF_MONTH, -1);
        assertThat(date.get(Calendar.DAY_OF_MONTH)).isEqualTo(now.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    @DataSet(value = "datasets/yml/date-replacements.yml",disableConstraints = true, executorId = "rules-it")
    public void shouldReplaceDateWithYearAfterPlaceHolder() {
        Tweet tweet = (Tweet) emProvider.em().createQuery("select t from Tweet t where t.id = '4'").getSingleResult();
        assertThat(tweet).isNotNull();
        Calendar date = (Calendar)tweet.getDate().clone();
        date.add(Calendar.YEAR, -1);
        assertThat(date.get(Calendar.YEAR)).isEqualTo(now.get(Calendar.YEAR));
    }

    @Test
    @DataSet(value = "datasets/yml/date-replacements.yml",disableConstraints = true, executorId = "rules-it")
    public void shouldReplaceDateWithYearBeforePlaceHolder() {
        Tweet tweet = (Tweet) emProvider.em().createQuery("select t from Tweet t where t.id = '5'").getSingleResult();
        assertThat(tweet).isNotNull();
        Calendar date = (Calendar)tweet.getDate().clone();
        date.add(Calendar.YEAR, 1);
        assertThat(date.get(Calendar.YEAR)).isEqualTo(now.get(Calendar.YEAR));
    }

    @Test
    @DataSet(value = "datasets/yml/date-replacements.yml",disableConstraints = true, executorId = "rules-it")
    public void shouldReplaceDateWithHourPlaceHolder() {
        Tweet tweet = (Tweet) emProvider.em().createQuery("select t from Tweet t where t.id = '6'").getSingleResult();
        assertThat(tweet).isNotNull();
        Calendar date = (Calendar)tweet.getDate().clone();
        date.add(Calendar.HOUR_OF_DAY, -1);
        assertThat(date.get(Calendar.HOUR_OF_DAY)).isEqualTo(now.get(Calendar.HOUR_OF_DAY));
    }
}
