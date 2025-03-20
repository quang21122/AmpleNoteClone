package com.example.amplenoteclone.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.adapters.TaskAdapter;
import com.example.amplenoteclone.models.Task;
import com.example.amplenoteclone.note.NotesActivity;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.calendar.CalendarActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.amplenoteclone.DrawerActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TasksPageActivity extends DrawerActivity {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent(R.layout.activity_tasks);

        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.recycler_view_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo danh sách tasks (có thể lấy từ API hoặc database)
        taskList = new ArrayList<>();

        // Thêm Task 1
        taskList.add(new Task(false, "Test Task", new Date(), "task1", "Doesn't repeat",
                "", "", "", "",
                "Important", "30 min", "", ""));

        // Thêm Task 2
        taskList.add(new Task(false, "Test Task", new Date(), "task2", "Doesn't repeat",
                "Monday, Mar 18", "Morning", "9:00 am", "5 min",
                "Important", "30 min", "Saturday, Mar 22", "9:00 pm"));


        taskAdapter = new TaskAdapter(taskList, new TaskAdapter.OnItemClickListener() {
            @Override
            public void onExpandClick(int position) {
                // Xử lý khi click expand (nếu cần)
            }
        });
        recyclerView.setAdapter(taskAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_tasks, menu);
        return true;
    }
    @Override
    public String getToolbarTitle() {
        return "Tasks";
    }
    @Override
    protected int getCurrentPageId() {
        return R.id.action_tasks;
    }

}