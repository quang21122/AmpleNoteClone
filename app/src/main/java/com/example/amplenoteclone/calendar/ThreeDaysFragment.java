package com.example.amplenoteclone.calendar;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.amplenoteclone.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ThreeDaysFragment extends Fragment implements DateSelectable {
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

        selectedDate = new Date(); // Default to today
    }

    private void updateDaysHeader() {
        Calendar cal = Calendar.getInstance();

        // Start from the selected date
        cal.setTime(selectedDate);

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat numberFormat = new SimpleDateFormat("d", Locale.getDefault());

        // Day 1 (Selected day)
        day1Number.setText(numberFormat.format(cal.getTime()));
        day1Name.setText(dayFormat.format(cal.getTime()).toUpperCase());
        day1Indicator.setVisibility(isToday(cal.getTime()) ? View.VISIBLE : View.INVISIBLE);

        // Day 2 (Selected day + 1)
        cal.add(Calendar.DAY_OF_MONTH, 1);
        day2Number.setText(numberFormat.format(cal.getTime()));
        day2Name.setText(dayFormat.format(cal.getTime()).toUpperCase());
        day2Indicator.setVisibility(isToday(cal.getTime()) ? View.VISIBLE : View.INVISIBLE);

        // Day 3 (Selected day + 2)
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

            // Left margin container
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

            // Horizontal divider beside time label
            View horizontalDivider = new View(getContext());
            LinearLayout.LayoutParams horizontalDividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(1));// Align with time label
            horizontalDivider.setLayoutParams(horizontalDividerParams);
            horizontalDivider.setBackgroundColor(getResources().getColor(R.color.light_gray));

            // Container for divider and day columns
            LinearLayout contentContainer = new LinearLayout(getContext());
            LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1);
            contentContainer.setLayoutParams(contentParams);
            contentContainer.setOrientation(LinearLayout.VERTICAL);
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

            // Add three equal width day columns
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

        if (isToday(selectedDate)) {
            updateCurrentTimeIndicator();
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
            updateCurrentTimeIndicator();
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
                    if (isAdded() && isToday(selectedDate)) {
                        requireActivity().runOnUiThread(() -> updateCurrentTimeIndicator());
                    }
                    handler.postDelayed(this, 30000); // Update mỗi 30 giây
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

    private void updateCurrentTimeIndicator() {
        if (!isAdded() || timelineContainer == null) return;

        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);

        // Remove old indicator
        if (currentTimeIndicator != null) {
            ViewGroup parent = (ViewGroup) currentTimeIndicator.getParent();
            if (parent != null) {
                parent.removeView(currentTimeIndicator);
            }
        }

        View hourSlot = timelineContainer.findViewWithTag("hour_" + currentHour);
        if (!(hourSlot instanceof LinearLayout)) return;

        LinearLayout timeSlot = (LinearLayout) hourSlot;

        View containerView = timeSlot.getChildAt(2);
        if (!(containerView instanceof LinearLayout)) return;

        LinearLayout contentContainer = (LinearLayout) containerView;
        LinearLayout daysContainer = (LinearLayout) contentContainer.getChildAt(1);

        // Calculate indicator position
        float minuteProgress = currentMinute / 60f;
        int slotHeight = timeSlot.getHeight() > 0 ? timeSlot.getHeight() : dpToPx(140);
        int maxTopMargin = slotHeight - dpToPx(14);
        int topMargin = Math.min((int)(slotHeight * minuteProgress), maxTopMargin);

        // Create container for indicator and dividers
        FrameLayout indicatorContainer = new FrameLayout(getContext());
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        indicatorContainer.setLayoutParams(containerParams);

        // Create and add the time indicator
        View timeIndicator = new View(getContext());
        FrameLayout.LayoutParams indicatorParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(2));
        indicatorParams.topMargin = topMargin;
        timeIndicator.setLayoutParams(indicatorParams);
        timeIndicator.setBackgroundColor(getResources().getColor(R.color.green));
        timeIndicator.setElevation(1f);

        // Add vertical dividers
        for (int i = 1; i <= 2; i++) {
            View divider = new View(getContext());
            FrameLayout.LayoutParams dividerParams = new FrameLayout.LayoutParams(
                    dpToPx(1),
                    ViewGroup.LayoutParams.MATCH_PARENT);
            dividerParams.leftMargin = (daysContainer.getWidth() * i) / 3 - dpToPx(1) / 2;
            divider.setLayoutParams(dividerParams);
            divider.setBackgroundColor(getResources().getColor(R.color.light_gray));
            divider.setElevation(2f);
            indicatorContainer.addView(divider);
        }

        // Add indicator under dividers
        indicatorContainer.addView(timeIndicator, 0);

        // Replace daysContainer content with indicator container
        daysContainer.removeAllViews();
        daysContainer.addView(indicatorContainer);

        currentTimeIndicator = indicatorContainer;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public void setSelectedDate(Date date) {
        this.selectedDate = date;
        if (getView() != null) {
            setupTimeline();
            updateDaysHeader();
            if (isToday(date)) {
                startTimeUpdates();
                updateCurrentTimeIndicator();
            } else {
                stopTimeUpdates();
            }
        }
    }
}