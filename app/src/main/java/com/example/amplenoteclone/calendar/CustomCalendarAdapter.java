package com.example.amplenoteclone.calendar;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.amplenoteclone.R;

import java.util.Date;
import java.util.Calendar;
import java.util.List;

public class CustomCalendarAdapter extends BaseAdapter {
    private final Context context;
    private final List<Date> dates;
    private final Calendar currentDate;
    private Date selectedDate;

    public CustomCalendarAdapter(Context context, List<Date> dates) {
        this.context = context;
        this.dates = dates;
        this.currentDate = Calendar.getInstance();
        this.selectedDate = currentDate.getTime();
    }

    public Date getSelectedDate() {
        return selectedDate;
    }

    @Override
    public int getCount() {
        return dates.size();
    }

    @Override
    public Object getItem(int position) {
        return dates.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.calendar_day_cell, parent, false);
        }

        TextView dayView = (TextView) convertView;
        Date date = dates.get(position);

        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            dayView.setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));

            // Reset states
            dayView.setTextColor(context.getResources().getColor(R.color.black));
            dayView.setBackground(null);

            // Check if date is today
            boolean isToday = isSameDay(cal, currentDate.getTime());
            if (isToday) {
                dayView.setTextColor(context.getResources().getColor(R.color.calendar_today));
            }

            // Check if date is selected
            if (selectedDate != null && isSameDay(cal, selectedDate)) {
                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.OVAL);
                shape.setColor(context.getResources().getColor(R.color.calendar_selected));
                dayView.setBackground(shape);
                dayView.setTextColor(context.getResources().getColor(R.color.white));
            }
        } else {
            dayView.setText("");
            dayView.setBackground(null);
        }

        return convertView;
    }

    private boolean isSameDay(Calendar cal1, Date date2) {
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    public void setSelectedDate(Date date) {
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            this.selectedDate = cal.getTime();
        } else {
            this.selectedDate = null;
        }
        notifyDataSetChanged();
    }
}
