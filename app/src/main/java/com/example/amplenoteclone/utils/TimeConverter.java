package com.example.amplenoteclone.utils;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeConverter {
    public static String convertToTimeAgo(long time) {
        Timestamp timestamp = new Timestamp(TimeUnit.MILLISECONDS.toSeconds(time), 0);

        Date date = timestamp.toDate();
        long timeDifferenceMillis = System.currentTimeMillis() - date.getTime();

        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifferenceMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(timeDifferenceMillis);
        long days = TimeUnit.MILLISECONDS.toDays(timeDifferenceMillis);

        if (seconds < 60) {
            return seconds + " seconds ago";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else if (days < 7) {
            return days + " days ago";
        } else {
            // Format date if older than a week
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(date);
        }
    }

    public static String formatLastUpdated(long time) {
        return "Last updated: " + convertToTimeAgo(time);
    }
}
