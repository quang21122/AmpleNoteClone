package com.example.amplenoteclone.settings;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.amplenoteclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class ConfirmationDialogFragment extends DialogFragment {

    private static final String ARG_PLAN = "selected_plan";

    public static ConfirmationDialogFragment newInstance() {
        ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLAN, "free_trial");
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirmation, container, false);

        // Lấy dữ liệu từ arguments
        String selectedPlan = getArguments() != null ? getArguments().getString(ARG_PLAN) : "monthly";

        // Cập nhật tiêu đề plan
        TextView planTitle = view.findViewById(R.id.plan_title);
        planTitle.setText(selectedPlan.equals("monthly") ? R.string.pro_plan_monthly : R.string.pro_plan_free_trial);

        // Cập nhật giá
        TextView priceTextView = view.findViewById(R.id.price);
        priceTextView.setText(selectedPlan.equals("monthly") ? getString(R.string.price_monthly) : getString(R.string.price_free_trial));

        // Cập nhật ngày bắt đầu
        TextView startDateTextView = view.findViewById(R.id.start_date);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        String startDate = dateFormat.format(calendar.getTime());
        startDateTextView.setText(startDate);

        // Initialize UI elements for email confirmation
        Button btnSubscribe = view.findViewById(R.id.subscribe_button);
        TextView tvUpgradeConfirmationMessage = view.findViewById(R.id.tv_confirmation_upgraded);
        Button btnSendUpgradeConfirmationCode = view.findViewById(R.id.btn_send_upgrade_confirmation_code);
        EditText etUpgradeConfirmationCode = view.findViewById(R.id.et_upgrade_confirmation_code);
        Button btnConfirmAccountUpgrade = view.findViewById(R.id.btn_confirm_account_upgrade);
        ProgressBar progressBar = view.findViewById(R.id.upgrade_progress_bar);

        btnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                v.setBackgroundColor(Color.GRAY);
                tvUpgradeConfirmationMessage.setVisibility(View.VISIBLE);
                btnSendUpgradeConfirmationCode.setVisibility(View.VISIBLE);
            }
        });

        // Send upgrade confirmation code button click listener
        btnSendUpgradeConfirmationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve current user's email from Firestore
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseFirestore.getInstance().collection("users").document(uid)
                        .get().addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String email = documentSnapshot.getString("email");
                                if (email != null) {
                                    // Generate a random 6-digit confirmation code
                                    String upgradeConfirmationCode = String.valueOf(100000 + (int)(Math.random() * 900000));

                                    // Save the code to Firestore (for later verification)
                                    FirebaseFirestore.getInstance().collection("userConfirmations")
                                            .document(uid)
                                            .set(new HashMap<String, Object>() {{
                                                put("upgradeConfirmationCode", upgradeConfirmationCode);
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
                                                        sender.sendMail("Your Upgrade Confirmation Code",
                                                                "Your confirmation code is: " + upgradeConfirmationCode,
                                                                gmailUser,
                                                                email);
                                                        getActivity().runOnUiThread(() ->
                                                                Toast.makeText(getActivity(),
                                                                        "Confirmation code sent to " + email,
                                                                        Toast.LENGTH_SHORT).show());
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        getActivity().runOnUiThread(() ->
                                                                Toast.makeText(getActivity(),
                                                                        "Failed to send confirmation code: " + e.getMessage(),
                                                                        Toast.LENGTH_SHORT).show());
                                                    }
                                                }).start();
                                            })
                                            .addOnFailureListener(e -> {
                                                e.printStackTrace();
                                                Toast.makeText(getActivity(),
                                                        "Failed to store confirmation code: " + e.getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    Toast.makeText(getActivity(), "User email not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(e -> {
                            e.printStackTrace();
                            Toast.makeText(getActivity(),
                                    "Failed to retrieve user email: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });

                // Update UI to show the confirmation code input
                btnSendUpgradeConfirmationCode.setVisibility(View.GONE);
                etUpgradeConfirmationCode.setVisibility(View.VISIBLE);
                btnConfirmAccountUpgrade.setVisibility(View.VISIBLE);
            }
        });

        // Confirm account upgrade button click listener
        btnConfirmAccountUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show loading indicator
                progressBar.setVisibility(View.VISIBLE);
                // Disable button to prevent multiple clicks if needed
                btnConfirmAccountUpgrade.setEnabled(false);

                // Retrieve the entered confirmation code from the EditText
                String enteredCode = etUpgradeConfirmationCode.getText().toString().trim();
                if (enteredCode.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter the confirmation code.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    btnConfirmAccountUpgrade.setEnabled(true);
                    return;
                }

                // Get the current user's UID
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // Retrieve the stored confirmation code from Firestore (userConfirmations collection)
                FirebaseFirestore.getInstance().collection("userConfirmations")
                        .document(uid)
                        .get().addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String storedCode = documentSnapshot.getString("upgradeConfirmationCode");
                                if (enteredCode.equals(storedCode)) {
                                    // Code matches, proceed with the plan upgrade
                                    // Update the user's plan in Firestore
                                    FirebaseFirestore.getInstance().collection("users")
                                            .document(uid)
                                            .update("isPremium", "true")
                                            .addOnSuccessListener(aVoid -> {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(getActivity(), "Plan upgraded successfully.", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                progressBar.setVisibility(View.GONE);
                                                btnConfirmAccountUpgrade.setEnabled(true);
                                                Toast.makeText(getActivity(), "Failed to upgrade plan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    btnConfirmAccountUpgrade.setEnabled(true);
                                    Toast.makeText(getActivity(), "Incorrect confirmation code.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                progressBar.setVisibility(View.GONE);
                                btnConfirmAccountUpgrade.setEnabled(true);
                                Toast.makeText(getActivity(), "Confirmation code not found. Please request a new code.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            btnConfirmAccountUpgrade.setEnabled(true);
                            Toast.makeText(getActivity(), "Error verifying confirmation code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            getDialog().getWindow().setGravity(Gravity.BOTTOM);
        }
    }
}