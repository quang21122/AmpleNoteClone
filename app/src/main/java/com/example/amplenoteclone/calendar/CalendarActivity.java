package com.example.amplenoteclone.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.amplenoteclone.NotesActivity;
import com.example.amplenoteclone.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private Date currentSelectedDate = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setSelectedItemId(R.id.action_calendar);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.action_notes) {
                Intent intent = new Intent(CalendarActivity.this, NotesActivity.class);
                startActivity(intent);
                return true;
            }
            // Do nothing, already on Calendar page
            return item.getItemId() == R.id.action_calendar;
        });

        initializeViews();
        setupCalendarBottomSheet();
        setupListeners();
        updateTabState(true);
        updateCurrentDate(System.currentTimeMillis());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_calendar, menu);
        return true;
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

        setupTabsBottom(bottomSheetView, bottomSheetDialog);

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
                updateSelectedDate(selectedDate); // Use new method here
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
            updateSelectedDate(today.getTime()); // Use new method here
            updateCalendarGrid(calendarGrid, monthYearText);
            updateGoToTodayButtonVisibility(goToTodayButton);
            bottomSheetDialog.dismiss();
        });

        calendarPickerButton.setOnClickListener(v -> bottomSheetDialog.show());
    }

    private void setupTabsBottom(View bottomSheetView, BottomSheetDialog dialog) {
        TextView oneDay = bottomSheetView.findViewById(R.id.tab_one_day);
        TextView threeDays = bottomSheetView.findViewById(R.id.tab_three_days);
        TextView week = bottomSheetView.findViewById(R.id.tab_week);
        TextView month = bottomSheetView.findViewById(R.id.tab_month);

        TextView[] tabs = {oneDay, threeDays, week, month};

        // Initialize with separate OneDayFragment instances
        Fragment initialWorkFragment = new OneDayFragment();
        Fragment initialPersonalFragment = new OneDayFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.work_content_container, initialWorkFragment)
                .commit();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.personal_content_container, initialPersonalFragment)
                .commit();

        // Set initial colors and background
        oneDay.setTextColor(getResources().getColor(android.R.color.black));
        oneDay.setBackgroundResource(R.color.light_gray);
        for (int i = 1; i < tabs.length; i++) {
            tabs[i].setTextColor(getResources().getColor(android.R.color.darker_gray));
            tabs[i].setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }

        View.OnClickListener tabClickListener = v -> {
            // Reset all tabs
            for (TextView tab : tabs) {
                tab.setTextColor(getResources().getColor(android.R.color.darker_gray));
                tab.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }

            // Update selected tab
            TextView selectedTab = (TextView) v;
            selectedTab.setTextColor(getResources().getColor(android.R.color.black));
            selectedTab.setBackgroundResource(R.color.light_gray);

            // Create new fragments based on selected tab
            Fragment newWorkFragment;
            Fragment newPersonalFragment;

            int id = v.getId();
            if (id == R.id.tab_one_day) {
                newWorkFragment = new OneDayFragment();
                newPersonalFragment = new OneDayFragment();
            } else if (id == R.id.tab_three_days) {
                newWorkFragment = new ThreeDaysFragment();
                newPersonalFragment = new ThreeDaysFragment();
            } else if (id == R.id.tab_week) {
                newWorkFragment = new WeekFragment();
                newPersonalFragment = new WeekFragment();
            } else {
                newWorkFragment = new MonthFragment();
                newPersonalFragment = new MonthFragment();
            }

            // Replace fragments in both containers
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.work_content_container, newWorkFragment)
                    .commit();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.personal_content_container, newPersonalFragment)
                    .commit();

            // Execute transactions immediately
            getSupportFragmentManager().executePendingTransactions();

            // Update selected date for both fragments
            if (newWorkFragment instanceof DateSelectable) {
                ((DateSelectable) newWorkFragment).setSelectedDate(currentSelectedDate);
            }
            if (newPersonalFragment instanceof DateSelectable) {
                ((DateSelectable) newPersonalFragment).setSelectedDate(currentSelectedDate);
            }

            dialog.dismiss();
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

        // Update tab UI
        workText.setTextColor(getResources().getColor(
                selectWork ? R.color.tab_selected : R.color.tab_unselected));
        personalText.setTextColor(getResources().getColor(
                selectWork ? R.color.tab_unselected : R.color.tab_selected));
        workIndicator.setVisibility(selectWork ? View.VISIBLE : View.INVISIBLE);
        personalIndicator.setVisibility(selectWork ? View.INVISIBLE : View.VISIBLE);

        // Show/hide containers first
        findViewById(R.id.work_content_container).setVisibility(
                selectWork ? View.VISIBLE : View.GONE);
        findViewById(R.id.personal_content_container).setVisibility(
                selectWork ? View.GONE : View.VISIBLE);

        // Get or create fragments
        Fragment workFragment = getSupportFragmentManager().findFragmentById(R.id.work_content_container);
        Fragment personalFragment = getSupportFragmentManager().findFragmentById(R.id.personal_content_container);

        // Create fragments if needed
        if (workFragment == null) {
            workFragment = new OneDayFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.work_content_container, workFragment)
                    .commit();
            getSupportFragmentManager().executePendingTransactions();
        }

        if (personalFragment == null) {
            personalFragment = new OneDayFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.personal_content_container, personalFragment)
                    .commit();
            getSupportFragmentManager().executePendingTransactions();
        }

        // Force refresh both fragments
        if (workFragment instanceof OneDayFragment) {
            ((DateSelectable) workFragment).setSelectedDate(currentSelectedDate);
        }
        if (personalFragment instanceof OneDayFragment) {
            ((DateSelectable) personalFragment).setSelectedDate(currentSelectedDate);
        }

        // Update current fragment view
        Fragment currentFragment = selectWork ? workFragment : personalFragment;
        if (currentFragment instanceof OneDayFragment) {
            // Delay để đảm bảo view đã được inflate
            new Handler().post(() -> {
                ((DateSelectable) currentFragment).setSelectedDate(currentSelectedDate);
            });
        }
    }

    private void updateSelectedDate(Date date) {
        currentSelectedDate = date;
        updateCurrentDate(date.getTime());

        // Update both fragments
        updateFragmentDate(R.id.work_content_container, date);
        updateFragmentDate(R.id.personal_content_container, date);
    }

    private void updateFragmentDate(int containerId, Date date) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(containerId);
        if (fragment instanceof DateSelectable) {
            ((DateSelectable) fragment).setSelectedDate(date);
        }
    }
}