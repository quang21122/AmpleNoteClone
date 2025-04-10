package com.example.amplenoteclone.note;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.amplenoteclone.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.Query;

public class NoteSortOrderBottomSheet extends BottomSheetDialogFragment {
    public interface OnSortSelectedListener {
        void onSortSelected(String sortField, Query.Direction direction, String sortOption);
    }
    private OnSortSelectedListener listener;

    public NoteSortOrderBottomSheet(OnSortSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.note_bottom_sheet_sort_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView sortByDate = view.findViewById(R.id.sortByDate);
        TextView sortByTitle = view.findViewById(R.id.sortByTitle);
        TextView sortByLastUpdated = view.findViewById(R.id.sortByLastUpdated);

        sortByDate.setOnClickListener(v -> {
            // Handle "Date created" sort
            if (listener != null) {
                listener.onSortSelected("createdAt", Query.Direction.DESCENDING, "Date created");
            }
            dismiss();
        });

        sortByTitle.setOnClickListener(v -> {
            // Handle "Title A-Z" sort
            if (listener != null) {
                listener.onSortSelected("title", Query.Direction.ASCENDING, "Title A-Z");
            }
            dismiss();
        });

        sortByLastUpdated.setOnClickListener(v -> {
            // Handle "Last updated" sort
            if (listener != null) {
                listener.onSortSelected("updatedAt", Query.Direction.DESCENDING, "Last updated");
            }
            dismiss();
        });
    }
}

