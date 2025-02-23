package com.example.amplenoteclone;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class InviteFriendsActivity extends AppCompatActivity {

    private LinearLayout emailContainer;
    private TextInputEditText emailEditText;
    private TextView invitationMessage;
    private LinearLayout invitedEmailsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);

        emailContainer = findViewById(R.id.emailContainer);
        emailEditText = findViewById(R.id.textInputEditText);
        TextView addAnotherEmail = findViewById(R.id.add_another_email);
        Button sendInvitation = findViewById(R.id.send_invitation);
        invitationMessage = findViewById(R.id.invitation_message);
        invitedEmailsContainer = findViewById(R.id.invited_emails_container);

        addAnotherEmail.setOnClickListener(v -> addEmailField());
        sendInvitation.setOnClickListener(v -> verifyAndSendEmail());

        ImageView menuIcon = findViewById(R.id.back_icon);
        menuIcon.setOnClickListener(v -> finish());
    }

    private void addEmailField() {
        // Create a new TextInputLayout with the same style
        TextInputLayout newTextInputLayout = new TextInputLayout(this);
        newTextInputLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        newTextInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        newTextInputLayout.setBoxStrokeColor(ContextCompat.getColor(this, R.color.lightBlue));
        newTextInputLayout.setHint("Email Address");
        newTextInputLayout.setBoxStrokeWidth(2);
        newTextInputLayout.setBoxStrokeWidthFocused(3);

        // Create a new TextInputEditText with the same style
        TextInputEditText newEditText = new TextInputEditText(this);
        newEditText.setLayoutParams(new TextInputLayout.LayoutParams(
                TextInputLayout.LayoutParams.MATCH_PARENT,
                TextInputLayout.LayoutParams.WRAP_CONTENT
        ));
        newEditText.setTextColor(ContextCompat.getColor(this, R.color.black));
        newEditText.setTextCursorDrawable(null);
        newEditText.setHighlightColor(ContextCompat.getColor(this, R.color.lightBlue));

        // Add EditText to TextInputLayout
        newTextInputLayout.addView(newEditText);

        // Add TextInputLayout to emailContainer
        emailContainer.addView(newTextInputLayout);
    }

    private void verifyAndSendEmail() {
        String email = emailEditText.getText().toString().trim();
        if (isValidEmail(email)) {
            // Display the invitation message
            invitationMessage.setText("You sent invitations to:");
            invitationMessage.setVisibility(View.VISIBLE);
            invitedEmailsContainer.setVisibility(View.VISIBLE);

            LinearLayout newInvitedEmailContainer = new LinearLayout(this);
            newInvitedEmailContainer.setOrientation(LinearLayout.HORIZONTAL);
            newInvitedEmailContainer.setGravity(Gravity.CENTER_VERTICAL); // Center vertically
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            containerParams.setMargins(0, 16, 0, 16); // Add margin between items
            newInvitedEmailContainer.setLayoutParams(containerParams);
            newInvitedEmailContainer.setPadding(24, 0, 24, 0); // Adjust padding

            newInvitedEmailContainer.setBackgroundColor(0xFFE8E8E8);

            // TextView
            TextView newInvitedEmail = new TextView(this);
            LinearLayout.LayoutParams emailParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            );
            newInvitedEmail.setLayoutParams(emailParams);
            newInvitedEmail.setText(email);
            newInvitedEmail.setTextSize(16);
            newInvitedEmail.setTextColor(ContextCompat.getColor(this, R.color.black));

            ImageView newSuccessIcon = new ImageView(this);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                    36,
                    36
            );
            newSuccessIcon.setLayoutParams(iconParams);
            newSuccessIcon.setImageResource(R.drawable.ic_success);
            newSuccessIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            // Add the TextView and ImageView to the new LinearLayout
            newInvitedEmailContainer.addView(newInvitedEmail);
            newInvitedEmailContainer.addView(newSuccessIcon);

            // Add the new LinearLayout to the invitedEmailsContainer
            invitedEmailsContainer.addView(newInvitedEmailContainer);
        } else {
            // Show error message
            emailEditText.setError("Invalid email address");
        }
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}