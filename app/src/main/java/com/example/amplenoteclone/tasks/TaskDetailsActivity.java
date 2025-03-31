package com.example.amplenoteclone.tasks;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.adapters.TaskAdapter;
import com.example.amplenoteclone.models.Task;
import com.example.amplenoteclone.ui.customviews.TaskCardView;

import java.util.ArrayList;
import java.util.List;

public class TaskDetailsActivity extends AppCompatActivity {

    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<TaskCardView> taskCardViewList;
    private boolean isEditable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        // Khởi tạo nút Back
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Khởi tạo RecyclerView
        taskRecyclerView = findViewById(R.id.taskRecyclerView);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo danh sách TaskCardView
        taskCardViewList = new ArrayList<>();

        // Lấy Task từ Intent
        Task task = (Task) getIntent().getSerializableExtra("task");
        isEditable = getIntent().getBooleanExtra("editable", true);

        if (task != null) {
            TaskCardView taskCardView = new TaskCardView(this);
            taskCardView.setTask(task);
            taskCardViewList.add(taskCardView);
            Log.d("TaskDetailsActivity", "Received task: " + task.getTitle());
        } else {
            Log.e("TaskDetailsActivity", "No task received");
            finish();
            return;
        }

        // Khởi tạo TaskAdapter
        taskAdapter = new TaskAdapter(taskCardViewList);
        taskRecyclerView.setAdapter(taskAdapter);
        taskAdapter.setExpandedPosition(0);
        taskAdapter.setShowDeleteButton(false);
        taskAdapter.setEditable(isEditable);
    }
}