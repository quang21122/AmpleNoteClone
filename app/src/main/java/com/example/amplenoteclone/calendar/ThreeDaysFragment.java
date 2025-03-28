package com.example.amplenoteclone.calendar;

import android.graphics.Paint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Task;
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

public class ThreeDaysFragment extends Fragment implements DateSelectable, TaskView {
    private LinearLayout timelineContainer;
    private View currentTimeIndicator;
    private ScrollView scrollView;
    private Handler handler = new Handler();
    private Runnable timeUpdateRunnable;
    private Date selectedDate;

    private TextView day1Number, day2Number, day3Number;
    private TextView day1Name, day2Name, day3Name;
    private View day1Indicator, day2Indicator, day3Indicator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_three_days, container, false);

        initializeViews(view);
        setupTimeline();
        updateDaysHeader();
        loadTasksForDate(selectedDate);
        if(isToday(selectedDate)) {
            startTimeUpdates();
            scrollToCurrentTime();
        }
        return view;
    }

    private void initializeViews(View view) {
        timelineContainer = view.findViewById(R.id.timelineContainer);
        scrollView = view.findViewById(R.id.scrollView);

        day1Number = view.findViewById(R.id.day1Number);
        day2Number = view.findViewById(R.id.day2Number);
        day3Number = view.findViewById(R.id.day3Number);

        day1Name = view.findViewById(R.id.day1Name);
        day2Name = view.findViewById(R.id.day2Name);
        day3Name = view.findViewById(R.id.day3Name);

        day1Indicator = view.findViewById(R.id.day1Indicator);
        day2Indicator = view.findViewById(R.id.day2Indicator);
        day3Indicator = view.findViewById(R.id.day3Indicator);

        selectedDate = new Date();
    }

    private void updateDaysHeader() {
        Calendar cal = Calendar.getInstance();

        cal.setTime(selectedDate);

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat numberFormat = new SimpleDateFormat("d", Locale.getDefault());

        day1Number.setText(numberFormat.format(cal.getTime()));
        day1Name.setText(dayFormat.format(cal.getTime()).toUpperCase());
        day1Indicator.setVisibility(isToday(cal.getTime()) ? View.VISIBLE : View.INVISIBLE);

        cal.add(Calendar.DAY_OF_MONTH, 1);
        day2Number.setText(numberFormat.format(cal.getTime()));
        day2Name.setText(dayFormat.format(cal.getTime()).toUpperCase());
        day2Indicator.setVisibility(isToday(cal.getTime()) ? View.VISIBLE : View.INVISIBLE);

        cal.add(Calendar.DAY_OF_MONTH, 1);
        day3Number.setText(numberFormat.format(cal.getTime()));
        day3Name.setText(dayFormat.format(cal.getTime()).toUpperCase());
        day3Indicator.setVisibility(isToday(cal.getTime()) ? View.VISIBLE : View.INVISIBLE);
    }

    private void setupTimeline() {
        timelineContainer.removeAllViews();

        for (int hour = 0; hour < 24; hour++) {
            LinearLayout timeSlot = new LinearLayout(getContext());
            LinearLayout.LayoutParams slotParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(140));
            timeSlot.setLayoutParams(slotParams);
            timeSlot.setOrientation(LinearLayout.HORIZONTAL);
            timeSlot.setTag("hour_" + hour);

            LinearLayout timeContainer = new LinearLayout(getContext());
            LinearLayout.LayoutParams timeContainerParams = new LinearLayout.LayoutParams(
                    dpToPx(60),
                    LinearLayout.LayoutParams.MATCH_PARENT);
            timeContainer.setLayoutParams(timeContainerParams);
            timeContainer.setOrientation(LinearLayout.VERTICAL);

            TextView timeLabel = createTimeLabel(hour);
            timeContainer.addView(timeLabel);
            timeSlot.addView(timeContainer);

            View verticalDivider = new View(getContext());
            LinearLayout.LayoutParams verticalDividerParams = new LinearLayout.LayoutParams(
                    dpToPx(1),
                    LinearLayout.LayoutParams.MATCH_PARENT);
            verticalDivider.setLayoutParams(verticalDividerParams);
            verticalDivider.setBackgroundColor(getResources().getColor(R.color.light_gray));
            timeSlot.addView(verticalDivider);

            View horizontalDivider = new View(getContext());
            LinearLayout.LayoutParams horizontalDividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(1));
            horizontalDivider.setLayoutParams(horizontalDividerParams);
            horizontalDivider.setBackgroundColor(getResources().getColor(R.color.light_gray));

            LinearLayout contentContainer = new LinearLayout(getContext());
            LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1);
            contentContainer.setLayoutParams(contentParams);
            contentContainer.setOrientation(LinearLayout.VERTICAL);
            contentContainer.addView(horizontalDivider);

            LinearLayout daysContainer = new LinearLayout(getContext());
            LinearLayout.LayoutParams daysContainerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1);
            daysContainer.setLayoutParams(daysContainerParams);
            daysContainer.setOrientation(LinearLayout.HORIZONTAL);
            contentContainer.addView(daysContainer);
            timeSlot.addView(contentContainer);

            for (int i = 0; i < 3; i++) {
                FrameLayout dayColumn = new FrameLayout(getContext());
                LinearLayout.LayoutParams columnParams = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1);
                dayColumn.setLayoutParams(columnParams);
                dayColumn.setTag("day_" + i + "_hour_" + hour);
                daysContainer.addView(dayColumn);

                if (i < 2) {
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
        timeLabelParams.setMargins(dpToPx(8), 0, 0, 0);

        String timeText = String.format(Locale.getDefault(), "%d %s",
                hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour),
                hour >= 12 ? "PM" : "AM");
        timeLabel.setText(timeText);
        timeLabel.setTextColor(getResources().getColor(R.color.light_gray));
        timeLabel.setTextSize(14);
        return timeLabel;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isToday(selectedDate)) {
            startTimeUpdates();
        }
    }

    private void scrollToCurrentTime() {
        if (scrollView == null || timelineContainer == null) return;

        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);

        View hourSlot = timelineContainer.findViewWithTag("hour_" + currentHour);
        if (hourSlot != null) {
            int scrollY = hourSlot.getTop() - dpToPx(100);

            scrollView.post(() -> scrollView.smoothScrollTo(0, Math.max(0, scrollY)));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimeUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimeUpdates();
    }

    private void startTimeUpdates() {
        stopTimeUpdates();

        if (timeUpdateRunnable == null) {
            timeUpdateRunnable = new Runnable() {
                @Override
                public void run() {
                    handler.postDelayed(this, 30000); // Update every 30 seconds
                }
            };
        }
        handler.post(timeUpdateRunnable);
    }

    private void stopTimeUpdates() {
        if (handler != null && timeUpdateRunnable != null) {
            handler.removeCallbacks(timeUpdateRunnable);
            timeUpdateRunnable = null;
        }
    }

    private boolean isToday(Date date) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        Calendar cal2 = Calendar.getInstance();
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public void setSelectedDate(Date date) {
        this.selectedDate = date;
        if (getView() != null) {
            clearExistingTasks();

            setupTimeline();
            updateDaysHeader();

            new Handler().post(() -> loadTasksForDate(date));

            if (isToday(date)) {
                startTimeUpdates();
            } else {
                stopTimeUpdates();
            }
        }
    }

    @Override
    public void loadTasksForDate(Date date) {
        if (!isAdded() || date == null) return;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, 3);
        Date endDate = cal.getTime();

        FirebaseFirestore.getInstance()
                .collection("tasks")
                .whereEqualTo("userId", currentUser.getUid())
                .whereGreaterThanOrEqualTo("startAt", startDate)
                .whereLessThan("startAt", endDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        try {
                            Task task = new Task();
                            task.setId(document.getId());
                            task.setTitle(document.getString("title"));
                            task.setStartAt(document.getDate("startAt"));
                            task.setCompleted(document.getBoolean("isCompleted") != null ?
                                    document.getBoolean("isCompleted") : false);
                            task.setNoteId(document.getString("noteId"));
                            Object durationObj = document.get("duration");
                            if (durationObj instanceof Long) {
                                task.setDuration(Integer.parseInt(String.valueOf(durationObj)));
                            } else {
                                task.setDuration(Integer.parseInt(document.getString("duration")));
                            }

                            task.setUserId(document.getString("userId"));
                            addTaskToTimeline(task);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }

    private int getDayPosition(Date taskDate) {
        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTime(selectedDate);
        selectedCal.set(Calendar.HOUR_OF_DAY, 0);
        selectedCal.set(Calendar.MINUTE, 0);
        selectedCal.set(Calendar.SECOND, 0);
        selectedCal.set(Calendar.MILLISECOND, 0);

        Calendar taskCal = Calendar.getInstance();
        taskCal.setTime(taskDate);
        taskCal.set(Calendar.HOUR_OF_DAY, 0);
        taskCal.set(Calendar.MINUTE, 0);
        taskCal.set(Calendar.SECOND, 0);
        taskCal.set(Calendar.MILLISECOND, 0);

        long diff = taskCal.getTimeInMillis() - selectedCal.getTimeInMillis();
        int dayDiff = (int) (diff / (24 * 60 * 60 * 1000));

        return dayDiff;
    }

    @Override
    public void addTaskToTimeline(Task task) {
        if (task.getStartAt() == null || timelineContainer == null) return;

        Calendar taskCal = Calendar.getInstance();
        taskCal.setTime(task.getStartAt());

        int taskHour = taskCal.get(Calendar.HOUR_OF_DAY);
        int taskMinute = taskCal.get(Calendar.MINUTE);

        View hourSlot = timelineContainer.findViewWithTag("hour_" + taskHour);
        if (hourSlot instanceof LinearLayout) {
            LinearLayout timeSlot = (LinearLayout) hourSlot;
            LinearLayout contentContainer = (LinearLayout) timeSlot.getChildAt(2);
            LinearLayout daysContainer = (LinearLayout) contentContainer.getChildAt(1);

            int dayPosition = getDayPosition(task.getStartAt());
            if (dayPosition < 0 || dayPosition > 2) return; // Bỏ qua nếu task không thuộc 3 ngày đang xem

            FrameLayout dayColumn = (FrameLayout) daysContainer.getChildAt(dayPosition * 2);

            FrameLayout wrapper = new FrameLayout(requireContext());

            int slotHeight = hourSlot.getHeight() > 0 ? hourSlot.getHeight() : dpToPx(140);
            int taskHeight;
            switch (task.getDuration()) {
                case 15:
                    taskHeight = slotHeight / 4;
                    break;
                case 30:
                    taskHeight = slotHeight / 2;
                    break;
                case 45:
                    taskHeight = (slotHeight * 3) / 4;
                    break;
                case 60:
                    taskHeight = slotHeight;
                    break;
                default:
                    taskHeight = dpToPx(30);
            }

            FrameLayout.LayoutParams wrapperParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    taskHeight
            );

            int topMargin = Math.min((int)(slotHeight * (taskMinute / 60f)),
                    slotHeight - taskHeight);
            wrapperParams.topMargin = topMargin;
            wrapperParams.leftMargin = dpToPx(2);
            wrapperParams.rightMargin = dpToPx(2);
            wrapper.setLayoutParams(wrapperParams);

            TextView taskView = new TextView(getContext());
            taskView.setText(task.getTitle());
            taskView.setTextColor(getResources().getColor(android.R.color.black));
            if (task.isCompleted()) {
                taskView.setPaintFlags(taskView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            taskView.setBackgroundResource(R.drawable.task_calendar_background);
            taskView.setPadding(dpToPx(4), dpToPx(2), dpToPx(4), dpToPx(2));
            taskView.setSingleLine(true);
            taskView.setEllipsize(TextUtils.TruncateAt.END);
            taskView.setTextSize(12);

            FrameLayout.LayoutParams taskParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            taskView.setLayoutParams(taskParams);
            taskView.setOnClickListener(v -> showTaskDialog(task));

            wrapper.addView(taskView);
            dayColumn.addView(wrapper);
        }
    }

    private void showTaskDialog(Task task) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_task_details_bottom_sheet, null);
        dialog.setContentView(view);

        CheckBox checkbox = view.findViewById(R.id.task_checkbox);
        TextView titleText = view.findViewById(R.id.task_title);
        View incompleteSection = view.findViewById(R.id.incomplete_section);
        View completedSection = view.findViewById(R.id.completed_section);

        checkbox.setChecked(task.isCompleted());
        titleText.setText(task.getTitle());
        if (task.isCompleted()) {
            titleText.setPaintFlags(titleText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            completedSection.setVisibility(View.VISIBLE);
            incompleteSection.setVisibility(View.GONE);
        } else {
            titleText.setPaintFlags(titleText.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            completedSection.setVisibility(View.GONE);
            incompleteSection.setVisibility(View.VISIBLE);
        }

        checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FirebaseFirestore.getInstance()
                    .collection("tasks")
                    .document(task.getId())
                    .update("isCompleted", isChecked)
                    .addOnSuccessListener(aVoid -> {
                        task.setCompleted(isChecked);

                        dialog.dismiss();

                        if (requireActivity() instanceof CalendarActivity) {
                            ((CalendarActivity) requireActivity()).refreshCurrentFragment();
                        }
                    })
                    .addOnFailureListener(e -> {
                        checkbox.setChecked(!isChecked);
                        Toast.makeText(requireContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
                    });
        });

        setupRescheduleButtons(view, task, dialog);

        view.findViewById(R.id.btn_remove_schedule).setOnClickListener(v -> {
            removeSchedule(task);
            dialog.dismiss();
        });

        view.findViewById(R.id.btn_edit_details).setOnClickListener(v -> {
            // Show edit dialog
            dialog.dismiss();
        });

        view.findViewById(R.id.btn_view_details).setOnClickListener(v -> {
            // view details
            dialog.dismiss();
        });

        view.findViewById(R.id.btn_view_notes).setOnClickListener(v -> {
            // view notes
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupRescheduleButtons(View view, Task task, BottomSheetDialog dialog) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(task.getStartAt());
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        Button todayButton = view.findViewById(R.id.btn_today);
        if (!isToday(task.getStartAt())) {
            todayButton.setVisibility(View.VISIBLE);
            todayButton.setOnClickListener(v -> {
                rescheduleTaskFromCurrent(task, hour, minute); // Today relative to current date
                dialog.dismiss();
            });
        } else {
            todayButton.setVisibility(View.GONE);
        }

        view.findViewById(R.id.btn_tomorrow).setOnClickListener(v -> {
            Calendar taskCal = Calendar.getInstance();
            taskCal.setTime(task.getStartAt());
            taskCal.add(Calendar.DAY_OF_MONTH, 1);
            rescheduleTaskToDate(task, taskCal.getTime());
            dialog.dismiss();
        });

        view.findViewById(R.id.btn_two_days).setOnClickListener(v -> {
            Calendar taskCal = Calendar.getInstance();
            taskCal.setTime(task.getStartAt());
            taskCal.add(Calendar.DAY_OF_MONTH, 2);
            rescheduleTaskToDate(task, taskCal.getTime());
            dialog.dismiss();
        });

        view.findViewById(R.id.btn_one_week).setOnClickListener(v -> {
            Calendar taskCal = Calendar.getInstance();
            taskCal.setTime(task.getStartAt());
            taskCal.add(Calendar.DAY_OF_MONTH, 7);
            rescheduleTaskToDate(task, taskCal.getTime());
            dialog.dismiss();
        });

        view.findViewById(R.id.btn_three_weeks).setOnClickListener(v -> {
            Calendar taskCal = Calendar.getInstance();
            taskCal.setTime(task.getStartAt());
            taskCal.add(Calendar.DAY_OF_MONTH, 21);
            rescheduleTaskToDate(task, taskCal.getTime());
            dialog.dismiss();
        });

        view.findViewById(R.id.btn_select_date).setOnClickListener(v -> {
            showDatePicker(task, hour, minute, dialog);
        });
    }

    private void rescheduleTaskFromCurrent(Task task, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        rescheduleTaskToDate(task, cal.getTime());
    }

    private void rescheduleTaskToDate(Task task, Date newDate) {
        FirebaseFirestore.getInstance()
                .collection("tasks")
                .document(task.getId())
                .update("startAt", newDate)
                .addOnSuccessListener(aVoid -> {
                    if (requireActivity() instanceof CalendarActivity) {
                        ((CalendarActivity) requireActivity()).selectDate(newDate);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to reschedule task", Toast.LENGTH_SHORT).show());
    }

    private void showDatePicker(Task task, int hour, int minute, BottomSheetDialog currentDialog) {
        BottomSheetDialog datePickerDialog = new BottomSheetDialog(requireContext());
        View datePickerView = getLayoutInflater().inflate(R.layout.layout_date_picker, null);
        datePickerDialog.setContentView(datePickerView);

        TextView monthYearText = datePickerView.findViewById(R.id.monthYearText);
        GridView calendarGrid = datePickerView.findViewById(R.id.calendar_grid);
        ImageButton prevMonth = datePickerView.findViewById(R.id.previousMonth);
        ImageButton nextMonth = datePickerView.findViewById(R.id.nextMonth);
        ImageButton backButton = datePickerView.findViewById(R.id.backButton);
        ImageButton closeButton = datePickerView.findViewById(R.id.closeButton);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);
        updateCalendarView(calendar, monthYearText, calendarGrid);

        prevMonth.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendarView(calendar, monthYearText, calendarGrid);
        });

        nextMonth.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendarView(calendar, monthYearText, calendarGrid);
        });

        backButton.setOnClickListener(v -> {
            datePickerDialog.dismiss();
            currentDialog.show();
        });

        closeButton.setOnClickListener(v -> {
            datePickerDialog.dismiss();
            currentDialog.dismiss();
        });

        calendarGrid.setOnItemClickListener((parent, view, position, id) -> {
            Date selectedDate = (Date) calendarGrid.getItemAtPosition(position);
            if (selectedDate != null) {
                Calendar newDate = Calendar.getInstance();
                newDate.setTime(selectedDate);
                newDate.set(Calendar.HOUR_OF_DAY, hour);
                newDate.set(Calendar.MINUTE, minute);

                rescheduleTaskToDate(task, newDate.getTime());
                datePickerDialog.dismiss();
                currentDialog.dismiss();
            }
        });

        currentDialog.dismiss();
        datePickerDialog.show();
    }

    private void updateCalendarView(Calendar calendar, TextView monthYearText, GridView calendarGrid) {
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        monthYearText.setText(monthYearFormat.format(calendar.getTime()));

        List<Date> dates = generateCalendarDates(calendar);
        CustomCalendarAdapter adapter = new CustomCalendarAdapter(requireContext(), dates);
        calendarGrid.setAdapter(adapter);
    }

    private List<Date> generateCalendarDates(Calendar calendar) {
        List<Date> dates = new ArrayList<>();

        Calendar current = (Calendar) calendar.clone();

        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < firstDayOfWeek; i++) {
            dates.add(null);
        }

        for (int i = 1; i <= daysInMonth; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i);
            dates.add(calendar.getTime());
        }

        int remainingDays = 42 - dates.size();
        for (int i = 0; i < remainingDays; i++) {
            dates.add(null);
        }

        calendar.setTime(current.getTime());

        return dates;
    }

    private void removeSchedule(Task task) {
        FirebaseFirestore.getInstance()
                .collection("tasks")
                .document(task.getId())
                .update("startAt", null, "duration", 0)
                .addOnSuccessListener(aVoid -> {
                    if (requireActivity() instanceof CalendarActivity) {
                        ((CalendarActivity) requireActivity()).refreshCurrentFragment();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to remove schedule", Toast.LENGTH_SHORT).show());
    }

    private void clearExistingTasks() {
        if (timelineContainer == null) return;

        for (int hour = 0; hour < 24; hour++) {
            View hourSlot = timelineContainer.findViewWithTag("hour_" + hour);
            if (hourSlot instanceof LinearLayout) {
                LinearLayout timeSlot = (LinearLayout) hourSlot;
                LinearLayout contentContainer = (LinearLayout) timeSlot.getChildAt(2);
                LinearLayout daysContainer = (LinearLayout) contentContainer.getChildAt(1);

                for (int i = 0; i < 3; i++) {
                    FrameLayout dayColumn = (FrameLayout) daysContainer.getChildAt(i * 2);
                    dayColumn.removeAllViews();
                }
            }
        }
    }
}