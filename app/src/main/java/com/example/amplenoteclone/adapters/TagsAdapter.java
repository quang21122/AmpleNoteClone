package com.example.amplenoteclone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Tag;
import com.example.amplenoteclone.note.ViewNoteActivity;
import com.example.amplenoteclone.tag.AddTagDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class TagsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TAG = 0;
    private static final int TYPE_ADD_TAG = 1;

    private List<Tag> tagsList;
    private final Context context;
    private OnTagItemClickListener tagItemClickListener;

    public interface OnTagItemClickListener {
        void onTagItemClick(Tag tag);
    }

    public TagsAdapter(Context context, List<Tag> tagsList, OnTagItemClickListener tagItemClickListener) {
        this.context = context;
        this.tagsList = tagsList != null ? tagsList : new ArrayList<>();
        this.tagItemClickListener = tagItemClickListener;
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

            // Kiểm tra nếu tag name là "daily-jots" thì đổi màu tag_icon thành textBlue
            Context context = tagHolder.tagIcon.getContext();
            if ("daily-jots".equalsIgnoreCase(tag.getName())) {
                tagHolder.tagIcon.setColorFilter(ContextCompat.getColor(context, R.color.textBlue));
            } else {
                tagHolder.tagIcon.clearColorFilter();
            }

            // Xóa sự kiện onClick của more_icon (chỉ để hiển thị)
            tagHolder.moreIcon.setClickable(false);
            tagHolder.moreIcon.setFocusable(false);

            // Thêm sự kiện onClick cho toàn bộ item
            tagHolder.itemView.setOnClickListener(v -> tagItemClickListener.onTagItemClick(tag));
        } else {
            AddTagViewHolder addTagHolder = (AddTagViewHolder) holder;
            addTagHolder.addTagButton.setOnClickListener(v -> {
                // Hiển thị AddTagDialogFragment
                AddTagDialogFragment dialogFragment = new AddTagDialogFragment();
                dialogFragment.setOnTagAddedListener(tagName -> {
                    if (context instanceof ViewNoteActivity) {
                        ((ViewNoteActivity) context).addNewTag(tagName);
                    }
                });
                dialogFragment.show(((FragmentActivity) context).getSupportFragmentManager(), "AddTagDialog");
            });
        }
    }

    @Override
    public int getItemCount() {
        return tagsList.size() + 1;
    }

    static class TagViewHolder extends RecyclerView.ViewHolder {
        TextView tagName;
        ImageView tagIcon;
        ImageView moreIcon;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            tagName = itemView.findViewById(R.id.tag_name);
            tagIcon = itemView.findViewById(R.id.tag_icon);
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