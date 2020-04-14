package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.model.lowercase.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
@DBUnit(leakHunter = true, caseSensitiveTableNames = true, escapePattern = "\"?\"")
public class LowercaseCrudIt {

	public EntityManagerProvider emProvider = EntityManagerProvider.instance("lowercase-it");

	@Rule
	public TestRule theRule = RuleChain.outerRule(emProvider).
			around(DBUnitRule.instance(emProvider.connection()));


	@Test
	@DataSet("yml/lowercaseUsers.yml")
	public void shouldListUsers() {
		List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
		assertThat(users).isNotNull().isNotEmpty().hasSize(2);
	}

    @Test
    @DataSet("xml/lowercaseUsers.xml")
    public void shouldListUsersUsingXmlDataSet() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }

    @Test
    @DataSet("json/lowercaseUsers.json")
    public void shouldListUsersUsingJsonDataSet() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }

	@Test
	@DataSet(cleanBefore=true) //avoid conflict with other tests
	public void shouldInsertUser() {
		User user = new User();
		user.setName("user");
		user.setName("@rmpestano");
		EntityManagerProvider.tx().begin();
		EntityManagerProvider.em().persist(user);
		EntityManagerProvider.tx().commit();
		User insertedUser = (User) EntityManagerProvider.em().createQuery("select u from User u where u.name = '@rmpestano'").getSingleResult();
		assertThat(insertedUser).isNotNull();
		assertThat(insertedUser.getId()).isNotNull();
	}

	@Test
	@DataSet("yml/lowercaseUsers.yml") //no need for clean before because DBUnit uses CLEAN_INSERT seeding strategy which clears involved tables before seeding
	public void shouldUpdateUser() {
		User user = (User) EntityManagerProvider.em().createQuery("select u from User u  where u.id = 1").getSingleResult();
		assertThat(user).isNotNull();
		assertThat(user.getName()).isEqualTo("@realpestano");
		//tx().begin(); 
		user.setName("@rmpestano");
		EntityManagerProvider.em().merge(user);
		//tx().commit(); //no needed because of first level cache 
		User updatedUser = getUser(1);
		assertThat(updatedUser).isNotNull();
	    assertThat(updatedUser.getName()).isEqualTo("@rmpestano");
	}

	@Test
	@DataSet(value = "yml/lowercaseUsers.yml", disableConstraints=true)//disable constraints because User 1 has one tweet and a follower
	public void shouldDeleteUser() {
		User user = (User) EntityManagerProvider.em().createQuery("select u from User u  where u.id = 1").getSingleResult();
		assertThat(user).isNotNull();
		assertThat(user.getName()).isEqualTo("@realpestano");
		EntityManagerProvider.tx().begin();
		EntityManagerProvider.em().remove(user);
		EntityManagerProvider.tx().commit();
		List<User> users = EntityManagerProvider.em().createQuery("select u from User u ").getResultList();
		assertThat(users).hasSize(1);
	}
	
	@Test
	@DataSet(value = "yml/lowercaseUsers.yml")
	public void shouldDeleteUserWithoutDisablingConstraints() {
		User user = (User) EntityManagerProvider.em().createQuery("select u from User u  where u.id = 1").getSingleResult();
		assertThat(user).isNotNull();
		assertThat(user.getName()).isEqualTo("@realpestano");
		EntityManagerProvider.em().getTransaction().begin();
		EntityManagerProvider.em().createQuery("Delete from Tweet t where t.user.id = 1 ").executeUpdate();
		EntityManagerProvider.em().createQuery("Delete from Follower f where f.followedUser.id = 1 ").executeUpdate();
		EntityManagerProvider.em().remove(user);
		EntityManagerProvider.em().getTransaction().commit();
		List<User> users = EntityManagerProvider.em().createQuery("select u from User u ").getResultList();
		assertThat(users).hasSize(1);
	}


	public User getUser(Integer id){
		return (User) EntityManagerProvider.em().createQuery("select u from User u where u.id = :id").
				setParameter("id", id).getSingleResult();
	}

}
