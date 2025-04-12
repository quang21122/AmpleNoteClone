package com.example.amplenoteclone.models;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Jot {
    private String id;
    private String text;
    private Date createdAt;

    // Empty constructor required for Firestore deserialization
    public Jot() {}

    public Jot(String text, Date createdAt) {
        this.text = text;
        this.createdAt = createdAt;
    }

    public static Jot fromDocument(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;

        Jot jot = new Jot(
                doc.getString("text"),
                doc.getDate("createdAt")
        );
        jot.setId(doc.getId());
        return jot;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("text", text);
        map.put("createdAt", createdAt != null ? createdAt : new Date());
        return map;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}

