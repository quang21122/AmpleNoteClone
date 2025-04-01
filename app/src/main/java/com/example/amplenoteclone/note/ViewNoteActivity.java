package com.example.amplenoteclone.note;

import static com.example.amplenoteclone.utils.TimeConverter.formatLastUpdated;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.DrawerActivity;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.adapters.TaskAdapter;
import com.example.amplenoteclone.models.Note;
import com.example.amplenoteclone.models.Task;
import com.example.amplenoteclone.ui.customviews.TaskCardView;
import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class ViewNoteActivity extends DrawerActivity {
    private static final long AUTOSAVE_DELAY = 5000; // 5 seconds

    private EditText titleEditText;
    private TextView lastUpdatedTextView;
    private TextView addTagTextView;
    private EditText contentEditText;
    private TextView hiddenTabTextView;
    private TextView completedTabTextView;
    private TextView backlinksTabTextView;
    private TextView noCompletedTasksTextView;
    private FlexboxLayout tagsContainer;

    private Note currentNote;
    private Timer autoSaveTimer;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private TaskAdapter taskAdapter;
    private ArrayList<TaskCardView> taskCardList = new ArrayList<>();
    private List<String> tagsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent(R.layout.activity_view_note);

        initializeViews();
        initializeNote();
        setupListeners();
        setupAutoSave();

        tagsList = new ArrayList<>();
        // Ví dụ: Giả lập danh sách tag
        tagsList.add("daily-jots");
        tagsList.add("f");
        tagsList.add("fu");
        tagsList.add("gu");
        tagsList.add("ht");
        tagsList.add("vu");

        displayTags();
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
        tagsContainer = findViewById(R.id.tags_container);
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
        currentNote.setTitle("");
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
        collectionRef.document(noteId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentNote = new Note(
                                documentSnapshot.getId(),
                                documentSnapshot.getString("title"),
                                documentSnapshot.getString("content"),
                                (ArrayList<String>) documentSnapshot.get("tags"),
                                (ArrayList<String>) documentSnapshot.get("tasks"),
                                documentSnapshot.getTimestamp("createdAt").toDate(),
                                documentSnapshot.getTimestamp("updatedAt").toDate(),
                                documentSnapshot.getBoolean("isProtected")

                        );

                        updateLastUpdated();
                        titleEditText.setText(currentNote.getTitle());
                        contentEditText.setText(currentNote.getContent());

                        // Set up tasks section
                        setupTaskSection();
                    } else {
                        Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load note", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updateLastUpdated() {
        lastUpdatedTextView.setText(formatLastUpdated(currentNote.getUpdatedAt()));
    }

    private void setupListeners() {
        hiddenTabTextView.setOnClickListener(v -> {
            saveNote();
            updateLastUpdated();
        });

        completedTabTextView.setOnClickListener(v -> {
            saveNote();
            updateLastUpdated();
        });

        backlinksTabTextView.setOnClickListener(v -> Toast.makeText(this, "Backlinks feature coming soon", Toast.LENGTH_SHORT).show());
        addTagTextView.setOnClickListener(v -> Toast.makeText(this, "Tag feature coming soon", Toast.LENGTH_SHORT).show());
    }

    private void setupAutoSave() {
        autoSaveTimer = new Timer();
        autoSaveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (hasNoteChanged()) {
                    mainThreadHandler.post(() -> saveNote());
                }
            }
        }, AUTOSAVE_DELAY, AUTOSAVE_DELAY);
    }

    private boolean hasNoteChanged() {
        return currentNote != null &&
                (!Objects.equals(titleEditText.getText().toString(), currentNote.getTitle()) ||
                        !Objects.equals(contentEditText.getText().toString(), currentNote.getContent()));
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
            collectionRef.add(noteData)
                    .addOnSuccessListener(documentReference -> {
                        currentNote.setId(documentReference.getId());
                        Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
                    });
            return;
        }

        // Save the note to Firebase
        collectionRef.document(currentNote.getId())
                .set(noteData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_notes, menu);
        return true;
    }

    private void deleteNote() {
        Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void cancelAutoSave() {
        if (autoSaveTimer != null) {
            autoSaveTimer.cancel();
            autoSaveTimer = null;
        }
    }

    private void setupTaskSection() {
        // Check if the note is newly created
        if (currentNote.getId() == null || currentNote.getTasks() == null || currentNote.getTasks().isEmpty()) {
            return;
        }

        // Recycler View
        RecyclerView recyclerView = findViewById(R.id.tasks_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Task Adapter
        taskAdapter = new TaskAdapter();
        recyclerView.setAdapter(taskAdapter);
        taskAdapter.notifyDataSetChanged();

        // Get tasks
        getTasks();
    }

    private void getTasks() {
        ArrayList<Task> taskList = new ArrayList<>();
        Task.loadTasksInNote(currentNote.getId(), tasks -> runOnUiThread(() -> {
            // Add tasks to taskList
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

    public String getToolbarTitle() {
        return "View Note";
    }

    public int getCurrentPageId() {
        return R.id.action_notes;
    }

    private void displayTags() {
        // Xóa các tag cũ (trừ nút "Add a tag")
        tagsContainer.removeViews(0, tagsContainer.getChildCount() - 1);

        // Thêm các tag vào FlexboxLayout
        for (String tag : tagsList) {
            // Inflate layout tag_item.xml
            View tagView = LayoutInflater.from(this).inflate(R.layout.tag_item, tagsContainer, false);

            // Gán dữ liệu cho tag
            TextView tagName = tagView.findViewById(R.id.tag_name);
            tagName.setText(tag);

            // Thiết lập margin cho tag
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, (int) (8 * getResources().getDisplayMetrics().density), (int) (8 * getResources().getDisplayMetrics().density));
            tagView.setLayoutParams(params);

            // Xử lý sự kiện click vào icon ic_more (ví dụ: hiển thị menu)
            ImageView moreIcon = tagView.findViewById(R.id.more_icon);
            moreIcon.setOnClickListener(v -> {
                // Hiển thị menu hoặc dialog để chỉnh sửa/xóa tag
                Log.d("ViewNoteActivity", "Clicked more icon for tag: " + tag);
                // Ví dụ: Xóa tag (giả lập)
                tagsList.remove(tag);
                displayTags();
            });

            // Thêm tag vào FlexboxLayout (trước nút "Add a tag")
            tagsContainer.addView(tagView, tagsContainer.getChildCount() - 1);
        }
    }
}