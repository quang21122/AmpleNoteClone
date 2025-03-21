package com.example.amplenoteclone.calendar;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WeekFragment extends Fragment implements DateSelectable, TaskView {
    private Date selectedDate;
    private LinearLayout timelineContainer;
    private View[] dayIndicators = new View[7];
    private TextView[] dayNumbers = new TextView[7];
    private LinearLayout[] dayContainers = new LinearLayout[7];
    private Handler timeUpdateHandler;
    private Runnable timeUpdateRunnable;

    public WeekFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week, container, false);
        timelineContainer = view.findViewById(R.id.timelineContainer);

        // Initialize views
        for (int i = 0; i < 7; i++) {
            int dayNumber = getResources().getIdentifier("day" + (i + 1) + "Number", "id", requireContext().getPackageName());
            int dayContainer = getResources().getIdentifier("day" + (i + 1) + "Container", "id", requireContext().getPackageName());
            int dayIndicator = getResources().getIdentifier("day" + (i + 1) + "Indicator", "id", requireContext().getPackageName());

            dayNumbers[i] = view.findViewById(dayNumber);
            dayContainers[i] = view.findViewById(dayContainer);
            dayIndicators[i] = view.findViewById(dayIndicator);
        }

        setupTimeline();
        loadTasksForDate(selectedDate);
        return view;
    }

    @Override
    public void setSelectedDate(Date date) {
        this.selectedDate = date;
        if (getView() != null) {
            updateDayHeaders();
            new Handler().postDelayed(() -> loadTasksForDate(selectedDate), 100);
        }
    }

    private void updateDayHeaders() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);

        // Find Monday of the week
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        // Update all 7 days
        for (int i = 0; i < 7; i++) {
            Date currentDate = calendar.getTime();
            dayNumbers[i].setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));

            // Highlight current day
            boolean isToday = isToday(currentDate);
            dayNumbers[i].setTypeface(null, Typeface.BOLD);
            dayIndicators[i].setVisibility(isToday ? View.VISIBLE : View.INVISIBLE);

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void setupTimeline() {
        timelineContainer.removeAllViews();

        for (int hour = 0; hour < 24; hour++) {
            LinearLayout timeSlot = new LinearLayout(getContext());
            LinearLayout.LayoutParams slotParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(120));
            timeSlot.setLayoutParams(slotParams);
            timeSlot.setOrientation(LinearLayout.HORIZONTAL);
            timeSlot.setTag("hour_" + hour);

            // Time label container
            LinearLayout timeContainer = new LinearLayout(getContext());
            LinearLayout.LayoutParams timeContainerParams = new LinearLayout.LayoutParams(
                    dpToPx(60),
                    LinearLayout.LayoutParams.MATCH_PARENT);
            timeContainer.setLayoutParams(timeContainerParams);
            timeContainer.setOrientation(LinearLayout.VERTICAL);

            // Time label
            TextView timeLabel = createTimeLabel(hour);
            timeContainer.addView(timeLabel);
            timeSlot.addView(timeContainer);

            // Vertical divider after time label
            View verticalDivider = new View(getContext());
            LinearLayout.LayoutParams verticalDividerParams = new LinearLayout.LayoutParams(
                    dpToPx(1),
                    LinearLayout.LayoutParams.MATCH_PARENT);
            verticalDivider.setLayoutParams(verticalDividerParams);
            verticalDivider.setBackgroundColor(getResources().getColor(R.color.light_gray));
            timeSlot.addView(verticalDivider);

            // Content container
            LinearLayout contentContainer = new LinearLayout(getContext());
            LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1);
            contentContainer.setLayoutParams(contentParams);
            contentContainer.setOrientation(LinearLayout.VERTICAL);

            // Horizontal divider at top
            View horizontalDivider = new View(getContext());
            LinearLayout.LayoutParams horizontalDividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(1));
            horizontalDivider.setLayoutParams(horizontalDividerParams);
            horizontalDivider.setBackgroundColor(getResources().getColor(R.color.light_gray));
            contentContainer.addView(horizontalDivider);

            // Days container
            LinearLayout daysContainer = new LinearLayout(getContext());
            LinearLayout.LayoutParams daysContainerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1);
            daysContainer.setLayoutParams(daysContainerParams);
            daysContainer.setOrientation(LinearLayout.HORIZONTAL);
            contentContainer.addView(daysContainer);
            timeSlot.addView(contentContainer);

            // Add 7 day columns
            for (int i = 0; i < 7; i++) {
                LinearLayout dayColumn = new LinearLayout(getContext());
                LinearLayout.LayoutParams columnParams = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1);
                dayColumn.setLayoutParams(columnParams);
                dayColumn.setTag("day_" + i + "_hour_" + hour);
                daysContainer.addView(dayColumn);

                // Add vertical divider between days (except last)
                if (i < 6) {
                    View divider = new View(getContext());
                    LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                            dpToPx(1),
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    divider.setLayoutParams(dividerParams);
                    divider.setBackgroundColor(getResources().getColor(R.color.light_gray));
                    daysContainer.addView(divider);
                }
            }

            timelineContainer.addView(timeSlot);
        }
    }

    private TextView createTimeLabel(int hour) {
        TextView timeLabel = new TextView(getContext());
        LinearLayout.LayoutParams timeLabelParams = new LinearLayout.LayoutParams(
                dpToPx(60),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        timeLabelParams.gravity = android.view.Gravity.START | android.view.Gravity.TOP;
        timeLabel.setLayoutParams(timeLabelParams);

        timeLabel.setGravity(Gravity.START);
        timeLabel.setPadding(dpToPx(4), 0, 0, 0);

        String timeText = String.format(Locale.getDefault(), "%d %s",
                hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour),
                hour >= 12 ? "PM" : "AM");
        timeLabel.setText(timeText);
        timeLabel.setTextColor(getResources().getColor(R.color.light_gray));
        timeLabel.setTextSize(14);
        return timeLabel;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private boolean isToday(Date date) {
        Calendar today = Calendar.getInstance();
        Calendar checkDate = Calendar.getInstance();
        checkDate.setTime(date);
        return today.get(Calendar.YEAR) == checkDate.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == checkDate.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (selectedDate != null && isToday(selectedDate)) {
            startTimeUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimeUpdates();
    }

    private void startTimeUpdates() {
        if (timeUpdateHandler == null) {
            timeUpdateHandler = new Handler();
        }
        stopTimeUpdates();
        timeUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                timeUpdateHandler.postDelayed(this, 60000); // Update every minute
            }
        };
        timeUpdateHandler.post(timeUpdateRunnable);
    }

    private void stopTimeUpdates() {
        if (timeUpdateHandler != null && timeUpdateRunnable != null) {
            timeUpdateHandler.removeCallbacks(timeUpdateRunnable);
        }
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public void loadTasksForDate(Date date) {
        if (!isAdded() || date == null) return;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        // Get Monday of the week
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startWeek = cal.getTime();

        // Get next Monday
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date endWeek = cal.getTime();


        FirebaseFirestore.getInstance()
                .collection("tasks")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        try {
                            Task task = new Task();
                            task.setId(document.getId());
                            task.setTitle(document.getString("title"));
                            task.setStartAt(document.getDate("startAt"));
                            task.setUserId(document.getString("userId"));

                            // Filter tasks by date range in memory
                            if (task.getStartAt() != null &&
                                    task.getStartAt().after(startWeek) &&
                                    task.getStartAt().before(endWeek)) {

                                // Handle duration conversion
                                Object durationObj = document.get("duration");
                                if (durationObj instanceof Long) {
                                    task.setDuration(String.valueOf(durationObj));
                                } else {
                                    task.setDuration(document.getString("duration"));
                                }
                                addTaskToTimeline(task);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }

    @Override
    public void addTaskToTimeline(Task task) {
        if (task.getStartAt() == null || timelineContainer == null) return;

        Calendar taskCal = Calendar.getInstance();
        taskCal.setTime(task.getStartAt());

        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTime(selectedDate);
        selectedCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        selectedCal.set(Calendar.HOUR_OF_DAY, 0);
        selectedCal.set(Calendar.MINUTE, 0);
        selectedCal.set(Calendar.SECOND, 0);
        selectedCal.set(Calendar.MILLISECOND, 0);

        // Calculate day position (0-6)
        int dayPosition = -1;
        for (int i = 0; i < 7; i++) {
            Calendar checkCal = (Calendar) selectedCal.clone();
            checkCal.add(Calendar.DAY_OF_MONTH, i);
            if (isSameDay(checkCal.getTime(), task.getStartAt())) {
                dayPosition = i;
                break;
            }
        }

        if (dayPosition != -1) {
            int taskHour = taskCal.get(Calendar.HOUR_OF_DAY);
            int taskMinute = taskCal.get(Calendar.MINUTE);

            View hourSlot = timelineContainer.findViewWithTag("hour_" + taskHour);
            if (hourSlot instanceof LinearLayout) {
                LinearLayout timeSlot = (LinearLayout) hourSlot;
                LinearLayout contentContainer = (LinearLayout) timeSlot.getChildAt(2);
                LinearLayout daysContainer = (LinearLayout) contentContainer.getChildAt(1);

                // Get the correct day column (accounting for dividers)
                LinearLayout dayColumn = (LinearLayout) daysContainer.getChildAt(dayPosition * 2);

                // Create task view
                TextView taskView = new TextView(getContext());
                taskView.setText(task.getTitle());
                taskView.setTextColor(getResources().getColor(android.R.color.black));
                taskView.setBackgroundResource(R.drawable.task_calendar_background);
                taskView.setPadding(dpToPx(4), dpToPx(2), dpToPx(4), dpToPx(2));
                taskView.setSingleLine(true);
                taskView.setEllipsize(TextUtils.TruncateAt.END);
                taskView.setTextSize(12);

                // Calculate vertical position based on minutes
                float minuteProgress = taskMinute / 60f;
                int slotHeight = hourSlot.getHeight() > 0 ? hourSlot.getHeight() : dpToPx(120);
                int maxTopMargin = slotHeight - dpToPx(30);
                int topMargin = Math.min((int)(slotHeight * minuteProgress), maxTopMargin);

                LinearLayout.LayoutParams taskParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                taskParams.leftMargin = dpToPx(2);
                taskParams.rightMargin = dpToPx(2);
                taskParams.topMargin = topMargin;
                taskView.setLayoutParams(taskParams);

                dayColumn.addView(taskView);
            }
        }
    }
}