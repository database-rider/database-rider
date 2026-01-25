package com.github.database.rider.example;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id
    private long id;

    private String name;

    public String getName() {
        return name;
    }
}
