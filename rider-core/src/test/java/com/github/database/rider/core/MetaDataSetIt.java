package com.github.database.rider.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.github.database.rider.core.api.dataset.AnotherMetaDataSet;
import com.github.database.rider.core.api.dataset.MetaDataSet;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import static com.github.database.rider.core.util.EntityManagerProvider.*;


//tag::expectedDeclaration[]
@RunWith(JUnit4.class)
@MetaDataSet
public class MetaDataSetIt {

	@Rule
	public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it"); 

	@Rule
	public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection()); 
	
// end::expectedDeclaration[]	

// tag::class-level[]
	@Test
	public void testMetaAnnotationOnClass() {
		List<User> users = em().createQuery("select u from User u").getResultList();
		assertThat(users).isNotNull().isNotEmpty().hasSize(2);
	}
// end::class-level[]
	
// tag::method-level[]
	@Test
	@AnotherMetaDataSet
	public void testMetaAnnotationOnMethod() {
		List<User> users = em().createQuery("select u from User u").getResultList();
		assertThat(users).isNotNull().isNotEmpty().hasSize(1);
	}
// end::method-level[]
}
