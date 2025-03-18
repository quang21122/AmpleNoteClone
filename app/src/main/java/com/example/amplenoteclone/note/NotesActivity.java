package com.example.amplenoteclone.note;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.DrawerActivity;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.adapters.NotesAdapter;
import com.example.amplenoteclone.models.Note;
import com.example.amplenoteclone.ui.customviews.NoteCardView;

import java.util.ArrayList;
import java.util.List;

public class NotesActivity extends DrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Must have methods for initializing the activity
        super.onCreate(savedInstanceState);
        setActivityContent(R.layout.activity_notes);

        // Add some notes to the adapter
        Note note = new Note();
        note.setTitle("Note Title");
        note.setContent("Note Content");
        note.getTags().add("Tag 1");
        note.getTags().add("Tag 2");
        note.getTags().add("Tag 3");
        note.setCreatedAt("2023-09-01");
        note.setUpdatedAt("2023-09-02");
        note.setUserId(1);
        note.setProtected(false);
        NoteCardView noteCard = new NoteCardView(this);
        noteCard.setNote(note);

        List<NoteCardView> notes = new ArrayList<NoteCardView>();
        notes.add(noteCard);

        // RecyclerView for displaying notes
        RecyclerView recyclerView = findViewById(R.id.recyclerNotes);
        recyclerView.setHasFixedSize(true);

        // Adapter for the RecyclerView
        NotesAdapter adapter = new NotesAdapter(notes);
        recyclerView.setAdapter(adapter);

        // Layout manager for the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set click listener for the notes
        adapter.setOnNoteCardClickListener(noteCardView -> {
            Intent intent = new Intent(this, ViewNoteActivity.class);
            startActivity(intent);
        });

        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_notes, menu);
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
}
