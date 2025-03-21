package com.example.amplenoteclone.calendar;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Task;
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

        adapter = new CalendarAdapter(getContext());
        calendarGrid.setAdapter(adapter);


        return view;
    }

    @Override
    public void setSelectedDate(Date date) {
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

        // Format date key
        String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(task.getStartAt());

        // Add task to map
        if (!tasksByDate.containsKey(dateKey)) {
            tasksByDate.put(dateKey, new ArrayList<>());
        }
        tasksByDate.get(dateKey).add(task);

        // Update view
        adapter.notifyDataSetChanged();
    }

    @Override
    public void loadTasksForDate(Date date) {
        if (!isAdded() || date == null) return;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        // Calendar.getInstance();
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

        // Clear existing tasks
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
                            System.out.println("Task: " + task.getTitle());
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
            calendar.set(Calendar.DAY_OF_MONTH, 1);

            days.clear();

            // Previous month days
            int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            calendar.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek);

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

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            Calendar current = Calendar.getInstance();
            current.setTime(selectedDate);

            Calendar today = Calendar.getInstance();

            // Set day number
            dayText.setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));

            // Set day text color
            if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    cal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    cal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                dayText.setTextColor(getResources().getColor(R.color.green));
            } else if (cal.get(Calendar.MONTH) != current.get(Calendar.MONTH)) {
                dayText.setTextColor(getResources().getColor(android.R.color.darker_gray));
            } else {
                dayText.setTextColor(getResources().getColor(android.R.color.black));
            }

            // Clear previous tasks
            taskContainer.removeAllViews();

            // Add task blocks
            String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(date);
            List<Task> tasksForDay = tasksByDate.get(dateKey);

            if (tasksForDay != null) {
                for (Task task : tasksForDay) {
                    TextView taskView = new TextView(context);
                    taskView.setText(task.getTitle());
                    taskView.setTextSize(12);
                    taskView.setMaxLines(1);
                    taskView.setEllipsize(TextUtils.TruncateAt.END);
                    taskView.setPadding(4, 2, 4, 2);
                    taskView.setBackgroundResource(R.drawable.task_calendar_background);
                    taskView.setTextColor(context.getResources().getColor(android.R.color.black));

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(2, 2, 2, 2);
                    taskView.setLayoutParams(params);

                    taskContainer.addView(taskView);
                }
            }

            return convertView;
        }
    }
}