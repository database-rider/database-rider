package com.github.database.rider.springboot4.entity.company;

public class Company {

	private Long id;

    private String name;

    public Company() {}

    public Company(Long id) {
		this.id = id;
	}

    public Company(String name) {
		this.name = name;
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}