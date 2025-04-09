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
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.DrawerActivity;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.adapters.NotesAdapter;
import com.example.amplenoteclone.adapters.TaskAdapter;
import com.example.amplenoteclone.models.Note;
import com.example.amplenoteclone.models.Task;
import com.example.amplenoteclone.ocr.ScanImageToNoteActivity;
import com.example.amplenoteclone.ui.customviews.NoteCardView;
import com.example.amplenoteclone.ui.customviews.TaskCardView;
import com.example.amplenoteclone.utils.FirestoreListCallback;
import com.example.amplenoteclone.utils.PremiumChecker;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

public class NotesActivity extends DrawerActivity {
    private NotesAdapter notesAdapter;
    private TaskAdapter taskAdapter;
    private ArrayList<NoteCardView> allNotes = new ArrayList<>();
    private List<TaskCardView> allTaskCardList = new ArrayList<>(); // Lưu danh sách đầy đủ
    private String selectedTagId;
    private String selectedTagName;
    private ListenerRegistration notesListener;

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

        Intent tagIntent = getIntent();
        selectedTagId = tagIntent.getStringExtra("tagId");
        selectedTagName = tagIntent.getStringExtra("tagName");
        setupToolbar();

        RecyclerView recyclerView = findViewById(R.id.recyclerNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notesAdapter = new NotesAdapter(); // Empty list initially
        taskAdapter = new TaskAdapter(new ArrayList<>());
        recyclerView.setAdapter(notesAdapter);

        getNotesFromFirebase(this.userId, notes -> runOnUiThread(() -> {
            allNotes = notes;
            notesAdapter.setNotes(notes);
            notesAdapter.notifyDataSetChanged();
            updateNotesNumber();
        }));

        getTasksFromFirebase(this.userId, tasks -> runOnUiThread(() -> {
            allTaskCardList = tasks;
        }));

        notesAdapter.setOnNoteCardClickListener(noteCardView -> {
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
            notesAdapter.setNotes(notes);
            notesAdapter.notifyDataSetChanged();
            updateNotesNumber();
        }));

        // Refresh tasks when returning to the activity
        getTasksFromFirebase(this.userId, tasks -> runOnUiThread(() -> {
            allTaskCardList = tasks;
        }));
    }

    // Hủy listener khi activity bị hủy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notesListener != null) {
            notesListener.remove();
            notesListener = null;
        }
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
        searchItem.setOnMenuItemClickListener(item -> {
            List<NoteCardView> allNotesList = new ArrayList<>(allNotes); // Ensure allNotes is initialized
            List<TaskCardView> allTasks = new ArrayList<>(allTaskCardList); // Load all tasks here
            SearchBottomSheetFragment searchFragment = new SearchBottomSheetFragment(allNotesList, allTasks);
            searchFragment.show(getSupportFragmentManager(), "searchFragment");
            return true;
        });

        return true;
    }

    @Override
    protected int getCurrentPageId() {
        return R.id.action_notes;
    }

    @Override
    protected String getToolbarTitle() {
        return selectedTagName != null ? "#" + selectedTagName : "Notes";
    }

    private void getNotesFromFirebase(String userId, FirestoreListCallback<NoteCardView> firestoreListCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("notes");

        // Truy vấn note dựa trên userId và tagId (nếu có)
        Query query = collectionRef.whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING);
        if (selectedTagId != null) {
            query = query.whereArrayContains("tags", selectedTagId);
        }

        // Hủy listener cũ nếu tồn tại
        if (notesListener != null) {
            notesListener.remove();
        }

        // Thêm real-time listener
        notesListener = query.addSnapshotListener((querySnapshot, error) -> {
            if (error != null) {
                Log.e("NotesActivity", "Error listening to notes: ", error);
                return;
            }

            ArrayList<NoteCardView> notes = new ArrayList<>();
            if (querySnapshot != null) {
                for (QueryDocumentSnapshot document : querySnapshot) {
                    Note note = new Note();
                    note.setId(document.getId());
                    note.setTitle(document.getString("title"));
                    note.setContent(document.getString("content"));
                    note.setUserId(document.getString("userId"));
                    note.setIsProtected(document.getBoolean("isProtected"));

                    Timestamp createdAt = document.getTimestamp("createdAt");
                    Timestamp updatedAt = document.getTimestamp("updatedAt");
                    if (createdAt != null) note.setCreatedAt(createdAt.toDate());
                    if (updatedAt != null) note.setUpdatedAt(updatedAt.toDate());

                    note.setTags((ArrayList<String>) document.get("tags"));
                    note.setTasks((ArrayList<String>) document.get("tasks"));

                    if (note.getTitle() != null) {
                        NoteCardView noteCard = new NoteCardView(NotesActivity.this);
                        noteCard.setNote(note);
                        notes.add(noteCard);
                    }
                }
            }

            firestoreListCallback.onCallback(notes);
        });
    }



    private void getTasksFromFirebase(String userId, FirestoreListCallback<TaskCardView> firestoreListCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("tasks");

        // Fetch only tasks for the given userId
        collectionRef.whereEqualTo("userId", userId)
                .orderBy("createAt", Query.Direction.DESCENDING)
                .get(Source.SERVER) // Force fetch from server
                .addOnCompleteListener(task -> {
                    ArrayList<TaskCardView> tasks = new ArrayList<>();

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task taskItem = new Task(
                                    document.getString("userId"),
                                    document.getString("noteId"),
                                    document.getString("title"),
                                    document.getDate("createAt"),
                                    document.getBoolean("isCompleted"),
                                    document.getString("repeat"),
                                    document.getDate("startAt"),
                                    document.getString("startAtDate"),
                                    document.getString("startAtPeriod"),
                                    document.getString("startAtTime"),
                                    document.getLong("startNoti") != null ? document.getLong("startNoti").intValue() : 0,
                                    document.getDate("hideUntil"),
                                    document.getString("hideUntilDate"),
                                    document.getString("hideUntilTime"),
                                    document.getString("priority"),
                                    document.getLong("duration") != null ? document.getLong("duration").intValue() : 0,
                                    document.getDouble("score") != null ? document.getDouble("score").floatValue() : 0.0f
                            );
                            taskItem.setId(document.getId());
                            if (taskItem.getTitle() != null) {
                                TaskCardView taskCard = new TaskCardView(NotesActivity.this);
                                taskCard.setTask(taskItem);
                                tasks.add(taskCard);
                            }
                        }
                    } else {
                        Log.e("ERROR", "Firestore query failed: ", task.getException());
                    }

                    firestoreListCallback.onCallback(tasks);
                });
    }

    private void filterNotes(String query) {
        ArrayList<NoteCardView> filteredNotes = new ArrayList<>();
        for (NoteCardView noteCard : allNotes) {
            if (noteCard.getNote().getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredNotes.add(noteCard);
            }
        }
        notesAdapter.setNotes(filteredNotes);
        notesAdapter.notifyDataSetChanged();
    }

    private void filterTasks(String query) {
        ArrayList<TaskCardView> filteredTasks = new ArrayList<>();
        for (TaskCardView taskCard : allTaskCardList) {
            if (taskCard.getTask().getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredTasks.add(taskCard);
            }
        }
        taskAdapter.setTasks(filteredTasks);
        taskAdapter.notifyDataSetChanged();
    }

    private void updateNotesNumber() {
        // Update the notes number in the toolbar
        int NotesNumber = notesAdapter.getItemCount();

        TextView txtNotesCount = findViewById(R.id.txtNotesCount);

        if (NotesNumber == 1) {
            txtNotesCount.setText(R.string.notes_count_1);
        } else {
            txtNotesCount.setText(String.format(getString(R.string.notes_count), NotesNumber));
        }
    }
}