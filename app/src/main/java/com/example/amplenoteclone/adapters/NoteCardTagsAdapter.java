package com.example.amplenoteclone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Tag;

import java.util.ArrayList;
import java.util.List;

public class NoteCardTagsAdapter extends RecyclerView.Adapter<NoteCardTagsAdapter.NoteCardTagViewHolder> {

    private List<Tag> tagsList;

    public NoteCardTagsAdapter() {
        this.tagsList = new ArrayList<>();
    }

    public void setTags(List<Tag> tags) {
        this.tagsList = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteCardTagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note_card_tag, parent, false);
        return new NoteCardTagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteCardTagViewHolder holder, int position) {
        Tag tag = tagsList.get(position);
        holder.tagName.setText(tag.getName());

        // Màu icon cho daily-jots
        Context context = holder.tagIcon.getContext();
        if ("daily-jots".equalsIgnoreCase(tag.getName())) {
            holder.tagIcon.setColorFilter(ContextCompat.getColor(context, R.color.textGray));
        } else {
            holder.tagIcon.setColorFilter(ContextCompat.getColor(context, R.color.textBlue));
        }
    }

    @Override
    public int getItemCount() {
        int count = tagsList.size();
        return count;
    }

    static class NoteCardTagViewHolder extends RecyclerView.ViewHolder {
        TextView tagName;
        ImageView tagIcon;

        public NoteCardTagViewHolder(@NonNull View itemView) {
            super(itemView);
            tagName = itemView.findViewById(R.id.tag_name2);
            tagIcon = itemView.findViewById(R.id.tag_icon2);
        }
    }
}