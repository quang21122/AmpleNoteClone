package com.example.amplenoteclone.models;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.Date;

public class Note {
    private String id;
    private String title;
    private String content;
    private ArrayList<String> tags;
    private ArrayList<String> tasks;
    private Date createdAt;
    private String userId;
    private Date updatedAt;
    Boolean isProtected;

    public Note() {
        this.tags = new ArrayList<>();
        this.tasks = new ArrayList<>();
    }

    public Note(String id, String title, String content, ArrayList<String> tags, ArrayList<String> tasks, Date createdAt, Date updatedAt, Boolean isProtected) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isProtected = isProtected;

        // Get the current user ID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            this.userId = user.getUid();
        }
    }

    @Exclude
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
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
