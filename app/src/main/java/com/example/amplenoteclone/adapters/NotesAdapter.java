package com.example.amplenoteclone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.ui.customviews.NoteCardView;

import java.util.ArrayList;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {

    private final List<NoteCardView> noteList;

    private NoteCardView.OnNoteCardClickListener clickListener;

    // Constructor
    public NotesAdapter() {
        this.noteList = new ArrayList<>();
    }
    public NotesAdapter(List<NoteCardView> notes) {
        this.noteList = notes;
    }

    // Setters
    public void setNotes(List<NoteCardView> notes) {
        if (this.noteList != null)
            this.noteList.clear();
        this.noteList.addAll(notes);
    }

    public void setOnNoteCardClickListener(NoteCardView.OnNoteCardClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_note_card, parent, false);
        return new NotesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        NoteCardView noteItem = noteList.get(position);

        if (noteItem.getNote().getIsProtected() != null && noteItem.getNote().getIsProtected()) {
            holder.iconView.setImageResource(R.drawable.ic_lock);
        } else {
            holder.iconView.setImageResource(R.drawable.ic_note);
        }
        holder.titleView.setText(noteItem.getTitle());
        holder.contentView.setText(noteItem.getContent());
        holder.dateView.setText(noteItem.getDate());
        holder.setupTags(noteItem);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onNoteCardClick(noteItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    // ViewHolder class
    static class NotesViewHolder extends RecyclerView.ViewHolder {
        TextView titleView, contentView, dateView;
        ImageView iconView;
        RecyclerView tagsRecyclerView;

        public NotesViewHolder(@NonNull View itemView) {

            super(itemView);

            iconView = itemView.findViewById(R.id.note_icon);
            titleView = itemView.findViewById(R.id.note_title);
            contentView = itemView.findViewById(R.id.note_content);
            dateView = itemView.findViewById(R.id.note_date);
            tagsRecyclerView = itemView.findViewById(R.id.note_tags_recycler_view);
        }

        public void setupTags(NoteCardView noteCardView) {
            if (tagsRecyclerView != null) {
                noteCardView.setupTags(tagsRecyclerView);
            }
        }
    }
}
