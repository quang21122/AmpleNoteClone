package com.example.amplenoteclone.calendar;

import android.graphics.Paint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;
import android.widget.FrameLayout;
import android.widget.Toast;

public class OneDayFragment extends Fragment implements DateSelectable, TaskView {
    private LinearLayout timelineContainer;
    private ScrollView scrollView;
    private View currentTimeIndicator;
    private Date selectedDate = new Date();
    private Handler handler = new Handler();
    private Runnable timeUpdateRunnable;
    private boolean pendingTimelineUpdate = false;
    private static final String KEY_SELECTED_DATE = "selected_date";
    private boolean pendingIndicatorUpdate = false;
    private static final int INDICATOR_UPDATE_DELAY = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_one_day, container, false);
        timelineContainer = view.findViewById(R.id.timeline_container);
        scrollView = view.findViewById(R.id.scroll_view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setupTimeline();
                loadTasksForDate(selectedDate);
                if (isToday(selectedDate)) {
                    startTimeUpdates();
                    updateCurrentTimeIndicator();
                    scrollToCurrentTime();
                }
            }
        });
    }

    private void scrollToCurrentTime() {
        if (currentTimeIndicator == null || scrollView == null || !isAdded()) {
            return;
        }

        scrollView.post(() -> {
            if (!isAdded() || currentTimeIndicator == null || scrollView == null) {
                return;
            }

            try {
                int parentHeight = scrollView.getHeight();
                if (parentHeight == 0) {
                    new Handler().postDelayed(this::scrollToCurrentTime, 100);
                    return;
                }

                Calendar now = Calendar.getInstance();
                int currentHour = now.get(Calendar.HOUR_OF_DAY);
                int currentMinute = now.get(Calendar.MINUTE);

                int totalMinutes = (currentHour * 60) + currentMinute;

                int hourHeight = dpToPx(140);
                int scrollPosition = (totalMinutes * hourHeight) / 60;

                scrollPosition = Math.max(0, scrollPosition - (parentHeight / 2));

                scrollView.smoothScrollTo(0, scrollPosition);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isToday(selectedDate)) {
            startTimeUpdates();
            updateCurrentTimeIndicator();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimeUpdates();
    }

    private void startTimeUpdates() {
        stopTimeUpdates();

        if (timeUpdateRunnable == null) {
            timeUpdateRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isAdded() && isToday(selectedDate)) {
                        requireActivity().runOnUiThread(() -> updateCurrentTimeIndicator());
                    }
                    handler.postDelayed(this, 30000);
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

    @Override
    public void setSelectedDate(Date date) {
        this.selectedDate = date;

        if (timelineContainer != null && isAdded()) {
            setupTimeline();
            loadTasksForDate(date);

            if (isToday(date)) {
                if (currentTimeIndicator != null) {
                    ViewGroup parent = (ViewGroup) currentTimeIndicator.getParent();
                    if (parent != null) {
                        parent.removeView(currentTimeIndicator);
                    }
                    currentTimeIndicator = null;
                }

                // Add delay to ensure view is properly laid out
                new Handler().postDelayed(() -> {
                    if (isAdded()) {
                        startTimeUpdates();
                        updateCurrentTimeIndicator();
                        scrollToCurrentTime();
                    }
                }, INDICATOR_UPDATE_DELAY);
            } else {
                stopTimeUpdates();
                if (currentTimeIndicator != null) {
                    ViewGroup parent = (ViewGroup) currentTimeIndicator.getParent();
                    if (parent != null) {
                        parent.removeView(currentTimeIndicator);
                    }
                    currentTimeIndicator = null;
                }
            }
        } else {
            pendingTimelineUpdate = true;
            pendingIndicatorUpdate = isToday(date);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimeUpdates();
    }

    private boolean isToday(Date date) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        Calendar cal2 = Calendar.getInstance();
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void setupTimeline() {
        timelineContainer.removeAllViews();

        for (int hour = 0; hour < 24; hour++) {
            FrameLayout timeSlot = new FrameLayout(getContext());
            LinearLayout.LayoutParams slotParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(140));
            timeSlot.setLayoutParams(slotParams);
            timeSlot.setTag("hour_" + hour);

            TextView timeLabel = new TextView(getContext());
            FrameLayout.LayoutParams timeLabelParams = new FrameLayout.LayoutParams(
                    dpToPx(60),
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            timeLabel.setLayoutParams(timeLabelParams);

            String timeText = String.format(Locale.getDefault(), "%d %s",
                    hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour),
                    hour >= 12 ? "PM" : "AM");
            timeLabel.setText(timeText);
            timeLabel.setTextColor(getResources().getColor(R.color.light_gray));
            timeLabel.setTextSize(14);

            View divider = new View(getContext());
            FrameLayout.LayoutParams dividerParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(1));
            dividerParams.topMargin = dpToPx(8);
            dividerParams.leftMargin = dpToPx(60);
            divider.setLayoutParams(dividerParams);
            divider.setBackgroundColor(getResources().getColor(R.color.light_gray));

            timeSlot.addView(divider);
            timeSlot.addView(timeLabel);
            timelineContainer.addView(timeSlot);
        }

        if (isToday(selectedDate)) {
            updateCurrentTimeIndicator();
        }
    }

    public void updateCurrentTimeIndicator() {
        if (!isAdded() || timelineContainer == null) {
            pendingIndicatorUpdate = true;
            return;
        }

        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);

        if (currentTimeIndicator != null) {
            ViewGroup parent = (ViewGroup) currentTimeIndicator.getParent();
            if (parent != null) {
                parent.removeView(currentTimeIndicator);
            }
            currentTimeIndicator = null;
        }

        View hourSlot = timelineContainer.findViewWithTag("hour_" + currentHour);
        if (hourSlot instanceof FrameLayout) {
            FrameLayout timeSlot = (FrameLayout) hourSlot;

            currentTimeIndicator = new View(getContext());
            currentTimeIndicator.setBackgroundColor(getResources().getColor(R.color.green));

            float minuteProgress = currentMinute / 60f;
            int slotHeight = timeSlot.getHeight() > 0 ? timeSlot.getHeight() : dpToPx(140);

            int maxTopMargin = slotHeight - dpToPx(14);
            int topMargin = Math.min((int)(slotHeight * minuteProgress), maxTopMargin);

            FrameLayout.LayoutParams indicatorParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(2));
            indicatorParams.topMargin = topMargin + dpToPx(12);
            currentTimeIndicator.setLayoutParams(indicatorParams);

            timeSlot.addView(currentTimeIndicator);

            scrollToCurrentTime();
        } else if (currentMinute >= 57) {
            int nextHour = (currentHour + 1) % 24;
            View nextHourSlot = timelineContainer.findViewWithTag("hour_" + nextHour);
            if (nextHourSlot instanceof FrameLayout) {
                FrameLayout nextTimeSlot = (FrameLayout) nextHourSlot;

                currentTimeIndicator = new View(getContext());
                currentTimeIndicator.setBackgroundColor(getResources().getColor(R.color.green));

                FrameLayout.LayoutParams indicatorParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(2));
                indicatorParams.topMargin = dpToPx(12);
                currentTimeIndicator.setLayoutParams(indicatorParams);

                nextTimeSlot.addView(currentTimeIndicator);
                scrollToCurrentTime();
            }
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public void addTaskToTimeline(Task task) {
        if (task.getStartAt() == null || timelineContainer == null) return;

        Calendar taskCal = Calendar.getInstance();
        taskCal.setTime(task.getStartAt());

        int taskHour = taskCal.get(Calendar.HOUR_OF_DAY);
        int taskMinute = taskCal.get(Calendar.MINUTE);

        View hourSlot = timelineContainer.findViewWithTag("hour_" + taskHour);
        if (hourSlot == null) return;

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
        wrapperParams.leftMargin = dpToPx(60); // Margin from time label
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
        ((FrameLayout) hourSlot).addView(wrapper);
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
        Date startOfDay = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = cal.getTime();

        FirebaseFirestore.getInstance()
                .collection("tasks")
                .whereGreaterThanOrEqualTo("startAt", startOfDay)
                .whereLessThan("startAt", endOfDay)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Task task = new Task();
                        task.setId(document.getId());
                        task.setTitle(document.getString("title"));
                        task.setStartAt(document.getDate("startAt"));
                        task.setDuration(Integer.parseInt(String.valueOf(document.get("duration"))));
                        task.setUserId(document.getString("userId"));
                        task.setCompleted(document.getBoolean("isCompleted") != null ?
                                document.getBoolean("isCompleted") : false);
                        addTaskToTimeline(task);
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
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
                rescheduleTask(task, 0, hour, minute);
                dialog.dismiss();
            });
        } else {
            todayButton.setVisibility(View.GONE);
        }

        view.findViewById(R.id.btn_tomorrow).setOnClickListener(v -> {
            rescheduleTask(task, 1, hour, minute);
            dialog.dismiss();
        });

        view.findViewById(R.id.btn_two_days).setOnClickListener(v -> {
            rescheduleTask(task, 2, hour, minute);
            dialog.dismiss();
        });

        view.findViewById(R.id.btn_one_week).setOnClickListener(v -> {
            rescheduleTask(task, 7, hour, minute);
            dialog.dismiss();
        });

        view.findViewById(R.id.btn_three_weeks).setOnClickListener(v -> {
            rescheduleTask(task, 21, hour, minute);
            dialog.dismiss();
        });

        view.findViewById(R.id.btn_select_date).setOnClickListener(v -> {
            showDatePicker(task, hour, minute, dialog);
        });
    }

    private int getDaysBetween(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        return (int) ((cal2.getTimeInMillis() - cal1.getTimeInMillis()) / (24 * 60 * 60 * 1000));
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
                int daysToAdd = getDaysBetween(Calendar.getInstance().getTime(), selectedDate);
                rescheduleTask(task, daysToAdd, hour, minute);
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

    private void rescheduleTask(Task task, int daysToAdd, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, daysToAdd);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);

        FirebaseFirestore.getInstance()
                .collection("tasks")
                .document(task.getId())
                .update("startAt", cal.getTime())
                .addOnSuccessListener(aVoid -> {
                    if (requireActivity() instanceof CalendarActivity) {
                        ((CalendarActivity) requireActivity()).selectDate(cal.getTime());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to reschedule task", Toast.LENGTH_SHORT).show());
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