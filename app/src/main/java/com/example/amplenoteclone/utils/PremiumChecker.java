package com.example.amplenoteclone.utils;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.example.amplenoteclone.settings.ChoosePlanActivity;

public class PremiumChecker {
    public interface PremiumCheckCallback {
        void onIsPremium();
        void onNotPremium();
    }

    public static void checkPremium(Context context, String userId, PremiumCheckCallback callback) {
        DocumentReference userRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId);

        userRef.get(Source.CACHE).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Boolean isPremium = task.getResult().getBoolean("isPremium");
                handlePremiumStatus(context, isPremium, callback);
            } else {
                userRef.get(Source.SERVER).addOnSuccessListener(documentSnapshot -> {
                    Boolean isPremium = documentSnapshot.getBoolean("isPremium");
                    handlePremiumStatus(context, isPremium, callback);
                });
            }
        });
    }

    private static void handlePremiumStatus(Context context, Boolean isPremium, PremiumCheckCallback callback) {
        if (isPremium != null && isPremium) {
            callback.onIsPremium();
        } else {
            callback.onNotPremium();
            showPremiumRequiredDialog(context);
        }
    }

    private static void showPremiumRequiredDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Premium Feature")
                .setMessage("This feature is only available for premium users. Please upgrade to premium to use this feature.")
                .setPositiveButton("Upgrade", (dialog, which) -> {
                    Intent intent = new Intent(context, ChoosePlanActivity.class);
                    context.startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}