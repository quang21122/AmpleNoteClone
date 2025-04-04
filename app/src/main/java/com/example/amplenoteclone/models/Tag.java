package com.example.amplenoteclone.models;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
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
        Context appContext = context.getApplicationContext();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Kiểm tra id
        if (id == null) {
            onFailure.accept("Tag ID is null");
            return;
        }

        // Transaction để đảm bảo tất cả các thao tác được thực hiện hoặc không có thao tác nào được thực hiện
        db.runTransaction(transaction -> {
            // 1. Lấy tất cả notes chứa tag này
            db.collection("notes")
                    .whereArrayContains("tags", id)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        // 2. Xóa tag khỏi tất cả notes
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            List<String> currentTags = (List<String>) doc.get("tags");
                            if (currentTags != null) {
                                List<String> updatedTags = new ArrayList<>(currentTags);
                                updatedTags.remove(id);

                                // Cập nhật note
                                db.collection("notes")
                                        .document(doc.getId())
                                        .update("tags", updatedTags)
                                        .addOnSuccessListener(aVoid -> {
                                            // 3. Sau khi xóa khỏi tất cả notes, xóa tag chính
                                            db.collection("tags")
                                                    .document(id)
                                                    .delete()
                                                    .addOnSuccessListener(aVoid2 -> {
                                                        onSuccess.run();
                                                    })
                                                    .addOnFailureListener(e ->
                                                            onFailure.accept("Failed to delete tag: " + e.getMessage())
                                                    );
                                        })
                                        .addOnFailureListener(e ->
                                                onFailure.accept("Failed to update notes: " + e.getMessage())
                                        );
                            }
                        }

                        // Nếu không có note nào chứa tag này
                        if (queryDocumentSnapshots.isEmpty()) {
                            db.collection("tags")
                                    .document(id)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> onSuccess.run())
                                    .addOnFailureListener(e ->
                                            onFailure.accept("Failed to delete tag: " + e.getMessage())
                                    );
                        }
                    })
                    .addOnFailureListener(e ->
                            onFailure.accept("Failed to query notes: " + e.getMessage())
                    );
            return null;
        });
    }
}