package com.example.amplenoteclone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.amplenoteclone.calendar.CalendarActivity;
import com.example.amplenoteclone.jots.JotsActivity;
import com.example.amplenoteclone.models.Tag;
import com.example.amplenoteclone.models.User;
import com.example.amplenoteclone.note.NotesActivity;
import com.example.amplenoteclone.settings.SettingsActivity;
import com.example.amplenoteclone.tasks.CreateTaskBottomSheet;
import com.example.amplenoteclone.tasks.TasksPageActivity;
import com.example.amplenoteclone.utils.FirestoreCallback;
import com.example.amplenoteclone.utils.FirestoreListCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    BottomNavigationView bottomNavigationView;
    ImageView profileImage;
    TextView profileName;
    boolean isVietnamese = false;

    protected boolean isSubMenuExpanded = false;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    protected final String userId = user != null ? user.getUid() : null;

    protected ArrayList<Tag> currentTags = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        isVietnamese = preferences.getBoolean("isVietnamese", false);
        updateAppLocale(isVietnamese ? "vi" : "en");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_base);


        // Setup Drawer Layout
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize the drawer as locked
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        if (useDrawerToggle()) {
            // Setup ActionBarDrawerToggle with special handling for icon clicks
            ActionBarDrawerToggle toggle = getActionBarDrawerToggle(toolbar);
            drawerLayout.addDrawerListener(toggle);

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
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back); // your custom icon
            toolbar.setNavigationOnClickListener(v -> onCustomNavigationClick());
        }



        setupToolbar(); // Keep title setup

        // Setup Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();

        setupDrawerHeader();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserTags();
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

    @NonNull
    private ActionBarDrawerToggle getActionBarDrawerToggle(Toolbar toolbar) {
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
        return toggle;
    }

    public void onTagsChanged() {
        loadUserTags();
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
            startActivity(new Intent(this, TasksPageActivity.class));
        } else if (id == R.id.drawer_tags) {
            // This is just a header, do nothing
        } else if (id == R.id.drawer_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.drawer_logout) {
            User.signOut(this);
        }
        else if (id == R.id.drawer_jots){
            startActivity(new Intent(this, JotsActivity.class));
        } else {
            // Handle tags click
            for (Tag tag : currentTags) {
                if (item.getTitle().toString().startsWith(tag.getName())) {
                    Intent intent = new Intent(this, NotesActivity.class);
                    intent.putExtra("tagId", tag.getId());
                    intent.putExtra("tagName", tag.getName());
                    startActivity(intent);
                    drawerLayout.closeDrawers();
                    return true;
                }
            }
        }

        drawerLayout.closeDrawers(); // Close drawer only when selecting a new item
        return true;
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(getCurrentPageId());
        bottomNavigationView.setOnItemSelectedListener(this::onBottomNavItemSelected);
    }

    protected void setBottomNavigationVisibility(boolean isVisible) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Handles the Bottom Navigation item selection
     */
    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_new_task) {
            createNewTask();
        }

        // If the selected item is already the current page, do nothing
        if (id == getCurrentPageId()) {
            return false;
        }

        if (id == R.id.action_calendar) {
            startActivity(new Intent(this, CalendarActivity.class));
        } else if (id == R.id.action_notes) {
            startActivity(new Intent(this, NotesActivity.class));
        } else if (id == R.id.action_tasks) {
            startActivity(new Intent(this, TasksPageActivity.class));
        } else if (id == R.id.action_jots) {
            startActivity(new Intent(this, JotsActivity.class));
        }
        return true;
    }

    protected boolean createNewTask() {
        CreateTaskBottomSheet bottomSheet = new CreateTaskBottomSheet();
        bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        return true;
    }

    protected void setupDrawerHeader() {
        Menu menu = navigationView.getMenu();
        MenuItem header = menu.findItem(R.id.main_item);

        // Find header elements inside the navigation drawer
        View headerView = navigationView.getHeaderView(0);
        profileImage = headerView.findViewById(R.id.profile_image);
        profileName = headerView.findViewById(R.id.default_avatar_text);

        User.getCurrentUser(getUserFirestoreCallback(header, headerView));
    }

    @NonNull
    private FirestoreCallback<User> getUserFirestoreCallback(MenuItem header, View headerView) {
        return user -> {
            if (user == null) {
                Log.d("User", "No user found");
                return;
            }

            String title = getString(R.string.navigation);

            // Set User Name in Menu Item
            header.setTitle(title);

            SpannableString s = new SpannableString(header.getTitle());
            s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
            header.setTitle(s);

            // Load Profile Image
            User.loadUserProfile(DrawerActivity.this, null, profileImage, profileName);

            // Set User Name in Drawer Header
            TextView headerName = headerView.findViewById(R.id.user_name);
            headerName.setText(user.getName());
        };
    }

    protected void loadUserTags() {
        Menu menu = navigationView.getMenu();

        // Get the TAGS menu item
        MenuItem tagsItem = menu.findItem(R.id.drawer_tags);

        // Make sure it exists
        if (tagsItem == null) {
            Log.e("DrawerActivity", "Tags menu item not found!");
            return;
        }

        SubMenu tagsSubMenu = tagsItem.getSubMenu();

        // Clear existing tags
        if (tagsSubMenu != null) {
            tagsSubMenu.clear();

            // Change the color of the TAGS header like you did with NAVIGATION
            SpannableString s = new SpannableString(tagsItem.getTitle());
            s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
            tagsItem.setTitle(s);

            getUserTagsFromFirebase(userId, getTagFirestoreListCallback(tagsSubMenu));
        }
    }

    @NonNull
    private static FirestoreListCallback<Tag> getTagFirestoreListCallback(SubMenu tagsSubMenu) {
        return tags -> {
            for (Tag tag : tags) {
                // Create menu item with tag name
                MenuItem tagItem = tagsSubMenu.add(Menu.NONE,
                        View.generateViewId(), // Generate unique ID
                        Menu.NONE,
                        tag.getName());

                // Set tag icon
                tagItem.setIcon(R.drawable.ic_tag); // Use appropriate icon

                // If you want to show count like in the screenshot
                if (tag.getCount() > 0) {
                    SpannableString tagText = new SpannableString(
                            tag.getName() + "   " + tag.getCount());

                    // Style the count part differently
                    tagText.setSpan(
                            new ForegroundColorSpan(Color.GRAY),
                            tag.getName().length(),
                            tagText.length(),
                            0);

                    tagItem.setTitle(tagText);
                }

                // Make it checkable
                tagItem.setCheckable(true);
            }
        };
    }

    private void getUserTagsFromFirebase(String userId, FirestoreListCallback<Tag> firestoreListCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference tagsRef = db.collection("tags");

        currentTags.clear(); // Xóa danh sách cũ trước khi load mới

        tagsRef.whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            firestoreListCallback.onCallback(currentTags);
                            return;
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String tagId = document.getId();
                            Tag tag = new Tag(name, userId);
                            tag.setId(tagId);
                            currentTags.add(tag);
                        }

                        // Gọi callback lần đầu để hiển thị tag mà chưa có count
                        firestoreListCallback.onCallback(currentTags);

                        // Đếm số note cho tất cả tag
                        AtomicInteger pendingRequests = new AtomicInteger(currentTags.size());
                        for (Tag tag : currentTags) {
                            db.collection("notes")
                                    .whereEqualTo("userId", userId)
                                    .whereArrayContains("tags", tag.getId())
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        tag.setCount(querySnapshot.size());
                                        if (pendingRequests.decrementAndGet() == 0) {
                                            runOnUiThread(() -> {
                                                firestoreListCallback.onCallback(currentTags); // Cập nhật lại UI với count
                                                refreshTagsUI();
                                            });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("DrawerActivity", "Failed to count notes for tag: " + tag.getName(), e);
                                        if (pendingRequests.decrementAndGet() == 0) {
                                            runOnUiThread(() -> {
                                                firestoreListCallback.onCallback(currentTags);
                                                refreshTagsUI();
                                            });
                                        }
                                    });
                        }
                    } else {
                        Log.e("ERROR", "Firestore query failed: ", task.getException());
                        firestoreListCallback.onCallback(new ArrayList<>());
                    }
                });
    }

    protected void refreshTagsUI() {
        // Update the UI with the latest tag information
        // This is similar to what you do in loadUserTags but WITHOUT
        // triggering another Firebase query

        Menu menu = navigationView.getMenu();
        MenuItem tagsItem = menu.findItem(R.id.drawer_tags);
        if (tagsItem == null) return;

        SubMenu tagsSubMenu = tagsItem.getSubMenu();
        if (tagsSubMenu != null) {
            tagsSubMenu.clear();

            SpannableString s = new SpannableString(tagsItem.getTitle());
            s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
            tagsItem.setTitle(s);

            // You'll need to keep a reference to your tags somewhere
            // This could be a class-level variable
            for (Tag tag : currentTags) {
                MenuItem tagItem = tagsSubMenu.add(Menu.NONE,
                        View.generateViewId(),
                        Menu.NONE,
                        tag.getName());

                tagItem.setIcon(R.drawable.ic_tag);

                if (tag.getCount() > 0) {
                    SpannableString tagText = new SpannableString(
                            tag.getName() + "   " + tag.getCount());
                    tagText.setSpan(
                            new ForegroundColorSpan(Color.GRAY),
                            tag.getName().length(),
                            tagText.length(),
                            0);
                    tagItem.setTitle(tagText);
                }

                tagItem.setCheckable(true);
            }
        }
    }

    protected void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getToolbarTitle());
        }
    }

    protected abstract String getToolbarTitle();

    protected abstract int getCurrentPageId();

    protected boolean useDrawerToggle() {
        return true;
    }
    protected void onCustomNavigationClick() {
        finish();
    }

    private void updateAppLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}
