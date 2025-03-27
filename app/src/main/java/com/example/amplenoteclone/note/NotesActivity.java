package com.example.amplenoteclone.note;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.DrawerActivity;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.adapters.NotesAdapter;
import com.example.amplenoteclone.models.Note;
import com.example.amplenoteclone.ocr.ScanImageToNoteActivity;
import com.example.amplenoteclone.ui.customviews.NoteCardView;
import com.example.amplenoteclone.utils.FirestoreListCallback;
import com.example.amplenoteclone.utils.PremiumChecker;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;

public class NotesActivity extends DrawerActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent(R.layout.activity_notes);

        RecyclerView recyclerView = findViewById(R.id.recyclerNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        NotesAdapter adapter = new NotesAdapter(); // Empty list initially
        recyclerView.setAdapter(adapter);

        getNotesFromFirebase(this.userId, notes -> runOnUiThread(() -> {
            adapter.setNotes(notes);
            adapter.notifyDataSetChanged();
        }));

        adapter.setOnNoteCardClickListener(noteCardView -> {
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
        NotesAdapter adapter = ((NotesAdapter) ((RecyclerView) findViewById(R.id.recyclerNotes)).getAdapter());

        getNotesFromFirebase(this.userId, notes -> runOnUiThread(() -> {
            adapter.setNotes(notes);
            adapter.notifyDataSetChanged();
        }));
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
                                NoteCardView noteCard = new NoteCardView(NotesActivity.this);
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
}
