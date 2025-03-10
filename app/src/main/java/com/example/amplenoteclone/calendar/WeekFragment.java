package com.example.amplenoteclone.calendar;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.amplenoteclone.R;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WeekFragment extends Fragment implements DateSelectable {
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
        return view;
    }

    @Override
    public void setSelectedDate(Date date) {
        this.selectedDate = date;
        updateDayHeaders();
        setupTimeline();
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
}