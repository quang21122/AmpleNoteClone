package com.example.amplenoteclone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.amplenoteclone.settings.SettingsActivity;
import com.example.amplenoteclone.authentication.Login;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextView userNameTextView;
    private CircleImageView profileImage;
    private TextView defaultAvatarText;
    private Button signOutButton;
    private Button settingsButton;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize Google Sign In client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
        userNameTextView = findViewById(R.id.user_name);
        signOutButton = findViewById(R.id.sign_out_button);
        user = mAuth.getCurrentUser();
        if (user != null) {
            userNameTextView.setText(user.getEmail());
        } else {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        initializeViews();
        loadUserProfile();
    }

    private void initializeViews() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userNameTextView = findViewById(R.id.user_name);
        profileImage = findViewById(R.id.profile_image);
        defaultAvatarText = findViewById(R.id.default_avatar_text);
        signOutButton = findViewById(R.id.sign_out_button);
        settingsButton = findViewById(R.id.settings_button);

        signOutButton.setOnClickListener(v -> signOut());
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String name = document.getString("name");
                            boolean hasCustomAvatar = Boolean.TRUE.equals(document.getBoolean("hasCustomAvatar"));
                            String base64Image = document.getString("avatarBase64");

                            userNameTextView.setText(name.isEmpty() ? user.getEmail() : name);

                            if (hasCustomAvatar && base64Image != null && !base64Image.isEmpty()) {
                                // Decode base64 string to bitmap
                                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                                profileImage.setImageBitmap(bitmap);
                                profileImage.setVisibility(View.VISIBLE);
                                defaultAvatarText.setVisibility(View.GONE);
                            } else {
                                // Show default avatar
                                String firstLetter = name.isEmpty() ?
                                        user.getEmail().substring(0, 1).toUpperCase() :
                                        name.substring(0, 1).toUpperCase();
                                defaultAvatarText.setText(firstLetter);
                                profileImage.setVisibility(View.GONE);
                                defaultAvatarText.setVisibility(View.VISIBLE);
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(MainActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show()
                    );
        } else {
            startActivity(new Intent(this, Login.class));
            finish();
        }
    }

    private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
    }



}