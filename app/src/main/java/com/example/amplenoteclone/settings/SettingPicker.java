package com.example.amplenoteclone.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.amplenoteclone.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class SettingPicker {
    private final Context context;
    private final String title;
    private final String[] options;
    private final String prefKey;
    private final SharedPreferences preferences;
    private final TextView textView;

    public SettingPicker(Context context, String title, String[] options, String prefKey, TextView textView) {
        this.context = context;
        this.title = title;
        this.options = options;
        this.prefKey = prefKey;
        this.textView = textView;
        this.preferences = context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);

        // Load saved value
        String savedValue = preferences.getString(prefKey, options[0]);
        textView.setText(savedValue);

        // Change from getRootView() to textView itself
        textView.setOnClickListener(v -> showPickerDialog());
    }


    public void showPickerDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_day_picker, null);
        bottomSheetDialog.setContentView(sheetView);

        ImageView btnCloseDialog = sheetView.findViewById(R.id.btnCloseDialog);
        ListView listView = sheetView.findViewById(R.id.listViewDays);

        btnCloseDialog.setOnClickListener(v -> bottomSheetDialog.dismiss());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.item_day_picker, options) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_day_picker, parent, false);
                }

                TextView textView = convertView.findViewById(R.id.textViewDay);
                ImageView checkIcon = convertView.findViewById(R.id.imageViewCheck);

                textView.setText(options[position]);

                // Show checkmark if selected
                if (options[position].equals(preferences.getString(prefKey, options[0]))) {
                    checkIcon.setVisibility(View.VISIBLE);
                } else {
                    checkIcon.setVisibility(View.GONE);
                }

                return convertView;
            }
        };

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedOption = options[position];
            textView.setText(selectedOption);

            // Save selection
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(prefKey, selectedOption);
            editor.apply();

            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }
}
