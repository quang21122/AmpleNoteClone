package com.example.amplenoteclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.amplenoteclone.calendar.CalendarActivity;
import com.example.amplenoteclone.tasks.TasksActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NotesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setSelectedItemId(R.id.action_notes);

        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationOnClickListener(v -> {
            Toast.makeText(this, "Menu Clicked", Toast.LENGTH_SHORT).show();
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            // Do nothing, already on Notes page
            if (item.getItemId() == R.id.action_calendar) {
                Intent intent = new Intent(NotesActivity.this, CalendarActivity.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.action_task) {
                Intent intent = new Intent(NotesActivity.this, TasksActivity.class);
                startActivity(intent);
                return true;
            }else return item.getItemId() == R.id.action_notes;
        });

    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_notes, menu);
        return true;
    }
}
