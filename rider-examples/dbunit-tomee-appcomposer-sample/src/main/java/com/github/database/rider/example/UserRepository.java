package com.github.database.rider.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import static jakarta.transaction.Transactional.TxType.SUPPORTS;

@ApplicationScoped
public class UserRepository {
    @PersistenceContext
    private EntityManager em;

    @Transactional(SUPPORTS)
    public User find(final long id) {
        return em.find(User.class, id);
    }
}
