package com.example.amplenoteclone.settings;

import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.amplenoteclone.R;

public class ConfirmationDialogFragment extends DialogFragment {

    private static final String ARG_PLAN = "selected_plan";

    public static ConfirmationDialogFragment newInstance(String selectedPlan) {
        ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLAN, selectedPlan);
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
        planTitle.setText(selectedPlan.equals("monthly") ? R.string.pro_plan_monthly : R.string.pro_plan_annual);

        // Cập nhật giá
        TextView priceTextView = view.findViewById(R.id.price);
        priceTextView.setText(selectedPlan.equals("monthly") ? getString(R.string.price_monthly) : getString(R.string.price_annual));

        // Cập nhật ngày bắt đầu
        TextView startDateTextView = view.findViewById(R.id.start_date);
        startDateTextView.setText(getString(R.string.start_date_default));

        // Cập nhật điều khoản
        TextView termsTextView = view.findViewById(R.id.terms);
        termsTextView.setText(Html.fromHtml(getString(R.string.terms)));

        // Cập nhật thanh toán thay thế
        TextView alternativePaymentTextView = view.findViewById(R.id.alternative_payment);
        alternativePaymentTextView.setText(getString(R.string.alternative_payment));

        // Cập nhật tùy chọn thêm thẻ
        TextView addCardTextView = view.findViewById(R.id.add_card);
        addCardTextView.setText(getString(R.string.add_card));

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