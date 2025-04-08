package com.example.amplenoteclone.models;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.tasks.TaskNotificationReceiver;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.WriteBatch;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;

public class Task implements Serializable {
    private String id;
    private String userId;
    private String noteId;
    private String title;
    private Date createAt;
    private boolean isCompleted;
    private String repeat;
    private Date startAt;
    private String startAtDate;
    private String startAtPeriod;
    private String startAtTime;
    private int startNoti;
    private Date hideUntil;
    private String hideUntilDate;
    private String hideUntilTime;
    private String priority;
    private int duration;
    private float score;
    private String details;

    // Constructor mặc định (yêu cầu bởi Firestore)
    public Task() {}

    // Constructor để tạo task mới
    public Task(String userId, String noteId, String title) {
        this.userId = userId;
        this.noteId = noteId;
        this.title = title;
        this.createAt = new Date();
        this.isCompleted = false;
        this.repeat = "Doesn't repeat";
        this.startAtDate = "";
        this.startAtTime = "";
        this.startAtPeriod = "";
        this.startNoti = 0;
        this.hideUntilDate = "";
        this.hideUntilTime = "";
        this.priority = "";
        this.duration = 0;
        this.score = 1.0f;
    }

    // Constructor đầy đủ
    public Task(
            String userId,
            String noteId,
            String title,
            Date createAt,
            boolean isCompleted,
            String repeat,
            Date startAt,
            String startAtDate,
            String startAtPeriod,
            String startAtTime,
            int startNoti,
            Date hideUntil,
            String hideUntilDate,
            String hideUntilTime,
            String priority,
            int duration,
            float score) {
        this.userId = userId;
        this.noteId = noteId;
        this.title = title;
        this.createAt = createAt;
        this.isCompleted = isCompleted;
        this.repeat = repeat;
        this.startAt = startAt;
        this.startAtDate = startAtDate;
        this.startAtPeriod = startAtPeriod;
        this.startAtTime = startAtTime;
        this.startNoti = startNoti;
        this.hideUntil = hideUntil;
        this.hideUntilDate = hideUntilDate;
        this.hideUntilTime = hideUntilTime;
        this.priority = priority;
        this.duration = duration;
        this.score = score;
    }

    @Override
    public String toString() {
        return "Task{"
                + "id='"
                + id
                + '\''
                + ", userId='"
                + userId
                + '\''
                + ", noteId='"
                + noteId
                + '\''
                + ", title='"
                + title
                + '\''
                + ", createAt="
                + createAt
                + ", isCompleted="
                + isCompleted
                + ", repeat='"
                + repeat
                + '\''
                + ", startAt="
                + startAt
                + ", startAtDate='"
                + startAtDate
                + '\''
                + ", startAtPeriod='"
                + startAtPeriod
                + '\''
                + ", startAtTime='"
                + startAtTime
                + '\''
                + ", startNoti="
                + startNoti
                + ", hideUntil="
                + hideUntil
                + ", hideUntilDate='"
                + hideUntilDate
                + '\''
                + ", hideUntilTime='"
                + hideUntilTime
                + '\''
                + ", priority='"
                + priority
                + '\''
                + ", duration="
                + duration
                + ", score="
                + score
                + '}';
    }

    // Getters và Setters với @PropertyName để ánh xạ tên trường trên Firestore
    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("userId")
    public String getUserId() {
        return userId;
    }

    @PropertyName("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @PropertyName("noteId")
    public String getNoteId() {
        return noteId;
    }

    @PropertyName("noteId")
    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @PropertyName("createAt")
    public Date getCreateAt() {
        return createAt;
    }

    @PropertyName("createAt")
    public void setCreateAt(Date createAt) {
        this.createAt = createAt != null ? createAt : new Date();
    }

    @PropertyName("isCompleted")
    public boolean isCompleted() {
        return isCompleted;
    }

    @PropertyName("isCompleted")
    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    @PropertyName("startAt")
    public Date getStartAt() {
        return startAt;
    }

    @PropertyName("startAt")
    public void setStartAt(Date startAt) {
        this.startAt = startAt;
        updateStartAtComponent();
    }

    @PropertyName("startAtDate")
    public String getStartAtDate() {
        return startAtDate;
    }

    @PropertyName("startAtDate")
    public void setStartAtDate(String startAtDate) {
        this.startAtDate = startAtDate;
        updateStartAt();
    }

    @PropertyName("startAtPeriod")
    public String getStartAtPeriod() {
        return startAtPeriod;
    }

    @PropertyName("startAtPeriod")
    public void setStartAtPeriod(String startAtPeriod) {
        this.startAtPeriod = startAtPeriod;
    }

    @PropertyName("startAtTime")
    public String getStartAtTime() {
        return startAtTime;
    }

    @PropertyName("startAtTime")
    public void setStartAtTime(String startAtTime) {
        this.startAtTime = startAtTime;
        updateStartAt();
    }

    @PropertyName("startNoti")
    public int getStartNoti() {
        return startNoti;
    }

    @PropertyName("startNoti")
    public void setStartNoti(int startNoti) {
        this.startNoti = startNoti;
    }

    @PropertyName("hideUntil")
    public Date getHideUntil() {
        return hideUntil;
    }

    @PropertyName("hideUntil")
    public void setHideUntil(Date hideUntil) {
        this.hideUntil = hideUntil;
    }

    @PropertyName("hideUntilDate")
    public String getHideUntilDate() {
        return hideUntilDate;
    }

    @PropertyName("hideUntilDate")
    public void setHideUntilDate(String hideUntilDate) {
        this.hideUntilDate = hideUntilDate;
        updateHideUntil();
    }

    @PropertyName("hideUntilTime")
    public String getHideUntilTime() {
        return hideUntilTime;
    }

    @PropertyName("hideUntilTime")
    public void setHideUntilTime(String hideUntilTime) {
        this.hideUntilTime = hideUntilTime;
        updateHideUntil();
    }

    @PropertyName("priority")
    public String getPriority() {
        return priority;
    }

    @PropertyName("priority")
    public void setPriority(String priority) {
        this.priority = priority;
        calculateScore();
    }

    @PropertyName("duration")
    public int getDuration() {
        return duration;
    }

    @PropertyName("duration")
    public void setDuration(int duration) {
        this.duration = duration;
        calculateScore();
    }

    @PropertyName("score")
    public float getScore() {
        return score;
    }

    @PropertyName("score")
    public void setScore(float score) {
        this.score = score;
    }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public void createInFirestore(Context context, Runnable onSuccess, Consumer<Exception> onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String taskId = this.id;
        if(taskId == null || taskId.isEmpty()) {
            taskId = db.collection("tasks").document().getId(); // Tạo ID mới
            this.id = taskId;
        }

        db.collection("tasks")
                .document(taskId)
                .set(this)
                .addOnSuccessListener(
                        aVoid -> {
                            scheduleNotification(context); // Lên lịch thông báo
                            onSuccess.run();
                        })
                .addOnFailureListener(onFailure::accept);
    }

    public void updateInFirestore(Runnable onSuccess, Consumer<Exception> onFailure) {
        if (id == null || id.isEmpty()) {
            onFailure.accept(new Exception("Task ID is null or empty"));
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tasks")
                .document(id)
                .set(this)
                .addOnSuccessListener(
                        aVoid -> {
                            onSuccess.run();
                            scheduleNotification(
                                    db.getApp()
                                            .getApplicationContext()); // Lên lịch sau khi cập nhật
                        })
                .addOnFailureListener(onFailure::accept);
    }

    public void deleteFromFirestore(Runnable onSuccess, Consumer<Exception> onFailure) {
        if (id == null || id.isEmpty()) {
            onFailure.accept(new Exception("Task ID is null or empty"));
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();
        batch.delete(db.collection("tasks").document(id));
        if (noteId != null && !noteId.isEmpty()) {
            batch.update(
                    db.collection("notes").document(noteId), "tasks", FieldValue.arrayRemove(id));
        }
        batch.commit()
                .addOnSuccessListener(
                        aVoid -> {
                            cancelNotification(
                                    db.getApp()
                                            .getApplicationContext()); // Hủy thông báo trước khi
                                                                       // xóa
                            onSuccess.run();
                        })
                .addOnFailureListener(onFailure::accept);
    }

    public void scheduleNotification(Context context) {
        if (startAt == null || startNoti <= 0) {
            cancelNotification(context);
            return;
        }

        // Check if we have notification permission using NotificationManagerCompat
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (!notificationManager.areNotificationsEnabled()) {
            Log.d(
                    "Task",
                    "Notifications are not enabled - skipping notification schedule for task "
                            + id);
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TaskNotificationReceiver.class);
        intent.putExtra("taskId", id);
        intent.putExtra("taskTitle", title);

        // Tạo PendingIntent với flag để cập nhật nếu đã tồn tại
        int requestCode = id.hashCode(); // Đảm bảo mỗi task có requestCode riêng
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Tính thời gian thông báo: startAt - startNoti (phút)
        long startAtMillis = startAt.getTime();
        long notificationTimeMillis =
                startAtMillis - (startNoti * 60 * 1000); // Chuyển phút thành milliseconds
        long currentTimeMillis = System.currentTimeMillis();

        if (notificationTimeMillis > currentTimeMillis) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTimeMillis, pendingIntent);
            Log.d(
                    "Task",
                    "Notification scheduled for task "
                            + id
                            + " at "
                            + new Date(notificationTimeMillis));
        } else {
            Log.d("Task", "Notification time is in the past for task " + id);
        }
    }

    public void cancelNotification(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TaskNotificationReceiver.class);
        int requestCode = id.hashCode();
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
        Log.d("Task", "Notification canceled for task " + id);
    }

    public int calculateBorderTypeByScore() {
        if (score >= 4) {
            return R.drawable.task_border_high;
        } else if (score >= 2) {
            return R.drawable.task_border_medium;
        } else {
            return R.drawable.task_border_low;
        }
    }

    public int calculatePriorityBarColor(Context context) {
        if (score >= 4) {
            return ContextCompat.getColor(context, R.color.textRed);
        } else if (score >= 2) {
            return ContextCompat.getColor(context, R.color.textBlue);
        } else {
            return ContextCompat.getColor(context, R.color.light_gray);
        }
    }

    private void calculateScore() {
        float baseScore = 1;

        // Calculate score based on priority
        if (priority != null) {
            switch (priority) {
                case "Important":
                    baseScore += 0.6;
                    break;
                case "Urgent":
                    baseScore += 3;
                    break;
            }
        }

        // Calculate score based on duration
        if (duration > 0) {
            if (duration <= 15) {
                baseScore += 2;
            } else if (duration <= 30) {
                baseScore += 1;
            } else {
                baseScore += 0.5;
            }
        }

        this.score = baseScore;
    }

    private void updateStartAt() {
        if (startAtDate != null
                && !startAtDate.isEmpty()
                && startAtTime != null
                && !startAtTime.isEmpty()) {
            try {
                SimpleDateFormat dateTimeFormat =
                        new SimpleDateFormat("EEEE, MMM d h:mm a", Locale.getDefault());
                String dateTimeString = startAtDate + " " + startAtTime;
                Date parsedDate = dateTimeFormat.parse(dateTimeString);

                if (parsedDate != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(parsedDate);
                    calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                    this.startAt = calendar.getTime();
                }
            } catch (ParseException e) {
                Log.e("Task", "Error parsing date/time", e);
            }
        } else {
            this.startAt = null;
        }
    }

    private void updateHideUntil() {
        if (hideUntilDate != null
                && !hideUntilDate.isEmpty()
                && hideUntilTime != null
                && !hideUntilTime.isEmpty()) {
            try {
                SimpleDateFormat dateTimeFormat =
                        new SimpleDateFormat("EEEE, MMM d h:mm a", Locale.getDefault());
                String dateTimeString = hideUntilDate + " " + hideUntilTime;
                Date parsedDate = dateTimeFormat.parse(dateTimeString);

                if (parsedDate != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(parsedDate);
                    calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                    this.hideUntil = calendar.getTime();
                }
            } catch (ParseException e) {
                Log.e("Task", "Error parsing date/time", e);
            }
        } else {
            this.hideUntil = null;
        }
    }

    private void updateStartAtComponent() {
        if (startAt != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            SimpleDateFormat hourFormat = new SimpleDateFormat("HH", Locale.getDefault());

            this.startAtDate = dateFormat.format(startAt);
            this.startAtTime = timeFormat.format(startAt);
            int hour = Integer.parseInt(hourFormat.format(startAt));
            this.startAtPeriod = getPeriodFromHour(hour);

        } else {
            this.startAtDate = "";
            this.startAtTime = "";
            this.startAtPeriod = "";
        }
    }

    private String getPeriodFromHour(int hour) {
        if (hour >= 3 && hour < 7) {
            return "Early morning";
        } else if (hour >= 8 && hour < 11) {
            return "Morning";
        } else if (hour >= 12 && hour < 16) {
            return "Afternoon";
        } else if (hour >= 16 && hour < 19) {
            return "Evening";
        } else if (hour >= 20 && hour < 23) {
            return "Latenight";
        } else {
            return "Anytime";
        }
    }
}
