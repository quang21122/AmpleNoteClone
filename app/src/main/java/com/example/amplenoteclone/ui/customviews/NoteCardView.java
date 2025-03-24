package com.example.amplenoteclone.ui.customviews;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Note;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;

public class NoteCardView extends CardView {

    private ImageView iconView;
    private TextView titleView, contentView, tagsView, dateView;
    private Note note;
    private OnNoteCardClickListener clickListener;

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
        iconView.setImageResource(R.drawable.ic_note);
        titleView = findViewById(R.id.note_title);
        contentView = findViewById(R.id.note_content);
        tagsView = findViewById(R.id.note_tags);
        dateView = findViewById(R.id.note_date);

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

            // Handle tags
            ArrayList<String> tags = note.getTags();
            if (tags != null && !tags.isEmpty()) {
                tagsView.setText(TextUtils.join(", ", tags));
                tagsView.setVisibility(VISIBLE);
            } else {
                tagsView.setVisibility(GONE);
            }

            // Set date (typically you'd use the updated date)
            Timestamp updatedAt = new Timestamp(new Date(note.getUpdatedAt()));

            // Use the updated date if available, otherwise use the created date
            dateView.setText(updatedAt.toDate().toString());

            iconView.setImageResource(R.drawable.ic_note);

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

    public void setTags(String tagsText) {
        tagsView.setText(tagsText);
        // Don't update note.tags here as the format might be different
    }

    public void setDate(String date) {
        dateView.setText(date);
        // Don't update note.updatedAt here as the format might be different
    }

    public String getTitle() {
        return titleView.getText().toString();
    }

    public String getContent() {
        return note != null ? note.getContent() : contentView.getText().toString();
    }

    public String getTags() {
        return tagsView.getText().toString();
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
}