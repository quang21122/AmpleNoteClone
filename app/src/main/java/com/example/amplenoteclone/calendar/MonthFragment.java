package com.example.amplenoteclone.calendar;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.example.amplenoteclone.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MonthFragment extends Fragment implements DateSelectable {
    private GridView calendarGrid;
    private Date selectedDate;
    private CalendarAdapter adapter;

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
    }

    private void updateCalendar() {
        if (adapter != null && selectedDate != null) {
            adapter.setDate(selectedDate);
        }
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
            Date date = days.get(position);

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            Calendar current = Calendar.getInstance();
            current.setTime(selectedDate);

            Calendar today = Calendar.getInstance();

            dayText.setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));

            // Check if date is today
            if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    cal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    cal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                dayText.setTextColor(getResources().getColor(R.color.green));
            }
            // Check if date is in current month
            else if (cal.get(Calendar.MONTH) != current.get(Calendar.MONTH)) {
                dayText.setTextColor(getResources().getColor(android.R.color.darker_gray));
            } else {
                dayText.setTextColor(getResources().getColor(android.R.color.black));
            }

            return convertView;
        }
    }
}