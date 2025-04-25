package com.example.amplenoteclone.jots;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.DrawerActivity;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.adapters.EditableJotsAdapter;
import com.example.amplenoteclone.models.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class JotsActivity extends DrawerActivity implements EditableJotsAdapter.OnJotContentChangedListener {
    private RecyclerView jotsRecyclerView;
    private List<Note> jotsList = new ArrayList<>();
    private EditableJotsAdapter jotsAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private static final long AUTOSAVE_DELAY = 2000; // 2 seconds
    private Map<String, Timer> saveTimers = new HashMap<>();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private boolean isJotsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent(R.layout.activity_jots);
        setTitle("Jots");

        // Initialize RecyclerView
        jotsRecyclerView = findViewById(R.id.jots_recycler_view);
        jotsAdapter = new EditableJotsAdapter(jotsList, this);
        jotsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Add item decoration for spacing between jots
        jotsRecyclerView.addItemDecoration(new JotsSpacingItemDecoration(16));
        jotsRecyclerView.setAdapter(jotsAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload jots only if they haven't been loaded yet
        if (!isJotsLoaded) {
            loadJots();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save any pending changes when leaving the activity
        for (Note jot : jotsList) {
            if (jot.getContent() != null && !jot.getContent().isEmpty()) {
                saveJotImmediately(jot);
            }
        }

        // Cancel all timers
        for (Timer timer : saveTimers.values()) {
            if (timer != null) {
                timer.cancel();
            }
        }
        saveTimers.clear();
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.jots_page);
    }

    @Override
    protected int getCurrentPageId() {
        return R.id.action_jots;
    }

    private void loadJots() {
        // Show loading indicator
        View progressBar = findViewById(R.id.progress_bar);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        jotsList.clear();

        // Query Firestore for existing jots with the daily-jots tag
        db.collection("notes")
                .whereEqualTo("userId", userId)
                .whereArrayContains("tags", "daily-jots")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    // Create a map to store existing jots by date
                    Map<String, Note> existingJotsByDate = new HashMap<>();
                    SimpleDateFormat dateKeyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    // Process existing jots from Firestore
                    for (DocumentSnapshot doc : querySnapshots) {
                        Note jot = doc.toObject(Note.class);
                        if (jot != null) {
                            jot.setId(doc.getId());
                            Date createdAt = jot.getCreatedAt();
                            if (createdAt != null) {
                                String dateKey = dateKeyFormat.format(createdAt);
                                existingJotsByDate.put(dateKey, jot);
                            }
                        }
                    }

                    // Create entries for today, yesterday, and the day before
                    for (int dayOffset = 0; dayOffset > -3; dayOffset--) {
                        // Calculate target date
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_YEAR, dayOffset);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        Date targetDate = calendar.getTime();

                        // Create display title (e.g., "April 12th, 2025")
                        String displayTitle = formatDayWithSuffix(targetDate);

                        // Check if we have an existing jot for this date
                        String dateKey = dateKeyFormat.format(targetDate);
                        if (existingJotsByDate.containsKey(dateKey)) {
                            // Use the existing jot from Firestore
                            Note existingJot = existingJotsByDate.get(dateKey);
                            existingJot.setTitle(displayTitle); // Ensure title format is consistent
                            jotsList.add(existingJot);
                        } else {
                            // Create a placeholder jot that's not yet saved to Firestore
                            Note placeholderJot = new Note(
                                    null, // No ID since it's not saved yet
                                    displayTitle,
                                    "", // Empty content
                                    new ArrayList<>(Collections.singletonList("daily-jots")),
                                    new ArrayList<>(), // No tasks
                                    targetDate, // Created at this date
                                    targetDate, // Last modified at this date
                                    false // Not protected
                            );
                            placeholderJot.setUserId(userId);
                            jotsList.add(placeholderJot);
                        }
                    }

                    // Hide loading indicator
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    // Notify the adapter that data has changed
                    jotsAdapter.notifyDataSetChanged();

                    // Set the flag to true to prevent reloading
                    isJotsLoaded = true;
                })
                .addOnFailureListener(e -> {
                    // Hide loading indicator
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    // Handle any errors
                    Toast.makeText(JotsActivity.this,
                            "Error loading jots: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();

                    // Still show placeholder jots even if Firestore query fails
                    createPlaceholderJots();
                });
    }

    // Helper method to create placeholder jots if Firestore query fails
    private void createPlaceholderJots() {
        jotsList.clear();

        for (int dayOffset = 0; dayOffset > -3; dayOffset--) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, dayOffset);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date targetDate = calendar.getTime();

            String displayTitle = formatDayWithSuffix(targetDate);

            Note placeholderJot = new Note(
                    null,
                    displayTitle,
                    "",
                    new ArrayList<>(Collections.singletonList("daily-jots")),
                    new ArrayList<>(),
                    targetDate,
                    targetDate,
                    false
            );
            placeholderJot.setUserId(userId);
            jotsList.add(placeholderJot);
        }

        jotsAdapter.notifyDataSetChanged();
    }

    // Implement the interface method for content changes
    @Override
    public void onContentChanged(Note jot, String newContent) {
        // Only trigger autosave if content has actually changed
        if (!newContent.equals(jot.getContent())) {
            jot.setContent(newContent);
            jot.setUpdatedAt(new Date());
            
            // Cancel any existing timer for this jot
            if (saveTimers.containsKey(jot.getTitle())) {
                saveTimers.get(jot.getTitle()).cancel();
            }
            
            // Schedule a new save
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mainThreadHandler.post(() -> saveJot(jot));
                }
            }, AUTOSAVE_DELAY);
            
            saveTimers.put(jot.getTitle(), timer);
        }
    }

    private void saveJot(Note jot) {
        if (jot.getContent() == null || jot.getContent().trim().isEmpty()) {
            // Don't save empty jots to Firestore
            return;
        }

        if (jot.getId() == null) {
            // This is a new jot that needs to be created
            saveNewJot(jot);
        } else {
            // This is an existing jot that needs to be updated
            updateExistingJot(jot);
        }
    }

    private void saveJotImmediately(Note jot) {
        if (jot.getContent() == null || jot.getContent().trim().isEmpty()) {
            // Don't save empty jots to Firestore
            return;
        }

        // Cancel any pending save timer
        if (saveTimers.containsKey(jot.getTitle())) {
            saveTimers.get(jot.getTitle()).cancel();
            saveTimers.remove(jot.getTitle());
        }

        // Save now
        saveJot(jot);
    }

    private void saveNewJot(Note jot) {
        jot.setUserId(userId); // Ensure userId is set

        // Create a new document in Firestore
        db.collection("notes")
                .add(createJotData(jot))
                .addOnSuccessListener(documentReference -> {
                    String newId = documentReference.getId();
                    jot.setId(newId);
                    Toast.makeText(JotsActivity.this, "Jot saved", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(JotsActivity.this, 
                            "Error saving jot: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateExistingJot(Note jot) {
        // Update the existing document in Firestore
        db.collection("notes")
                .document(jot.getId())
                .update(createJotData(jot))
                .addOnSuccessListener(aVoid -> {
                    // Jot updated successfully
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(JotsActivity.this, 
                            "Error updating jot: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }

    private Map<String, Object> createJotData(Note jot) {
        Map<String, Object> jotData = new HashMap<>();
        jotData.put("title", jot.getTitle());
        jotData.put("content", jot.getContent());
        jotData.put("createdAt", jot.getCreatedAt());
        jotData.put("updatedAt", jot.getUpdatedAt());
        jotData.put("userId", jot.getUserId());
        jotData.put("isProtected", jot.getIsProtected());

        // Ensure the "daily-jots" tag is always included
        List<String> tags = new ArrayList<>(jot.getTags());
        if (!tags.contains("daily-jots")) {
            tags.add("daily-jots");
        }
        jotData.put("tags", tags);

        jotData.put("tasks", jot.getTasks());
        return jotData;
    }

    // Helper method to format dates properly (handling suffix like 1st, 2nd, 3rd, etc.)
    private String formatDayWithSuffix(Date date) {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

        String month = monthFormat.format(date);
        int day = Integer.parseInt(dayFormat.format(date));
        String year = yearFormat.format(date);

        String suffix;
        if (day >= 11 && day <= 13) {
            suffix = "th";
        } else {
            switch (day % 10) {
                case 1:
                    suffix = "st";
                    break;
                case 2:
                    suffix = "nd";
                    break;
                case 3:
                    suffix = "rd";
                    break;
                default:
                    suffix = "th";
                    break;
            }
        }

        return month + " " + day + suffix + ", " + year;
    }
}
