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
import com.example.amplenoteclone.ui.customviews.NoteCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;

public class NotesActivity extends DrawerActivity {
    public interface FirestoreCallback {
        void onCallback(ArrayList<NoteCardView> notes);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent(R.layout.activity_notes);

        RecyclerView recyclerView = findViewById(R.id.recyclerNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        NotesAdapter adapter = new NotesAdapter(); // Empty list initially
        recyclerView.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;
        getNotesFromFirebase(userId, notes -> runOnUiThread(() -> {
            adapter.setNotes(notes);
            adapter.notifyDataSetChanged();
        }));

        adapter.setOnNoteCardClickListener(noteCardView -> {
            Intent intent = new Intent(this, ViewNoteActivity.class);

            // Get the Note object from the NoteCardView
            Note note = noteCardView.getNote();

            // Pass data as an Extra (Note must implement Serializable or Parcelable)
            intent.putExtra("note", note);

            startActivity(intent);
        });

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

    private void getNotesFromFirebase(String userId, FirestoreCallback firestoreCallback) {
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
                            note.setTitle(document.getString("title"));
                            note.setContent(document.getString("content"));
                            note.setUserId(document.getString("userId"));
                            note.setProtected(document.getBoolean("isProtected"));

                            // Handle possible null timestamps
                            Timestamp createdAt = document.getTimestamp("createdAt");
                            Timestamp updatedAt = document.getTimestamp("updatedAt");
                            note.setCreatedAt(createdAt != null ? createdAt.toDate().toString() : null);
                            note.setUpdatedAt(updatedAt != null ? updatedAt.toDate().toString() : null);

                            // Get lists safely
                            note.setTags((ArrayList<String>) document.get("tag"));
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

                    firestoreCallback.onCallback(notes);
                });
    }
}
