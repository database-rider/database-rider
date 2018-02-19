package com.github.database.rider.spring.model;

/**
 * @author Artemy Osipov
 */
public class Entity {

    private long id;

    private String value;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
