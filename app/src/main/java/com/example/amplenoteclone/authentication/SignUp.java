package com.example.amplenoteclone.authentication;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.amplenoteclone.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ImageView logoImageView;
    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView logInTextView;
    private Button signUpButton;
    private ProgressBar progressBar;

    private Button signUpGoogleButton;
    private GoogleSignInOptions gOptions;
    private GoogleSignInClient gClient;
    private static final int RC_SIGN_IN = 9001;
    private FirebaseFirestore db;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        logoImageView = findViewById(R.id.logo);
        signUpGoogleButton = findViewById(R.id.sign_up_with_google);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        logInTextView = findViewById(R.id.login);
        signUpButton = findViewById(R.id.signup_button);
        progressBar = findViewById(R.id.progress_bar);

        gOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        gClient = GoogleSignIn.getClient(this, gOptions);

        mAuth.setLanguageCode("en");

        db = FirebaseFirestore.getInstance();

        // Get the theme attribute value
        int isNightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = isNightModeFlags == Configuration.UI_MODE_NIGHT_YES;

        // Set appropriate logo
        logoImageView.setImageResource(isDarkMode ?
                R.drawable.main_logo : R.drawable.main_logo_light);

        // Set appropriate button background
        signUpGoogleButton.setBackgroundResource(isDarkMode ?
                R.drawable.signin_gg_button : R.drawable.signin_gg_button_light);


        logInTextView.setOnClickListener(v -> {
            Intent intent = new Intent(SignUp.this, Login.class);
            startActivity(intent);
            finish();
        });

        signUpGoogleButton.setOnClickListener(v -> signUpWithGoogle());

        signUpButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignUp.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                signUpWithEmailPassword(email, password);
            }
        });
    }

    private void signUpWithGoogle() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = gClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(SignUp.this, "Failed to login! Please check your credentials", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Toast.makeText(SignUp.this, "Failed to login! Please check your credentials", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void signUpWithEmailPassword(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Toast.makeText(SignUp.this, "Failed to login! Please check your credentials", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            progressBar.setVisibility(View.VISIBLE);

            userID = user.getUid();
            DocumentReference documentReference = db.collection("users").document(userID);
            // Create user data map
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", user.getEmail());
            userData.put("createdAt", System.currentTimeMillis());

            // Add user data to Firestore
            documentReference.set(userData).addOnSuccessListener(aVoid -> {
                Toast.makeText(SignUp.this, "User created successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUp.this, AddProfile.class);
                startActivity(intent);
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(SignUp.this, "Failed to create user", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            });
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
}