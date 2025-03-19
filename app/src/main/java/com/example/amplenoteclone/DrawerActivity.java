package com.example.amplenoteclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.amplenoteclone.calendar.CalendarActivity;
import com.example.amplenoteclone.note.NotesActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public abstract class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_base);

        // Setup Drawer Layout
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup ActionBarDrawerToggle with special handling for icon clicks
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open_drawer,
                R.string.close_drawer
        ) {
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }

            @Override
            public boolean onOptionsItemSelected(MenuItem item) {
                if (item.getItemId() == android.R.id.home) {
                    // Handle home/up button - this is the hamburger icon
                    if (drawerLayout.getDrawerLockMode(androidx.core.view.GravityCompat.START)
                            == DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        drawerLayout.openDrawer(androidx.core.view.GravityCompat.START);
                        return true;
                    }
                }
                return super.onOptionsItemSelected(item);
            }
        };

        // Configure the toggle properly
        toggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(toggle);

        // Initialize the drawer as locked
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        toggle.syncState();

        // Set a click listener on the toolbar's navigation icon
        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout.getDrawerLockMode(androidx.core.view.GravityCompat.START)
                    == DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
                // If drawer is locked, unlock it and open
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                drawerLayout.openDrawer(androidx.core.view.GravityCompat.START);
            } else if (!drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
                // If drawer is unlocked but closed, open it
                drawerLayout.openDrawer(androidx.core.view.GravityCompat.START);
            } else {
                // If drawer is open, close it
                drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START);
            }
        });

        setupToolbar(); // Keep title setup

        // Setup Bottom Navigation
        setupBottomNavigation();
    }

    /**
     * Handles the Navigation Drawer item selection
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // If the selected item is already the current page, just close the drawer
        if (id == getCurrentPageId()) {
            drawerLayout.closeDrawers();
            return true;
        }

        if (id == R.id.drawer_calendar) {
            startActivity(new Intent(this, CalendarActivity.class));
        } else if (id == R.id.drawer_notes) {
            startActivity(new Intent(this, NotesActivity.class));
        } else if (id == R.id.drawer_tasks) {
            startActivity(new Intent(this, TasksActivity.class));
        }

        drawerLayout.closeDrawers(); // Close drawer only when selecting a new item
        return true;
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(getCurrentPageId());
        bottomNavigationView.setOnItemSelectedListener(this::onBottomNavItemSelected);
    }

    /**
     * Handles the Bottom Navigation item selection
     */
    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // If the selected item is already the current page, do nothing
        if (id == getCurrentPageId()) {
            return false;
        }

        if (id == R.id.action_calendar) {
            startActivity(new Intent(this, CalendarActivity.class));
        } else if (id == R.id.action_notes) {
            startActivity(new Intent(this, NotesActivity.class));
        } else if (id == R.id.action_tasks) {
            startActivity(new Intent(this, TasksActivity.class));
        }

        return true;
    }

    protected void setActivityContent(int layoutResID) {
        // Get the content frame that exists in the drawer layout
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        if (contentFrame != null) {
            // Clear existing views
            contentFrame.removeAllViews();
            // Inflate the child activity's layout into the content frame
            getLayoutInflater().inflate(layoutResID, contentFrame);
        }
    }

    protected void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getToolbarTitle());
        }
    }

    protected abstract String getToolbarTitle();

    protected abstract int getCurrentPageId();
}
