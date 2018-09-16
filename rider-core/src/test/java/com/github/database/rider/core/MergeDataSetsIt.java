package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DBUnit;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.github.database.rider.core.api.dataset.AnotherMetaDataSet;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.model.Tweet;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import static com.github.database.rider.core.util.EntityManagerProvider.*;


@RunWith(JUnit4.class)
@DataSet(value="yml/tweet.yml")
@DBUnit(mergeDataSets = true)
public class MergeDataSetsIt {

	@Rule
	public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it"); 

	@Rule
	public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection()); 
	
	@Test
    @DataSet(value="yml/user.yml")
	public void shouldMergeDataSetsFromClassAndMethod() {
		List<User> users = em().createQuery("select u from User u").getResultList();
		assertThat(users).isNotNull().isNotEmpty().hasSize(2);
        
        User user = (User) em().createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user.getTweets()).isNotEmpty(); //tweets comes from class level annotation merged with method level
        assertThat(user.getTweets().get(0).getContent()).isEqualTo("dbunit rules again!"); 
	}
    
	@Test
	@AnotherMetaDataSet
	public void shouldMergeDataSetsUsingMetaAnnotation() {
		List<User> users = em().createQuery("select u from User u").getResultList();
		assertThat(users).isNotNull().isNotEmpty().hasSize(1); //metadataset dataset has 1 user
        Tweet tweet = (Tweet) em().createQuery("select t from Tweet t where t.id = 'abcdef12345'").getSingleResult();
        assertThat(tweet).isNotNull(); //tweets comes from class level annotation merged with method level
	}
}
