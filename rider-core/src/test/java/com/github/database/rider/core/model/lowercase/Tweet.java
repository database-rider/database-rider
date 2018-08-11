package com.github.database.rider.core.model.lowercase;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Calendar;

@Entity
@Table(name = "\"tweet\"")
public class Tweet {

    @Id
    @GeneratedValue
    @Column(name = "\"id\"")
    private String id;

    @Size(min = 1, max = 140)
    @Column(name = "\"content\"")
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

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }


    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
