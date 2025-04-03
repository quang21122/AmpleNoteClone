package com.example.amplenoteclone.utils;

import android.content.Context;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.adapters.TagsAdapter;
import com.example.amplenoteclone.models.Note;
import com.example.amplenoteclone.models.Tag;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TagManager {
    private final Context context;
    private final FirebaseFirestore db;
    private final TagsAdapter tagsAdapter;
    private final List<Tag> tagsList;
    private final Note currentNote;

    public TagManager(Context context, TagsAdapter tagsAdapter, List<Tag> tagsList, Note currentNote) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.tagsAdapter = tagsAdapter;
        this.tagsList = tagsList;
        this.currentNote = currentNote;
    }

    public void addNewTag(String tagName) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Kiểm tra xem tag đã tồn tại chưa
        db.collection("tags")
                .whereEqualTo("name", tagName)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Tag đã tồn tại, sử dụng tag hiện có
                        Tag existingTag = queryDocumentSnapshots.getDocuments().get(0).toObject(Tag.class);
                        existingTag.setId(queryDocumentSnapshots.getDocuments().get(0).getId());
                        addTagToNote(existingTag);
                    } else {
                        // Tạo tag mới
                        Tag newTag = new Tag(tagName, userId);
                        db.collection("tags")
                                .add(newTag)
                                .addOnSuccessListener(documentReference -> {
                                    String newTagId = documentReference.getId();
                                    newTag.setId(newTagId);
                                    addTagToNote(newTag);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("TagManager", "Failed to add new tag: " + e.getMessage());
                                    Toast.makeText(context, "Failed to add tag", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("TagManager", "Failed to check existing tag: " + e.getMessage());
                    Toast.makeText(context, "Failed to add tag", Toast.LENGTH_SHORT).show();
                });
    }

    private void addTagToNote(Tag tag) {
        List<String> updatedTagIds = new ArrayList<>(currentNote.getTags());
        if (!updatedTagIds.contains(tag.getId())) {
            updatedTagIds.add(tag.getId());
            currentNote.setTags((ArrayList<String>) updatedTagIds);

            // Cập nhật Note trên Firestore
            db.collection("notes").document(currentNote.getId())
                    .update("tags", updatedTagIds)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("TagManager", "Tag added to note successfully");
                        tagsList.add(tag);
                        tagsAdapter.setTags(tagsList);
                        Toast.makeText(context, "Tag added", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("TagManager", "Failed to update note tags: " + e.getMessage());
                        Toast.makeText(context, "Failed to add tag", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    public void removeTagFromNote(Tag tag) {
        List<String> updatedTagIds = new ArrayList<>(currentNote.getTags());
        updatedTagIds.remove(tag.getId());
        currentNote.setTags((ArrayList<String>) updatedTagIds);

        // Cập nhật Note trên Firestore
        db.collection("notes").document(currentNote.getId())
                .update("tags", updatedTagIds)
                .addOnSuccessListener(aVoid -> {
                    Log.d("TagManager", "Tag removed from note successfully");
                    tagsList.remove(tag);
                    tagsAdapter.setTags(tagsList);
                    Toast.makeText(context, "Tag removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("TagManager", "Failed to remove tag from note: " + e.getMessage());
                    Toast.makeText(context, "Failed to remove tag", Toast.LENGTH_SHORT).show();
                });
    }

    public void editTag(Tag tag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Tag");

        final EditText input = new EditText(context);
        input.setText(tag.getName());
        input.setHint("Enter new tag name");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newTagName = input.getText().toString().trim();
            if (!newTagName.isEmpty() && !newTagName.equals(tag.getName())) {
                // Cập nhật tên tag trên Firestore
                db.collection("tags").document(tag.getId())
                        .update("name", newTagName)
                        .addOnSuccessListener(aVoid -> {
                            tag.setName(newTagName);
                            tagsAdapter.setTags(tagsList);
                            Toast.makeText(context, "Tag updated", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to update tag", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public void deleteTag(Tag tag) {
        // Xóa tag khỏi tất cả các note
        db.collection("notes")
                .whereArrayContains("tags", tag.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var doc : queryDocumentSnapshots.getDocuments()) {
                        Note note = doc.toObject(Note.class);
                        List<String> updatedTags = new ArrayList<>(note.getTags());
                        updatedTags.remove(tag.getId());
                        note.setTags((ArrayList<String>) updatedTags);
                        db.collection("notes").document(doc.getId())
                                .update("tags", updatedTags);
                    }

                    // Xóa tag khỏi collection "tags"
                    db.collection("tags").document(tag.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                tagsList.remove(tag);
                                tagsAdapter.setTags(tagsList);
                                Toast.makeText(context, "Tag deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to delete tag", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete tag", Toast.LENGTH_SHORT).show();
                });
    }
}