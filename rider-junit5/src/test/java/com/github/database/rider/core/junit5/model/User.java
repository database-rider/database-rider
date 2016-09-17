package com.github.database.rider.core.junit5.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by pestano on 22/07/15.
 */
@Entity
public class User {

    @Id
    @GeneratedValue
    private long id;

    private String name;

    @OneToMany(mappedBy = "user")
    private List<Tweet> tweets;


    public User() {
    }

    public User(long id) {
        this.id = id;
    }

    public User(long id, String name, String tweetId, String tweetContent, Integer tweetLikes) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Tweet> getTweets() {
        return tweets;
    }

    public void setTweets(List<Tweet> tweets) {
        this.tweets = tweets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return id == user.id;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : (int) id;
    }
}
