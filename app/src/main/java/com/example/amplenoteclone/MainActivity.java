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

import com.example.amplenoteclone.calendar.CalendarActivity;
import com.example.amplenoteclone.models.User;
import com.example.amplenoteclone.note.NotesActivity;
import com.example.amplenoteclone.settings.SettingsActivity;
import com.example.amplenoteclone.authentication.Login;
import com.example.amplenoteclone.tasks.TasksPageActivity;
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
    private Button calendarButton;
    private Button notesButton;
    private Button tasksButton;
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
        User.loadUserProfile(this, userNameTextView, profileImage, defaultAvatarText);
    }

    private void initializeViews() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userNameTextView = findViewById(R.id.user_name);
        profileImage = findViewById(R.id.profile_image);
        defaultAvatarText = findViewById(R.id.default_avatar_text);
        signOutButton = findViewById(R.id.sign_out_button);
        settingsButton = findViewById(R.id.settings_button);
        calendarButton = findViewById(R.id.calendar);
        notesButton = findViewById(R.id.notes);
        tasksButton = findViewById(R.id.tasks);

        signOutButton.setOnClickListener(v -> User.signOut(this));
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        });

        calendarButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
            startActivity(intent);
        });

        notesButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), NotesActivity.class);
            startActivity(intent);
        });

        tasksButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), TasksPageActivity.class);
            startActivity(intent);
        });
    }
}