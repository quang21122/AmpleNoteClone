package com.example.amplenoteclone.tasks;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.amplenoteclone.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ScheduleTaskBottomSheet extends BottomSheetDialogFragment {

    private LinearLayout layoutToday, layoutTomorrow, layoutOneWeek, layoutThreeWeeks, layoutChooseDateTime;
    private TextView textViewTodayDate, textViewTomorrowDate, textViewOneWeekDate, textViewThreeWeeksDate;
    private ImageView imageViewClose;
    private OnScheduleSelectedListener onScheduleSelectedListener;

    // Interface để trả về thời gian đã chọn
    public interface OnScheduleSelectedListener {
        void onScheduleSelected(String date, String time, String period);
    }

    public ScheduleTaskBottomSheet(OnScheduleSelectedListener listener) {
        this.onScheduleSelectedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_task_schedule, container, false);

        // Khởi tạo các view
        layoutToday = view.findViewById(R.id.layout_today);
        layoutTomorrow = view.findViewById(R.id.layout_tomorrow);
        layoutOneWeek = view.findViewById(R.id.layout_one_week);
        layoutThreeWeeks = view.findViewById(R.id.layout_three_weeks);
        layoutChooseDateTime = view.findViewById(R.id.layout_choose_date_time);
        textViewTodayDate = view.findViewById(R.id.text_view_today_date);
        textViewTomorrowDate = view.findViewById(R.id.text_view_tomorrow_date);
        textViewOneWeekDate = view.findViewById(R.id.text_view_one_week_date);
        textViewThreeWeeksDate = view.findViewById(R.id.text_view_three_weeks_date);
        imageViewClose = view.findViewById(R.id.image_view_close);

        // Thiết lập ngày và giờ cho các tùy chọn
        setupDates();

        // Xử lý sự kiện nhấn nút đóng
        imageViewClose.setOnClickListener(v -> dismiss());

        // Xử lý sự kiện nhấn các tùy chọn
        layoutToday.setOnClickListener(v -> {
            String date = getDateString(0);
            String time = "9:00 am";
            String period = getPeriodFromHour(9); // 9:00 AM
            onScheduleSelectedListener.onScheduleSelected(date, time, period);
            dismiss();
        });

        layoutTomorrow.setOnClickListener(v -> {
            String date = getDateString(1);
            String time = "9:00 am";
            String period = getPeriodFromHour(9); // 9:00 AM
            onScheduleSelectedListener.onScheduleSelected(date, time, period);
            dismiss();
        });

        layoutOneWeek.setOnClickListener(v -> {
            String date = getDateString(7);
            String time = "9:00 am";
            String period = getPeriodFromHour(9); // 9:00 AM
            onScheduleSelectedListener.onScheduleSelected(date, time, period);
            dismiss();
        });

        layoutThreeWeeks.setOnClickListener(v -> {
            String date = getDateString(21);
            String time = "9:00 am";
            String period = getPeriodFromHour(9); // 9:00 AM
            onScheduleSelectedListener.onScheduleSelected(date, time, period);
            dismiss();
        });

        layoutChooseDateTime.setOnClickListener(v -> showDateTimePicker());

        return view;
    }

    private void setupDates() {
        // Today
        textViewTodayDate.setText("Today, " + getDateString(0) + " at 9:00AM");

        // Tomorrow
        textViewTomorrowDate.setText("Tomorrow, " + getDateString(1) + " at 9:00AM");

        // One week
        textViewOneWeekDate.setText(getDateString(7) + " at 9:00AM");

        // Three weeks
        textViewThreeWeeksDate.setText(getDateString(21) + " at 9:00AM");
    }

    private String getDateString(int daysToAdd) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    private String getPeriodFromHour(int hourOfDay) {
        if (hourOfDay >= 0 && hourOfDay < 12) {
            return "Morning";
        } else if (hourOfDay >= 12 && hourOfDay < 18) {
            return "Afternoon";
        } else {
            return "Evening";
        }
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year1, month1, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, month1, dayOfMonth);

                    // Sau khi chọn ngày, hiển thị TimePickerDialog
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            getContext(),
                            (view1, hourOfDay, minute1) -> {
                                Calendar selectedTime = Calendar.getInstance();
                                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedTime.set(Calendar.MINUTE, minute1);

                                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
                                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                                String date = dateFormat.format(selectedDate.getTime());
                                String time = timeFormat.format(selectedTime.getTime()).toLowerCase();
                                String period = getPeriodFromHour(hourOfDay); // Sử dụng phương thức mới

                                onScheduleSelectedListener.onScheduleSelected(date, time, period);
                                dismiss();
                            },
                            hour, minute, false
                    );
                    timePickerDialog.show();
                },
                year, month, day
        );
        datePickerDialog.show();
    }
}