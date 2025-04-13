package com.example.amplenoteclone.models;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class Tag implements Serializable {
    private String id;
    private String name;
    private String userId;
    private int count;

    public Tag(String name, String userId) {
        this.name = name;
        this.userId = userId;
        this.count = 0;
    }

    public Tag() {}

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }

    @Exclude
    public int getCount() {
        return count;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    // Phương thức tạo tag mới trong Firestore
    public static void createTagInFirestore(String tagName, Consumer<Tag> onSuccess, Consumer<String> onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Kiểm tra xem tag đã tồn tại chưa
        db.collection("tags")
                .whereEqualTo("name", tagName)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Tag đã tồn tại, trả về tag hiện có
                        Tag existingTag = queryDocumentSnapshots.getDocuments().get(0).toObject(Tag.class);
                        existingTag.setId(queryDocumentSnapshots.getDocuments().get(0).getId());
                        onSuccess.accept(existingTag);
                    } else {
                        // Tạo tag mới
                        Tag newTag = new Tag(tagName, userId);
                        db.collection("tags")
                                .add(newTag)
                                .addOnSuccessListener(documentReference -> {
                                    newTag.setId(documentReference.getId());
                                    onSuccess.accept(newTag);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Tag", "Failed to create tag: " + e.getMessage());
                                    onFailure.accept("Failed to create tag");
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Tag", "Failed to check existing tag: " + e.getMessage());
                    onFailure.accept("Failed to check existing tag");
                });
    }

    public void editTagInFirestore(String newTagName, Runnable onSuccess, Consumer<String> onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (id == null) {
            onFailure.accept("Tag ID is null");
            return;
        }

        // Check if the new tag name already exists for this user
        db.collection("tags")
                .whereEqualTo("name", newTagName)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // If a tag with this name exists and it's not the current tag
                    if (!queryDocumentSnapshots.isEmpty() &&
                            !queryDocumentSnapshots.getDocuments().get(0).getId().equals(id)) {
                        onFailure.accept("A tag with this name already exists");
                        return;
                    }

                    WriteBatch batch = db.batch();
                    batch.update(db.collection("tags").document(id), "name", newTagName);

                    // Update updatedAt for all Notes containing this tag
                    db.collection("notes")
                            .whereArrayContains("tags", id)
                            .get()
                            .addOnSuccessListener(noteSnapshots -> {
                                for (QueryDocumentSnapshot doc : noteSnapshots) {
                                    batch.update(
                                            db.collection("notes").document(doc.getId()),
                                            "updatedAt", new Date()
                                    );
                                }
                                batch.commit()
                                        .addOnSuccessListener(aVoid -> {
                                            this.name = newTagName;
                                            onSuccess.run();
                                        })
                                        .addOnFailureListener(e ->
                                                onFailure.accept("Failed to edit tag: " + e.getMessage()));
                            })
                            .addOnFailureListener(e ->
                                    onFailure.accept("Failed to fetch notes: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        onFailure.accept("Failed to check tag existence: " + e.getMessage()));
    }
    public void deleteTagInFirestore(Runnable onSuccess, Consumer<String> onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (id == null) {
            onFailure.accept("Tag ID is null");
            return;
        }

        WriteBatch batch = db.batch();

        db.collection("notes")
                .whereArrayContains("tags", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        List<String> currentTags = (List<String>) doc.get("tags");
                        if (currentTags != null) {
                            List<String> updatedTags = new ArrayList<>(currentTags);
                            updatedTags.remove(id);
                            batch.update(
                                    db.collection("notes").document(doc.getId()),
                                    "tags", updatedTags,
                                    "updatedAt", new Date()
                            );
                        }
                    }
                    batch.delete(db.collection("tags").document(id));
                    batch.commit()
                            .addOnSuccessListener(aVoid -> onSuccess.run())
                            .addOnFailureListener(e -> onFailure.accept("Failed to delete tag"));
                })
                .addOnFailureListener(e -> onFailure.accept("Failed to query notes"));
    }
    public void removeTagFromNoteInFirestore(String noteId, Runnable onSuccess, Consumer<String> onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (id == null) {
            onFailure.accept("Tag ID is null");
            return;
        }
        if (noteId == null) {
            onFailure.accept("Note ID is null");
            return;
        }

        db.collection("notes").document(noteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Note note = documentSnapshot.toObject(Note.class);
                        if (note != null && note.getTags() != null && note.getTags().contains(id)) {
                            List<String> updatedTags = new ArrayList<>(note.getTags());
                            updatedTags.remove(id);

                            // Cập nhật note trong Firestore
                            db.collection("notes").document(noteId)
                                    .update(
                                        "tags", updatedTags,
                                        "updatedAt", new Date())
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Tag", "Tag " + id + " removed from note " + noteId);
                                        // Kiểm tra xem tag có còn được sử dụng không
                                        checkAndDeleteIfUnused(onSuccess, onFailure);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Tag", "Failed to update note: " + e.getMessage());
                                        onFailure.accept("Failed to remove tag from note");
                                    });
                        } else {
                            Log.w("Tag", "Tag not found or already removed from note");
                            onSuccess.run(); // Treat as successful to allow loop continuation
                        }
                    } else {
                        onFailure.accept("Note not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Tag", "Failed to fetch note: " + e.getMessage());
                    onFailure.accept("Failed to fetch note");
                });
    }

    // Kiểm tra và xóa tag nếu không còn được sử dụng
    private void checkAndDeleteIfUnused(Runnable onSuccess, Consumer<String> onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("notes")
                .whereArrayContains("tags", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Không còn note nào sử dụng tag này, xóa tag
                        db.collection("tags").document(id)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Tag", "Tag " + id + " deleted as it is unused");
                                    onSuccess.run();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Tag", "Failed to delete unused tag: " + e.getMessage());
                                    onFailure.accept("Failed to delete unused tag");
                                });
                    } else {
                        // Tag vẫn được sử dụng, chỉ gọi onSuccess mà không xóa
                        onSuccess.run();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Tag", "Failed to check tag usage: " + e.getMessage());
                    onFailure.accept("Failed to check tag usage");
                });
    }
}