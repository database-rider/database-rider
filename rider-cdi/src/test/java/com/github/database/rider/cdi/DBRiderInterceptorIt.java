package com.github.database.rider.cdi;

import com.github.database.rider.cdi.api.DBRider;
import com.github.database.rider.cdi.model.User;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetProvider;
import com.github.database.rider.core.dataset.builder.DataSetBuilder;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by pestano on 23/07/15.
 */

@RunWith(CdiTestRunner.class)
@DBRider
public class DBRiderInterceptorIt {

    @Inject
    EntityManager em;

    @Test
	@DataSet("yml/users.yml")
	public void shouldListUsers() {
		List<User> users = em.createQuery("select u from User u").getResultList();
		assertThat(users).isNotNull().isNotEmpty().hasSize(2);
	}

	@Test
	@DataSet(provider = UserDataSetProvider.class)
	public void shouldListUsersUsingDataSetProvider() {
		List<User> users = em.createQuery("select u from User u").getResultList();
		assertThat(users).isNotNull().isNotEmpty().hasSize(2);
	}

	@Test
	@DataSet(cleanBefore=true) //avoid conflict with other tests
	public void shouldInsertUser() {
		User user = new User();
		user.setName("user");
		user.setName("@rmpestano");
		em.getTransaction().begin();
		em.persist(user);
		em.getTransaction().commit(); 
		User insertedUser = (User)em.createQuery("select u from User u where u.name = '@rmpestano'").getSingleResult();
		assertThat(insertedUser).isNotNull();
		assertThat(insertedUser.getId()).isNotNull();
	}

	@Test
	@DataSet("yml/users.yml") //no need for clean before because DBUnit uses CLEAN_INSERT seeding strategy which clears involved tables before seeding
	public void shouldUpdateUser() {
		User user = (User) em.createQuery("select u from User u  where u.id = 1").getSingleResult();
		assertThat(user).isNotNull();
		assertThat(user.getName()).isEqualTo("@realpestano");
		//em.getTransaction().begin();
		user.setName("@rmpestano");
		em.merge(user);
		//em.getTransaction().commit(); //no needed because of first level cache 
		User updatedUser = getUser(1);
		assertThat(updatedUser).isNotNull();
	    assertThat(updatedUser.getName()).isEqualTo("@rmpestano");
	}

	@Test
	@DataSet(value = "yml/users.yml", disableConstraints=true)//disable constraints because User 1 has one tweet and a follower
	public void shouldDeleteUser() {
		User user = (User) em.createQuery("select u from User u  where u.id = 1").getSingleResult();
		assertThat(user).isNotNull();
		assertThat(user.getName()).isEqualTo("@realpestano");
		em.getTransaction().begin(); 
		em.remove(user);
		em.getTransaction().commit(); 
		List<User> users = em.createQuery("select u from User u ").getResultList();
		assertThat(users).hasSize(1);
	}
	
	@Test
	@DataSet(value = "yml/users.yml")
	public void shouldDeleteUserWithoutDisablingConstraints() {
		User user = (User) em.createQuery("select u from User u  where u.id = 1").getSingleResult();
		assertThat(user).isNotNull();
		assertThat(user.getName()).isEqualTo("@realpestano");
		em.getTransaction().begin(); 
		em.createQuery("Delete from Tweet t where t.user.id = 1 ").executeUpdate();
		em.createQuery("Delete from Follower f where f.followedUser.id = 1 ").executeUpdate();
		em.remove(user);
		em.getTransaction().commit(); 
		List<User> users = em.createQuery("select u from User u ").getResultList();
		assertThat(users).hasSize(1);
	}

	
	public User getUser(Integer id){
		return (User) em.createQuery("select u from User u where u.id = :id").
				setParameter("id", id).getSingleResult();
	}

	public static class UserDataSetProvider implements DataSetProvider {

		@Override
		public IDataSet provide() throws DataSetException {
			DataSetBuilder builder = new DataSetBuilder();
			builder.table("user")
					.row()
					    .column("id", 1)
					    .column("name", "@dbunit")
					.row()
					     .column("id", 2)
					     .column("name", "@dbrider");
			return builder.build();
		}
	}
}