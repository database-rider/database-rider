package com.github.database.rider.core.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Table name "LIMIT" belongs to H2 database reserved words.
 */
@Entity
@Table(name = "\"LIMIT\"")
public class Limit {

    @Id
    @GeneratedValue
    private long id;

    private String name;

    public Limit() {
    }

    public Limit(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Limit groups = (Limit) o;

        return id == groups.id;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : (int) id;
    }
}
