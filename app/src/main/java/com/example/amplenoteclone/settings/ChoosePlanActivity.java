package com.example.amplenoteclone.settings;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.amplenoteclone.R;

public class ChoosePlanActivity extends AppCompatActivity {  // Extend AppCompatActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_plan); // Ensure you have the correct XML layout file

        RadioGroup radioGroup = findViewById(R.id.radio_group);
        RadioButton monthlyBilling = findViewById(R.id.monthly_billing);
        RadioButton annualBilling = findViewById(R.id.annual_billing);
        TextView subscriptionInfo = findViewById(R.id.subscription_info);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.monthly_billing) {
                subscriptionInfo.setText("We'll charge 59.000 đ at the end of your free trial.");
            } else if (checkedId == R.id.annual_billing) {
                subscriptionInfo.setText("We'll charge 499.000 đ at the end of your free trial.");
            }
        });

        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> finish());
    }
}
