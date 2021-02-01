package com.github.database.rider.core.model;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Calendar;

/**
 * Created by pestano on 22/07/15.
 */
@Entity
public class Tweet {

    @Id
    @GeneratedValue
    private String id;

    @Size(min = 1, max = 140)
    private String content;

    private Integer likes;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar date;

    private Long timestamp;

    @ManyToOne
    User user;

    public String getContent() {
        return content;
    }

    public Tweet setContent(String content) {
        this.content = content;
        return this;
    }

    public Integer getLikes() {
        return likes;
    }

    public Tweet setLikes(Integer likes) {
        this.likes = likes;
        return this;
    }

    public Calendar getDate() {
        return date;
    }

    public Tweet setDate(Calendar date) {
        this.date = date;
        return this;
    }

    public String getId() {
        return id;
    }

    public Tweet setId(String id) {
        this.id = id;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Tweet setUser(User user) {
        this.user = user;
        return this;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tweet tweet = (Tweet) o;

        return !(id != null ? !id.equals(tweet.id) : tweet.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
