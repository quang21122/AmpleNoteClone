package com.example.amplenoteclone.note;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.SearchView; // Correct import

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.DrawerActivity;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.adapters.NotesAdapter;
import com.example.amplenoteclone.models.Note;
import com.example.amplenoteclone.ocr.ScanImageToNoteActivity;
import com.example.amplenoteclone.ui.customviews.NoteCardView;
import com.example.amplenoteclone.utils.FirestoreListCallback;
import com.example.amplenoteclone.utils.PremiumChecker;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;

public class NotesActivity extends DrawerActivity {
    private NotesAdapter adapter;
    private ArrayList<NoteCardView> allNotes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent(R.layout.activity_notes);

        // Check for POST_NOTIFICATIONS permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1);
            }
        }

        // Check for SCHEDULE_EXACT_ALARM permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotesAdapter(); // Empty list initially
        recyclerView.setAdapter(adapter);

        getNotesFromFirebase(this.userId, notes -> runOnUiThread(() -> {
            allNotes = notes;
            adapter.setNotes(notes);
            adapter.notifyDataSetChanged();
        }));

        adapter.setOnNoteCardClickListener(noteCardView -> {
            Intent intent = new Intent(this, ViewNoteActivity.class);

            // Get the Note object from the NoteCardView
            Note note = noteCardView.getNote();

            // Pass the note ID to the next activity
            intent.putExtra("noteId", note.getId());

            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();

        // Refresh notes when returning to the activity
        getNotesFromFirebase(this.userId, notes -> runOnUiThread(() -> {
            allNotes = notes;
            adapter.setNotes(notes);
            adapter.notifyDataSetChanged();
        }));
    }

    private void checkPremiumAndOpenScan() {
        if (userId == null) return;

        PremiumChecker.checkPremium(this, userId, new PremiumChecker.PremiumCheckCallback() {
            @Override
            public void onIsPremium() {
                Intent intent = new Intent(NotesActivity.this, ScanImageToNoteActivity.class);
                startActivity(intent);
            }

            @Override
            public void onNotPremium() {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_notes, menu);

        // Add a listener to the add note button
        menu.findItem(R.id.action_add_note).setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(this, ViewNoteActivity.class);
            startActivity(intent);
            return true;
        });

        // Add listener for camera icon
        menu.findItem(R.id.action_ocr).setOnMenuItemClickListener(item -> {
            checkPremiumAndOpenScan();
            return true;
        });

        // Handle search action
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search notes");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterNotes(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterNotes(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    protected int getCurrentPageId() {
        return R.id.action_notes;
    }

    @Override
    protected String getToolbarTitle() {
        return "Notes";
    }

    private void getNotesFromFirebase(String userId, FirestoreListCallback<NoteCardView> firestoreListCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("notes");

        // Fetch only notes for the given userId
        collectionRef.whereEqualTo("userId", userId)
                .get(Source.SERVER) // Force fetch from server
                .addOnCompleteListener(task -> {
                    ArrayList<NoteCardView> notes = new ArrayList<>();

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Note note = new Note();

                            // Directly get values from document
                            note.setId(document.getId());
                            note.setTitle(document.getString("title"));
                            note.setContent(document.getString("content"));
                            note.setUserId(document.getString("userId"));
                            note.setProtected(document.getBoolean("isProtected"));

                            // Handle possible null timestamps
                            Timestamp createdAt = document.getTimestamp("createdAt");
                            Timestamp updatedAt = document.getTimestamp("updatedAt");
                            if (createdAt != null)
                                note.setCreatedAt(createdAt.toDate());

                            if (updatedAt != null)
                                note.setUpdatedAt(updatedAt.toDate());

                            // Get lists safely
                            note.setTags((ArrayList<String>) document.get("tags"));
                            note.setTasks((ArrayList<String>) document.get("tasks"));

                            // Only add note if title exists
                            if (note.getTitle() != null) {
                                NoteCardView noteCard = new NoteCardView(NotesActivity.this);
                                noteCard.setNote(note);
                                notes.add(noteCard);
                            }
                        }
                    } else {
                        Log.e("ERROR", "Firestore query failed: ", task.getException());
                    }

                    firestoreListCallback.onCallback(notes);
                });
    }

    private void filterNotes(String query) {
        ArrayList<NoteCardView> filteredNotes = new ArrayList<>();
        for (NoteCardView noteCard : allNotes) {
            if (noteCard.getNote().getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredNotes.add(noteCard);
            }
        }
        adapter.setNotes(filteredNotes);
        adapter.notifyDataSetChanged();
    }
}