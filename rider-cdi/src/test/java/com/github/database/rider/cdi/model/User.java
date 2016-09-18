package com.github.database.rider.cdi.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @OneToMany(mappedBy = "followedUser")
    private Set<Follower> followers;

    public User() {
    }

    public User(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Tweet> getTweets() {
        return tweets;
    }

    public void setTweets(List<Tweet> tweets) {
        this.tweets = tweets;
    }

    public Set<Follower> getFollowers() {
        return followers;
    }

    public void setFollowers(HashSet<Follower> followers) {
        this.followers = followers;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
        return (int) (id ^ (id >>> 32));
    }
}
