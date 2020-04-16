package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
@DataSet(value = "xml/user-with-omitted-columns.xml", cleanBefore = true)
public class DBUnitColumnSensingIt {
	@Rule
	public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");
	@Rule
	public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection());

	@Test
	public void columnNotDefinedBeforeShouldBeNull() {
		// GIVEN a dataset whose first row does not include all possible columns
		// AND GIVEN that the DBUnit column sensing feature is disabled

		// WHEN querying for a row that is known to have additional columns compared to the first row
		User userWithNullName = (User) EntityManagerProvider.em().createQuery("from User u where u.id = '2'")
				.getSingleResult();

		// THEN the value of one of those columns is null
		assertThat(userWithNullName.getName()).isNull();
	}

	@Test
	@DBUnit(columnSensing = true)
	public void columnNotDefinedBeforeShouldNotBeNull() {
		// GIVEN a dataset whose first row does not include all possible columns
		// AND GIVEN that the DBUnit column sensing feature is now enabled

		// WHEN querying for a row that is known to have additional columns compared to the first row
		User userWithName = (User) EntityManagerProvider.em().createQuery("from User u where u.id = '2'")
				.getSingleResult();

		// THEN the value of one of those columns is not null
		assertThat(userWithName.getName()).isNotNull();
	}
}
