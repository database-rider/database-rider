package com.github.database.rider.core.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "`Order`")
public class Order {

    @Id
    @GeneratedValue
    private long id;

    private String name;

    public Order() {
    }

    public Order(long id) {
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order user = (Order) o;

        return id == user.id;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : (int) id;
    }
}
