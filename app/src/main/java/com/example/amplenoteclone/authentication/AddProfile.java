package com.example.amplenoteclone.authentication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.amplenoteclone.MainActivity;
import com.example.amplenoteclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddProfile extends AppCompatActivity {
    private CircleImageView profileImage;
    private TextView defaultAvatarText;
    private EditText nameInput;
    private Button saveButton;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Uri selectedImageUri;
    private String userID;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile);

        initializeViews();
        setupDefaultAvatar();
        setupClickListeners();
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profile_image);
        defaultAvatarText = findViewById(R.id.default_avatar_text);
        nameInput = findViewById(R.id.full_name);
        saveButton = findViewById(R.id.save_button);
        progressBar = findViewById(R.id.progress_bar);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Make the entire profile image container clickable
        View profileContainer = findViewById(R.id.profile_container);
        if (profileContainer != null) {
            profileContainer.setOnClickListener(v -> openImagePicker());
        } else {
            // If no container, make both views clickable
            profileImage.setOnClickListener(v -> openImagePicker());
            defaultAvatarText.setOnClickListener(v -> openImagePicker());
        }
    }

    private void setupDefaultAvatar() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            String firstLetter = user.getEmail().substring(0, 1).toUpperCase();
            defaultAvatarText.setText(firstLetter);
            defaultAvatarText.setVisibility(View.VISIBLE);
            profileImage.setVisibility(View.INVISIBLE); // Change GONE to INVISIBLE
        }
    }


    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                saveProfile(user);
            }
        });

        profileImage.setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening image picker: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                profileImage.setImageURI(selectedImageUri);
                profileImage.setVisibility(View.VISIBLE);
                defaultAvatarText.setVisibility(View.GONE);
            }
        }
    }

    private void saveProfile(FirebaseUser user) {
        String name = nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            nameInput.setError("Please enter your name");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        // First save basic info
        DocumentReference userRef = db.collection("users").document(user.getUid());
        Map<String, Object> basicProfile = new HashMap<>();
        basicProfile.put("name", name);
        basicProfile.put("hasCustomAvatar", selectedImageUri != null);

        userRef.set(basicProfile, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // If there's an image, save it separately
                    if (selectedImageUri != null) {
                        saveProfileImage(user.getUid());
                    } else {
                        // No image to save, complete the process
                        finishProfileUpdate();
                    }
                })
                .addOnFailureListener(e -> handleError(e));
    }

    private void saveProfileImage(String userId) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);

            // Calculate sample size to reduce memory usage
            int originalWidth = bitmap.getWidth();
            int originalHeight = bitmap.getHeight();
            int targetSize = 800; // max dimension

            float scale = Math.min(
                    (float) targetSize / originalWidth,
                    (float) targetSize / originalHeight);

            // Create scaled bitmap
            int newWidth = Math.round(originalWidth * scale);
            int newHeight = Math.round(originalHeight * scale);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

            // Recycle original bitmap to free memory
            if (bitmap != scaledBitmap) {
                bitmap.recycle();
            }

            // Compress with lower quality
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
            byte[] data = baos.toByteArray();

            // Check final size
            if (data.length > 200000) { // 200KB limit
                Toast.makeText(this, "Image still too large after compression", Toast.LENGTH_SHORT).show();
                handleError(new Exception("Image too large"));
                return;
            }

            String base64Image = Base64.encodeToString(data, Base64.DEFAULT);

            // Save in smaller chunks to avoid Firestore limitations
            Map<String, Object> imageUpdate = new HashMap<>();
            imageUpdate.put("avatarBase64", base64Image);
            imageUpdate.put("imageWidth", newWidth);
            imageUpdate.put("imageHeight", newHeight);

            DocumentReference userRef = db.collection("users").document(userId);
            userRef.set(imageUpdate, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        scaledBitmap.recycle();
                        finishProfileUpdate();
                    })
                    .addOnFailureListener(e -> {
                        scaledBitmap.recycle();
                        handleError(e);
                    });

        } catch (IOException e) {
            handleError(e);
        } catch (OutOfMemoryError e) {
            Toast.makeText(this, "Image too large to process", Toast.LENGTH_SHORT).show();
            handleError(new Exception("Out of memory error"));
        }
    }

    private void finishProfileUpdate() {
        progressBar.setVisibility(View.GONE);
        saveButton.setEnabled(true);
        Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void handleError(Exception e) {
        progressBar.setVisibility(View.GONE);
        saveButton.setEnabled(true);
        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e("AddProfile", "Error saving profile", e);
    }
}