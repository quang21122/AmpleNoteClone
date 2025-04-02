package com.example.amplenoteclone.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.adapters.NotesAdapterForCreateTask;
import com.example.amplenoteclone.models.Note;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SelectNoteForTaskBottomSheet extends BottomSheetDialogFragment {

    private RecyclerView recyclerViewNotes;
    private LinearLayout layoutStartNewNote;
    private NotesAdapterForCreateTask notesAdapter;
    private List<Note> noteList;
    private OnNoteSelectionListener onNoteSelectionListener;

    public interface OnNoteSelectionListener {
        void onNoteSelected(Note note); // Khi chọn một note có sẵn
        void onStartNewNoteSelected();  // Khi chọn "Start a new note"
    }
    public SelectNoteForTaskBottomSheet(OnNoteSelectionListener listener) {
        this.onNoteSelectionListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_task_select_note, container, false);

        // Khởi tạo các view
        recyclerViewNotes = view.findViewById(R.id.recycler_view_notes);
        layoutStartNewNote = view.findViewById(R.id.layout_start_new_note);

        // Cấu hình RecyclerView
        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapterForCreateTask(noteList, this::onNoteSelected);
        recyclerViewNotes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewNotes.setAdapter(notesAdapter);

        // Load danh sách note từ Firestore
        loadNotes();

        // Xử lý sự kiện nhấn "Start a new note"
        layoutStartNewNote.setOnClickListener(v -> {
            if (onNoteSelectionListener != null) {
                onNoteSelectionListener.onStartNewNoteSelected(); // Thông báo rằng người dùng chọn "Start a new note"
            }
            dismiss();
        });

        // Áp dụng hiệu ứng trồi lên
        Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        view.startAnimation(slideUp);

        return view;
    }

    private void loadNotes() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    noteList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Note note = new Note();
                        note.setId(document.getId());
                        note.setTitle(document.getString("title"));
                        Timestamp createdAt = document.getTimestamp("createdAt");
                        note.setCreatedAt(createdAt != null ? createdAt.toDate() : new Date());
                        noteList.add(note);
                    }
                    notesAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading notes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void onNoteSelected(Note note) {
        if (onNoteSelectionListener != null) {
            onNoteSelectionListener.onNoteSelected(note); // Thông báo rằng người dùng chọn một note có sẵn
        }
        dismiss();
    }
}