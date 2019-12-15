package com.github.database.rider.springboot.model.company;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "company")
public class Company {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    private String name;


    public Company() {
    }

    public Company(long id) {
        this.id = id;
    }

    public Company(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long value) {
        this.id = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Company user = (Company) o;

        return id == user.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
