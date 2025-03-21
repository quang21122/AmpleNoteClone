package com.example.amplenoteclone.calendar;

import com.example.amplenoteclone.models.Task;

import java.util.Date;

public interface TaskView {
    void addTaskToTimeline(Task task);
    void loadTasksForDate(Date date);
}
