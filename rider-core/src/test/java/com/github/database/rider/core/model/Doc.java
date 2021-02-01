package com.github.database.rider.core.model;

import javax.persistence.*;

@Entity
@Table(name = "doc")
public class Doc {

    @Id
    @GeneratedValue
    private long id;

    @Lob
    private byte[] content;


    public long getId() {
        return id;
    }

    public Doc setId(long id) {
        this.id = id;
        return this;
    }

    public byte[] getContent() {
        return content;
    }

    public Doc setContent(byte[] content) {
        this.content = content;
        return this;
    }
}
