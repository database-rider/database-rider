package com.github.database.rider.cdi.infra;


import com.github.database.rider.cdi.api.RiderPU;
import com.github.database.rider.core.util.EntityManagerProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.persistence.EntityManager;

/**
 * Created by pestano on 09/10/15.
 */
@ApplicationScoped
public class EntityManagerProducer {


    @Produces
    public EntityManager produce() {
        return EntityManagerProvider.instance("cdipu").em();
    }

    @Produces
    @RiderPU("cdipu2")
    public EntityManager produceEm2() {
        return EntityManagerProvider.instance("cdipu2").em();
    }

    @Produces
    @RiderPU("cdipu3")
    public EntityManager produceEm() {
        return EntityManagerProvider.instance("cdipu3").em();
    }

}
