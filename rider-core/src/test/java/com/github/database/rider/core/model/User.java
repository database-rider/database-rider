package com.github.database.rider.core.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pestano on 22/07/15.
 */
@Entity
@Table(name = "USER")
public class User {

    @Id
    @GeneratedValue
    @Column(name = "\"id\"")
    private long id;

    @Column(name = "\"name\"")
    private String name;

    @OneToMany(mappedBy = "user")
    private List<Tweet> tweets;

    @OneToMany(mappedBy = "followedUser")
    private List<Follower> followers;

    public User() {
    }

    public User(long id) {
        this.id = id;
    }

    public User(long id, String name, String tweetId, String tweetContent, Integer tweetLikes) {
        this.id = id;
        this.name = name;
        this.tweets = new ArrayList<>();
        Tweet tweet = new Tweet();
        tweet.setId(tweetId);
        tweet.setContent(tweetContent);
        tweet.setLikes(tweetLikes);
        tweets.add(tweet);
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

    public List<Follower> getFollowers() {
        return followers;
    }

    public void setFollowers(List<Follower> followers) {
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
        return name != null ? name.hashCode() : (int) id;
    }
}
