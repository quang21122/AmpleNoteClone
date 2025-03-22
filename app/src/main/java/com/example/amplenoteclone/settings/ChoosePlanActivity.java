package com.example.amplenoteclone.settings;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import com.example.amplenoteclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChoosePlanActivity extends AppCompatActivity {  // Extend AppCompatActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_plan);

        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // UI elements
        TextView subscriptionInfo = findViewById(R.id.subscription_info);
        Button continueButton = findViewById(R.id.continue_button);

        // Retrieve user's plan status
        updatePlanStatus(db, uid, subscriptionInfo, continueButton);

        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> finish());

        // Set up continue button listener
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmationDialogFragment dialog = ConfirmationDialogFragment.newInstance();
                dialog.show(getSupportFragmentManager(), "confirmation_dialog");
            }
        });

        // Set up FragmentResultListener
        getSupportFragmentManager().setFragmentResultListener("requestKey", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                // Refresh the plan status
                updatePlanStatus(db, uid, subscriptionInfo, continueButton);
            }
        });
    }

    private void updatePlanStatus(FirebaseFirestore db, String uid, TextView subscriptionInfo, Button continueButton) {
        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Object isPremiumObj = document.get("isPremium");
                    boolean isPremium = false;
                    if (isPremiumObj instanceof Boolean) {
                        isPremium = (Boolean) isPremiumObj;
                    } else if (isPremiumObj instanceof String) {
                        isPremium = Boolean.parseBoolean((String) isPremiumObj);
                    }

                    if (isPremium) {
                        subscriptionInfo.setText("You are currently subscribed to the Pro Plan.");
                        continueButton.setText("Manage Subscription");
                    } else {
                        subscriptionInfo.setText("We'll charge 59000 đ at the end of your free trial.");
                        continueButton.setText("Continue");
                    }
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}