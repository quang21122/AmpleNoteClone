package com.example.amplenoteclone.calendar;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.DrawerActivity;
import com.example.amplenoteclone.R;

import com.example.amplenoteclone.models.Task;
import com.example.amplenoteclone.tasks.TasksPageActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class CalendarActivity extends DrawerActivity {
    private TextView currentDateText;
    private ImageButton calendarPickerButton;
    private CustomCalendarAdapter adapter;
    private java.util.Calendar currentCalendar;
    private Date currentSelectedDate = new Date();
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Must have methods for initializing the activity
        super.onCreate(savedInstanceState);
        setActivityContent(R.layout.activity_calendar);
        initializeViews();
        setupCalendarBottomSheet();
        setupAddTaskButton();
        updateCurrentDate(System.currentTimeMillis());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_calendar, menu);
        return true;
    }

    public int getCurrentPageId() {
        return R.id.action_calendar;
    }
    public String getToolbarTitle() {
        return "Calendar";
    }

    private void initializeViews() {
        currentDateText = findViewById(R.id.current_date);
        calendarPickerButton = findViewById(R.id.calendar_picker_button);
        currentCalendar = java.util.Calendar.getInstance();

        currentFragment = new OneDayFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_container, currentFragment)
                .commit();

        new Handler().postDelayed(() -> {
            if (currentFragment instanceof DateSelectable) {
                ((DateSelectable) currentFragment).setSelectedDate(new Date());
            }
        }, 100);
    }

    private void setupCalendarBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_calendar, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        setupTabsBottom(bottomSheetView, bottomSheetDialog);

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

        updateCalendarGrid(calendarGrid, monthYearText);
        updateGoToTodayButtonVisibility(goToTodayButton);

        calendarGrid.setOnItemClickListener((parent, view, position, id) -> {
            Date selectedDate = (Date) adapter.getItem(position);
            if (selectedDate != null) {
                adapter.setSelectedDate(selectedDate);
                updateSelectedDate(selectedDate);
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
            updateSelectedDate(today.getTime());
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

        oneDay.setTextColor(getResources().getColor(android.R.color.black));
        oneDay.setBackgroundResource(R.color.light_gray);
        for (int i = 1; i < tabs.length; i++) {
            tabs[i].setTextColor(getResources().getColor(android.R.color.darker_gray));
            tabs[i].setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }

        View.OnClickListener tabClickListener = v -> {
            for (TextView tab : tabs) {
                tab.setTextColor(getResources().getColor(android.R.color.darker_gray));
                tab.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }

            TextView selectedTab = (TextView) v;
            selectedTab.setTextColor(getResources().getColor(android.R.color.black));
            selectedTab.setBackgroundResource(R.color.light_gray);

            Fragment newFragment;
            int id = v.getId();
            if (id == R.id.tab_one_day) {
                newFragment = new OneDayFragment();
            } else if (id == R.id.tab_three_days) {
                newFragment = new ThreeDaysFragment();
            } else if (id == R.id.tab_week) {
                newFragment = new WeekFragment();
            } else {
                newFragment = new MonthFragment();
            }

            currentFragment = newFragment;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_container, newFragment)
                    .commit();

            getSupportFragmentManager().executePendingTransactions();

            if (newFragment instanceof DateSelectable) {
                ((DateSelectable) newFragment).setSelectedDate(currentSelectedDate);
            }

            updateCurrentDate(currentSelectedDate.getTime());
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
        Calendar calendar = (Calendar) currentCalendar.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        for (int i = 0; i < firstDayOfWeek; i++) {
            dates.add(null);
        }

        while (calendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) {
            dates.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        while (dates.size() < 42) {
            dates.add(null);
        }

        adapter = new CustomCalendarAdapter(this, dates);
        calendarGrid.setAdapter(adapter);

        Calendar today = Calendar.getInstance();
        if (currentCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                currentCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            adapter.setSelectedDate(today.getTime());
        } else {
            for (Date date : dates) {
                if (date != null) {
                    adapter.setSelectedDate(date);
                    break;
                }
            }
        }

        SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        monthYearText.setText(formatter.format(currentCalendar.getTime()));
    }

    private void updateCurrentDate(long timeInMillis) {
        try {
            Date date = new Date(timeInMillis);
            android.text.SpannableStringBuilder builder = new android.text.SpannableStringBuilder();

            if (currentFragment instanceof MonthFragment) {
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
                SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
                String month = monthFormat.format(date);
                String year = yearFormat.format(date);

                builder.append(month);
                builder.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                        0, month.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.append(" " + year);
                builder.setSpan(new android.text.style.AbsoluteSizeSpan(20, true),
                        0, builder.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (currentFragment instanceof WeekFragment) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

                builder.append("Week of\n");
                builder.setSpan(new android.text.style.AbsoluteSizeSpan(14, true),
                        0, builder.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                int dateStart = builder.length();
                String dateStr = dateFormat.format(date);
                builder.append(dateStr);
                builder.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                        dateStart, builder.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new android.text.style.AbsoluteSizeSpan(20, true),
                        dateStart, builder.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
                SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

                String dayOfWeek = dayFormat.format(date);
                String dateWithoutYear = dateFormat.format(date);
                String year = yearFormat.format(date);

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
            }

            currentDateText.setText(builder);
        } catch (Exception e) {
            currentDateText.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    .format(new Date()));
        }
    }

    private void updateSelectedDate(Date date) {
        currentSelectedDate = date;
        updateCurrentDate(date.getTime());

        if (currentFragment instanceof DateSelectable) {
            ((DateSelectable) currentFragment).setSelectedDate(date);
        }
    }

    public void selectDate(Date date) {
        updateSelectedDate(date);

        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTime(date);

        Calendar currentCal = Calendar.getInstance();
        currentCal.setTime(currentSelectedDate);

        // Switch fragment if month changed
        if (selectedCal.get(Calendar.MONTH) != currentCal.get(Calendar.MONTH) ||
                selectedCal.get(Calendar.YEAR) != currentCal.get(Calendar.YEAR)) {
            adapter.setSelectedDate(date);
        }

        // Update current fragment's selected date
        if (currentFragment instanceof DateSelectable) {
            ((DateSelectable) currentFragment).setSelectedDate(date);
        }

        // Update month/year text if needed
        if (currentFragment instanceof MonthFragment) {
            updateCurrentDate(date.getTime());
        }
    }

    private void setupAddTaskButton() {
        ImageButton addTaskButton = findViewById(R.id.add_task);
        addTaskButton.setOnClickListener(v -> showAddTaskDialog());
    }

        private void setupDurationSpinner(View bottomSheetView) {
            Spinner durationSpinner = bottomSheetView.findViewById(R.id.spinner_duration);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    bottomSheetView.getContext(),
                    R.array.task_duration,
                    android.R.layout.simple_spinner_item
            );

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            durationSpinner.setAdapter(adapter);

            durationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String duration = parent.getItemAtPosition(position).toString();
                    String minutes = duration.split(" ")[0];
                    if (view instanceof TextView) {
                        ((TextView) view).setText(minutes);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });
        }

    public void refreshCurrentFragment() {
        if (currentFragment instanceof DateSelectable) {
            ((DateSelectable) currentFragment).setSelectedDate(currentSelectedDate);
        }
    }

    private void showAddTaskDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_add_task_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);
        setupDurationSpinner(bottomSheetView);

        // Get reference to existing duration spinner
        Spinner durationSpinner = bottomSheetView.findViewById(R.id.spinner_duration);

        // Setup RecyclerView with existing spinner
        RecyclerView tasksRecyclerView = bottomSheetView.findViewById(R.id.tasks_recycler_view);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        TaskCalendarAdapter adapter = new TaskCalendarAdapter(this, durationSpinner);
        adapter.setSelectedDate(currentSelectedDate);
        tasksRecyclerView.setAdapter(adapter);

        // Load tasks from Firestore
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore.getInstance()
                    .collection("tasks")
                    .whereEqualTo("userId", currentUser.getUid())
                    .whereEqualTo("startAt", null)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Task> tasks = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            try {
                                Task task = new Task(
                                        document.getBoolean("isCompleted") != null ? document.getBoolean("isCompleted") : false,
                                        document.getString("title"),
                                        document.getDate("createdAt"),
                                        document.getId(),
                                        document.getString("repeat"),
                                        document.getString("startAtDate"),
                                        document.getString("startAtPeriod"),
                                        document.getString("startAtTime"),
                                        document.getString("startNoti"),
                                        document.getString("priority"),
                                        document.get("duration") != null ? document.getLong("duration").intValue() : 0,
                                        document.getDate("startAt"),
                                        document.getString("hideUntilDate"),
                                        document.getString("hideUntilTime")
                                );
                                task.setUserId(document.getString("userId"));
                                System.out.println("Task: " + task.getStartAt());
                                tasks.add(task);
                            } catch (Exception e) {
                                System.out.println("Error parsing document: " + document.getId());
                                e.printStackTrace();
                            }
                        }
                        adapter.setTasks(tasks);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error loading tasks: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });
        }

        View parentView = (View) bottomSheetView.getParent();
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parentView);
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int dialogHeight = (int) (screenHeight * 0.75);

        bottomSheetView.setMinimumHeight(dialogHeight);
        behavior.setPeekHeight(dialogHeight);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        bottomSheetDialog.show();
    }
}