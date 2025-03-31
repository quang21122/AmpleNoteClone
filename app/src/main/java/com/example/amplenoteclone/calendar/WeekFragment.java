package com.example.amplenoteclone.calendar;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Task;
import com.example.amplenoteclone.note.ViewNoteActivity;
import com.example.amplenoteclone.tasks.TaskDetailsActivity;
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
        if (timelineContainer != null) {
            clearAllTasks();
        }

        this.selectedDate = date;
        if (getView() != null) {
            updateDayHeaders();
            new Handler().postDelayed(() -> loadTasksForDate(date), 100);
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

            View horizontalDivider = new View(getContext());
            LinearLayout.LayoutParams horizontalDividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(1));
            horizontalDivider.setLayoutParams(horizontalDividerParams);
            horizontalDivider.setBackgroundColor(getResources().getColor(R.color.light_gray));
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

            for (int i = 0; i < 7; i++) {
                LinearLayout dayColumn = new LinearLayout(getContext());
                LinearLayout.LayoutParams columnParams = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1);
                dayColumn.setLayoutParams(columnParams);
                dayColumn.setTag("day_" + i + "_hour_" + hour);
                daysContainer.addView(dayColumn);

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

        clearAllTasks();

        // Get Monday of the week
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.setFirstDayOfWeek(Calendar.MONDAY); // Set Monday as first day of week

        // Điều chỉnh về Monday của tuần hiện tại
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startWeek = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date endWeek = cal.getTime();

        FirebaseFirestore.getInstance()
                .collection("tasks")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("startAt")
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
                            task.setCompleted(document.getBoolean("isCompleted") != null ?
                                    document.getBoolean("isCompleted") : false);
                            task.setNoteId(document.getString("noteId"));
                            Object durationObj = document.get("duration");
                            if (durationObj instanceof Long) {
                                task.setDuration(Integer.parseInt(String.valueOf(durationObj)));
                            } else {
                                task.setDuration(Integer.parseInt(document.getString("duration")));
                            }
                            System.out.println("Task: " + task.getTitle() + " - " + task.getStartAt());
                            addTaskToTimeline(task);
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

        Calendar mondayCal = Calendar.getInstance();
        mondayCal.setTime(selectedDate);
        mondayCal.setFirstDayOfWeek(Calendar.MONDAY);

        while (mondayCal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            mondayCal.add(Calendar.DAY_OF_MONTH, -1);
        }

        mondayCal.set(Calendar.HOUR_OF_DAY, 0);
        mondayCal.set(Calendar.MINUTE, 0);
        mondayCal.set(Calendar.SECOND, 0);
        mondayCal.set(Calendar.MILLISECOND, 0);

        Calendar taskCal = Calendar.getInstance();
        taskCal.setTime(task.getStartAt());
        int taskHour = taskCal.get(Calendar.HOUR_OF_DAY);
        int taskMinute = taskCal.get(Calendar.MINUTE);

        int dayPosition = -1;
        Calendar tempCal = (Calendar) mondayCal.clone();
        for (int i = 0; i < 7; i++) {
            if (isSameDay(tempCal.getTime(), task.getStartAt())) {
                dayPosition = i;
                break;
            }
            tempCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (dayPosition != -1) {
            View hourSlot = timelineContainer.findViewWithTag("hour_" + taskHour);
            if (hourSlot instanceof LinearLayout) {
                LinearLayout timeSlot = (LinearLayout) hourSlot;
                LinearLayout contentContainer = (LinearLayout) timeSlot.getChildAt(2);
                LinearLayout daysContainer = (LinearLayout) contentContainer.getChildAt(1);
                LinearLayout dayColumn = (LinearLayout) daysContainer.getChildAt(dayPosition * 2);

                if (dayColumn != null) {
                    int slotHeight = hourSlot.getHeight() > 0 ? hourSlot.getHeight() : dpToPx(120);

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

                    int taskHeight = calculateTaskHeight(task.getDuration(), slotHeight);
                    float minuteProgress = taskMinute / 60f;
                    int topMargin = Math.min((int)(slotHeight * minuteProgress), slotHeight - taskHeight);

                    LinearLayout.LayoutParams taskParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            taskHeight);
                    taskParams.leftMargin = dpToPx(2);
                    taskParams.rightMargin = dpToPx(2);
                    taskParams.topMargin = topMargin;
                    taskView.setLayoutParams(taskParams);

                    taskView.setOnClickListener(v -> showTaskDialog(task));
                    dayColumn.addView(taskView);
                }
            }
        }
    }

    private int calculateTaskHeight(int duration, int slotHeight) {
        switch (duration) {
            case 15: return slotHeight / 4;
            case 30: return slotHeight / 2;
            case 45: return (slotHeight * 3) / 4;
            case 60: return slotHeight;
            default: return dpToPx(30);
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
            Intent intent = new Intent(requireContext(), TaskDetailsActivity.class);
            intent.putExtra("task", task);
            startActivity(intent);
            dialog.dismiss();
        });

        view.findViewById(R.id.btn_view_details).setOnClickListener(v -> {
            // view details
            dialog.dismiss();
        });

        view.findViewById(R.id.btn_view_notes).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ViewNoteActivity.class);
            intent.putExtra("noteId", task.getNoteId());
            startActivity(intent);
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

    private void clearAllTasks() {
        if (!isAdded() || timelineContainer == null) return;

        for (int hour = 0; hour < 24; hour++) {
            View hourSlot = timelineContainer.findViewWithTag("hour_" + hour);
            if (hourSlot instanceof LinearLayout) {
                LinearLayout timeSlot = (LinearLayout) hourSlot;
                LinearLayout contentContainer = (LinearLayout) timeSlot.getChildAt(2);
                LinearLayout daysContainer = (LinearLayout) contentContainer.getChildAt(1);

                for (int i = 0; i < daysContainer.getChildCount(); i++) {
                    View child = daysContainer.getChildAt(i);
                    if (child instanceof ViewGroup) {
                        ((ViewGroup) child).removeAllViews();
                    }
                }
            }
        }
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
}