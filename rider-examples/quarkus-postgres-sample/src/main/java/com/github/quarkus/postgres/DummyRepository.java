package com.github.quarkus.postgres;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DummyRepository implements PanacheRepository<DummyEntity> {
}
