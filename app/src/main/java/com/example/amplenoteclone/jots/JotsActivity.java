package com.example.amplenoteclone.jots;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.DrawerActivity;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.adapters.JotsAdapter;
import com.example.amplenoteclone.models.Note;
import com.example.amplenoteclone.note.ViewNoteActivity;
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

public class JotsActivity extends DrawerActivity {
    private RecyclerView jotsRecyclerView;
    private List<Note> jotsList = new ArrayList<>();
    private JotsAdapter jotsAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private boolean isJotsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent(R.layout.activity_jots);
        setTitle("Jots");

        // Initialize RecyclerView
        jotsRecyclerView = findViewById(R.id.jots_recycler_view);
        jotsAdapter = new JotsAdapter(jotsList, this::openJot);
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
    protected String getToolbarTitle() {
        return "Jots";
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
                                    new ArrayList<>(), // No attachments
                                    targetDate, // Created at this date
                                    targetDate, // Last modified at this date
                                    false // Not a favorite
                            );
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
            jotsList.add(placeholderJot);
        }

        jotsAdapter.notifyDataSetChanged();
    }

    // Method to open a jot when clicked
    private void openJot(Note jot) {
        if (jot.getId() == null) {
            // This is a placeholder jot that hasn't been saved yet
            // Create a new note in Firestore before opening it
            jot.setUserId(userId);

            // Show loading indicator or dialog
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Creating jot...");
            progressDialog.show();

            db.collection("notes")
                    .add(jot)
                    .addOnSuccessListener(documentReference -> {
                        String newId = documentReference.getId();
                        jot.setId(newId);

                        // Dismiss progress dialog
                        progressDialog.dismiss();

                        // Now open the note editor with the new ID
                        Intent intent = new Intent(this, ViewNoteActivity.class);
                        intent.putExtra("noteId", newId);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        // Dismiss progress dialog
                        progressDialog.dismiss();

                        Toast.makeText(this, "Error creating jot: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Open an existing jot
            Intent intent = new Intent(this, ViewNoteActivity.class);
            intent.putExtra("noteId", jot.getId());
            startActivity(intent);
        }
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
