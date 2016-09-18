package com.github.database.rider.example;


import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User {
    @Id
    private long id;

    private String name;

    public String getName() {
        return name;
    }
}
