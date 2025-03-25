package com.example.amplenoteclone.settings;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.amplenoteclone.DrawerActivity;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.authentication.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SettingsActivity extends DrawerActivity {
    private final String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private final String[] noteWidths = {"Standard width", "Full width"};
    private final String[] taskCompletionEffects = {"Minimal", "Normal", "Dazzle"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent(R.layout.activity_settings);

        setBottomNavigationVisibility(false);

        ProgressBar progressBar = findViewById(R.id.progress_bar);

        LinearLayout btnInviteFriends = findViewById(R.id.invite_friends_layout);
        Button btnChoosePlan = findViewById(R.id.btn_choose_plan);

        Button btnDeleteAccount = findViewById(R.id.btn_delete_account);
        TextView tvConfirmationMessage1 = findViewById(R.id.tv_confirmation_message_1);
        TextView tvConfirmationMessage2 = findViewById(R.id.tv_confirmation_message_2);
        Button btnSendConfirmationCode = findViewById(R.id.btn_send_confirmation_code);
        Button btnCancel = findViewById(R.id.btn_cancel);
        EditText etConfirmationCode = findViewById(R.id.et_confirmation_code);
        Button btnConfirmAccountDeletion = findViewById(R.id.btn_confirm_account_deletion);

        // Invite friends button
        btnInviteFriends.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, InviteFriendsActivity.class);
            startActivity(intent);
        });

        // Choose plan button
        btnChoosePlan.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ChoosePlanActivity.class);
            startActivity(intent);
        });

        // Delete account button click listener
        btnDeleteAccount.setOnClickListener(v -> {
            v.setBackgroundColor(Color.GRAY);
            tvConfirmationMessage1.setVisibility(View.VISIBLE);
            tvConfirmationMessage2.setVisibility(View.VISIBLE);
            btnSendConfirmationCode.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        });

        // Cancel deletion process
        btnCancel.setOnClickListener(v -> {
            btnDeleteAccount.setBackgroundColor(ContextCompat.getColor(SettingsActivity.this, R.color.gray));
            tvConfirmationMessage1.setVisibility(View.GONE);
            tvConfirmationMessage2.setVisibility(View.GONE);
            btnSendConfirmationCode.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
            etConfirmationCode.setVisibility(View.GONE);
            btnConfirmAccountDeletion.setVisibility(View.GONE);
        });

        // Send confirmation code button click listener
        btnSendConfirmationCode.setOnClickListener(v -> {
            // Retrieve current user's email from Firestore
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore.getInstance().collection("users").document(uid)
                    .get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String email = documentSnapshot.getString("email");
                            if (email != null) {
                                // Generate a random 6-digit confirmation code
                                String deleteConfirmationCode = String.valueOf(100000 + (int) (Math.random() * 900000));

                                // Save the code to Firestore (for later verification)
                                FirebaseFirestore.getInstance().collection("userConfirmations")
                                        .document(uid)
                                        .set(new HashMap<String, Object>() {{
                                            put("deleteConfirmationCode", deleteConfirmationCode);
                                            put("createdAt", com.google.firebase.Timestamp.now());
                                        }})
                                        .addOnSuccessListener(aVoid -> {
                                            // Once saved, send the email using our helper class on a background thread
                                            new Thread(() -> {
                                                try {
                                                    // Use your Gmail account credentials (remember: use an App Password if using 2FA)
                                                    final String gmailUser = "duongngo164@gmail.com";
                                                    final String gmailPassword = "agyc ioea qaai cubv";

                                                    // Ensure you're using the correct package path for GMailSender if you moved it
                                                    com.example.amplenoteclone.settings.GMailSender sender =
                                                            new com.example.amplenoteclone.settings.GMailSender(gmailUser, gmailPassword);
                                                    sender.sendMail("Your Confirmation Code",
                                                            "Your confirmation code is: " + deleteConfirmationCode,
                                                            gmailUser,
                                                            email);
                                                    runOnUiThread(() ->
                                                            Toast.makeText(SettingsActivity.this,
                                                                    "Confirmation code sent to " + email,
                                                                    Toast.LENGTH_SHORT).show());
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    runOnUiThread(() ->
                                                            Toast.makeText(SettingsActivity.this,
                                                                    "Failed to send confirmation code: " + e.getMessage(),
                                                                    Toast.LENGTH_SHORT).show());
                                                }
                                            }).start();
                                        })
                                        .addOnFailureListener(e -> {
                                            e.printStackTrace();
                                            Toast.makeText(SettingsActivity.this,
                                                    "Failed to store confirmation code: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(SettingsActivity.this, "User email not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(e -> {
                        e.printStackTrace();
                        Toast.makeText(SettingsActivity.this,
                                "Failed to retrieve user email: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });

            // Update UI to show the confirmation code input
            btnSendConfirmationCode.setVisibility(View.GONE);
            etConfirmationCode.setVisibility(View.VISIBLE);
            btnConfirmAccountDeletion.setVisibility(View.VISIBLE);
        });

        // Confirm account deletion button click listener
        btnConfirmAccountDeletion.setOnClickListener(v -> {
            // Show loading indicator
            progressBar.setVisibility(View.VISIBLE);
            // Disable button to prevent multiple clicks if needed
            btnConfirmAccountDeletion.setEnabled(false);

            // Retrieve the entered confirmation code from the EditText
            String enteredCode = etConfirmationCode.getText().toString().trim();
            if (enteredCode.isEmpty()) {
                Toast.makeText(SettingsActivity.this, "Please enter the confirmation code.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                btnConfirmAccountDeletion.setEnabled(true);
                return;
            }

            // Get the current user's UID
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Retrieve the stored confirmation code from Firestore (userConfirmations collection)
            FirebaseFirestore.getInstance().collection("userConfirmations")
                    .document(uid)
                    .get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String storedCode = documentSnapshot.getString("deleteConfirmationCode");
                            if (enteredCode.equals(storedCode)) {
                                // Code matches, delete the user document from "users" collection
                                FirebaseFirestore.getInstance().collection("users")
                                        .document(uid)
                                        .delete().addOnSuccessListener(aVoid -> {
                                            // Optionally, remove the confirmation document as well
                                            FirebaseFirestore.getInstance().collection("userConfirmations")
                                                    .document(uid).delete();

                                            // Delete the Firebase Authentication account
                                            FirebaseAuth.getInstance().getCurrentUser().delete()
                                                    .addOnSuccessListener(unused -> {
                                                        progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(SettingsActivity.this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                                                        // Sign out and navigate to login or splash screen
                                                        FirebaseAuth.getInstance().signOut();
                                                        startActivity(new Intent(SettingsActivity.this, Login.class));
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        progressBar.setVisibility(View.GONE);
                                                        btnConfirmAccountDeletion.setEnabled(true);
                                                        Toast.makeText(SettingsActivity.this, "Failed to delete Firebase Authentication account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        }).addOnFailureListener(e -> {
                                            progressBar.setVisibility(View.GONE);
                                            btnConfirmAccountDeletion.setEnabled(true);
                                            Toast.makeText(SettingsActivity.this, "Failed to delete account from Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                progressBar.setVisibility(View.GONE);
                                btnConfirmAccountDeletion.setEnabled(true);
                                Toast.makeText(SettingsActivity.this, "Incorrect confirmation code.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            btnConfirmAccountDeletion.setEnabled(true);
                            Toast.makeText(SettingsActivity.this, "Confirmation code not found. Please request a new code.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        btnConfirmAccountDeletion.setEnabled(true);
                        Toast.makeText(SettingsActivity.this, "Error verifying confirmation code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // Additional settings pickers (unchanged)
        TextView tvSelectedDay = findViewById(R.id.tv_selected_day);
        TextView tvSelectedNoteWidth = findViewById(R.id.tv_selected_note_width);
        TextView tvSelectedTaskCompletionEffect = findViewById(R.id.tv_selected_task_completion_effect);
        LinearLayout dayOfWeekLayout = findViewById(R.id.day_of_week_layout);
        LinearLayout noteWidthLayout = findViewById(R.id.note_width_layout);
        LinearLayout taskCompletionEffectLayout = findViewById(R.id.task_completion_effect_layout);

        SettingPicker settingPicker = new SettingPicker(this, "First Day of Week", daysOfWeek, "FirstDayOfWeek", tvSelectedDay);
        SettingPicker settingPicker2 = new SettingPicker(this, "Note Width", noteWidths, "NoteWidth", tvSelectedNoteWidth);
        SettingPicker settingPicker3 = new SettingPicker(this, "Task Completion Effect", taskCompletionEffects, "TaskCompletionEffect", tvSelectedTaskCompletionEffect);

        dayOfWeekLayout.setOnClickListener(v -> settingPicker.showPickerDialog());
        noteWidthLayout.setOnClickListener(v -> settingPicker2.showPickerDialog());
        taskCompletionEffectLayout.setOnClickListener(v -> settingPicker3.showPickerDialog());
    }

    @Override
    protected String getToolbarTitle() {
        return "Settings";
    }

    @Override
    protected int getCurrentPageId() {
        return -1;
    }
}