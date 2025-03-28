package com.example.amplenoteclone.ui.customviews;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Task;
import com.example.amplenoteclone.note.ViewNoteActivity;
import com.example.amplenoteclone.tasks.TaskHandler;
import com.example.amplenoteclone.tasks.TasksPageActivity;
import com.example.amplenoteclone.utils.TimeConverter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaskCardView extends CardView {

    private Task task;
    private CheckBox checkTask;
    private EditText taskTitle;
    private TextView createdDate;
    private TextView createdTimeAgo1;
    private TextView createdTimeAgo2;
    private TextView taskScore;
    private TextView noteTitle;
    private ImageView expandButton;
    private LinearLayout expandableLayout;
    private ImageView repeatIcon;
    private ImageView startAtIcon;
    private ImageView startAtClockIcon;
    private ImageView startNotiIcon;
    private ImageView hideUntilIcon;
    private ImageView priorityIcon;
    private ImageView durationIcon;
    private LinearLayout repeatCard;
    private TextView repeatOptionText;
    private LinearLayout startAtDateContainer;
    private TextView startAtDateText;
    private LinearLayout startAtPeriodContainer;
    private TextView startAtPeriodText;
    private LinearLayout startAtTimeContainer;
    private TextView startAtTimeText;
    private LinearLayout startNotiCard;
    private MaterialButton startNoti5MinButton;
    private MaterialButton startNoti15MinButton;
    private MaterialButton startNoti60MinButton;
    private MaterialButton startNoti1DayButton;
    private MaterialButton priorityImportantButton;
    private MaterialButton priorityUrgentButton;
    private MaterialButton duration15MinButton;
    private MaterialButton duration30MinButton;
    private MaterialButton duration60MinButton;
    private MaterialButton durationCustomButton;
    private LinearLayout hideUntilDateContainer;
    private TextView hideUntilDateText;
    private LinearLayout hideUntilTimeContainer;
    private TextView hideUntilTimeText;
    private TextView deleteButton;
    private View priorityBar;
    private TextView textDoneUndone;

    public TaskCardView(Context context) {
        super(context);
        init(context);
    }

    public TaskCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TaskCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.item_task, this);

        // Khởi tạo các view
        checkTask = findViewById(R.id.check_task);
        taskTitle = findViewById(R.id.task_title);
        createdDate = findViewById(R.id.task_create_at_date);
        createdTimeAgo1 = findViewById(R.id.task_created_at_time_ago);
        createdTimeAgo2 = findViewById(R.id.task_created_time_ago);
        taskScore = findViewById(R.id.task_score);
        noteTitle = findViewById(R.id.note_title);
        expandButton = findViewById(R.id.expand_button);
        expandableLayout = findViewById(R.id.expandable_layout);
        repeatIcon = findViewById(R.id.repeat_icon);
        startAtIcon = findViewById(R.id.start_at_icon);
        startAtClockIcon = findViewById(R.id.start_at_clock_icon);
        startNotiIcon = findViewById(R.id.start_noti_icon);
        hideUntilIcon = findViewById(R.id.hide_until_icon);
        priorityIcon = findViewById(R.id.priority_icon);
        durationIcon = findViewById(R.id.duration_icon);
        repeatCard = findViewById(R.id.repeat_card);
        repeatOptionText = findViewById(R.id.repeat_option_text);
        startAtDateContainer = findViewById(R.id.start_at_date_container);
        startAtDateText = findViewById(R.id.start_at_date_text);
        startAtPeriodContainer = findViewById(R.id.start_at_period_container);
        startAtPeriodText = findViewById(R.id.start_at_period_text);
        startAtTimeContainer = findViewById(R.id.start_at_time_container);
        startAtTimeText = findViewById(R.id.start_at_time_text);
        startNotiCard = findViewById(R.id.start_noti_card);
        startNoti5MinButton = findViewById(R.id.start_noti_5min);
        startNoti15MinButton = findViewById(R.id.start_noti_15min);
        startNoti60MinButton = findViewById(R.id.start_noti_60min);
        startNoti1DayButton = findViewById(R.id.start_noti_1day);
        priorityImportantButton = findViewById(R.id.priority_important);
        priorityUrgentButton = findViewById(R.id.priority_urgent);
        duration15MinButton = findViewById(R.id.duration_15min);
        duration30MinButton = findViewById(R.id.duration_30min);
        duration60MinButton = findViewById(R.id.duration_60min);
        durationCustomButton = findViewById(R.id.duration_custom);
        hideUntilDateContainer = findViewById(R.id.hide_until_date_container);
        hideUntilDateText = findViewById(R.id.hide_until_date_text);
        hideUntilTimeContainer = findViewById(R.id.hide_until_time_container);
        hideUntilTimeText = findViewById(R.id.hide_until_time_text);
        deleteButton = findViewById(R.id.delete_button);
        priorityBar = findViewById(R.id.task_priority_bar);
        textDoneUndone = findViewById(R.id.text_done_undone);
    }

    public void setTask(Task task) {
        this.task = task;
        if (task != null) {
            setupListeners();
            updateUI();
        }
    }

    public Task getTask() {
        return task;
    }

    public ImageView getExpandButton() {
        return expandButton;
    }

    public void setExpanded(boolean expanded) {
        expandableLayout.setVisibility(expanded ? View.VISIBLE : View.GONE);
        expandButton.setImageResource(expanded ? R.drawable.ic_arrow_collapsed : R.drawable.ic_arrow_expanded);
    }

    private void updateUI() {
        if (task == null) {
            Log.w("TaskCardView", "Task is null, cannot update UI");
            return;
        }

        checkTask.setChecked(task.isCompleted());
        updateDoneUndoneText(task.isCompleted());
        taskTitle.setText(task.getTitle() != null ? task.getTitle() : "");

        taskTitle.setPaintFlags(task.isCompleted() ?
                taskTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG :
                taskTitle.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);

        priorityBar.setBackgroundColor(task.calculatePriorityBarColor(getContext()));
        repeatOptionText.setText(task.getRepeat() != null ? task.getRepeat() : "Doesn't repeat");

        if(task.getStartAtDate() == null || task.getStartAtDate().isEmpty()) {
            startAtDateText.setText(getCurDateSample());
        }
        else {
            startAtDateText.setText(task.getStartAtDate());
        }
        startAtPeriodText.setText(task.getStartAtPeriod() != null ? task.getStartAtPeriod() : "");
        startAtTimeText.setText(task.getStartAtTime() != null ? task.getStartAtTime() : "");


        if(task.getHideUntilDate() == null || task.getHideUntilDate().isEmpty()) {
            hideUntilDateText.setText(getCurDateSample());
            hideUntilDateText.setTextColor(ContextCompat.getColor(getContext(), R.color.textGray));
        }
        else {
            hideUntilDateText.setText(task.getHideUntilDate());
            hideUntilDateText.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        }
        hideUntilTimeText.setText(task.getHideUntilTime() != null ? task.getHideUntilTime() : "");

        taskScore.setText("Task Score " + task.getScore());

        String formattedDate = task.getCreateAt() != null ? getFormattedDateWithSuffix(task.getCreateAt()) : "No Date";
        createdDate.setText(formattedDate);

        String timeAgo = task.getCreateAt() != null ? TimeConverter.convertToTimeAgo(task.getCreateAt()) : "No Time";
        createdTimeAgo1.setText("Created " + timeAgo);
        createdTimeAgo2.setText(" - Created " + timeAgo);

        updateStartAtComponentsColor();
        startNotiCard.setVisibility(task.getStartAtDate().equals(getCurDateSample()) || task.getStartAtDate().isEmpty() ? View.GONE : View.VISIBLE);

        boolean isPast = isPastTime(task.getStartAtDate(), task.getStartAtTime());
        startNoti5MinButton.setEnabled(!isPast);
        startNoti15MinButton.setEnabled(!isPast);
        startNoti60MinButton.setEnabled(!isPast);
        startNoti1DayButton.setEnabled(!isPast);

        updateNotificationButtonStates();
        updatePriorityButtonStates();
        updateDurationButtonStates();

        fetchNoteTitle();
    }

    private void setupListeners() {
        checkTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            updateDoneUndoneText(isChecked);
            taskTitle.setPaintFlags(isChecked ?
                    taskTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG :
                    taskTitle.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            updateTaskInFirestore();
        });

        textDoneUndone.setOnClickListener(v -> {
            task.setCompleted(!task.isCompleted());
            checkTask.setChecked(task.isCompleted());
            updateDoneUndoneText(task.isCompleted());
            taskTitle.setPaintFlags(task.isCompleted() ?
                    taskTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG :
                    taskTitle.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            updateTaskInFirestore();
        });

        setupTaskTitleEditing();

        // Repeat Card
        repeatCard.setOnClickListener(v -> showRepeatOptionsMenu());
        repeatIcon.setOnClickListener(v -> showPopup(v, R.layout.popup_repeat_info));

        // Start At
        startAtDateContainer.setOnClickListener(v -> {
            String defaultDate = getCurDateSample();
            if (task.getStartAtDate().equals(defaultDate) || task.getStartAtDate().isEmpty()) {
                showStartAtDatePickerDialog();
            } else {
                showStartDateOptionsMenu();
            }
        });
        startAtPeriodContainer.setOnClickListener(v -> {
            // Initialize startAtPeriod if null
            if (task.getStartAtPeriod() == null) {
                task.setStartAtPeriod("Morning"); // Set default period
            }
            showPeriodOptionsMenu();
        });
        startAtTimeContainer.setOnClickListener(v -> {
            // Initialize startAtTime if null
            if (task.getStartAtTime() == null) {
                task.setStartAtTime("9:00 am"); // Set default time
            }
            showStartAtTimeOptionsMenu();
        });
        startAtIcon.setOnClickListener(v -> showPopup(v, R.layout.popup_start_at_info));
        startAtClockIcon.setOnClickListener(v -> showPopup(v, R.layout.popup_start_at_clock_icon));

        // Start Noti
        startNotiIcon.setOnClickListener(v -> showPopup(v, R.layout.popup_start_noti_info));
        startNoti5MinButton.setOnClickListener(v -> toggleNotificationButton("5 min"));
        startNoti15MinButton.setOnClickListener(v -> toggleNotificationButton("15 min"));
        startNoti60MinButton.setOnClickListener(v -> toggleNotificationButton("60 min"));
        startNoti1DayButton.setOnClickListener(v -> toggleNotificationButton("1 day"));

        // Hide Until
        hideUntilDateContainer.setOnClickListener(v -> {
            String defaultDate = getCurDateSample();
            if (task.getHideUntilDate().equals(defaultDate) || task.getHideUntilDate().isEmpty()) {
                showHideUntilDatePickerDialog();
            } else {
                showHideUntilDateOptionsMenu();
            }
        });
        hideUntilTimeContainer.setOnClickListener(v -> showHideUntilTimeOptionsMenu());
        hideUntilIcon.setOnClickListener(v -> showPopup(v, R.layout.popup_hide_until_info));

        // Priority
        priorityIcon.setOnClickListener(v -> showPopup(v, R.layout.popup_priority_info));
        priorityImportantButton.setOnClickListener(v -> togglePriorityButton("Important"));
        priorityUrgentButton.setOnClickListener(v -> togglePriorityButton("Urgent"));

        // Duration
        durationIcon.setOnClickListener(v -> showPopup(v, R.layout.popup_duration_info));
        duration15MinButton.setOnClickListener(v -> toggleDurationButton(15));
        duration30MinButton.setOnClickListener(v -> toggleDurationButton(30));
        duration60MinButton.setOnClickListener(v -> toggleDurationButton(60));
        durationCustomButton.setOnClickListener(v -> {
            int currentDuration = task.getDuration();
            String customButtonText = durationCustomButton.getText().toString();

            // Nếu text là "Custom..." hoặc duration là giá trị custom, hiển thị dialog
            if (customButtonText.equals("Custom...") ||
                    (currentDuration != 0 && currentDuration != 15 && currentDuration != 30 && currentDuration != 60)) {
                showCustomDurationDialog();
            }
            // Nếu đã có giá trị "Custom: X min", áp dụng giá trị đó ngay lập tức
            else if (customButtonText.startsWith("Custom:")) {
                if(currentDuration != 0 && currentDuration != 15 && currentDuration != 30 && currentDuration != 60) {
                    showCustomDurationDialog();
                }
                else {
                    String numericPart = customButtonText.replace("Custom:", "").replace("min", "").trim();
                    int customDuration = Integer.parseInt(numericPart);
                    task.setDuration(customDuration);
                    updateTaskInFirestore();
                    updateDurationButtonStates();
                }

            }
        });
        // Delete
        deleteButton.setOnClickListener(v -> deleteTaskFromFirestore());

        // Note Title
        noteTitle.setOnClickListener(v -> {
            if (task.getNoteId() != null && !task.getNoteId().isEmpty()) {
                Intent intent = new Intent(getContext(), ViewNoteActivity.class);
                intent.putExtra("noteId", task.getNoteId());
                getContext().startActivity(intent);
            }
        });
    }

    private void updateTaskInFirestore() {
        if (task != null) {
            task.updateInFirestore(
                    () -> Log.d("TaskCardView", "Update success"),
                    e -> Toast.makeText(getContext(), "Error updating task: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void deleteTaskFromFirestore() {
        if (task != null) {
            task.deleteFromFirestore(
                    () -> Log.d("TaskCardView", "Delete success"),
                    e -> Toast.makeText(getContext(), "Error deleting task: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void updateDoneUndoneText(boolean isCompleted) {
        textDoneUndone.setText(isCompleted ? "UNDONE" : "DONE");
        textDoneUndone.setTextColor(ContextCompat.getColor(getContext(), isCompleted ? R.color.textGray : R.color.textBlue));
    }

    private void fetchNoteTitle() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (task.getNoteId() != null && !task.getNoteId().isEmpty()) {
            db.collection("notes").document(task.getNoteId()).get()
                    .addOnSuccessListener(document -> noteTitle.setText(document.exists() ? document.getString("title") : "Unidentified Note"))
                    .addOnFailureListener(e -> noteTitle.setText("Unidentified Note"));
        } else {
            noteTitle.setText("No Note Linked");
        }
    }

    private void setupTaskTitleEditing() {
        taskTitle.setText(task.getTitle());
        taskTitle.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                saveTaskTitle();
                taskTitle.clearFocus();
                hideKeyboard();
                return true;
            }
            return false;
        });

        taskTitle.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveTaskTitle();
                hideKeyboard();
            }
        });
    }

    private void saveTaskTitle() {
        System.out.println("Saving task title...");
        System.out.println("Current task title: " + task.getTitle());
        System.out.println("New task title: " + taskTitle.getText().toString().trim());
        String newTitle = taskTitle.getText().toString().trim();
        if (!newTitle.equals(task.getTitle())) {
            task.setTitle(newTitle);
            updateTaskInFirestore();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(taskTitle.getWindowToken(), 0);
        }
        taskTitle.clearFocus();
    }

    private String getFormattedDateWithSuffix(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String suffix;
        if (day >= 11 && day <= 13) {
            suffix = "th";
        } else {
            switch (day % 10) {
                case 1:
                    suffix = "st";
                    break;
                case 2:
                    suffix = "nd";
                    break;
                case 3:
                    suffix = "rd";
                    break;
                default:
                    suffix = "th";
                    break;
            }
        }
        return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) +
                " " + day + suffix + ", " +
                calendar.get(Calendar.YEAR);
    }

    private String getCurDateSample() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
        return "e.g. Today, " + dateFormat.format(calendar.getTime());
    }

    private boolean isPastTime(String date, String time) {
        if (date == null || time == null || date.isEmpty() || time.isEmpty()) return false;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
            Calendar taskDateTime = Calendar.getInstance();
            taskDateTime.setTime(dateFormat.parse(date));
            taskDateTime.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));

            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Calendar tempTime = Calendar.getInstance();
            tempTime.setTime(timeFormat.parse(time));

            taskDateTime.set(Calendar.HOUR_OF_DAY, tempTime.get(Calendar.HOUR_OF_DAY));
            taskDateTime.set(Calendar.MINUTE, tempTime.get(Calendar.MINUTE));
            taskDateTime.set(Calendar.SECOND, 0);
            taskDateTime.set(Calendar.MILLISECOND, 0);

            Calendar currentTime = Calendar.getInstance();
            currentTime.set(Calendar.SECOND, 0);
            currentTime.set(Calendar.MILLISECOND, 0);

            return taskDateTime.before(currentTime);
        } catch (Exception e) {
            Log.e("TaskCardView", "Error parsing date/time: " + e.getMessage());
            return false;
        }
    }

    private void toggleNotificationButton(String time) {
        int currentNotificationTime = task.getStartNoti();
        int newNotificationTime;

        switch (time) {
            case "5 min":
                newNotificationTime = 5;
                break;
            case "15 min":
                newNotificationTime = 15;
                break;
            case "60 min":
                newNotificationTime = 60;
                break;
            case "1 day":
                newNotificationTime = 1440;
                break;
            default:
                newNotificationTime = 0;
                break;
        }

        task.setStartNoti(currentNotificationTime == newNotificationTime ? 0 : newNotificationTime);
        updateNotificationButtonStates();
        updateTaskInFirestore();
    }

    private void updateNotificationButtonStates() {
        int selectedTime = task.getStartNoti();
        int defaultBackgroundColor = ContextCompat.getColor(getContext(), R.color.dark_blue_gray);
        int selectedBackgroundColor = ContextCompat.getColor(getContext(), R.color.textBlue);
        int defaultTextColor = ContextCompat.getColor(getContext(), R.color.textGray);
        int selectedTextColor = ContextCompat.getColor(getContext(), R.color.white);

        String selectedTimeString = selectedTime == 1440 ? "1 day" : selectedTime + " min";

        startNoti5MinButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedTimeString.equals("5 min") ? selectedBackgroundColor : defaultBackgroundColor));
        startNoti5MinButton.setTextColor(selectedTimeString.equals("5 min") ? selectedTextColor : defaultTextColor);

        startNoti15MinButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedTimeString.equals("15 min") ? selectedBackgroundColor : defaultBackgroundColor));
        startNoti15MinButton.setTextColor(selectedTimeString.equals("15 min") ? selectedTextColor : defaultTextColor);

        startNoti60MinButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedTimeString.equals("60 min") ? selectedBackgroundColor : defaultBackgroundColor));
        startNoti60MinButton.setTextColor(selectedTimeString.equals("60 min") ? selectedTextColor : defaultTextColor);

        startNoti1DayButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedTimeString.equals("1 day") ? selectedBackgroundColor : defaultBackgroundColor));
        startNoti1DayButton.setTextColor(selectedTimeString.equals("1 day") ? selectedTextColor : defaultTextColor);
    }

    private void togglePriorityButton(String priority) {
        task.setPriority(task.getPriority() != null && task.getPriority().equals(priority) ? "" : priority);
        updatePriorityButtonStates();
        updateTaskInFirestore();
    }

    private void updatePriorityButtonStates() {
        String priority = task.getPriority();
        int defaultBackgroundColor = ContextCompat.getColor(getContext(), R.color.dark_blue_gray);
        int selectedBackgroundColor = ContextCompat.getColor(getContext(), R.color.textBlue);
        int defaultTextColor = ContextCompat.getColor(getContext(), R.color.textGray);
        int selectedTextColor = ContextCompat.getColor(getContext(), R.color.white);

        priorityImportantButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf("Important".equals(priority) ? selectedBackgroundColor : defaultBackgroundColor));
        priorityImportantButton.setTextColor("Important".equals(priority) ? selectedTextColor : defaultTextColor);

        priorityUrgentButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf("Urgent".equals(priority) ? selectedBackgroundColor : defaultBackgroundColor));
        priorityUrgentButton.setTextColor("Urgent".equals(priority) ? selectedTextColor : defaultTextColor);
    }

    private void toggleDurationButton(int duration) {
        task.setDuration(task.getDuration() == duration ? 0 : duration);
        updateDurationButtonStates();
        updateTaskInFirestore();
    }

    private void updateDurationButtonStates() {
        int duration = task.getDuration();
        int defaultBackgroundColor = ContextCompat.getColor(getContext(), R.color.dark_blue_gray);
        int selectedBackgroundColor = ContextCompat.getColor(getContext(), R.color.textBlue);
        int defaultTextColor = ContextCompat.getColor(getContext(), R.color.textGray);
        int selectedTextColor = ContextCompat.getColor(getContext(), R.color.white);

        duration15MinButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(duration == 15 ? selectedBackgroundColor : defaultBackgroundColor));
        duration15MinButton.setTextColor(duration == 15 ? selectedTextColor : defaultTextColor);

        duration30MinButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(duration == 30 ? selectedBackgroundColor : defaultBackgroundColor));
        duration30MinButton.setTextColor(duration == 30 ? selectedTextColor : defaultTextColor);

        duration60MinButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(duration == 60 ? selectedBackgroundColor : defaultBackgroundColor));
        duration60MinButton.setTextColor(duration == 60 ? selectedTextColor : defaultTextColor);

        durationCustomButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(duration > 0 && duration != 15 && duration != 30 && duration != 60 ? selectedBackgroundColor : defaultBackgroundColor));
        if(duration > 0 && duration != 15 && duration != 30 && duration != 60) {
            durationCustomButton.setText("Custom: " + duration + " min");
        }
        durationCustomButton.setTextColor(duration > 0 && duration != 15 && duration != 30 && duration != 60 ? selectedTextColor : defaultTextColor);
    }

    // Popup and Menu Methods
    private void showPopup(View anchorView, int layoutResId) {
        View popupView = LayoutInflater.from(getContext()).inflate(layoutResId, null);
        PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupHeight = popupView.getMeasuredHeight();

        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        int anchorX = location[0];
        int anchorY = location[1];

        int xOffset = anchorX;
        int yOffset = anchorY - popupHeight - anchorView.getHeight() - 20;

        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xOffset, yOffset);
        popupView.setOnTouchListener((v, event) -> {
            popupWindow.dismiss();
            return false;
        });
    }

    private void showRepeatOptionsMenu() {
        Context context = new ContextThemeWrapper(getContext(), R.style.PopupMenuStyle);
        PopupMenu popupMenu = new PopupMenu(context, repeatCard);
        popupMenu.getMenuInflater().inflate(R.menu.repeat_options_menu, popupMenu.getMenu());

        String currentOption = task.getRepeat();
        if ("Doesn't repeat".equals(currentOption)) popupMenu.getMenu().findItem(R.id.option_doesnt_repeat).setChecked(true);
        else if ("On a fixed schedule".equals(currentOption)) popupMenu.getMenu().findItem(R.id.option_fixed_schedule).setChecked(true);
        else if ("When task is complete".equals(currentOption)) popupMenu.getMenu().findItem(R.id.option_when_task_complete).setChecked(true);

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.option_doesnt_repeat) task.setRepeat("Doesn't repeat");
            else if (itemId == R.id.option_fixed_schedule) task.setRepeat("On a fixed schedule");
            else if (itemId == R.id.option_when_task_complete) task.setRepeat("When task is complete");
            else return false;

            repeatOptionText.setText(task.getRepeat());
            updateTaskInFirestore();
            return true;
        });
        popupMenu.show();
    }

    private void updateStartAtComponentsColor() {
        String defaultDate = getCurDateSample();
        String startAtDate = task.getStartAtDate();
        String startAtTime = task.getStartAtTime();
        startAtClockIcon.setVisibility(startAtTime == null || startAtTime.isEmpty() ? GONE : VISIBLE);

        if (startAtDate == null || startAtDate.isEmpty()) {
            startAtDateText.setTextColor(ContextCompat.getColor(getContext(), R.color.textGray));
            startAtPeriodText.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            startAtTimeText.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            startAtClockIcon.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.black));
        } else if (startAtDate.equals(defaultDate) || isPastTime(startAtDate, startAtTime)) {
            startAtDateText.setTextColor(ContextCompat.getColor(getContext(), R.color.textRed));
            startAtPeriodText.setTextColor(ContextCompat.getColor(getContext(), R.color.textRed));
            startAtTimeText.setTextColor(ContextCompat.getColor(getContext(), R.color.textRed));
            startAtClockIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.textRed));
        } else {
            startAtDateText.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            startAtPeriodText.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            startAtTimeText.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            startAtClockIcon.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.black));
        }
    }

    private void ensureStartAtComponents() {
        String defaultDate = getCurDateSample();
        if (task.getStartAtDate() == null || task.getStartAtDate().isEmpty() || task.getStartAtDate().equals(defaultDate)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
            String formattedDate = dateFormat.format(Calendar.getInstance().getTime());
            task.setStartAtDate(formattedDate);
            startAtDateText.setText(formattedDate);
        }
        if (task.getStartAtPeriod() == null || task.getStartAtPeriod().isEmpty()) {
            task.setStartAtPeriod("Morning");
            startAtPeriodText.setText("Morning");
        }
        if (task.getStartAtTime() == null || task.getStartAtTime().isEmpty()) {
            task.setStartAtTime("9:00 am");
            startAtTimeText.setText("9:00 am");
        }
        updateStartAtComponentsColor();
        updateTaskInFirestore();
    }

    private void showStartDateOptionsMenu() {
        Context context = new ContextThemeWrapper(getContext(), R.style.AppTheme);
        PopupMenu popupMenu = new PopupMenu(context, startAtDateContainer);
        popupMenu.getMenuInflater().inflate(R.menu.start_date_options_menu, popupMenu.getMenu());

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        updateDateOptionsMenu(popupMenu, calendar, dateFormat);

        popupMenu.setOnMenuItemClickListener(item -> {
            Calendar newDate = (Calendar) calendar.clone();
            int itemId = item.getItemId();
            if (itemId == R.id.option_today) {}
            else if (itemId == R.id.option_tomorrow) newDate.add(Calendar.DAY_OF_MONTH, 1);
            else if (itemId == R.id.option_this_weekend) {
                int dayOfWeek = newDate.get(Calendar.DAY_OF_WEEK);
                if (dayOfWeek >= Calendar.SATURDAY) newDate.add(Calendar.WEEK_OF_YEAR, 1);
                newDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
            }
            else if (itemId == R.id.option_one_week) newDate.add(Calendar.WEEK_OF_YEAR, 1);
            else if (itemId == R.id.option_three_weeks) newDate.add(Calendar.WEEK_OF_YEAR, 3);
            else if (itemId == R.id.option_choose_date) {
                showStartAtDatePickerDialog();
                return true;
            }
            else return false;

            String formattedDate = dateFormat.format(newDate.getTime());
            task.setStartAtDate(formattedDate);
            startAtDateText.setText(formattedDate);
            ensureStartAtComponents();
            return true;
        });
        popupMenu.show();
    }

    private void showStartAtDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
                    String formattedDate = dateFormat.format(selectedDate.getTime());

                    task.setStartAtDate(formattedDate);
                    startAtDateText.setText(formattedDate);
                    ensureStartAtComponents();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showPeriodOptionsMenu() {
        Context context = new ContextThemeWrapper(getContext(), R.style.AppTheme);
        PopupMenu popupMenu = new PopupMenu(context, startAtPeriodContainer);
        String[] periods = {"Early morning", "Morning", "Afternoon", "Evening", "Late night", "Any time"};
        for (int i = 0; i < periods.length; i++) popupMenu.getMenu().add(0, i, i, periods[i]);

        popupMenu.setOnMenuItemClickListener(item -> {
            String selectedPeriod = item.getTitle().toString();
            task.setStartAtPeriod(selectedPeriod);
            startAtPeriodText.setText(selectedPeriod);

            String time;
            switch (selectedPeriod) {
                case "Early morning":
                    time = "4:00 am";
                    break;
                case "Morning":
                    time = "9:00 am";
                    break;
                case "Afternoon":
                    time = "2:00 pm";
                    break;
                case "Evening":
                    time = "5:00 pm";
                    break;
                case "Late night":
                    time = "9:00 pm";
                    break;
                case "Any time":
                    time = "11:00 am";
                    break;
                default:
                    time = "9:00 am";
                    break;
            }
            task.setStartAtTime(time);
            startAtTimeText.setText(time);

            ensureStartAtComponents();
            return true;
        });
        popupMenu.show();
    }

    private void showStartAtTimeOptionsMenu() {
        Context context = new ContextThemeWrapper(getContext(), R.style.AppTheme);
        PopupMenu popupMenu = new PopupMenu(context, startAtTimeContainer);
        String period = task.getStartAtPeriod() != null ? task.getStartAtPeriod() : "Morning";
        String[] times;

        switch (period) {
            case "Early morning":
                times = new String[]{"3:00 am", "4:00 am", "5:00 am"};
                break;
            case "Morning":
                times = new String[]{"8:00 am", "9:00 am", "10:00 am"};
                break;
            case "Afternoon":
                times = new String[]{"1:00 pm", "2:00 pm", "3:00 pm"};
                break;
            case "Evening":
                times = new String[]{"4:00 pm", "5:00 pm", "6:00 pm"};
                break;
            case "Late night":
                times = new String[]{"8:00 pm", "9:00 pm", "10:00 pm"};
                break;
            case "Any time":
                times = new String[]{"10:00 am", "11:00 am", "12:00 pm"};
                break;
            default:
                times = new String[]{"8:00 am", "9:00 am", "10:00 am"};
                break;
        }

        for (int i = 0; i < times.length; i++) popupMenu.getMenu().add(0, i, i, times[i]);

        popupMenu.setOnMenuItemClickListener(item -> {
            String selectedTime = item.getTitle().toString();
            task.setStartAtTime(selectedTime);
            startAtTimeText.setText(selectedTime);

            if (task.getStartAtDate() == null || task.getStartAtDate().equals(getCurDateSample())) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
                task.setStartAtDate(dateFormat.format(Calendar.getInstance().getTime()));
                startAtDateText.setText(task.getStartAtDate());
            }
            updateStartAtComponentsColor();
            updateTaskInFirestore();
            return true;
        });
        popupMenu.show();
    }

    private void ensureHideUntilComponents() {
        String defaultDate = getCurDateSample();
        if (task.getHideUntilDate() == null || task.getHideUntilDate().isEmpty() || task.getHideUntilDate().equals(defaultDate)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
            String formattedDate = dateFormat.format(Calendar.getInstance().getTime());
            task.setHideUntilDate(formattedDate);
            hideUntilDateText.setText(formattedDate);
        }
        if (task.getHideUntilTime() == null || task.getHideUntilTime().isEmpty()) {
            task.setHideUntilTime("9:00 am");
            hideUntilTimeText.setText("9:00 am");
        }
        updateTaskInFirestore();
    }

    private void showHideUntilDateOptionsMenu() {
        Context context = new ContextThemeWrapper(getContext(), R.style.AppTheme);
        PopupMenu popupMenu = new PopupMenu(context, hideUntilDateContainer);
        popupMenu.getMenuInflater().inflate(R.menu.hide_date_options_menu, popupMenu.getMenu());

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEE, MMM dd 'at' h:mm a", Locale.getDefault());
        updateHideUntilDateOptionsMenu(popupMenu, calendar, dateTimeFormat);

        popupMenu.setOnMenuItemClickListener(item -> {
            Calendar newDate = (Calendar) calendar.clone();
            int itemId = item.getItemId();
            if (itemId == R.id.option_hide_this_afternoon) {
                newDate.set(Calendar.HOUR_OF_DAY, 14);
                newDate.set(Calendar.MINUTE, 0);
            } else if (itemId == R.id.option_hide_tomorrow) {
                newDate.add(Calendar.DAY_OF_MONTH, 1);
                newDate.set(Calendar.HOUR_OF_DAY, 9);
                newDate.set(Calendar.MINUTE, 0);
            } else if (itemId == R.id.option_hide_this_weekend) {
                int dayOfWeek = newDate.get(Calendar.DAY_OF_WEEK);
                if (dayOfWeek >= Calendar.SATURDAY) newDate.add(Calendar.WEEK_OF_YEAR, 1);
                newDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                newDate.set(Calendar.HOUR_OF_DAY, 9);
                newDate.set(Calendar.MINUTE, 0);
            } else if (itemId == R.id.option_hide_one_week) {
                newDate.add(Calendar.WEEK_OF_YEAR, 1);
                newDate.set(Calendar.HOUR_OF_DAY, 9);
                newDate.set(Calendar.MINUTE, 0);
            } else if (itemId == R.id.option_hide_three_weeks) {
                newDate.add(Calendar.WEEK_OF_YEAR, 3);
                newDate.set(Calendar.HOUR_OF_DAY, 9);
                newDate.set(Calendar.MINUTE, 0);
            } else if (itemId == R.id.option_hide_choose_date) {
                showHideUntilDatePickerDialog();
                return true;
            } else return false;

            String formattedDateTime = dateTimeFormat.format(newDate.getTime());
            String[] parts = formattedDateTime.split(" at ");
            if (parts.length == 2) {
                task.setHideUntilDate(parts[0]);
                task.setHideUntilTime(parts[1]);
                hideUntilDateText.setText(parts[0]);
                hideUntilTimeText.setText(parts[1]);
            }
            ensureHideUntilComponents();
            return true;
        });
        popupMenu.show();
    }

    private void showHideUntilTimeOptionsMenu() {
        Context context = new ContextThemeWrapper(getContext(), R.style.AppTheme);
        PopupMenu popupMenu = new PopupMenu(context, hideUntilTimeContainer);
        String[] times = {"8:00 am", "9:00 am", "10:00 am"};
        for (int i = 0; i < times.length; i++) popupMenu.getMenu().add(0, i, i, times[i]);

        popupMenu.setOnMenuItemClickListener(item -> {
            String selectedTime = item.getTitle().toString();
            task.setHideUntilTime(selectedTime);
            hideUntilTimeText.setText(selectedTime);

            if (task.getHideUntilDate() == null || task.getHideUntilDate().equals(getCurDateSample())) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
                task.setHideUntilDate(dateFormat.format(Calendar.getInstance().getTime()));
                hideUntilDateText.setText(task.getHideUntilDate());
            }
            ensureHideUntilComponents();
            return true;
        });
        popupMenu.show();
    }

    private void showHideUntilDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
                    String formattedDate = dateFormat.format(selectedDate.getTime());

                    task.setHideUntilDate(formattedDate);
                    hideUntilDateText.setText(formattedDate);
                    ensureHideUntilComponents();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showCustomDurationDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_custom_duration, null);
        PopupWindow popupWindow = new PopupWindow(dialogView, 380 * (int) getResources().getDisplayMetrics().density, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), android.R.color.transparent));
        popupWindow.setOutsideTouchable(true);

        EditText customDurationInput = dialogView.findViewById(R.id.custom_duration_input);
        MaterialButton customDuration2Hours = dialogView.findViewById(R.id.custom_duration_2hours);
        MaterialButton customDuration4Hours = dialogView.findViewById(R.id.custom_duration_4hours);
        MaterialButton customDurationAllDay = dialogView.findViewById(R.id.custom_duration_allday);
        MaterialButton customDurationApply = dialogView.findViewById(R.id.custom_duration_apply);

        if (task.getDuration() > 0 && task.getDuration() != 15 && task.getDuration() != 30 && task.getDuration() != 60) {
            customDurationInput.setText(task.getDuration() + " min");
        }

        customDuration2Hours.setOnClickListener(v -> customDurationInput.setText("120 min"));
        customDuration4Hours.setOnClickListener(v -> customDurationInput.setText("240 min"));
        customDurationAllDay.setOnClickListener(v -> customDurationInput.setText("1440 min"));

        customDurationApply.setOnClickListener(v -> {
            String newDuration = customDurationInput.getText().toString().trim();
            if (!newDuration.isEmpty()) {
                task.setDuration(Integer.parseInt(newDuration.replace(" min", "")));
                updateDurationButtonStates();
                updateTaskInFirestore();
            }
            popupWindow.dismiss();
        });

        int[] location = new int[2];
        durationCustomButton.getLocationOnScreen(location);
        int xOffset = location[0] - (popupWindow.getWidth() - durationCustomButton.getWidth()) / 2;
        int yOffset = location[1] + durationCustomButton.getHeight();
        popupWindow.showAtLocation(durationCustomButton, Gravity.NO_GRAVITY, xOffset, yOffset);
    }

    private void updateDateOptionsMenu(PopupMenu popupMenu, Calendar calendar, SimpleDateFormat dateFormat) {
        popupMenu.getMenu().findItem(R.id.option_today).setTitle("Today (" + dateFormat.format(calendar.getTime()) + ")");

        Calendar tomorrow = (Calendar) calendar.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        popupMenu.getMenu().findItem(R.id.option_tomorrow).setTitle("Tomorrow (" + dateFormat.format(tomorrow.getTime()) + ")");

        Calendar thisWeekend = (Calendar) calendar.clone();
        int dayOfWeek = thisWeekend.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek >= Calendar.SATURDAY) thisWeekend.add(Calendar.WEEK_OF_YEAR, 1);
        thisWeekend.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        popupMenu.getMenu().findItem(R.id.option_this_weekend).setTitle("This weekend (" + dateFormat.format(thisWeekend.getTime()) + ")");

        Calendar oneWeek = (Calendar) calendar.clone();
        oneWeek.add(Calendar.WEEK_OF_YEAR, 1);
        popupMenu.getMenu().findItem(R.id.option_one_week).setTitle("One week (" + dateFormat.format(oneWeek.getTime()) + ")");

        Calendar threeWeeks = (Calendar) calendar.clone();
        threeWeeks.add(Calendar.WEEK_OF_YEAR, 3);
        popupMenu.getMenu().findItem(R.id.option_three_weeks).setTitle("Three weeks (" + dateFormat.format(threeWeeks.getTime()) + ")");
    }

    private void updateHideUntilDateOptionsMenu(PopupMenu popupMenu, Calendar calendar, SimpleDateFormat dateTimeFormat) {
        Calendar thisAfternoon = (Calendar) calendar.clone();
        thisAfternoon.set(Calendar.HOUR_OF_DAY, 14);
        thisAfternoon.set(Calendar.MINUTE, 0);
        popupMenu.getMenu().findItem(R.id.option_hide_this_afternoon).setTitle("This afternoon (" + dateTimeFormat.format(thisAfternoon.getTime()) + ")");

        Calendar tomorrow = (Calendar) calendar.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 9);
        tomorrow.set(Calendar.MINUTE, 0);
        popupMenu.getMenu().findItem(R.id.option_hide_tomorrow).setTitle("Tomorrow (" + dateTimeFormat.format(tomorrow.getTime()) + ")");

        Calendar thisWeekend = (Calendar) calendar.clone();
        int dayOfWeek = thisWeekend.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek >= Calendar.SATURDAY) thisWeekend.add(Calendar.WEEK_OF_YEAR, 1);
        thisWeekend.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        thisWeekend.set(Calendar.HOUR_OF_DAY, 9);
        thisWeekend.set(Calendar.MINUTE, 0);
        popupMenu.getMenu().findItem(R.id.option_hide_this_weekend).setTitle("This weekend (" + dateTimeFormat.format(thisWeekend.getTime()) + ")");

        Calendar oneWeek = (Calendar) calendar.clone();
        oneWeek.add(Calendar.WEEK_OF_YEAR, 1);
        oneWeek.set(Calendar.HOUR_OF_DAY, 9);
        oneWeek.set(Calendar.MINUTE, 0);
        popupMenu.getMenu().findItem(R.id.option_hide_one_week).setTitle("One week (" + dateTimeFormat.format(oneWeek.getTime()) + ")");

        Calendar threeWeeks = (Calendar) calendar.clone();
        threeWeeks.add(Calendar.WEEK_OF_YEAR, 3);
        threeWeeks.set(Calendar.HOUR_OF_DAY, 9);
        threeWeeks.set(Calendar.MINUTE, 0);
        popupMenu.getMenu().findItem(R.id.option_hide_three_weeks).setTitle("Three weeks (" + dateTimeFormat.format(threeWeeks.getTime()) + ")");
    }
}