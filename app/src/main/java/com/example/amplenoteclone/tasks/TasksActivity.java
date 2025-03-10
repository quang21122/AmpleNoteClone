package com.example.amplenoteclone.tasks;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.amplenoteclone.NotesActivity;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.calendar.CalendarActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TasksActivity extends AppCompatActivity {

    // Khai báo các thành phần trong layout
    private ImageView expandButton;
    private LinearLayout expandableLayout;

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

        // Khởi tạo BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setSelectedItemId(R.id.action_task);

        // Xử lý sự kiện cho BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_calendar) {
                Intent intent = new Intent(TasksActivity.this, CalendarActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.action_notes) {
                Intent intent = new Intent(TasksActivity.this, NotesActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.action_task) {
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