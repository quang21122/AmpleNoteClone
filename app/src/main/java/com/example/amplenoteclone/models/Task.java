package com.example.amplenoteclone.models;

import java.util.Date;

public class Task {
    private String id;
    private String noteId;
    private String userId;
    private boolean isCompleted;
    private String title;
    private Date createAt;

    private String repeat;
    private String startAtDate;
    private String startAtPeriod;
    private String startAtTime;
    private Date startAt;
    private int startNoti;
    private String priority;
    private int duration;
    private String hideUntilDate;
    private String hideUntilTime;
    private Date hideUntil;
    private float score;

    // Constructor
    public Task(boolean isCompleted, String title, Date createAt, String id, String repeat,
                String startAtDate, String startAtPeriod, String startAtTime, int startNoti, // Changed to int
                String priority, int duration, // Changed to int
                String hideUntilDate, String hideUntilTime) {
        this.isCompleted = isCompleted;
        this.title = title;
        this.createAt = createAt != null ? createAt : new Date();
        this.id = id;
        this.repeat = repeat;
        this.startAtDate = startAtDate;
        this.startAtPeriod = startAtPeriod;
        this.startAtTime = startAtTime;
        this.startNoti = startNoti; // Changed to int
        this.priority = priority;
        this.duration = duration; // Changed to int
        this.hideUntilDate = hideUntilDate;
        this.hideUntilTime = hideUntilTime;
        this.score = 0.0f;
    }

    // Getters and Setters
    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt != null ? createAt : new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public String getStartAtDate() {
        return startAtDate;
    }

    public void setStartAtDate(String startAtDate) {
        this.startAtDate = startAtDate;
    }

    public String getStartAtPeriod() {
        return startAtPeriod;
    }

    public void setStartAtPeriod(String startAtPeriod) {
        this.startAtPeriod = startAtPeriod;
    }

    public String getStartAtTime() {
        return startAtTime;
    }

    public void setStartAtTime(String startAtTime) {
        this.startAtTime = startAtTime;
    }

    public Date getStartAt() {
        return startAt;
    }

    public void setStartAt(Date startAt) {
        this.startAt = startAt;
    }

    public int getStartNoti() { // Changed to int
        return startNoti;
    }

    public void setStartNoti(int startNoti) { // Changed to int
        this.startNoti = startNoti;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public int getDuration() { // Changed to int
        return duration;
    }

    public void setDuration(int duration) { // Changed to int
        this.duration = duration;
    }

    public String getHideUntilDate() {
        return hideUntilDate;
    }

    public void setHideUntilDate(String hideUntilDate) {
        this.hideUntilDate = hideUntilDate;
    }

    public String getHideUntilTime() {
        return hideUntilTime;
    }

    public void setHideUntilTime(String hideUntilTime) {
        this.hideUntilTime = hideUntilTime;
    }

    public Date getHideUntil() {
        return hideUntil;
    }

    public void setHideUntil(Date hideUntil) {
        this.hideUntil = hideUntil;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}