package com.github.database.rider.junit5;

import org.junit.jupiter.api.extension.ExtendWith;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.junit5.util.EntityManagerProvider;

@ExtendWith(DBUnitExtension.class)
public abstract class BaseDBTest {
	
	 private ConnectionHolder connectionHolder = () ->
     EntityManagerProvider.instance("junit5-pu").clear().connection();


}
