package com.example.amplenoteclone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Note;

import java.util.List;

public class NotesAdapterForCreateTask extends RecyclerView.Adapter<NotesAdapterForCreateTask.NoteViewHolder> {

    private List<Note> notes;
    private OnNoteClickListener onNoteClickListener;

    public NotesAdapterForCreateTask(List<Note> notes, OnNoteClickListener onNoteClickListener) {
        this.notes = notes;
        this.onNoteClickListener = onNoteClickListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.create_task_item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.textViewNoteTitle.setText(note.getTitle() != null ? note.getTitle() : "untitled note");
        holder.itemView.setOnClickListener(v -> onNoteClickListener.onNoteClick(note));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNoteTitle;
        ImageView noteIcon;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteIcon = itemView.findViewById(R.id.note_icon);
            textViewNoteTitle = itemView.findViewById(R.id.text_view_note_title);
        }
    }

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }
}