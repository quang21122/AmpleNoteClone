package com.example.amplenoteclone.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Note;

import java.util.List;

public class JotsAdapter extends RecyclerView.Adapter<JotsAdapter.JotViewHolder> {
    private List<Note> jotsList;
    private JotClickListener clickListener;

    // Interface for click events
    public interface JotClickListener {
        void onJotClick(Note jot);
    }

    public JotsAdapter(List<Note> jotsList, JotClickListener clickListener) {
        this.jotsList = jotsList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public JotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_jot, parent, false);
        return new JotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JotViewHolder holder, int position) {
        Note jot = jotsList.get(position);
        holder.bind(jot);
    }

    @Override
    public int getItemCount() {
        return jotsList.size();
    }

    class JotViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView previewTextView;
        private ImageView chevronIcon;
        private CardView cardView;

        public JotViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.jot_title);
            previewTextView = itemView.findViewById(R.id.jot_preview);
            chevronIcon = itemView.findViewById(R.id.chevron_icon);
            cardView = itemView.findViewById(R.id.jot_card);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    clickListener.onJotClick(jotsList.get(position));
                }
            });
        }

        public void bind(Note jot) {
            // Set title with the date format
            titleTextView.setText(jot.getTitle());

            // Set preview text
            String content = jot.getContent();
            if (content != null && !content.isEmpty()) {
                // Limit preview to first 100 characters
                String preview = content.length() > 100
                        ? content.substring(0, 100) + "..."
                        : content;

                // Clean up preview by removing excessive whitespace
                preview = preview.replaceAll("\\s+", " ").trim();

                previewTextView.setText(preview);
                previewTextView.setVisibility(View.VISIBLE);
            } else {
                // No content, hide preview
                previewTextView.setVisibility(View.GONE);
            }

            // Make sure the chevron icon is visible
            chevronIcon.setVisibility(View.VISIBLE);

            // Add a light gray background for empty jots
            if (content == null || content.isEmpty()) {
                cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
            } else {
                cardView.setCardBackgroundColor(Color.WHITE);
            }
        }
    }
}

