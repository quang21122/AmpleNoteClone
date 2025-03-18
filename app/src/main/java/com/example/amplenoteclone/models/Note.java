package com.example.amplenoteclone.models;

import java.util.ArrayList;

public class Note {

    private long id;
    private String title;
    private String content;
    private ArrayList<String> tags;
    private ArrayList<String> tasks;
    private String createdAt;
    private int userId;
    private String updatedAt;
    Boolean isProtected;

    public Note() {
        this.tags = new ArrayList<>();
        this.tasks = new ArrayList<>();
    }

    public Note(long id, String title, String content, ArrayList<String> tags, String createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Boolean getProtected() {
        return isProtected;
    }

    public void setProtected(Boolean isProtected) {
        this.isProtected = isProtected;
    }
}
