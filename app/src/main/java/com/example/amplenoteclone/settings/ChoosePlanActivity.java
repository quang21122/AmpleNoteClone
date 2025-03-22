package com.example.amplenoteclone.settings;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
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

//        RadioGroup radioGroup = findViewById(R.id.radio_group);
//        RadioButton monthlyBilling = findViewById(R.id.monthly_billing);
//        RadioButton annualBilling = findViewById(R.id.annual_billing);
        TextView subscriptionInfo = findViewById(R.id.subscription_info);

//        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
//            if (checkedId == R.id.monthly_billing) {
//                subscriptionInfo.setText("We'll charge 59.000 đ at the end of your free trial.");
//            } else if (checkedId == R.id.annual_billing) {
//                subscriptionInfo.setText("We'll charge 499.000 đ at the end of your free trial.");
//            }
//        });

        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> finish());

        // Set up continue button listener
        Button continueButton = findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String selectedPlan;
//                RadioButton monthlyBilling = findViewById(R.id.monthly_billing);
//                RadioButton annualBilling = findViewById(R.id.annual_billing);
//
//                if (monthlyBilling.isChecked()) {
//                    selectedPlan = "monthly";
//                } else if (annualBilling.isChecked()) {
//                    selectedPlan = "annual";
//                } else {
//                    return;
//                }

                ConfirmationDialogFragment dialog = ConfirmationDialogFragment.newInstance();
                dialog.show(getSupportFragmentManager(), "confirmation_dialog");
            }
        });
    }
}
