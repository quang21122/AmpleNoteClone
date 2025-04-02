package com.example.amplenoteclone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Tag;

import java.util.ArrayList;
import java.util.List;

public class TagsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TAG = 0;
    private static final int TYPE_ADD_TAG = 1;

    private List<Tag> tagsList; // Thay List<String> thành List<Tag>
    private OnAddTagClickListener addTagClickListener;
    private OnMoreIconClickListener moreIconClickListener;

    public interface OnAddTagClickListener {
        void onAddTagClick();
    }

    public interface OnMoreIconClickListener {
        void onMoreIconClick(Tag tag); // Thay String thành Tag
    }

    public TagsAdapter(List<Tag> tagsList, OnAddTagClickListener addTagClickListener, OnMoreIconClickListener moreIconClickListener) {
        this.tagsList = tagsList != null ? tagsList : new ArrayList<>();
        this.addTagClickListener = addTagClickListener;
        this.moreIconClickListener = moreIconClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == tagsList.size()) {
            return TYPE_ADD_TAG;
        }
        return TYPE_TAG;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_TAG) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item, parent, false);
            return new TagViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_tag_item, parent, false);
            return new AddTagViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_TAG) {
            TagViewHolder tagHolder = (TagViewHolder) holder;
            Tag tag = tagsList.get(position);
            tagHolder.tagName.setText(tag.getName());
            tagHolder.moreIcon.setOnClickListener(v -> moreIconClickListener.onMoreIconClick(tag));
        } else {
            AddTagViewHolder addTagHolder = (AddTagViewHolder) holder;
            addTagHolder.addTagButton.setOnClickListener(v -> addTagClickListener.onAddTagClick());
        }
    }

    @Override
    public int getItemCount() {
        return tagsList.size() + 1;
    }

    static class TagViewHolder extends RecyclerView.ViewHolder {
        TextView tagName;
        ImageView moreIcon;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            tagName = itemView.findViewById(R.id.tag_name);
            moreIcon = itemView.findViewById(R.id.more_icon);
        }
    }

    static class AddTagViewHolder extends RecyclerView.ViewHolder {
        TextView addTagButton;

        public AddTagViewHolder(@NonNull View itemView) {
            super(itemView);
            addTagButton = itemView.findViewById(R.id.add_tag);
        }
    }

    public void setTags(List<Tag> tags) {
        this.tagsList = tags != null ? tags : new ArrayList<>();
        notifyDataSetChanged();
    }
}