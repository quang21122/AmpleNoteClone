package com.example.amplenoteclone.calendar;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.amplenoteclone.R;

import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import android.widget.FrameLayout;

public class OneDayFragment extends Fragment implements DateSelectable {
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

                if (isToday(selectedDate)) {
                    new Handler().postDelayed(() -> {
                        if (isAdded()) {
                            startTimeUpdates();
                            updateCurrentTimeIndicator();
                            scrollToCurrentTime();
                        }
                    }, INDICATOR_UPDATE_DELAY);
                }
            }
        });
    }

    private void scrollToCurrentTime() {
        if (currentTimeIndicator == null || scrollView == null || !isAdded()) {
            return;
        }

        scrollView.post(() -> {
            // Make sure fragment is still attached and views are valid
            if (!isAdded() || currentTimeIndicator == null || scrollView == null) {
                return;
            }

            try {
                // Get parent height first
                int parentHeight = scrollView.getHeight();
                if (parentHeight == 0) {
                    // View not measured yet, try again later
                    new Handler().postDelayed(this::scrollToCurrentTime, 100);
                    return;
                }

                // Calculate scroll position based on hour slot instead
                Calendar now = Calendar.getInstance();
                int currentHour = now.get(Calendar.HOUR_OF_DAY);
                int currentMinute = now.get(Calendar.MINUTE);

                // Calculate total minutes from midnight
                int totalMinutes = (currentHour * 60) + currentMinute;

                // Calculate scroll position (140dp is the height of each hour slot)
                int hourHeight = dpToPx(140);
                int scrollPosition = (totalMinutes * hourHeight) / 60;

                // Center the current time in the screen
                scrollPosition = Math.max(0, scrollPosition - (parentHeight / 2));

                // Smooth scroll to position
                scrollView.smoothScrollTo(0, scrollPosition);
            } catch (Exception e) {
                // Handle any potential exceptions silently
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Khởi động lại timer khi fragment resume
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
        stopTimeUpdates(); // Dừng timer cũ nếu có

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

    public void setSelectedDate(Date date) {
        this.selectedDate = date;

        if (timelineContainer != null && isAdded()) {
            setupTimeline();

            if (isToday(date)) {
                // Clear existing indicator first
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

            // Time label
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

            // Divider line
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

            // Tính toán vị trí
            float minuteProgress = currentMinute / 60f;
            int slotHeight = timeSlot.getHeight() > 0 ? timeSlot.getHeight() : dpToPx(140);

            // Giới hạn topMargin trong khoảng an toàn
            int maxTopMargin = slotHeight - dpToPx(14); // Để lại khoảng cách an toàn
            int topMargin = Math.min((int)(slotHeight * minuteProgress), maxTopMargin);

            // Tạo và thêm indicator
            FrameLayout.LayoutParams indicatorParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(2));
            indicatorParams.topMargin = topMargin + dpToPx(12);
            currentTimeIndicator.setLayoutParams(indicatorParams);

            timeSlot.addView(currentTimeIndicator);

            // Scroll đến vị trí hiện tại
            scrollToCurrentTime();
        } else if (currentMinute >= 55) {
            // Nếu gần hết giờ, hiển thị ở đầu giờ tiếp theo
            int nextHour = (currentHour + 1) % 24;
            View nextHourSlot = timelineContainer.findViewWithTag("hour_" + nextHour);
            if (nextHourSlot instanceof FrameLayout) {
                FrameLayout nextTimeSlot = (FrameLayout) nextHourSlot;

                currentTimeIndicator = new View(getContext());
                currentTimeIndicator.setBackgroundColor(getResources().getColor(R.color.green));

                FrameLayout.LayoutParams indicatorParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(2));
                indicatorParams.topMargin = dpToPx(12); // Đặt ở đầu slot tiếp theo
                currentTimeIndicator.setLayoutParams(indicatorParams);

                nextTimeSlot.addView(currentTimeIndicator);
                scrollToCurrentTime();
            }
        }
    }

    // Helper method to convert dp to pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}