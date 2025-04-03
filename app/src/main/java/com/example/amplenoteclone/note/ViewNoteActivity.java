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
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
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
import com.example.amplenoteclone.utils.TagManager;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
    private TagManager tagManager;

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
                this,
                tagsList,
                this::showTagMenu
        );

        tagsRecyclerView.setAdapter(tagsAdapter);
        tagManager = new TagManager(this, tagsAdapter, tagsList, currentNote);
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

                        // Cập nhật currentNote cho TagManager
                        tagManager = new TagManager(this, tagsAdapter, tagsList, currentNote);

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

    public Note getCurrentNote() {
        return currentNote;
    }

    private void loadTags() {
        tagsList.clear();
        List<String> tagIds = currentNote.getTags();
        if (tagIds == null || tagIds.isEmpty()) {
            tagsAdapter.setTags(tagsList);
            return;
        }

        // Lấy thông tin chi tiết của từng tag từ collection "tags"
        for (String tagId : tagIds) {
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
        }
    }

    private void showTagMenu(Tag tag) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_tag_menu, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Lấy chiều cao màn hình
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        // Điều chỉnh độ cao của BottomSheetDialog để chiếm toàn bộ màn hình
        ViewGroup.LayoutParams layoutParams = bottomSheetView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, screenHeight);
        } else {
            layoutParams.height = screenHeight;
        }
        bottomSheetView.setLayoutParams(layoutParams);

        // Cấu hình BottomSheetBehavior
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
        behavior.setPeekHeight(screenHeight); // Đặt peekHeight bằng chiều cao màn hình
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Mở rộng hoàn toàn
        behavior.setSkipCollapsed(true); // Bỏ qua trạng thái collapsed
        behavior.setFitToContents(false); // Không tự động điều chỉnh theo nội dung

        // Cập nhật tiêu đề (tên tag)
        TextView tagNameTitle = bottomSheetView.findViewById(R.id.tag_menu_name);
        tagNameTitle.setText(tag.getName());

        // Cập nhật icon (nếu tag là "daily-jots" thì đổi màu icon)
        ImageView tagIcon = bottomSheetView.findViewById(R.id.tag_menu_icon);
        if ("daily-jots".equalsIgnoreCase(tag.getName())) {
            tagIcon.setColorFilter(ContextCompat.getColor(this, R.color.textBlue));
        } else {
            tagIcon.setColorFilter(ContextCompat.getColor(this, R.color.dark));
        }

        // Xử lý nút X để đóng dialog
        ImageView closeButton = bottomSheetView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        // Xử lý các tùy chọn trong menu bằng TagManager
        bottomSheetView.findViewById(R.id.option_remove_tag).setOnClickListener(v -> {
            tagManager.removeTagFromNote(tag);
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.option_edit_tag).setOnClickListener(v -> {
            tagManager.editTag(tag);
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.option_delete_tag).setOnClickListener(v -> {
            tagManager.deleteTag(tag);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    public void addNewTag(String tagName) {
        tagManager.addNewTag(tagName);
    }
}