package com.example.amplenoteclone.models;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amplenoteclone.authentication.Login;
import com.example.amplenoteclone.utils.FirestoreCallback;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.atomic.AtomicReference;

public class User {
    private String id;
    private String avatarBase64;
    private Timestamp createdAt;
    private String email;
    private boolean hasCustomAvatar;
    private String name;
    private Boolean isPremium;

    public User() {}

    public User(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.createdAt = Timestamp.now();
        this.hasCustomAvatar = false;
        this.avatarBase64 = "";
        this.isPremium = false;
    }

    // Add ID getter/setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatarBase64() {
        return avatarBase64;
    }

    public void setAvatarBase64(String avatarBase64) {
        this.avatarBase64 = avatarBase64;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIsPremium(Boolean isPremium) {
        this.isPremium = isPremium;
    }

    public boolean isHasCustomAvatar() {
        return hasCustomAvatar;
    }

    public void setHasCustomAvatar(boolean hasCustomAvatar) {
        this.hasCustomAvatar = hasCustomAvatar;
    }

    public String getName() {
        return name;
    }

    public Boolean getIsPremium() {
        return isPremium;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static void loadUserProfile(Context context, TextView nameView, ImageView profileImage, TextView defaultAvatarText) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            db.collection("users").document(firebaseUser.getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            User user = document.toObject(User.class);
                            if (user != null) {
                                if (nameView != null)
                                    nameView.setText(user.getName().isEmpty() ? user.getEmail() : user.getName());

                                if (user.isHasCustomAvatar() && user.getAvatarBase64() != null && !user.getAvatarBase64().isEmpty()) {
                                    byte[] decodedString = Base64.decode(user.getAvatarBase64(), Base64.DEFAULT);
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                                    profileImage.setImageBitmap(bitmap);
                                    profileImage.setVisibility(android.view.View.VISIBLE);
                                    defaultAvatarText.setVisibility(android.view.View.GONE);
                                } else {
                                    String firstLetter = user.getName().isEmpty() ?
                                            user.getEmail().substring(0, 1).toUpperCase() :
                                            user.getName().substring(0, 1).toUpperCase();
                                    defaultAvatarText.setText(firstLetter);
                                    profileImage.setVisibility(android.view.View.GONE);
                                    defaultAvatarText.setVisibility(android.view.View.VISIBLE);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Error loading profile", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    public static void getCurrentUser(FirestoreCallback<User> callback) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        AtomicReference<User> user = new AtomicReference<>(new User());

        if (firebaseUser != null) {
            db.collection("users").document(firebaseUser.getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            callback.onCallback(document.toObject(User.class)); // Return the user object
                        } else {
                            callback.onCallback(null); // No user found
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("User", "Error loading user", e);
                        callback.onCallback(null); // Return null on error
                    });
        } else {
            callback.onCallback(null); // No authenticated user
        }
    }

    public static void signOut(Context context) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(com.example.amplenoteclone.R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);

        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent intent = new Intent(context, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        });
    }
}
