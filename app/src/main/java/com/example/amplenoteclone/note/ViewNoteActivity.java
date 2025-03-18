package com.example.amplenoteclone.note;

import android.os.Bundle;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amplenoteclone.DrawerActivity;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Note;

import java.util.Timer;
import java.util.TimerTask;

public class ViewNoteActivity extends DrawerActivity {
    public static final String EXTRA_NOTE_ID = "note_id";

    private EditText titleEditText;
    private TextView lastUpdatedText;
    private TextView addTagText;
    private EditText contentEditText;
    private TextView tabHidden;
    private TextView tabCompleted;
    private TextView tabBacklinks;
    private TextView noCompletedTasksText;

    private Note currentNote;
    private Timer autoSaveTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent(R.layout.activity_view_note);

        titleEditText = findViewById(R.id.note_title);
        lastUpdatedText = findViewById(R.id.last_updated);
        addTagText = findViewById(R.id.add_tag);
        contentEditText = findViewById(R.id.note_content);
        tabHidden = findViewById(R.id.tab_hidden);
        tabCompleted = findViewById(R.id.tab_completed);
        tabBacklinks = findViewById(R.id.tab_backlinks);
        noCompletedTasksText = findViewById(R.id.no_completed_tasks);

        long noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, -1);
        if (noteId != -1) {
            loadNote(noteId);
        } else {
            currentNote = new Note();
            currentNote.setTitle("");
            currentNote.setContent("");
            currentNote.setUpdatedAt(String.valueOf(System.currentTimeMillis()));
//            updateUI();
        }

        setupListeners();
        setupAutoSave();
    }

    private void loadNote(long noteId) {
        currentNote = new Note();
        currentNote.setId(noteId);
        currentNote.setTitle("Try faking it");
        currentNote.setContent("Feedback recordings into an acoustic situation");
        currentNote.setUpdatedAt(String.valueOf(System.currentTimeMillis() - 3600000));
//        updateUI();
    }

//    private void updateUI() {
//        titleEditText.setText(currentNote.getTitle());
//        contentEditText.setText(currentNote.getContent());
//
//        long timeDiff = System.currentTimeMillis() - currentNote.getUpdatedAt();
//        lastUpdatedText.setText("Updated " + formatTimeAgo(timeDiff));
//
//        tabHidden.setTextColor(getResources().getColor(currentNote.isHidden() ? android.R.color.white : android.R.color.darker_gray));
//        tabCompleted.setTextColor(getResources().getColor(currentNote.isCompleted() ? android.R.color.white : android.R.color.darker_gray));
//        noCompletedTasksText.setVisibility(currentNote.isCompleted() ? View.GONE : View.VISIBLE);
//    }

    private String formatTimeAgo(long timeDiff) {
        if (timeDiff < 60000) return "just now";
        if (timeDiff < 3600000) return (timeDiff / 60000) + " minutes ago";
        if (timeDiff < 86400000) return (timeDiff / 3600000) + " hours ago";
        return (timeDiff / 86400000) + " days ago";
    }

    private void setupListeners() {
//        tabHidden.setOnClickListener(v -> {
//            currentNote.setHidden(!currentNote.isHidden());
//            saveNote();
//            updateUI();
//        });

//        tabCompleted.setOnClickListener(v -> {
//            currentNote.setCompleted(!currentNote.isCompleted());
//            saveNote();
//            updateUI();
//        });

        tabBacklinks.setOnClickListener(v -> Toast.makeText(this, "Backlinks feature coming soon", Toast.LENGTH_SHORT).show());
        addTagText.setOnClickListener(v -> Toast.makeText(this, "Tag feature coming soon", Toast.LENGTH_SHORT).show());
    }

    private void setupAutoSave() {
        autoSaveTimer = new Timer();
        autoSaveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (hasNoteChanged()) {
                    runOnUiThread(() -> saveNote());
                }
            }
        }, 5000, 5000);
    }

    private boolean hasNoteChanged() {
        return currentNote != null &&
                (!titleEditText.getText().toString().equals(currentNote.getTitle()) ||
                        !contentEditText.getText().toString().equals(currentNote.getContent()));
    }

    private void saveNote() {
        currentNote.setTitle(titleEditText.getText().toString());
        currentNote.setContent(contentEditText.getText().toString());
        currentNote.setUpdatedAt(String.valueOf(System.currentTimeMillis()));
        Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
//        updateUI();
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
}