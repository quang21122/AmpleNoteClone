package com.example.amplenoteclone.calendar;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.amplenoteclone.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;

public class CalendarActivity extends AppCompatActivity {
    private LinearLayout workTab;
    private LinearLayout personalTab;
    private TextView workText;
    private TextView personalText;
    private View workIndicator;
    private View personalIndicator;
    private boolean isWorkSelected = true;

    private TextView currentDateText;
    private ImageButton calendarPickerButton;
    private CustomCalendarAdapter adapter;
    private java.util.Calendar currentCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        initializeViews();
        setupCalendarBottomSheet();
        setupListeners();
        updateTabState(true);
        updateCurrentDate(System.currentTimeMillis());
    }

    private void initializeViews() {
        workTab = findViewById(R.id.work_tab);
        personalTab = findViewById(R.id.personal_tab);
        workText = findViewById(R.id.work_text);
        personalText = findViewById(R.id.personal_text);
        workIndicator = findViewById(R.id.work_indicator);
        personalIndicator = findViewById(R.id.personal_indicator);
        currentDateText = findViewById(R.id.current_date);
        calendarPickerButton = findViewById(R.id.calendar_picker_button);
        currentCalendar = java.util.Calendar.getInstance();
    }

    private void setupListeners() {
        workTab.setOnClickListener(v -> {
            if (!isWorkSelected) {
                updateTabState(true);
            }
        });

        personalTab.setOnClickListener(v -> {
            if (isWorkSelected) {
                updateTabState(false);
            }
        });

    }

    private void setupCalendarBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_calendar, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        setupTabs(bottomSheetView);

        // Set height to 90% of screen height
        View parentView = (View) bottomSheetView.getParent();
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parentView);
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int dialogHeight = (int) (screenHeight * 0.95);

        bottomSheetView.setMinimumHeight(dialogHeight);
        behavior.setPeekHeight(dialogHeight);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        GridView calendarGrid = bottomSheetView.findViewById(R.id.calendarGrid);
        TextView monthYearText = bottomSheetView.findViewById(R.id.monthYearText);
        ImageButton prevButton = bottomSheetView.findViewById(R.id.previousButton);
        ImageButton nextButton = bottomSheetView.findViewById(R.id.nextButton);
        Button goToTodayButton = bottomSheetView.findViewById(R.id.goToTodayButton);
        ImageButton closeButton = bottomSheetView.findViewById(R.id.closeButton);

        closeButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        // First initialize the calendar grid
        updateCalendarGrid(calendarGrid, monthYearText);

        // Then update button visibility
        updateGoToTodayButtonVisibility(goToTodayButton);

        calendarGrid.setOnItemClickListener((parent, view, position, id) -> {
            Date selectedDate = (Date) adapter.getItem(position);
            if (selectedDate != null) {
                adapter.setSelectedDate(selectedDate);
                updateCurrentDate(selectedDate.getTime());
                updateGoToTodayButtonVisibility(goToTodayButton);
                bottomSheetDialog.dismiss();
            }
        });

        prevButton.setOnClickListener(v -> {
            currentCalendar.add(java.util.Calendar.MONTH, -1);
            updateCalendarGrid(calendarGrid, monthYearText);
            updateGoToTodayButtonVisibility(goToTodayButton);
        });

        nextButton.setOnClickListener(v -> {
            currentCalendar.add(java.util.Calendar.MONTH, 1);
            updateCalendarGrid(calendarGrid, monthYearText);
            updateGoToTodayButtonVisibility(goToTodayButton);
        });

        goToTodayButton.setOnClickListener(v -> {
            Calendar today = Calendar.getInstance();
            currentCalendar = today;
            adapter.setSelectedDate(today.getTime());
            updateCurrentDate(today.getTimeInMillis());
            updateCalendarGrid(calendarGrid, monthYearText);
            updateGoToTodayButtonVisibility(goToTodayButton);
            bottomSheetDialog.dismiss();
        });

        calendarPickerButton.setOnClickListener(v -> bottomSheetDialog.show());
    }

    private void setupTabs(View bottomSheetView) {
        TextView oneDay = bottomSheetView.findViewById(R.id.tab_one_day);
        TextView threeDays = bottomSheetView.findViewById(R.id.tab_three_days);
        TextView week = bottomSheetView.findViewById(R.id.tab_week);
        TextView month = bottomSheetView.findViewById(R.id.tab_month);

        TextView[] tabs = {oneDay, threeDays, week, month};

        // Set initial state
        oneDay.setSelected(true);

        View.OnClickListener tabClickListener = v -> {
            for (TextView tab : tabs) {
                boolean isSelected = tab == v;
                tab.setSelected(isSelected);
            }
        };

        for (TextView tab : tabs) {
            tab.setOnClickListener(tabClickListener);
        }
    }

    private void updateGoToTodayButtonVisibility(Button goToTodayButton) {
        if (adapter == null) return;

        Calendar today = Calendar.getInstance();
        Date selectedDate = adapter.getSelectedDate();

        if (selectedDate == null) {
            goToTodayButton.setVisibility(View.VISIBLE);
            return;
        }

        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTime(selectedDate);

        boolean isToday = isSameDay(today, selectedCal);
        goToTodayButton.setVisibility(isToday ? View.GONE : View.VISIBLE);
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private void updateCalendarGrid(GridView calendarGrid, TextView monthYearText) {
        List<Date> dates = new ArrayList<>();

        // Get start of month
        java.util.Calendar calendar = (java.util.Calendar) currentCalendar.clone();
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1;

        // Add empty spaces for days before start of month
        for (int i = 0; i < firstDayOfWeek; i++) {
            dates.add(null);
        }

        // Add days of month
        while (calendar.get(java.util.Calendar.MONTH) == currentCalendar.get(java.util.Calendar.MONTH)) {
            dates.add(calendar.getTime());
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }

        // Add empty spaces to complete 6 rows (42 cells)
        while (dates.size() < 42) {
            dates.add(null);
        }

        adapter = new CustomCalendarAdapter(this, dates);
        calendarGrid.setAdapter(adapter);

        // Select current date if it's in current month, otherwise select first day of month
        Calendar today = Calendar.getInstance();
        if (currentCalendar.get(java.util.Calendar.MONTH) == today.get(java.util.Calendar.MONTH) &&
                currentCalendar.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR)) {
            adapter.setSelectedDate(today.getTime());
        } else {
            // Select first valid date of current month
            for (Date date : dates) {
                if (date != null) {
                    adapter.setSelectedDate(date);
                    break;
                }
            }
        }

        // Set month year text
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        monthYearText.setText(formatter.format(currentCalendar.getTime()));
    }

    private void updateCurrentDate(long timeInMillis) {
        try {
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

            Date date = new Date(timeInMillis);
            String dayOfWeek = dayFormat.format(date);
            String dateWithoutYear = dateFormat.format(date);
            String year = yearFormat.format(date);

            android.text.SpannableStringBuilder builder = new android.text.SpannableStringBuilder();

            builder.append(dayOfWeek + "\n");
            builder.setSpan(new android.text.style.AbsoluteSizeSpan(14, true),
                    0, dayOfWeek.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            int dateStart = builder.length();
            builder.append(dateWithoutYear);
            builder.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    dateStart, builder.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new android.text.style.AbsoluteSizeSpan(20, true),
                    dateStart, builder.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            builder.append(", " + year);
            builder.setSpan(new android.text.style.AbsoluteSizeSpan(20, true),
                    builder.length() - year.length(), builder.length(),
                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            currentDateText.setText(builder);
        } catch (Exception e) {
            currentDateText.setText(new SimpleDateFormat("EEEE\nMMM d, yyyy", Locale.getDefault())
                    .format(new Date()));
        }
    }

    private void updateTabState(boolean selectWork) {
        isWorkSelected = selectWork;

        // Update text colors
        workText.setTextColor(getResources().getColor(
                selectWork ? R.color.tab_selected : R.color.tab_unselected));
        personalText.setTextColor(getResources().getColor(
                selectWork ? R.color.tab_unselected : R.color.tab_selected));

        // Update indicators
        workIndicator.setVisibility(selectWork ? View.VISIBLE : View.INVISIBLE);
        personalIndicator.setVisibility(selectWork ? View.INVISIBLE : View.VISIBLE);

        // You can update content here based on selection
        updateContent(selectWork);
    }

    private void updateContent(boolean isWork) {
        // Implement your content switching logic here
        // For example, you could show different calendar views
        // or load different data based on the selection
    }
}