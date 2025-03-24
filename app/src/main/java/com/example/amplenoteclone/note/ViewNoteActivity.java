package com.example.amplenoteclone.note;

import static com.example.amplenoteclone.utils.TimeConverter.formatLastUpdated;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amplenoteclone.DrawerActivity;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Note;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
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

    private Note currentNote;
    private Timer autoSaveTimer;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent(R.layout.activity_view_note);

        initializeViews();
        initializeNote();
        setupListeners();
        setupAutoSave();
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
        loadNote("note");
        if (currentNote == null) {
            createNewNote();
        }
        updateUI();
    }

    private void createNewNote() {
        currentNote = new Note();
        currentNote.setTitle("");
        currentNote.setContent("");
        currentNote.setUpdatedAt(Timestamp.now().toDate().getTime());
    }

    private void loadNote(String extra) {
        this.currentNote = (Note) getIntent().getSerializableExtra(extra);
    }

    private void updateUI() {
        titleEditText.setText(currentNote.getTitle());
        contentEditText.setText(currentNote.getContent());
        lastUpdatedTextView.setText(formatLastUpdated(currentNote.getUpdatedAt()));
    }

    private String formatTimeAgo(long timeDiff) {
        if (timeDiff < 60000) return "just now";
        if (timeDiff < 3600000) return (timeDiff / 60000) + " minutes ago";
        if (timeDiff < 86400000) return (timeDiff / 3600000) + " hours ago";
        return (timeDiff / 86400000) + " days ago";
    }

    private void setupListeners() {
        hiddenTabTextView.setOnClickListener(v -> {
            saveNote();
            updateUI();
        });

        completedTabTextView.setOnClickListener(v -> {
            saveNote();
            updateUI();
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
        currentNote.setUpdatedAt(Timestamp.now().toDate().getTime());
        saveNoteToFirebase();
        updateUI();
    }

    private void saveNoteToFirebase() {
        // Save note to Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("notes");

        // Create a map with the note data
        Map<String, Object> noteData = createUploadData();

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
        noteData.put("updatedAt", new Timestamp(new Date(currentNote.getUpdatedAt())));
        noteData.put("userId", userId);
        noteData.put("isProtected", currentNote.getProtected());
        noteData.put("tag", currentNote.getTags());
        noteData.put("tasks", currentNote.getTasks());
        noteData.put("createdAt", new Timestamp(new Date(currentNote.getCreatedAt())));

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

    @Override
    protected void onPause() {
        super.onPause();
        if (hasNoteChanged()) saveNote();
        cancelAutoSave();
    }

    private void cancelAutoSave() {
        if (autoSaveTimer != null) {
            autoSaveTimer.cancel();
            autoSaveTimer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelAutoSave();
    }

    public String getToolbarTitle() {
        return "View Note";
    }

    public int getCurrentPageId() {
        return R.id.action_notes;
    }
}