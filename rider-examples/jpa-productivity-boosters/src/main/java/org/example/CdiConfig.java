package org.example;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * @author Matti Tahvonen
 */
public class CdiConfig {

    @Produces
    @Dependent
    @PersistenceContext(unitName = "customerDB")
    private EntityManager entityManager;
}
