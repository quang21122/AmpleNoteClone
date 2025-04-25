package com.example.amplenoteclone.ui.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.adapters.NoteCardTagsAdapter;
import com.example.amplenoteclone.models.Note;
import com.example.amplenoteclone.models.Tag;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NoteCardView extends CardView {

    private ImageView iconView;
    private TextView titleView, contentView, dateView;
    private Note note;
    private OnNoteCardClickListener clickListener;
    private RecyclerView tagsRecyclerView;
    private List<Tag> tagsList;
    private NoteCardTagsAdapter tagsAdapter;

    public NoteCardView(Context context) {
        super(context);
        init();
    }

    public NoteCardView(Context context, AttributeSet args) {
        super(context, args);
        init();
    }

    public NoteCardView(Context context, AttributeSet args, int defStyle) {
        super(context, args, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_note_card, this);
        iconView = findViewById(R.id.note_icon);
        titleView = findViewById(R.id.note_title);
        contentView = findViewById(R.id.note_content);
        dateView = findViewById(R.id.note_date);
        tagsRecyclerView = findViewById(R.id.note_tags_recycler_view);

        // Set default click listener
        setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onNoteCardClick(this);
            }
        });
    }

    /**
     * Set the note and update all UI elements
     */
    public void setNote(Note note) {
        this.note = note;

        if (note != null) {
            titleView.setText(note.getTitle());
            contentView.setText(createContentPreview(note.getContent()));

            tagsRecyclerView.setVisibility(View.VISIBLE);

            // Set date (typically you'd use the updated date)
            Timestamp updatedAt = new Timestamp(note.getUpdatedAt());

            // Use the updated date if available, otherwise use the created date
            dateView.setText(updatedAt.toDate().toString());


            if (note.getIsProtected() != null && note.getIsProtected()) {
                iconView.setImageResource(R.drawable.ic_lock);
            } else {
                iconView.setImageResource(R.drawable.ic_note);
            }
        }
    }

    /**
     * Create a preview of the content (truncated if too long)
     */
    private String createContentPreview(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        // Remove new lines for preview
        String preview = content.replace("\n", " ").trim();

        // Truncate if needed (100 chars)
        if (preview.length() > 100) {
            return preview.substring(0, 97) + "...";
        }

        return preview;
    }

    /**
     * Get the note associated with this card
     */
    public Note getNote() {
        return note;
    }

    /**
     * For backward compatibility with existing code
     */
    public void setTitle(String title) {
        titleView.setText(title);
        if (note != null) {
            note.setTitle(title);
        }
    }

    public void setContent(String content) {
        contentView.setText(createContentPreview(content));
        if (note != null) {
            note.setContent(content);
        }
    }

    public void setDate(String date) {
        dateView.setText(date);
        // Don't update note.updatedAt here as the format might be different
    }

    public String getTitle() {
        return titleView.getText().toString();
    }

    public String getContent() {
        if (note != null ){
            if (!note.getIsProtected())
                return note.getContent();
            else
                return "";
        } else
            return contentView.getText().toString();
    }

    public String getDate() {
        return dateView.getText().toString();
    }

    public void setOnNoteCardClickListener(OnNoteCardClickListener listener) {
        this.clickListener = listener;
    }

    public interface OnNoteCardClickListener {
        void onNoteCardClick(NoteCardView noteCard);
    }

    private void loadTags(List<String> tagIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        tagsList.clear();

        final int totalTags = tagIds.size();
        final int[] completedQueries = {0};

        for (String tagId : tagIds) {
            db.collection("tags").document(tagId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Tag tag = documentSnapshot.toObject(Tag.class);
                            if (tag != null) {
                                tag.setId(tagId);
                                tagsList.add(tag);
                            }
                        } else {
                            Log.d("NoteCardView", "Tag not found for ID: " + tagId);
                        }

                        completedQueries[0]++;
                        if (completedQueries[0] == totalTags) {
                            tagsRecyclerView.post(() -> {
                                tagsAdapter.setTags(tagsList);
                                tagsAdapter.notifyDataSetChanged();
                                tagsRecyclerView.setVisibility(View.VISIBLE);
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("NoteCardView", "Failed to load tag: " + tagId, e);
                        completedQueries[0]++;
                        if (completedQueries[0] == totalTags) {
                            tagsRecyclerView.post(() -> {
                                tagsAdapter.setTags(tagsList);
                                tagsAdapter.notifyDataSetChanged();
                                tagsRecyclerView.setVisibility(tagsList.isEmpty() ? View.GONE : View.VISIBLE);
                            });
                        }
                    });
        }
    }

    private void setupTagFlexBoxLayout() {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        tagsRecyclerView.setLayoutManager(layoutManager);
    }


    public void setupTags(RecyclerView recyclerView) {
        this.tagsRecyclerView = recyclerView;
        tagsList = new ArrayList<>();

        setupTagFlexBoxLayout();

        tagsAdapter = new NoteCardTagsAdapter();
        tagsRecyclerView.setAdapter(tagsAdapter);

        if (note != null && note.getTags() != null && !note.getTags().isEmpty()) {
            loadTags(note.getTags());
        } else {
            tagsRecyclerView.setVisibility(View.GONE);
        }
    }
}