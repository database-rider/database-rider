package com.github.database.rider.cdi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.github.database.rider.cdi.api.DBUnitInterceptor;
import com.github.database.rider.cdi.model.MetaDataSet;
import com.github.database.rider.cdi.model.User;
import com.github.database.rider.cdi.model.AnotherMetaDataSet;

@RunWith(CdiTestRunner.class)
@DBUnitInterceptor
@MetaDataSet
public class MetaDataSetIt {

	@Inject
	EntityManager em;

	@Test
	public void testMetaAnnotationOnClass() {
		List<User> users = em.createQuery("select u from User u").getResultList();
		assertThat(users).isNotNull().isNotEmpty().hasSize(2);
	}

	@Test
	@AnotherMetaDataSet
	public void testMetaAnnotationOnMethod() {
		List<User> users = em.createQuery("select u from User u").getResultList();
		assertThat(users).isNotNull().isNotEmpty().hasSize(1);
	}
}
