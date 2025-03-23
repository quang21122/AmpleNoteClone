package com.example.amplenoteclone.models;

import com.google.firebase.Timestamp;

public class User {
    private String id;
    private String avatarBase64;
    private Timestamp createdAt;
    private String email;
    private boolean hasCustomAvatar;
    private String name;
    private Boolean isPremium;

    public User() {}

    public User(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.createdAt = Timestamp.now();
        this.hasCustomAvatar = false;
        this.avatarBase64 = "";
        this.isPremium = false;
    }

    // Add ID getter/setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatarBase64() {
        return avatarBase64;
    }

    public void setAvatarBase64(String avatarBase64) {
        this.avatarBase64 = avatarBase64;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIsPremium(Boolean isPremium) {
        this.isPremium = isPremium;
    }

    public boolean isHasCustomAvatar() {
        return hasCustomAvatar;
    }

    public void setHasCustomAvatar(boolean hasCustomAvatar) {
        this.hasCustomAvatar = hasCustomAvatar;
    }

    public String getName() {
        return name;
    }

    public Boolean getIsPremium() {
        return isPremium;
    }

    public void setName(String name) {
        this.name = name;
    }
}
