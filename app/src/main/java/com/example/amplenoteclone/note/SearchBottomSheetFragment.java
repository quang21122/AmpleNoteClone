package com.example.amplenoteclone.note;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.adapters.NotesAdapter;
import com.example.amplenoteclone.ui.customviews.NoteCardView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;

public class SearchBottomSheetFragment extends BottomSheetDialogFragment {
    private NotesAdapter notesAdapter;
    private ArrayList<NoteCardView> allNotes;

    public SearchBottomSheetFragment(ArrayList<NoteCardView> allNotes) {
        this.allNotes = allNotes;
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

        RecyclerView recyclerView = view.findViewById(R.id.recyclerSearchNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notesAdapter = new NotesAdapter();
        recyclerView.setAdapter(notesAdapter);

        // Use the EditText for search input instead of SearchView
        EditText searchEditText = view.findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* No-op */ }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { /* No-op */ }
        });

        ImageButton closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dismiss());

        // (Optional) Handle TabLayout selections if needed
        TabLayout tabLayout = view.findViewById(R.id.searchTabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // You can adjust your filtering logic here based on the selected tab
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        return view;
    }

    private void filterNotes(String query) {
        ArrayList<NoteCardView> filteredNotes = new ArrayList<>();
        for (NoteCardView noteCard : allNotes) {
            if (noteCard.getNote().getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredNotes.add(noteCard);
            }
        }
        notesAdapter.setNotes(filteredNotes);
        notesAdapter.notifyDataSetChanged();
    }
}
