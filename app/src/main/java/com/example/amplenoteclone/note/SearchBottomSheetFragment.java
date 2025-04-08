package com.example.amplenoteclone.note;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.adapters.NotesAdapter;
import com.example.amplenoteclone.adapters.TaskAdapter;
import com.example.amplenoteclone.ui.customviews.NoteCardView;
import com.example.amplenoteclone.ui.customviews.TaskCardView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class SearchBottomSheetFragment extends BottomSheetDialogFragment {
    private NotesAdapter notesAdapter;
    private TaskAdapter taskAdapter;
    private List<NoteCardView> allNotes;
    private List<TaskCardView> allTasks;
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private TabLayout tabLayout;
    private TextView defaultTextView;

    public SearchBottomSheetFragment(List<NoteCardView> allNotes, List<TaskCardView> allTasks) {
        this.allNotes = allNotes;
        this.allTasks = allTasks;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.requestLayout();
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_bottom_sheet, container, false);

        recyclerView = view.findViewById(R.id.recyclerSearchNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notesAdapter = new NotesAdapter(new ArrayList<>());
        taskAdapter = new TaskAdapter(new ArrayList<>());
        recyclerView.setAdapter(notesAdapter);

        searchEditText = view.findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* No-op */ }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { /* No-op */ }
        });

        ImageButton closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dismiss());

        tabLayout = view.findViewById(R.id.searchTabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterItems(searchEditText.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        // Initialize adapters with click listeners
        notesAdapter = new NotesAdapter(new ArrayList<>());
        notesAdapter.setOnNoteCardClickListener(noteCard -> {
            Intent intent = new Intent(getActivity(), ViewNoteActivity.class);
            intent.putExtra("noteId", noteCard.getNote().getId());
            startActivity(intent);
            dismiss(); // Close search bottom sheet
        });
        
        taskAdapter = new TaskAdapter(new ArrayList<>());
        recyclerView.setAdapter(notesAdapter);

        defaultTextView = view.findViewById(R.id.defaultTextView);

        // Display notes by default when the fragment is created
        view.post(() -> filterItems(""));

        return view;
    }

    private void filterItems(String query) {
        if (getView() == null) return;

        int selectedTabPosition = tabLayout.getSelectedTabPosition();

        if (selectedTabPosition == 0) { // Note Lookup
            List<NoteCardView> filteredNotes = new ArrayList<>();
            for (NoteCardView noteCard : allNotes) {
                if (noteCard.getNote().getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredNotes.add(noteCard);
                }
            }
            notesAdapter.setNotes(filteredNotes);
            recyclerView.setAdapter(notesAdapter);
            defaultTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else if (selectedTabPosition == 1) { // Task Lookup
            List<TaskCardView> filteredTasks = new ArrayList<>();
            for (TaskCardView taskCard : allTasks) {
                if (taskCard.getTask().getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredTasks.add(taskCard);
                }
            }
            taskAdapter.setTasks(filteredTasks);
            recyclerView.setAdapter(taskAdapter);
            defaultTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else if (selectedTabPosition == 2) { // Full Search
            if (query.isEmpty()) {
                defaultTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                List<NoteCardView> filteredNotes = new ArrayList<>();
                for (NoteCardView noteCard : allNotes) {
                    if (noteCard.getNote().getTitle().toLowerCase().contains(query.toLowerCase()) ||
                            noteCard.getNote().getContent().toLowerCase().contains(query.toLowerCase())) {
                        filteredNotes.add(noteCard);
                    }
                }
                notesAdapter.setNotes(filteredNotes);
                recyclerView.setAdapter(notesAdapter);
                defaultTextView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
}