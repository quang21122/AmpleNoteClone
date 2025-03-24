package com.example.amplenoteclone.models;

import java.io.Serializable;
import java.util.ArrayList;

public class Note implements Serializable {
    private String id;
    private String title;
    private String content;
    private ArrayList<String> tags;
    private ArrayList<String> tasks;
    private long createdAt;
    private String userId;
    private long updatedAt;
    Boolean isProtected;

    public Note() {
        this.tags = new ArrayList<>();
        this.tasks = new ArrayList<>();
    }

    public Note(String id, String title, String content, ArrayList<String> tags, ArrayList<String> tasks, long createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public ArrayList<String> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<String> tasks) {
        this.tasks = tasks;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getProtected() {
        return isProtected;
    }

    public void setProtected(Boolean isProtected) {
        this.isProtected = isProtected;
    }
}
