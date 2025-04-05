// TasksPageActivity.java
package com.example.amplenoteclone.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.DrawerActivity;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.adapters.NotesAdapter;
import com.example.amplenoteclone.adapters.TaskAdapter;
import com.example.amplenoteclone.models.Note;
import com.example.amplenoteclone.models.Task;
import com.example.amplenoteclone.note.SearchBottomSheetFragment;
import com.example.amplenoteclone.ui.customviews.NoteCardView;
import com.example.amplenoteclone.ui.customviews.TaskCardView;
import com.example.amplenoteclone.utils.FirestoreListCallback;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TasksPageActivity extends DrawerActivity {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private NotesAdapter notesAdapter;
    private List<TaskCardView> taskCardList;
    private List<TaskCardView> allTaskCardList; // Lưu danh sách đầy đủ
    private ArrayList<NoteCardView> allNotes = new ArrayList<>();
    private FirebaseFirestore db;
    private ListenerRegistration taskListener;
    private TextView tasksFilter;
    private CheckBox filterCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent(R.layout.activity_tasks);

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recycler_view_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tasksFilter = findViewById(R.id.tasks_filter);
        filterCheckbox = findViewById(R.id.filter_checkbox);

        taskCardList = new ArrayList<>();
        allTaskCardList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskCardList);
        notesAdapter = new NotesAdapter(new ArrayList<>());
        recyclerView.setAdapter(taskAdapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;

        if (userId != null) {
            loadTasks(userId);
            getNotesFromFirebase(userId, notes -> runOnUiThread(() -> {
                allNotes = notes;
            }));
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Xử lý khi checkbox thay đổi trạng thái
        filterCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tasksFilter.setText(isChecked ? "All tasks" : "Available tasks");
            updateTaskList(isChecked);
        });
    }

    private void loadTasks(String userId) {
        CollectionReference collectionRef = db.collection("tasks");
        taskListener = collectionRef
                .whereEqualTo("userId", userId)
                .orderBy("createAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Error listening to tasks: ", error);
                        Toast.makeText(this, "Error loading tasks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    allTaskCardList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot document : value) {
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
                                TaskCardView taskCard = new TaskCardView(this);
                                taskCard.setTask(taskItem);
                                allTaskCardList.add(taskCard);
                            }
                        }
                        // Cập nhật danh sách hiển thị dựa trên trạng thái checkbox
                        updateTaskList(filterCheckbox.isChecked());
                    }
                });
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
                                NoteCardView noteCard = new NoteCardView(this);
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

    private void updateTaskList(boolean showAll) {
        taskCardList.clear();
        Date currentTime = Calendar.getInstance().getTime();

        if (showAll) {
            taskCardList.addAll(allTaskCardList);
        } else {
            for (TaskCardView taskCard : allTaskCardList) {
                Task task = taskCard.getTask();
                Date hideUntil = task.getHideUntil();
                boolean isAvailable = !task.isCompleted() && (hideUntil == null || hideUntil.before(currentTime));
                if (isAvailable) {
                    taskCardList.add(taskCard);
                }
            }
        }
        taskAdapter.setTasks(new ArrayList<>(taskCardList));
        taskAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (taskListener != null) {
            taskListener.remove();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_tasks, menu);

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
    public String getToolbarTitle() {
        return "Tasks";
    }

    @Override
    protected int getCurrentPageId() {
        return R.id.action_tasks;
    }
}