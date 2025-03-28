package com.example.amplenoteclone;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.amplenoteclone.authentication.Login;
import com.example.amplenoteclone.note.NotesActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        new Handler().postDelayed(() -> {
            Intent intent;
            if (currentUser != null) {
                intent = new Intent(SplashActivity.this, NotesActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, Login.class);
            }
            startActivity(intent);
            finish();
        }, 1000);
    }
}