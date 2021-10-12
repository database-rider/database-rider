package com.github.database.rider.junit5;


import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.junit5.util.EntityManagerProvider;
import com.github.database.rider.junit5.model.User;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitPlatform.class)
@MetaDataSet
public class MetaDataSetIt {

    private ConnectionHolder connectionHolder = () -> //<3>
            EntityManagerProvider.instance("junit5-pu").connection();

    @Test
    public void testMetaAnnotationOnClass() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }

    @Test
    @AnotherMetaDataSet
    public void testMetaAnnotationOnMethod() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(1);
    }
}

