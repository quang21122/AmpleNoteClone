package com.example.amplenoteclone.note;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static com.example.amplenoteclone.utils.TimeConverter.formatLastUpdated;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
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
import com.example.amplenoteclone.ocr.ScanImageToNoteActivity;
import com.example.amplenoteclone.summary.GeminiSummary;
import com.example.amplenoteclone.tag.BottomSheetTagMenu;
import com.example.amplenoteclone.tasks.CreateTaskBottomSheet;
import com.example.amplenoteclone.ui.customviews.NoteCardView;
import com.example.amplenoteclone.ui.customviews.TaskCardView;
import com.example.amplenoteclone.utils.FirestoreListCallback;
import com.example.amplenoteclone.utils.PinManager;
import com.example.amplenoteclone.utils.PremiumChecker;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ViewNoteActivity extends DrawerActivity {
    private static final long AUTOSAVE_DELAY = 2000; // 2 seconds

    private EditText titleEditText;
    private TextView lastUpdatedTextView;
    private EditText contentEditText;
    private RecyclerView tagsRecyclerView;

    private Note currentNote;
    private Timer autoSaveTimer;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private TaskAdapter taskAdapter;
    private ArrayList<TaskCardView> taskCardList = new ArrayList<>();
    private List<Tag> tagsList;
    private TagsAdapter tagsAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration taskListener;
    private ListenerRegistration noteListener;
    private GeminiSummary geminiSummary;
    private PinManager pinManager;
    private boolean isContentDisplayed = false;

    public interface OnTaskCreationListener {
        void onTaskCreated();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent(R.layout.activity_view_note);

        Toolbar toolbar = findViewById(R.id.toolbar);
        // Set a custom navigation icon
        Drawable backIcon = ContextCompat.getDrawable(this, R.drawable.ic_back);
        if (backIcon != null) {
            backIcon.setTint(Color.WHITE);
            toolbar.setNavigationIcon(backIcon);
        }
        // Set a custom action on click
        toolbar.setNavigationOnClickListener(v -> {
            // Back to NotesActivity
            if (hasNoteChanged()) {
                saveNote();
            }
            finish();
        });

        pinManager = new PinManager(this);
        geminiSummary = new GeminiSummary(this);

        initializeViews();
        setupTaskSection();
        initializeNote();
        setupTag();
        setupNoteListener();
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
        searchItem.setOnMenuItemClickListener(item -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String userId = user.getUid();

                getNotesFromFirebase(userId, notes -> {
                    getTasksFromFirebase(userId, tasks -> {
                        SearchBottomSheetFragment searchFragment = new SearchBottomSheetFragment(notes, tasks);
                        searchFragment.show(getSupportFragmentManager(), "searchFragment");
                    });
                });
            }
            return true;
        });

        MenuItem deleteItem = menu.findItem(R.id.action_delete_note);
        deleteItem.setOnMenuItemClickListener(item -> {
            deleteNote();
            return true;
        });

        MenuItem summaryItem = menu.findItem(R.id.action_summary);
        summaryItem.setOnMenuItemClickListener(item -> {
            checkPremiumAndShowSummary();
            return true;
        });

        MenuItem lockItem = menu.findItem(R.id.action_lock);
        if (currentNote != null && currentNote.getIsProtected() != null && currentNote.getIsProtected()) {
            lockItem.setTitle("Unlock");
            lockItem.setIcon(R.drawable.ic_unlock);
        } else {
            lockItem.setTitle("Lock");
            lockItem.setIcon(R.drawable.ic_lock);
        }

        lockItem.setOnMenuItemClickListener(item -> {
            checkPremiumAndHandleLockAction(item);
            return true;
        });

        return true;
    }
    private void initializeViews() {
        titleEditText = findViewById(R.id.note_title);
        lastUpdatedTextView = findViewById(R.id.last_updated);
        contentEditText = findViewById(R.id.note_content);
        tagsRecyclerView = findViewById(R.id.tags_recycler_view);
    }

    private void initializeNote() {
        if (getIntent().hasExtra("noteId")) {
            loadNote();
        } else {
            createNewNote();
            displayNoteContent();
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
        currentNote.setIsProtected(false);
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

                                if (currentNote.getIsProtected() != null && currentNote.getIsProtected()) {
                                    PremiumChecker.checkPremium(this, userId, new PremiumChecker.PremiumCheckCallback() {
                                        @Override
                                        public void onIsPremium() {
                                            promptForPin(() -> displayNoteContent());
                                        }

                                        @Override
                                        public void onNotPremium() {
                                            Toast.makeText(ViewNoteActivity.this, "Locked notes are only accessible to Premium users", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });
                                } else {
                                    displayNoteContent();
                                }
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
        noteData.put("isProtected", currentNote.getIsProtected());
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

        Toast.makeText(this, "Deleting note", Toast.LENGTH_LONG).show();

        deleteAllTags(() -> {
            refreshTagsUI();
            deleteAllTasksFromFirebase(() -> {
                currentNote.deleteNoteFromFirebase(this::finish, error -> {
                    Toast.makeText(this, "Note delete error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                });
            }, error -> {
                Toast.makeText(this, "Task delete error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            });
        }, error -> {
            Toast.makeText(this, "Tag delete error: " + error, Toast.LENGTH_LONG).show();
        });
    }
    private void deleteAllTasksFromFirebase(Runnable onComplete, Consumer<Exception> onError) {
        int total = (int) taskCardList.stream().map(TaskCardView::getTask).filter(task -> task != null && task.getId() != null).count();

        if (total == 0) {
            onComplete.run();
            return;
        }

        final int[] completed = {0};
        final boolean[] failed = {false};

        for (TaskCardView taskCard : taskCardList) {
            Task task = taskCard.getTask();
            if (task != null && task.getId() != null) {
                task.deleteFromFirestore(
                        () -> {
                            if (failed[0]) return;
                            completed[0]++;
                            if (completed[0] == total) {
                                onComplete.run();
                            }
                        },
                        error -> {
                            if (!failed[0]) {
                                failed[0] = true;
                                onError.accept(error);
                            }
                        }
                );
            }
        }
    }


    private void deleteAllTags(Runnable onComplete, Consumer<String> onError) {
        List<Tag> validTags = tagsList.stream()
                .filter(tag -> tag != null && tag.getId() != null)
                .collect(Collectors.toList());

        if (validTags.isEmpty()) {
            onComplete.run();
            return;
        }

        deleteTagSequentially(validTags, 0, onComplete, onError);
    }

    private void deleteTagSequentially(List<Tag> tags, int index, Runnable onComplete, Consumer<String> onError) {
        if (index >= tags.size()) {
            onComplete.run();
            return;
        }

        Tag tag = tags.get(index);
        tag.removeTagFromNoteInFirestore(
                currentNote.getId(),
                () -> deleteTagSequentially(tags, index + 1, onComplete, onError),
                error -> {
                    Log.e("DeleteTag", "Failed to delete tag " + tag.getId() + ": " + error);
                    onError.accept(error);
                    deleteTagSequentially(tags, index + 1, onComplete, onError);
                }
        );
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
  
   public Note getCurrentNote() {
        return currentNote;
   }

    private void setupTaskSection() {
        // Recycler View
        RecyclerView recyclerView = findViewById(R.id.tasks_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter();
        recyclerView.setAdapter(taskAdapter);
        taskAdapter.setShowTaskTitleDetails(false);
        taskAdapter.setShowGoToNoteButton(false);
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

    private void loadTags() {
        tagsList.clear();
        List<String> tagIds = currentNote.getTags();
        if (tagIds == null || tagIds.isEmpty()) {
            tagsAdapter.setTags(tagsList);
            return;
        }

        final int totalTags = tagIds.size();
        final int[] completedQueries = {0};

        for (String tagId : tagIds) {
            db.collection("tags").document(tagId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Tag tag = documentSnapshot.toObject(Tag.class);
                            if (tag != null) {
                                tag.setId(tagId);
                                tagsList.add(tag);
                            }
                        }

                        completedQueries[0]++;
                        if (completedQueries[0] == totalTags) {
                            tagsAdapter.setTags(tagsList);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ViewNoteActivity", "Failed to load tag " + tagId + ": " + e.getMessage());
                        completedQueries[0]++;
                        if (completedQueries[0] == totalTags) {
                            tagsAdapter.setTags(tagsList);
                        }
                    });
        }
    }

    private void setupTagFlexBoxLayout() {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        tagsRecyclerView.setLayoutManager(layoutManager);
    }

    private void setupTag() {
        tagsList = new ArrayList<>();
        setupTagFlexBoxLayout();

        // Khởi tạo adapter
        tagsAdapter = new TagsAdapter(
                this,
                tagsList,
                tag -> {
                    BottomSheetTagMenu bottomSheetTagMenu = BottomSheetTagMenu.newInstance(tag);
                    bottomSheetTagMenu.setOnTagActionListener(new BottomSheetTagMenu.OnTagActionListener() {
                        @Override
                        public void onTagRemoved(Tag tag) {
                            tagsList.remove(tag);
                            tagsAdapter.setTags(tagsList);
                        }

                        @Override
                        public void onTagEdited(Tag tag) {
                            // Cập nhật tag trong tagsList
                            for (int i = 0; i < tagsList.size(); i++) {
                                Tag t = tagsList.get(i);
                                if (t.getId().equals(tag.getId())) {
                                    tagsList.set(i, tag);
                                    break;
                                }
                            }

                            tagsAdapter.setTags(tagsList);
                        }

                        @Override
                        public void onTagDeleted(Tag tag) {
                            tagsList.remove(tag);
                            tagsAdapter.setTags(tagsList);
                        }
                    });
                    bottomSheetTagMenu.show(getSupportFragmentManager(), "BottomSheetTagMenu");
                }
        );

        tagsRecyclerView.setAdapter(tagsAdapter);
    }
  
    @Override
    protected boolean useDrawerToggle() {
        return false;
    }

    private void setupNoteListener() {
        if (getIntent().hasExtra("noteId")) {
            String noteId = getIntent().getStringExtra("noteId");
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            noteListener = db.collection("notes")
                    .document(noteId)
                    .addSnapshotListener((documentSnapshot, error) -> {
                        if (error != null) {
                            Log.e("ViewNoteActivity", "Error listening to note updates: ", error);
                            return;
                        }
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            if (currentNote != null) {
                                Date updatedAt = documentSnapshot.getTimestamp("updatedAt").toDate();
                                currentNote.setUpdatedAt(updatedAt);
                                runOnUiThread(this::updateLastUpdated);
                            } else {
                                System.out.println("currentNote is null, skipping update in listener");
                            }
                        }
                    });
        }
    }

    private void getNotesFromFirebase(String userId, FirestoreListCallback<NoteCardView> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    ArrayList<NoteCardView> notes = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            try {
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
                                    NoteCardView noteCard = new NoteCardView(this);
                                    noteCard.setNote(note);
                                    notes.add(noteCard);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    callback.onCallback(notes);
                });
    }

    private void getTasksFromFirebase(String userId, FirestoreListCallback<TaskCardView> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    ArrayList<TaskCardView> tasks = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            try {
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
                                TaskCardView taskCard = new TaskCardView(this);
                                taskCard.setTask(taskItem);
                                tasks.add(taskCard);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    callback.onCallback(tasks);
                });
    }

    private void showSummaryDialog() {
        String content = contentEditText.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "No content to summarize", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi Gemini API để tóm tắt
        geminiSummary.generateSummary(
                content,
                summary -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Summary")
                            .setMessage(summary)
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();
                },
                error -> {
                    Toast.makeText(this, "Error generating summary: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        );
    }
    private void checkPremiumAndShowSummary() {
        if (userId == null) return;

        PremiumChecker.checkPremium(this, userId, new PremiumChecker.PremiumCheckCallback() {
            @Override
            public void onIsPremium() {
                showSummaryDialog();
            }

            @Override
            public void onNotPremium() {

            }
        });
    }

    private void displayNoteContent() {
        if (currentNote != null) {
            titleEditText.setText(currentNote.getTitle());
            contentEditText.setText(currentNote.getContent());

            updateLastUpdated();
            loadTags();
            getTasks();
            invalidateOptionsMenu();

            if (!isContentDisplayed) {
                setupAutoSave();
                isContentDisplayed = true;
            }
        } else {
            Log.e(TAG, "currentNote is null in displayNoteContent");
        }
    }

    private void promptForSetPin(Consumer<String> onPinSet) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set PIN");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String pin = input.getText().toString().trim();
            if (pin.length() >= 4) {
                onPinSet.accept(pin);
            } else {
                Toast.makeText(this, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show();
                promptForSetPin(onPinSet);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void promptForPin(Runnable onSuccess) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter PIN");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String enteredPin = input.getText().toString().trim();
            String storedPin = pinManager.getPin(currentNote.getId());
            if (enteredPin.equals(storedPin)) {
                onSuccess.run();
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                promptForPin(onSuccess);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void handleNoteUnlock(MenuItem lockItem) {
        promptForPin(() -> {
            currentNote.setIsProtected(false);
            pinManager.removePin(currentNote.getId());
            saveNoteToFirebase();

            lockItem.setTitle("Lock");
            lockItem.setIcon(R.drawable.ic_lock);
            Toast.makeText(ViewNoteActivity.this, "Note unlocked", Toast.LENGTH_SHORT).show();

            displayNoteContent();
        });
    }

    private void handleNoteLock(MenuItem lockItem) {
        promptForSetPin(pin -> {
            currentNote.setIsProtected(true);
            pinManager.setPin(currentNote.getId(), pin);
            saveNoteToFirebase();

            lockItem.setTitle("Unlock");
            lockItem.setIcon(R.drawable.ic_unlock);
            Toast.makeText(ViewNoteActivity.this, "Note locked", Toast.LENGTH_SHORT).show();
        });
    }

    private void checkPremiumAndHandleLockAction(MenuItem lockItem) {
        PremiumChecker.checkPremium(this, userId, new PremiumChecker.PremiumCheckCallback() {
            @Override
            public void onIsPremium() {
                if (currentNote.getIsProtected()) {
                    handleNoteUnlock(lockItem);
                } else {
                    handleNoteLock(lockItem);
                }
            }

            @Override
            public void onNotPremium() {
                Toast.makeText(ViewNoteActivity.this,
                        "Lock feature is only available for Premium users",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
