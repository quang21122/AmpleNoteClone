package com.example.amplenoteclone.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
