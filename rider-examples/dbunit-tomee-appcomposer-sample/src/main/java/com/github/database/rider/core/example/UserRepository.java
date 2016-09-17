package com.github.database.rider.core.example;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static javax.transaction.Transactional.TxType.SUPPORTS;

@ApplicationScoped
public class UserRepository {
    @PersistenceContext
    private EntityManager em;

    @Transactional(SUPPORTS)
    public User find(final long id) {
        return em.find(User.class, id);
    }
}
