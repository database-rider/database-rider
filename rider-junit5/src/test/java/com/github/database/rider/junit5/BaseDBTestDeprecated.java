package com.github.database.rider.junit5;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.junit5.util.EntityManagerProvider;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DBUnitExtension.class)
public abstract class BaseDBTestDeprecated {

    private ConnectionHolder connectionHolder = () ->
            EntityManagerProvider.instance("junit5-pu").clear().connection();


}
