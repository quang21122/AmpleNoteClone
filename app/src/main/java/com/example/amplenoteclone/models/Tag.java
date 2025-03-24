package com.example.amplenoteclone.models;

public class Tag {
    private String name;
    private String userId;
    private int count;
    public Tag(String name, String userId) {
        this.name = name;
        this.userId = userId;
        this.count = 0;
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }

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
