package com.github.database.rider.cdi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.cdi.api.DBUnitInterceptor;
import com.github.database.rider.cdi.model.User;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by pestano on 23/07/15.
 */

@RunWith(CdiTestRunner.class)
@DBUnitInterceptor
public class CrudCDIIt {

    @Inject
    EntityManager em;



    @Test
	@DataSet("yml/users.yml")
	public void shouldListUsers() {
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
}