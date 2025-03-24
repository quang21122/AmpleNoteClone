package com.example.amplenoteclone.models;
import com.example.amplenoteclone.R;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {
    private String id;
    private String userId;
    private String noteId;
    private String title;
    private Date createAt;
    private boolean isCompleted;
    private String repeat;
    private Date startAt;
    private String startAtDate;
    private String startAtPeriod;
    private String startAtTime;
    private int startNoti;
    private Date hideUntil;
    private String hideUntilDate;
    private String hideUntilTime;
    private String priority;
    private int duration;
    private float score;

    // Constructor mặc định (yêu cầu bởi Firestore)
    public Task() {
    }

    // Constructor đầy đủ
    public Task(String userId, String noteId, String title, Date createAt, boolean isCompleted, String repeat,
                Date startAt, String startAtDate, String startAtPeriod, String startAtTime, int startNoti,
                Date hideUntil, String hideUntilDate, String hideUntilTime, String priority, int duration, float score) {
        this.userId = userId;
        this.noteId = noteId;
        this.title = title;
        this.createAt = createAt;
        this.isCompleted = isCompleted;
        this.repeat = repeat;
        this.startAt = startAt;
        this.startAtDate = startAtDate;
        this.startAtPeriod = startAtPeriod;
        this.startAtTime = startAtTime;
        this.startNoti = startNoti;
        this.hideUntil = hideUntil;
        this.hideUntilDate = hideUntilDate;
        this.hideUntilTime = hideUntilTime;
        this.priority = priority;
        this.duration = duration;
        this.score = score;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", noteId='" + noteId + '\'' +
                ", title='" + title + '\'' +
                ", createAt=" + createAt +
                ", isCompleted=" + isCompleted +
                ", repeat='" + repeat + '\'' +
                ", startAt=" + startAt +
                ", startAtDate='" + startAtDate + '\'' +
                ", startAtPeriod='" + startAtPeriod + '\'' +
                ", startAtTime='" + startAtTime + '\'' +
                ", startNoti=" + startNoti +
                ", hideUntil=" + hideUntil +
                ", hideUntilDate='" + hideUntilDate + '\'' +
                ", hideUntilTime='" + hideUntilTime + '\'' +
                ", priority='" + priority + '\'' +
                ", duration=" + duration +
                ", score=" + score +
                '}';
    }

    // Getters và Setters với @PropertyName để ánh xạ tên trường trên Firestore
    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("userId")
    public String getUserId() {
        return userId;
    }

    @PropertyName("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @PropertyName("noteId")
    public String getNoteId() {
        return noteId;
    }

    @PropertyName("noteId")
    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @PropertyName("createAt")
    public Date getCreateAt() {
        return createAt;
    }

    @PropertyName("createAt")
    public void setCreateAt(Date createAt) {
        this.createAt = createAt != null ? createAt : new Date();
    }

    @PropertyName("isCompleted")
    public boolean isCompleted() {
        return isCompleted;
    }

    @PropertyName("isCompleted")
    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    @PropertyName("startAt")
    public Date getStartAt() {
        return startAt;
    }

    @PropertyName("startAt")
    public void setStartAt(Date startAt) {
        this.startAt = startAt;
    }

    @PropertyName("startAtDate")
    public String getStartAtDate() {
        return startAtDate;
    }

    @PropertyName("startAtDate")

    public void setStartAtDate(String startAtDate) {
        this.startAtDate = startAtDate;
    }
  
    @PropertyName("startAtPeriod")

    @PropertyName("startAtPeriod")

    public String getStartAtPeriod() {
        return startAtPeriod;
    }

    @PropertyName("startAtPeriod")

    public void setStartAtPeriod(String startAtPeriod) {
        this.startAtPeriod = startAtPeriod;
    }

    @PropertyName("startAtTime")

    public String getStartAtTime() {
        return startAtTime;
    }

    @PropertyName("startAtTime")

    public void setStartAtTime(String startAtTime) {
        this.startAtTime = startAtTime;
    }

    @PropertyName("startNoti")
    public int getStartNoti() {
        return startNoti;
    }

    @PropertyName("startNoti")
    public void setStartNoti(int startNoti) {
        this.startNoti = startNoti;
    }

    @PropertyName("hideUntil")
    public Date getHideUntil() {
        return hideUntil;
    }

    @PropertyName("hideUntil")
    public void setHideUntil(Date hideUntil) {
        this.hideUntil = hideUntil;
    }

    @PropertyName("hideUntilDate")
    public String getHideUntilDate() {
        return hideUntilDate;
    }

    @PropertyName("hideUntilDate")

    public void setHideUntilDate(String hideUntilDate) {
        this.hideUntilDate = hideUntilDate;
    }

    @PropertyName("hideUntilTime")

    public String getHideUntilTime() {
        return hideUntilTime;
    }

    @PropertyName("hideUntilTime")

    public void setHideUntilTime(String hideUntilTime) {
        this.hideUntilTime = hideUntilTime;
    }
  
    @PropertyName("priority")
    public String getPriority() {
        return priority;
    }

    @PropertyName("priority")
    public String getPriority() {
        return priority;
    }

    @PropertyName("priority")
    public void setPriority(String priority) {
        this.priority = priority;
    }
  
    @PropertyName("duration")
    public int getDuration() {
        return duration;
    }

    @PropertyName("duration")
    public void setDuration(int duration) {
        this.duration = duration;
    }

    @PropertyName("score")
    public float getScore() {
        return score;
    }

    @PropertyName("score")
    public void setScore(float score) {
        this.score = score;
    }

    public int getBorderTypeByScore() {
        if (score >= 5) {
            return R.drawable.task_border_high;
        } else if (score >= 1) {
            return R.drawable.task_border_medium;
        } else {
            return R.drawable.task_border_low;
        }
    }
}

