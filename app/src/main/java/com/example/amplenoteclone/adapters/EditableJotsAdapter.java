package com.example.amplenoteclone.adapters;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Note;

import java.util.List;

public class EditableJotsAdapter extends RecyclerView.Adapter<EditableJotsAdapter.EditableJotViewHolder> {
    private List<Note> jotsList;
    private OnJotContentChangedListener contentChangedListener;

    // Interface for content change events
    public interface OnJotContentChangedListener {
        void onContentChanged(Note jot, String newContent);
    }

    public EditableJotsAdapter(List<Note> jotsList, OnJotContentChangedListener contentChangedListener) {
        this.jotsList = jotsList;
        this.contentChangedListener = contentChangedListener;
    }

    @NonNull
    @Override
    public EditableJotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_editable_jot, parent, false);
        return new EditableJotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditableJotViewHolder holder, int position) {
        Note jot = jotsList.get(position);
        holder.bind(jot);
    }

    @Override
    public int getItemCount() {
        return jotsList.size();
    }

    class EditableJotViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private EditText contentEditText;
        private TextView tagTextView;
        private CardView cardView;
        private Note boundJot;

        public EditableJotViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.jot_title);
            contentEditText = itemView.findViewById(R.id.jot_content);
            tagTextView = itemView.findViewById(R.id.jot_tag);
            cardView = itemView.findViewById(R.id.jot_card);

            // Add text change listener
            contentEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Not needed
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Not needed
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (boundJot != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                        contentChangedListener.onContentChanged(boundJot, s.toString());
                    }
                }
            });
        }

        public void bind(Note jot) {
            boundJot = jot;
            
            // Set title with the date format
            titleTextView.setText(jot.getTitle());

            // Set content text
            String content = jot.getContent();
            contentEditText.setText(content);

            cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            
            // Apply different background for today vs other days
            if (getAdapterPosition() == 0) {
                // Today's jot
                contentEditText.setHint(R.string.jots_text);
            } else {
                // Previous days
                contentEditText.setHint("Write your thoughts for this day...");
            }
        }
    }
} 