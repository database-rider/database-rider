package com.github.database.rider.cdi.model;

import jakarta.persistence.*;

/**
 * Created by pestano on 22/07/15.
 */
@Entity
public class Follower {

    @Id
    @GeneratedValue
    private long id;

    @JoinColumn(name = "follower_id")
    private User followerUser;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User followedUser;

    public Follower() {
    }

    public Follower(long followerId, long followedId) {
        this.followerUser = new User(followerId);
        this.followedUser = new User(followedId);
    }

    public User getFollowedUser() {
        return followedUser;
    }

    public void setFollowedUser(User followedUser) {
        this.followedUser = followedUser;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getFollowerUser() {
        return followerUser;
    }

    public void setFollowerUser(User followerUser) {
        this.followerUser = followerUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Follower followerToCompare = (Follower) o;

        if (!followerUser.equals(followerToCompare.followerUser)) return false;
        return !(followedUser != null ? !followedUser.equals(followerToCompare.followedUser) : followerToCompare.followedUser != null);

    }

    @Override
    public int hashCode() {
        int result = followedUser.hashCode();
        result = 31 * result + (followedUser != null ? followedUser.hashCode() : 0);
        return result;
    }
}
