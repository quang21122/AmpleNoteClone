package com.example.amplenoteclone.tasks;

public class Task {
    private String title;
    private String date;
    private String createdTime;
    private boolean isChecked;

    public Task(String title, String date, String createdTime, boolean isChecked) {
        this.title = title;
        this.date = date;
        this.createdTime = createdTime;
        this.isChecked = isChecked;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public boolean isChecked() {
        return isChecked;
    }
}
