package com.example.amplenoteclone.tasks;

import com.example.amplenoteclone.models.Task;

public interface TaskHandler {
    void updateTaskInFirestore(Task task);
    void deleteTaskFromFirestore(Task task);
}