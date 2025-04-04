package com.example.amplenoteclone.models;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Tag {
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

    // Phương thức tạo tag mới trong Firestore
    public static void createTagInFirestore(Context context, String tagName, Consumer<Tag> onSuccess, Consumer<String> onFailure) {
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

    public void editTagInFirestore(Context context, String newTagName, Runnable onSuccess, Consumer<String> onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (id == null) {
            onFailure.accept("Tag ID is null");
            return;
        }

        db.collection("tags").document(id)
                .update("name", newTagName)
                .addOnSuccessListener(aVoid -> {
                    this.name = newTagName;
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("Tag", "Failed to edit tag: " + e.getMessage());
                    onFailure.accept("Failed to edit tag");
                });
    }

    public void deleteTagInFirestore(Context context, Runnable onSuccess, Consumer<String> onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (id == null) {
            onFailure.accept("Tag ID is null");
            return;
        }

        // Xóa tag khỏi tất cả các note
        db.collection("notes")
                .whereArrayContains("tags", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        List<String> updatedTags = new ArrayList<>(doc.get("tags", ArrayList.class));
                        updatedTags.remove(id);
                        db.collection("notes").document(doc.getId())
                                .update("tags", updatedTags);
                    }

                    // Xóa tag khỏi collection "tags"
                    db.collection("tags").document(id)
                            .delete()
                            .addOnSuccessListener(aVoid -> onSuccess.run())
                            .addOnFailureListener(e -> {
                                Log.e("Tag", "Failed to delete tag: " + e.getMessage());
                                onFailure.accept("Failed to delete tag");
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("Tag", "Failed to delete tag from notes: " + e.getMessage());
                    onFailure.accept("Failed to delete tag from notes");
                });
    }
}