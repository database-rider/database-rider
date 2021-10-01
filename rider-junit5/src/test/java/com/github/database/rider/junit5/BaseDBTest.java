package com.github.database.rider.junit5;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.junit5.incubating.DBRiderExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DBRiderExtension.class)
public abstract class BaseDBTest {

    private ConnectionHolder connectionHolder = () ->
            EntityManagerProvider.instance("junit5-pu").clear().connection();


}
