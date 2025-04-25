package com.example.amplenoteclone.utils;

import android.content.Context;

import com.example.amplenoteclone.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeConverter {
    public static String convertToTimeAgo(Context context, Date date) {
        long timeDifferenceMillis = System.currentTimeMillis() - date.getTime();

        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifferenceMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(timeDifferenceMillis);
        long days = TimeUnit.MILLISECONDS.toDays(timeDifferenceMillis);

        if (seconds < 60) {
            return seconds + " " + context.getString(R.string.seconds_ago);
        } else if (minutes < 60) {
            return minutes + " " + context.getString(R.string.minutes_ago);
        } else if (hours < 24) {
            return hours + " " + context.getString(R.string.hours_ago);
        } else if (days < 7) {
            return days + " " + context.getString(R.string.days_ago);
        } else {
            // Format date if older than a week
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(date);
        }
    }

    public static String formatLastUpdated(Context context, Date date) {
        return context.getString(R.string.last_updated) + " " + convertToTimeAgo(context, date);
    }
}
