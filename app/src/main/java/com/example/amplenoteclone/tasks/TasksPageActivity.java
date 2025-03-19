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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TasksPageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        // Khởi tạo Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationOnClickListener(v -> {
            Toast.makeText(this, "Menu Clicked", Toast.LENGTH_SHORT).show();
        });

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

        // Khởi tạo BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setSelectedItemId(R.id.action_tasks);

        // Xử lý sự kiện cho BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_calendar) {
                Intent intent = new Intent(TasksPageActivity.this, CalendarActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.action_notes) {
                Intent intent = new Intent(TasksPageActivity.this, NotesActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.action_tasks) {
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_calendar, menu);
        return true;
    }
}