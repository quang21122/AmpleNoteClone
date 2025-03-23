package com.example.amplenoteclone.calendar;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MonthFragment extends Fragment implements DateSelectable, TaskView {
    private GridView calendarGrid;
    private Date selectedDate;
    private CalendarAdapter adapter;

    private HashMap<String, List<Task>> tasksByDate = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_month, container, false);
        calendarGrid = view.findViewById(R.id.calendarGrid);

        selectedDate = new Date();

        adapter = new CalendarAdapter(getContext());
        calendarGrid.setAdapter(adapter);

        setSelectedDate(selectedDate);

        return view;
    }

    @Override
    public void setSelectedDate(Date date) {
        clearAllTasks();
        this.selectedDate = date;
        updateCalendar();
        new Handler().post(() -> loadTasksForDate(date));
    }

    private void updateCalendar() {
        if (adapter != null && selectedDate != null) {
            adapter.setDate(selectedDate);
        }
    }

    @Override
    public void addTaskToTimeline(Task task) {
        if (task.getStartAt() == null) return;

        String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(task.getStartAt());

        if (!tasksByDate.containsKey(dateKey)) {
            tasksByDate.put(dateKey, new ArrayList<>());
        }

        List<Task> tasksForDay = tasksByDate.get(dateKey);
        for (Task existingTask : tasksForDay) {
            if (existingTask.getId().equals(task.getId())) {
                return;
            }
        }

        tasksForDay.add(task);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void loadTasksForDate(Date date) {
        if (!isAdded() || date == null) return;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startMonth = cal.getTime();

        // Get first day of next month
        cal.add(Calendar.MONTH, 1);
        Date endMonth = cal.getTime();

        tasksByDate.clear();

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
                            task.setDuration(document.getLong("duration") != null ?
                                    document.getLong("duration").intValue() : 0);
                            task.setCompleted(document.getBoolean("isCompleted") != null ?
                                    document.getBoolean("isCompleted") : false);
                            addTaskToTimeline(task);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }

    private class CalendarAdapter extends BaseAdapter {
        private Calendar calendar;
        private Context context;
        private List<Date> days;

        public CalendarAdapter(Context context) {
            this.context = context;
            this.calendar = Calendar.getInstance();
            days = new ArrayList<>();
        }

        public void setDate(Date date) {
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1); // Đặt về ngày 1 của tháng

            days.clear();

            int currentMonth = calendar.get(Calendar.MONTH);

            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            calendar.add(Calendar.DAY_OF_MONTH, -(firstDayOfWeek - Calendar.MONDAY));

            while (days.size() < 42) {
                days.add(calendar.getTime());
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return days.size();
        }

        @Override
        public Object getItem(int position) {
            return days.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context)
                        .inflate(R.layout.layout_calendar_day, parent, false);
            }

            TextView dayText = convertView.findViewById(R.id.dayText);
            LinearLayout taskContainer = convertView.findViewById(R.id.taskContainer);
            Date date = days.get(position);

            Calendar displayMonth = Calendar.getInstance();
            displayMonth.setTime(selectedDate);

            Calendar dateCal = Calendar.getInstance();
            dateCal.setTime(date);

            boolean isCurrentMonth = dateCal.get(Calendar.MONTH) == displayMonth.get(Calendar.MONTH);

            dayText.setText(String.valueOf(dateCal.get(Calendar.DAY_OF_MONTH)));

            if (isToday(date)) {
                dayText.setTextColor(context.getResources().getColor(R.color.green));
            } else if (!isCurrentMonth) {
                dayText.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            } else {
                dayText.setTextColor(context.getResources().getColor(android.R.color.black));
            }

            taskContainer.removeAllViews();
            String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(date);
            List<Task> tasksForDay = tasksByDate.get(dateKey);
            if (tasksForDay != null) {
                addTasksToContainer(taskContainer, tasksForDay);
            }

            return convertView;
        }

        private void addTasksToContainer(LinearLayout container, List<Task> tasks) {
            for (Task task : tasks) {
                TextView taskView = new TextView(context);
                taskView.setText(task.getTitle());
                taskView.setTextSize(12);
                taskView.setMaxLines(1);
                taskView.setEllipsize(TextUtils.TruncateAt.END);
                if (task.isCompleted()) {
                    taskView.setPaintFlags(taskView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                taskView.setPadding(4, 2, 4, 2);
                taskView.setBackgroundResource(R.drawable.task_calendar_background);
                taskView.setTextColor(context.getResources().getColor(android.R.color.black));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(2, 2, 2, 2);
                taskView.setLayoutParams(params);
                taskView.setOnClickListener(v -> showTaskDialog(task));
                container.addView(taskView);
            }
        }
    }

    private boolean isToday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Calendar today = Calendar.getInstance();
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                cal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);
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
                        updateTaskCompletionStatus(task);
                        adapter.notifyDataSetChanged();
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

    private void updateTaskCompletionStatus(Task task) {
        String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(task.getStartAt());

        if (tasksByDate.containsKey(dateKey)) {
            List<Task> tasksForDay = tasksByDate.get(dateKey);
            for (Task existingTask : tasksForDay) {
                if (existingTask.getId().equals(task.getId())) {
                    existingTask.setCompleted(task.isCompleted());
                    break;
                }
            }
        }
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
                    clearAllTasks();
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

    private void clearAllTasks() {
        tasksByDate.clear();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}