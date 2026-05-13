package com.github.database.rider.springboot4.entity.user;

public class Users {

    private Long id;

    private String email;

    private String name;

    public Users() {}

    public Users(Long id) {
		this.id = id;
	}

    public Users(String email, String name) {
		this.email = email;
		this.name = name;
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}