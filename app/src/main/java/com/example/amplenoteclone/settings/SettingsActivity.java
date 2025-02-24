package com.example.amplenoteclone.settings;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.amplenoteclone.settings.ChoosePlanActivity;
import com.example.amplenoteclone.R;

public class SettingsActivity extends AppCompatActivity {
    private final String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private final String[] noteWidths = {"Standard width", "Full width"};
    private final String[] taskCompletionEffects = {"Minimal", "Normal", "Dazzle"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        LinearLayout btnInviteFriends = findViewById(R.id.invite_friends_layout);
        Button btnChoosePlan = findViewById(R.id.btn_choose_plan);

        Button btnDeleteAccount = findViewById(R.id.btn_delete_account);
        TextView tvConfirmationMessage1 = findViewById(R.id.tv_confirmation_message_1);
        TextView tvConfirmationMessage2 = findViewById(R.id.tv_confirmation_message_2);
        Button btnSendConfirmationCode = findViewById(R.id.btn_send_confirmation_code);
        Button btnCancel = findViewById(R.id.btn_cancel);
        EditText etConfirmationCode = findViewById(R.id.et_confirmation_code);
        Button btnConfirmAccountDeletion = findViewById(R.id.btn_confirm_account_deletion);

        // Bắt sự kiện click
        btnInviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mở InviteFriendsActivity
                Intent intent = new Intent(SettingsActivity.this, InviteFriendsActivity.class);
                startActivity(intent);
            }
        });

        btnChoosePlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open ChoosePlanActivity
                Intent intent = new Intent(SettingsActivity.this, ChoosePlanActivity.class);
                startActivity(intent);
            }
        });

        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change background color to gray
                v.setBackgroundColor(Color.GRAY);

                // Show the confirmation messages and buttons
                tvConfirmationMessage1.setVisibility(View.VISIBLE);
                tvConfirmationMessage2.setVisibility(View.VISIBLE);
                btnSendConfirmationCode.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset background color of delete button
                btnDeleteAccount.setBackgroundColor(ContextCompat.getColor(SettingsActivity.this, R.color.gray));

                // Hide the confirmation messages and buttons
                tvConfirmationMessage1.setVisibility(View.GONE);
                tvConfirmationMessage2.setVisibility(View.GONE);
                btnSendConfirmationCode.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                etConfirmationCode.setVisibility(View.GONE);
                btnConfirmAccountDeletion.setVisibility(View.GONE);
            }
        });

        btnSendConfirmationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide the send confirmation code button
                btnSendConfirmationCode.setVisibility(View.GONE);

                // Show the edit text for entering the confirmation code
                etConfirmationCode.setVisibility(View.VISIBLE);

                // Show the confirm account deletion button
                btnConfirmAccountDeletion.setVisibility(View.VISIBLE);
            }
        });

        btnConfirmAccountDeletion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a toast message
                Toast.makeText(SettingsActivity.this, "Account deleted", Toast.LENGTH_SHORT).show();
            }
        });

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

}
