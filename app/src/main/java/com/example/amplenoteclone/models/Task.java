package com.example.amplenoteclone.models;
import android.content.Context;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.amplenoteclone.R;
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

    // Constructor mặc định (yêu cầu bởi Firestore)
    public Task() {
    }

    // Constructor đầy đủ
    public Task(String userId, String noteId, String title, Date createAt, boolean isCompleted, String repeat,
                Date startAt, String startAtDate, String startAtPeriod, String startAtTime, int startNoti,
                Date hideUntil, String hideUntilDate, String hideUntilTime, String priority, int duration, float score) {
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
        return "Task{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", noteId='" + noteId + '\'' +
                ", title='" + title + '\'' +
                ", createAt=" + createAt +
                ", isCompleted=" + isCompleted +
                ", repeat='" + repeat + '\'' +
                ", startAt=" + startAt +
                ", startAtDate='" + startAtDate + '\'' +
                ", startAtPeriod='" + startAtPeriod + '\'' +
                ", startAtTime='" + startAtTime + '\'' +
                ", startNoti=" + startNoti +
                ", hideUntil=" + hideUntil +
                ", hideUntilDate='" + hideUntilDate + '\'' +
                ", hideUntilTime='" + hideUntilTime + '\'' +
                ", priority='" + priority + '\'' +
                ", duration=" + duration +
                ", score=" + score +
                '}';
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

    public void updateInFirestore(Runnable onSuccess, Consumer<Exception> onFailure) {
        if (id == null || id.isEmpty()) {
            onFailure.accept(new Exception("Task ID is null or empty"));
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tasks").document(id)
                .set(this)
                .addOnSuccessListener(aVoid -> onSuccess.run())
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
            batch.update(db.collection("notes").document(noteId), "tasks", FieldValue.arrayRemove(id));
        }
        batch.commit()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onFailure::accept);
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
        if (startAtDate != null && !startAtDate.isEmpty() &&
                startAtTime != null && !startAtTime.isEmpty()) {
            try {
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEEE, MMM d h:mm a", Locale.getDefault());
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
            // Nếu ngày hoặc giờ rỗng, đặt startAt là null
            this.startAt = null;
        }
    }
    private void updateHideUntil() {
        if (hideUntilDate != null && !hideUntilDate.isEmpty() &&
                hideUntilTime != null && !hideUntilTime.isEmpty()) {
            try {
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEEE, MMM d h:mm a", Locale.getDefault());
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
            // Nếu ngày hoặc giờ rỗng, đặt startAt là null
            this.hideUntil = null;
        }
    }
}

