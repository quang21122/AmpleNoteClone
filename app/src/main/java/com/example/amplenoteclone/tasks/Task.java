package com.example.amplenoteclone.tasks;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class Task {
    private boolean isChecked;
    private String title;
    private String date;
    private String createdTime;
    private String repeatOption;
    private String startDate;
    private String startPeriod;
    private String startTime;
    private String notificationTime;
    private Set<String> priority;
    private String duration;
    private String hideDate;
    private String hideTime;

    // Constructor
    public Task(boolean isChecked, String title, String date, String createdTime, String repeatOption,
                String startDate, String startPeriod, String startTime, String notificationTime,
                String priority, String duration, String hideDate, String hideTime) {
        this.isChecked = isChecked;
        this.title = title;
        this.date = date;
        this.createdTime = createdTime;
        this.repeatOption = repeatOption;
        this.startDate = startDate;
        this.startPeriod = startPeriod;
        this.startTime = startTime;
        this.notificationTime = notificationTime;
        this.priority = new HashSet<>(); // Khởi tạo Set rỗng
        if (priority != null && !priority.isEmpty()) {
            this.priority.add(priority); // Chuyển giá trị cũ (nếu có) vào Set
        }
        this.duration = duration;
        this.hideDate = hideDate;
        this.hideTime = hideTime;
    }

    // Getters and Setters
    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getRepeatOption() {
        return repeatOption;
    }

    public void setRepeatOption(String repeatOption) {
        this.repeatOption = repeatOption;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartPeriod() {
        return startPeriod;
    }

    public void setStartPeriod(String startPeriod) {
        this.startPeriod = startPeriod;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(String notificationTime) {
        this.notificationTime = notificationTime;
    }

    public Set<String> getPriority() {
        return priority;
    }
    public void setPriority(Set<String> priority) {
        this.priority = priority != null ? priority : new HashSet<>();
    }
    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getHideDate() {
        return hideDate;
    }
    public void setHideDate(String hideDate) {
        this.hideDate = hideDate;
    }

    public String getHideTime() {
        return hideTime;
    }

    public void setHideTime(String hideTime) {
        this.hideTime = hideTime;
    }
}