package com.example.amplenoteclone.note;

import static com.example.amplenoteclone.utils.TimeConverter.formatLastUpdated;

import android.app.AlertDialog;
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
import com.example.amplenoteclone.adapters.TagsAdapter;
import com.example.amplenoteclone.adapters.TaskAdapter;
import com.example.amplenoteclone.models.Note;
import com.example.amplenoteclone.models.Tag;
import com.example.amplenoteclone.models.Task;
import com.example.amplenoteclone.ui.customviews.TaskCardView;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
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
    private EditText contentEditText;
    private TextView hiddenTabTextView;
    private TextView completedTabTextView;
    private TextView backlinksTabTextView;
    private TextView noCompletedTasksTextView;
    private FlexboxLayout tagsContainer;
    private RecyclerView tagsRecyclerView;

    private Note currentNote;
    private Timer autoSaveTimer;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private TaskAdapter taskAdapter;
    private ArrayList<TaskCardView> taskCardList = new ArrayList<>();
    private List<Tag> tagsList;
    private TagsAdapter tagsAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent(R.layout.activity_view_note);

        initializeViews();
        initializeNote();
        setupListeners();
        setupAutoSave();


        tagsList = new ArrayList<>();

        // Thiết lập RecyclerView với FlexboxLayoutManager
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        tagsRecyclerView.setLayoutManager(layoutManager);

        // Khởi tạo adapter
        tagsAdapter = new TagsAdapter(
                tagsList,
                () -> {
                    // Hiển thị dialog để nhập tên tag mới
                    showAddTagDialog();
                },
                tag -> {
                    // Xử lý sự kiện click vào icon ic_more (xóa tag)
                    removeTagFromNote(tag);
                });

        tagsRecyclerView.setAdapter(tagsAdapter);
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
        contentEditText = findViewById(R.id.note_content);
        hiddenTabTextView = findViewById(R.id.tab_hidden);
        completedTabTextView = findViewById(R.id.tab_completed);
        backlinksTabTextView = findViewById(R.id.tab_backlinks);
        noCompletedTasksTextView = findViewById(R.id.no_completed_tasks);
        tagsRecyclerView = findViewById(R.id.tags_recycler_view);
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

                        // Load tags
                        loadTags();

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

    public String getToolbarTitle() {
        return "View Note";
    }

    public int getCurrentPageId() {
        return R.id.action_notes;
    }

//
private void loadTags() {
    tagsList.clear();
    List<String> tagIds = currentNote.getTags();
    if (tagIds == null || tagIds.isEmpty()) {
        tagsAdapter.setTags(tagsList);
        return;
    }

    for (String tagId : tagIds) {
        // Kiểm tra tagId hợp lệ
        if (tagId == null || tagId.trim().isEmpty()) {
            continue;
        }

        try {
            db.collection("tags").document(tagId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Tag tag = documentSnapshot.toObject(Tag.class);
                            if (tag != null) {
                                tag.setId(tagId);
                                tagsList.add(tag);
                                tagsAdapter.setTags(tagsList);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ViewNoteActivity", "Failed to load tag " + tagId + ": " + e.getMessage());
                    });
        } catch (IllegalArgumentException e) {
            Log.e("ViewNoteActivity", "Invalid tag ID: " + tagId);
            // Có thể xóa tagId không hợp lệ khỏi note
            removeInvalidTagFromNote(tagId);
        }
    }
}

    private void removeInvalidTagFromNote(String invalidTagId) {
        List<String> updatedTagIds = new ArrayList<>(currentNote.getTags());
        updatedTagIds.remove(invalidTagId);
        currentNote.setTags((ArrayList<String>) updatedTagIds);

        db.collection("notes").document(currentNote.getId())
                .update("tags", updatedTagIds)
                .addOnSuccessListener(aVoid ->
                        Log.d("ViewNoteActivity", "Removed invalid tag ID from note")
                )
                .addOnFailureListener(e ->
                        Log.e("ViewNoteActivity", "Failed to remove invalid tag ID: " + e.getMessage())
                );
    }
    private void showAddTagDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add a Tag");

        // Tạo EditText để nhập tên tag
        final EditText input = new EditText(this);
        input.setHint("Enter tag name");
        builder.setView(input);

        // Nút OK
        builder.setPositiveButton("OK", (dialog, which) -> {
            String tagName = input.getText().toString().trim();
            if (!tagName.isEmpty()) {
                addNewTag(tagName);
            }
        });

        // Nút Cancel
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addNewTag(String tagName) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Kiểm tra xem tag đã tồn tại chưa
        db.collection("tags")
                .whereEqualTo("name", tagName)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Tag đã tồn tại, sử dụng tag hiện có
                        Tag existingTag = queryDocumentSnapshots.getDocuments().get(0).toObject(Tag.class);
                        existingTag.setId(queryDocumentSnapshots.getDocuments().get(0).getId());
                        addTagToNote(existingTag);
                    } else {
                        // Tạo tag mới
                        Tag newTag = new Tag(tagName, userId);
                        db.collection("tags")
                                .add(newTag)
                                .addOnSuccessListener(documentReference -> {
                                    String newTagId = documentReference.getId();
                                    newTag.setId(newTagId);
                                    addTagToNote(newTag);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ViewNoteActivity", "Failed to add new tag: " + e.getMessage());
                                    Toast.makeText(this, "Failed to add tag", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ViewNoteActivity", "Failed to check existing tag: " + e.getMessage());
                    Toast.makeText(this, "Failed to add tag", Toast.LENGTH_SHORT).show();
                });
    }

    private void addTagToNote(Tag tag) {
        List<String> updatedTagIds = new ArrayList<>(currentNote.getTags());
        if (!updatedTagIds.contains(tag.getId())) {
            updatedTagIds.add(tag.getId());
            currentNote.setTags((ArrayList<String>) updatedTagIds);

            // Cập nhật Note trên Firestore
            db.collection("notes").document(currentNote.getId())
                    .update("tags", updatedTagIds)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("ViewNoteActivity", "Tag added to note successfully");
                        tagsList.add(tag);
                        tagsAdapter.setTags(tagsList);
                        Toast.makeText(this, "Tag added", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ViewNoteActivity", "Failed to update note tags: " + e.getMessage());
                        Toast.makeText(this, "Failed to add tag", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void removeTagFromNote(Tag tag) {
        List<String> updatedTagIds = new ArrayList<>(currentNote.getTags());
        updatedTagIds.remove(tag.getId());
        currentNote.setTags((ArrayList<String>) updatedTagIds);

        // Cập nhật Note trên Firestore
        db.collection("notes").document(currentNote.getId())
                .update("tags", updatedTagIds)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ViewNoteActivity", "Tag removed from note successfully");
                    tagsList.remove(tag);
                    tagsAdapter.setTags(tagsList);
                    Toast.makeText(this, "Tag removed", Toast.LENGTH_SHORT).show();

                    // (Tùy chọn) Xóa Tag khỏi collection "tags" nếu không còn note nào sử dụng
                    checkAndDeleteTag(tag);
                })
                .addOnFailureListener(e -> {
                    Log.e("ViewNoteActivity", "Failed to remove tag from note: " + e.getMessage());
                    Toast.makeText(this, "Failed to remove tag", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkAndDeleteTag(Tag tag) {
        // Kiểm tra xem tag có còn được sử dụng bởi note nào khác không
        db.collection("notes")
                .whereArrayContains("tags", tag.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Không còn note nào sử dụng tag này, xóa tag
                        db.collection("tags").document(tag.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("ViewNoteActivity", "Tag deleted from Firestore: " + tag.getId());
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ViewNoteActivity", "Failed to delete tag: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ViewNoteActivity", "Failed to check tag usage: " + e.getMessage());
                });
    }

    private void setupTaskSection() {
        if (currentNote.getId() == null || currentNote.getTasks() == null || currentNote.getTasks().isEmpty()) {
            return;
        }

        RecyclerView recyclerView = findViewById(R.id.tasks_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter();
        recyclerView.setAdapter(taskAdapter);
        taskAdapter.notifyDataSetChanged();
        getTasks();
    }

    private void getTasks() {
        ArrayList<Task> taskList = new ArrayList<>();
        Task.loadTasksInNote(currentNote.getId(), tasks -> runOnUiThread(() -> {
            taskList.addAll(tasks);
            taskCardList.clear();
            for (Task task : taskList) {
                TaskCardView taskCard = new TaskCardView(this);
                taskCard.setTask(task);
                taskCardList.add(taskCard);
            }
            taskAdapter.setTasks(taskCardList);
        }));
    }
}