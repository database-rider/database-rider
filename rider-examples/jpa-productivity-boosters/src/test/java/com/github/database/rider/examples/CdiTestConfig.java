package com.github.database.rider.examples;

import com.github.database.rider.core.util.EntityManagerProvider;
import org.example.CdiConfig;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Specializes;
import jakarta.persistence.EntityManager;


@Specializes
@ApplicationScoped
public class CdiTestConfig extends CdiConfig {


    @Produces
    public EntityManager produce() {
        synchronized (this) {
            return EntityManagerProvider.instance("customerTestDB").em();
        }
    }

}
