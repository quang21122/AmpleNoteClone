package com.example.amplenoteclone.utils;

import com.google.firebase.Timestamp;

public class TimeConverter {
    public static String convertToString(Timestamp timestamp) {
        return timestamp.toDate().toString();
    }
    public static String convertToTimeAgo(long time) {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - time;

        if (timeDifference < 60000) {
            return "Just now";
        } else if (timeDifference < 3600000) {
            return timeDifference / 60000 + " minutes ago";
        } else if (timeDifference < 86400000) {
            return timeDifference / 3600000 + " hours ago";
        } else if (timeDifference < 604800000) {
            return timeDifference / 86400000 + " days ago";
        } else if (timeDifference < 2592000000L) {
            return timeDifference / 604800000 + " weeks ago";
        } else if (timeDifference < 31536000000L) {
            return timeDifference / 2592000000L + " months ago";
        } else {
            return timeDifference / 31536000000L + " years ago";
        }
    }
}
