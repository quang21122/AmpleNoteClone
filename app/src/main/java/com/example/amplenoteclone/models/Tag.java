package com.example.amplenoteclone.models;

import com.google.firebase.firestore.Exclude;

public class Tag {
    private String id;
    private String name;
    private String userId;
    private int count;
    public Tag(String name, String userId) {
        this.name = name;
        this.userId = userId;
        this.count = 0;
    }

    public Tag(){}

    @Exclude
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }

    @Exclude
    public int getCount() {
        return count;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
