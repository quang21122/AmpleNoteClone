package com.example.amplenoteclone.note;

import static com.example.amplenoteclone.utils.TimeConverter.formatLastUpdated;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.DrawerActivity;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.adapters.TaskAdapter;
import com.example.amplenoteclone.models.Note;
import com.example.amplenoteclone.models.Task;
import com.example.amplenoteclone.tasks.CreateTaskBottomSheet;
import com.example.amplenoteclone.ui.customviews.TaskCardView;
import com.example.amplenoteclone.utils.FirestoreListCallback;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class ViewNoteActivity extends DrawerActivity {
    private static final long AUTOSAVE_DELAY = 2000; // 2 seconds

    private EditText titleEditText;
    private TextView lastUpdatedTextView;
    private TextView addTagTextView;
    private EditText contentEditText;
    private TextView hiddenTabTextView;
    private TextView completedTabTextView;
    private TextView backlinksTabTextView;
    private TextView noCompletedTasksTextView;

    private Note currentNote;
    private Timer autoSaveTimer;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private TaskAdapter taskAdapter;
    private ArrayList<TaskCardView> taskCardList = new ArrayList<>();
    private ListenerRegistration taskListener;

    public interface OnTaskCreationListener {
        void onTaskCreated();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent(R.layout.activity_view_note);

        initializeViews();
        setupTaskSection();
        initializeNote();
        setupListeners();
        setupAutoSave();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (hasNoteChanged()) saveNote();
        cancelAutoSave();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelAutoSave();

        if (taskListener != null) {
            taskListener.remove();
            taskListener = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_view_note, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        MenuItem deleteItem = menu.findItem(R.id.action_delete_note);
        deleteItem.setOnMenuItemClickListener(item -> {
            deleteNote();
            return true;
        });

        return true;
    }
    private void initializeViews() {
        titleEditText = findViewById(R.id.note_title);
        lastUpdatedTextView = findViewById(R.id.last_updated);
        addTagTextView = findViewById(R.id.add_tag);
        contentEditText = findViewById(R.id.note_content);
        hiddenTabTextView = findViewById(R.id.tab_hidden);
        completedTabTextView = findViewById(R.id.tab_completed);
        backlinksTabTextView = findViewById(R.id.tab_backlinks);
        noCompletedTasksTextView = findViewById(R.id.no_completed_tasks);
    }

    private void initializeNote() {
        if (getIntent().hasExtra("noteId")) {
            loadNote();
        } else {
            createNewNote();
            updateLastUpdated();
        }
    }

    private void createNewNote() {
        currentNote = new Note();
        currentNote.setTitle("Untitled Note");
        titleEditText.setText(currentNote.getTitle());

        currentNote.setContent("");
        currentNote.setCreatedAt(Timestamp.now().toDate());
        currentNote.setUpdatedAt(Timestamp.now().toDate());
        currentNote.setUserId(userId);
        currentNote.setProtected(false);
        currentNote.setTags(new ArrayList<>());
        currentNote.setTasks(new ArrayList<>());
        updateLastUpdated();
    }

    private void loadNote() {
        final String noteId = getIntent().getStringExtra("noteId");

        // Load note from Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("notes");
        collectionRef
                .document(noteId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                currentNote =
                                        new Note(
                                                documentSnapshot.getId(),
                                                documentSnapshot.getString("title"),
                                                documentSnapshot.getString("content"),
                                                (ArrayList<String>) documentSnapshot.get("tags"),
                                                (ArrayList<String>) documentSnapshot.get("tasks"),
                                                documentSnapshot.getTimestamp("createdAt").toDate(),
                                                documentSnapshot.getTimestamp("updatedAt").toDate(),
                                                documentSnapshot.getBoolean("isProtected"));

                                updateLastUpdated();
                                titleEditText.setText(currentNote.getTitle());
                                contentEditText.setText(currentNote.getContent());

                                getTasks();
                            } else {
                                Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            Toast.makeText(this, "Failed to load note", Toast.LENGTH_SHORT).show();
                            finish();
                        });
    }

    private void updateLastUpdated() {
        lastUpdatedTextView.setText(formatLastUpdated(currentNote.getUpdatedAt()));
    }

    private void setupListeners() {
        hiddenTabTextView.setOnClickListener(
                v -> {
                    saveNote();
                    updateLastUpdated();
                });

        completedTabTextView.setOnClickListener(
                v -> {
                    saveNote();
                    updateLastUpdated();
                });

        backlinksTabTextView.setOnClickListener(
                v ->
                        Toast.makeText(this, "Backlinks feature coming soon", Toast.LENGTH_SHORT)
                                .show());
        addTagTextView.setOnClickListener(
                v -> Toast.makeText(this, "Tag feature coming soon", Toast.LENGTH_SHORT).show());
    }

    private void setupAutoSave() {
        autoSaveTimer = new Timer();
        autoSaveTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        if (hasNoteChanged()) {
                            mainThreadHandler.post(() -> saveNote());
                        }
                    }
                },
                AUTOSAVE_DELAY,
                AUTOSAVE_DELAY);
    }

    private boolean hasNoteChanged() {
        return currentNote != null
                && (!Objects.equals(titleEditText.getText().toString(), currentNote.getTitle())
                        || !Objects.equals(
                                contentEditText.getText().toString(), currentNote.getContent()));
    }

    private void saveNote() {
        currentNote.setTitle(titleEditText.getText().toString());
        currentNote.setContent(contentEditText.getText().toString());
        currentNote.setUpdatedAt(Timestamp.now().toDate());

        // Only save if the note has a title
        if (currentNote.getTitle().isEmpty()) {
            return;
        }

        saveNoteToFirebase();
        updateLastUpdated();
    }

    private void saveNoteToFirebase() {
        // Save note to Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("notes");

        // Create a map with the note data
        Map<String, Object> noteData = createUploadData();

        if (currentNote.getId() == null) {
            // Create a new note
            collectionRef
                    .add(noteData)
                    .addOnSuccessListener(
                            documentReference -> {
                                currentNote.setId(documentReference.getId());
                                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
                            })
                    .addOnFailureListener(
                            e -> {
                                Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT)
                                        .show();
                            });
            return;
        }

        // Save the note toFirebase
        collectionRef
                .document(currentNote.getId())
                .set(noteData)
                .addOnSuccessListener(
                        aVoid -> {
                            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
                        })
                .addOnFailureListener(
                        e -> {
                            Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
                        });
    }

    private Map<String, Object> createUploadData() {
        Map<String, Object> noteData = new HashMap<>();
        noteData.put("title", currentNote.getTitle());
        noteData.put("content", currentNote.getContent());
        noteData.put("updatedAt", new Timestamp(currentNote.getUpdatedAt()));
        noteData.put("userId", userId);
        noteData.put("isProtected", currentNote.getProtected());
        noteData.put("tags", currentNote.getTags());
        noteData.put("tasks", currentNote.getTasks());
        noteData.put("createdAt", new Timestamp(currentNote.getCreatedAt()));

        return noteData;
    }
    private void deleteNote() {
        if (currentNote.getId() == null) {
            Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show();
            return;
        }

        deleteAllTasksFromFirebase();
        currentNote.deleteNoteFromFirebase();
        finish();
    }

    private void deleteAllTasksFromFirebase() {
       for (TaskCardView taskCard : taskCardList) {
            Task task = taskCard.getTask();
            if (task != null && task.getId() != null) {
               task.deleteFromFirestore(
                        () -> {
                            // Task deleted successfully
                            Log.d("Task", "Task deleted successfully: " + task.getId());
                        },
                        error -> {
                            // Handle error
                            Log.e("Task", "Error deleting task: " + error.getMessage());
                        }
               );
            }
        }
    }

    private void cancelAutoSave() {
        if (autoSaveTimer != null) {
            autoSaveTimer.cancel();
            autoSaveTimer = null;
        }
    }

    private void setupTaskSection() {
        // Recycler View
        RecyclerView recyclerView = findViewById(R.id.tasks_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Task Adapter
        taskAdapter = new TaskAdapter();
        recyclerView.setAdapter(taskAdapter);
        taskAdapter.notifyDataSetChanged();
    }

    private void getTasks() {
        ArrayList<Task> taskList = new ArrayList<>();

        if (currentNote.getId() == null) {
            return;
        }

        loadTasksInNote(
                currentNote.getId(),
                tasks ->
                        runOnUiThread(
                                () -> {
                                    // Add tasks to taskList
                                    taskList.clear();
                                    taskList.addAll(tasks);

                                    // Clear taskCardList
                                    taskCardList.clear();

                                    // Add tasks to taskCardList
                                    for (Task task : taskList) {
                                        TaskCardView taskCard = new TaskCardView(this);
                                        taskCard.setTask(task);
                                        taskCardList.add(taskCard);
                                    }

                                    // Add tasks to adapter
                                    taskAdapter.setTasks(taskCardList);
                                }));
    }

    @Override
    protected boolean createNewTask() {
        CreateTaskBottomSheet bottomSheet = new CreateTaskBottomSheet();
        bottomSheet.setTaskCreationListener(
                new OnTaskCreationListener() {
                    @Override
                    public void onTaskCreated() {
                        refreshTaskSection();
                    }
                });
        bottomSheet.setSelectedNote(currentNote);
        bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        return true;
    }

    public void loadTasksInNote(String noteId, FirestoreListCallback<Task> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("tasks");

        if (taskListener != null) {
            taskListener.remove();
        }

        taskListener =
                collectionRef
                        .whereEqualTo("noteId", noteId)
                        .orderBy("createAt", Query.Direction.DESCENDING)
                        .addSnapshotListener(
                                (queryDocumentSnapshots, error) -> {
                                    if (error != null) {
                                        Log.e("Firestore", "Error listening to tasks: ", error);
                                        Toast.makeText(
                                                        this,
                                                        "Error loading tasks: "
                                                                + error.getMessage(),
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    } else {
                                        // **Clear existing tasks before adding new ones**
                                        ArrayList<Task> tasks = new ArrayList<>();

                                        for (QueryDocumentSnapshot document :
                                                queryDocumentSnapshots) {
                                            Task task =
                                                    new Task(
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
                                                            document.getLong("startNoti") != null
                                                                    ? document.getLong("startNoti")
                                                                            .intValue()
                                                                    : 0,
                                                            document.getDate("hideUntil"),
                                                            document.getString("hideUntilDate"),
                                                            document.getString("hideUntilTime"),
                                                            document.getString("priority"),
                                                            document.getLong("duration") != null
                                                                    ? document.getLong("duration")
                                                                            .intValue()
                                                                    : 0,
                                                            document.getDouble("score") != null
                                                                    ? document.getDouble("score")
                                                                            .floatValue()
                                                                    : 0.0f);
                                            task.setId(document.getId());
                                            if (task.getTitle() != null) {
                                                tasks.add(task);
                                            }
                                        }
                                        callback.onCallback(tasks);
                                    }
                                });
    }

    private void refreshTaskSection() {
        if (taskAdapter == null) {
            return;
        }

        getTasks();

        taskAdapter.notifyDataSetChanged();
    }

    public String getToolbarTitle() {
        return "View Note";
    }

    public int getCurrentPageId() {
        return R.id.action_notes;
    }
}
