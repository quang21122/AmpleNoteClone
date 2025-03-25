package com.example.amplenoteclone.adapters;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.PopupMenu;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Note;
import com.example.amplenoteclone.models.Task;
import com.example.amplenoteclone.note.ViewNoteActivity;
import com.example.amplenoteclone.tasks.TasksPageActivity;
import com.example.amplenoteclone.utils.TimeConverter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnItemClickListener listener;

    public void setTasks(ArrayList<Task> tasks) {
        this.taskList = tasks;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onExpandClick(int position);
    }

    public TaskAdapter(List<Task> taskList, OnItemClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.checkTask.setChecked(task.isCompleted());
        holder.taskTitle.setText(task.getTitle());
        holder.repeatOptionText.setText(task.getRepeat());
        holder.startAtPeriodText.setText(task.getStartAtPeriod());
        holder.startAtTimeText.setText(task.getStartAtTime());
        holder.startAtDateText.setText(task.getStartAtDate());
        holder.hideUntilTimeText.setText(task.getHideUntilTime());
        holder.hideUntilDateText.setText(task.getHideUntilDate());
        holder.taskScore.setText("Task Score " + task.getScore());

        // Set task creation date
        String formattedDate = getFormattedDateWithSuffix(task.getCreateAt());
        holder.createdDate.setText(formattedDate);

        // Set task creation time ago
        String timeAgo = TimeConverter.convertToTimeAgo(task.getCreateAt());
        holder.createdTimeAgo1.setText("Created " + timeAgo);
        holder.createdTimeAgo2.setText(" - Created " + timeAgo);

        String curDateSample = getCurDateSample();
        if (holder.startAtDateText.getText() == null || holder.startAtDateText.getText().toString().isEmpty()) {
            holder.startAtDateText.setText(curDateSample);
        }
        if (holder.hideUntilDateText.getText() == null || holder.hideUntilDateText.getText().toString().isEmpty()) {
            holder.hideUntilDateText.setText(curDateSample);
        }

        // Cập nhật màu sắc cho các thành phần thời gian
        // Ensure startAtDate is not null before calling equals
        String startAtDate = task.getStartAtDate();
        System.out.println("StartAtDate: " + startAtDate);

        if (startAtDate != null) {
            updateStartAtComponentsColor(task, holder.startAtDateText, holder.startAtPeriodText,
                    holder.startAtTimeText, holder.startAtClockIcon, holder.itemView.getContext());
        }
        // Ẩn/hiện icon đồng hồ dựa trên giá trị của startAtTimeText
        if (task.getStartAtTime() == null || task.getStartAtTime().isEmpty()) {
            holder.startAtClockIcon.setVisibility(View.GONE);
        } else {
            holder.startAtClockIcon.setVisibility(View.VISIBLE);
        }

        // Ẩn/hiện Start Noti Card dựa trên giá trị của startDate
        if (task.getStartAtDate().equals(curDateSample) || task.getStartAtDate().isEmpty()) {
            holder.startNotiCard.setVisibility(View.GONE);
        } else {
            holder.startNotiCard.setVisibility(View.VISIBLE);
        }

        // Kiểm tra isPastTime và vô hiệu hóa các nút trong Start Noti Card nếu cần
        boolean isPast = isPastTime(task.getStartAtDate(), task.getStartAtTime());
        holder.startNoti5MinButton.setEnabled(!isPast);
        holder.startNoti15MinButton.setEnabled(!isPast);
        holder.startNoti60MinButton.setEnabled(!isPast);
        holder.startNoti1DayButton.setEnabled(!isPast);

        // Cập nhật trạng thái chọn cho các nút trong Start Noti Card, Priority Card và Duration Card
        updateNotificationButtonStates(task, holder);
        updatePriorityButtonStates(task, holder);
        updateDurationButtonStates(task, holder);

        // Fetch and set note title
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notes").document(task.getNoteId()).get().addOnSuccessListener(document -> {
            if (document.exists()) {
                String noteTitle = document.getString("title");
                holder.noteTitle.setText(noteTitle);
            } else {
                holder.noteTitle.setText("Unidentified Note");
            }
        }).addOnFailureListener(e -> {
            holder.noteTitle.setText("Unidentified Note");
        });


        // Xử lý sự kiện nhấn vào expand button
        holder.expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.expandableLayout.getVisibility() == View.GONE) {
                    holder.expandableLayout.setVisibility(View.VISIBLE);
                    holder.expandButton.setImageResource(R.drawable.ic_arrow_collapsed);
                } else {
                    holder.expandableLayout.setVisibility(View.GONE);
                    holder.expandButton.setImageResource(R.drawable.ic_arrow_expanded);
                }
                if (listener != null) {
                    listener.onExpandClick(position);
                }
            }
        });

        // Xử lý sự kiện nhấn vào icon
        holder.repeatIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v, R.layout.popup_repeat_info);
            }
        });
        holder.startAtIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v, R.layout.popup_start_at_info);
            }
        });
        holder.startAtClockIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v, R.layout.popup_start_at_clock_icon);
            }
        });
        holder.startNotiIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v, R.layout.popup_start_noti_info);
            }
        });
        holder.hideUntilIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v, R.layout.popup_hide_until_info);
            }
        });
        holder.priorityIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v, R.layout.popup_priority_info);
            }
        });
        holder.durationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v, R.layout.popup_duration_info);
            }
        });

        // Xử lý sự kiện nhấn vào REPEAT card để hiển thị PopupMenu
        holder.repeatCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = new ContextThemeWrapper(v.getContext(), R.style.PopupMenuStyle);
                PopupMenu popupMenu = new PopupMenu(context, holder.repeatCard);
                popupMenu.getMenuInflater().inflate(R.menu.repeat_options_menu, popupMenu.getMenu());

                String currentOption = task.getRepeat();
                if (currentOption.equals("Doesn't repeat")) {
                    popupMenu.getMenu().findItem(R.id.option_doesnt_repeat).setChecked(true);
                } else if (currentOption.equals("On a fixed schedule")) {
                    popupMenu.getMenu().findItem(R.id.option_fixed_schedule).setChecked(true);
                } else if (currentOption.equals("When task is complete")) {
                    popupMenu.getMenu().findItem(R.id.option_when_task_complete).setChecked(true);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.id.option_doesnt_repeat) {
                            holder.repeatOptionText.setText("Doesn't repeat");
                            task.setRepeat("Doesn't repeat");
                        } else if (itemId == R.id.option_fixed_schedule) {
                            holder.repeatOptionText.setText("On a fixed schedule");
                            task.setRepeat("On a fixed schedule");
                        } else if (itemId == R.id.option_when_task_complete) {
                            holder.repeatOptionText.setText("When task is complete");
                            task.setRepeat("When task is complete");
                        } else {
                            return false;
                        }

                        if (task.getId() != null && !task.getId().isEmpty()) {
                            ((TasksPageActivity) holder.itemView.getContext()).updateTaskInFirestore(task);
                        } else {
                            Log.e("TaskAdapter", "Task ID is null or empty. Cannot update Firestore.");
                        }
                        return true;
                    }
                });

                popupMenu.show();
            }
        });

        // Xử lý sự kiện nhấn vào phần START AT để chọn ngày
        holder.startAtDateContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String defaultDate = getCurDateSample();
                if (task.getStartAtDate().equals(defaultDate) || task.getStartAtDate().isEmpty()) {
                    showStartAtDatePickerDialog(holder.startAtDateText, task, position, holder.startAtPeriodText,
                            holder.startAtTimeText, holder.startAtClockIcon);
                } else {
                    showStartDateOptionsMenu(v.getContext(), holder.startAtDateContainer, holder.startAtDateText,
                            task, position, holder.startAtPeriodText, holder.startAtTimeText,
                            holder.startAtClockIcon);
                }
            }
        });

        // Xử lý sự kiện nhấn vào "Morning"
        holder.startAtPeriodContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPeriodOptionsMenu(v.getContext(), holder.startAtPeriodContainer, holder.startAtPeriodText,
                        task, position, holder.startAtDateText, holder.startAtTimeText,
                        holder.startAtClockIcon);
            }

        });

        // Xử lý sự kiện nhấn vào START AT "9:00 am"
        holder.startAtTimeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartAtTimeOptionsMenu(v.getContext(), holder.startAtTimeContainer, holder.startAtTimeText,
                        holder.startAtPeriodText, task, position, holder.startAtDateText,
                        holder.startAtClockIcon);
            }
        });

        // Xử lý sự kiện nhấn vào phần HIDE UNTIL để chọn ngày
        holder.hideUntilDateContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String defaultDate = getCurDateSample();
                if (task.getHideUntilDate().equals(defaultDate) || task.getHideUntilDate().isEmpty()) {
                    showHideUntilDatePickerDialog(holder.hideUntilDateText, task, position, holder.hideUntilTimeText);
                } else {
                    showHideUntilDateOptionsMenu(v.getContext(), holder.hideUntilDateContainer, holder.hideUntilDateText,
                            task, position, holder.hideUntilTimeText);
                }
            }
        });

        // Xử lý sự kiện nhấn vào HIDE UNTIL "9:00 am"
        holder.hideUntilTimeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHideUntilTimeOptionsMenu(v.getContext(), holder.hideUntilTimeContainer, holder.hideUntilTimeText,
                        task, position, holder.hideUntilDateText);
            }
        });

        // Xử lý sự kiện nhấn vào các nút trong Start Noti Card
        holder.startNoti5MinButton.setOnClickListener(v -> toggleNotificationButton(task, "5 min", holder));
        holder.startNoti15MinButton.setOnClickListener(v -> toggleNotificationButton(task, "15 min", holder));
        holder.startNoti60MinButton.setOnClickListener(v -> toggleNotificationButton(task, "60 min", holder));
        holder.startNoti1DayButton.setOnClickListener(v -> toggleNotificationButton(task, "1 day", holder));

        // Xử lý sự kiện nhấn vào các nút trong Priority Card
        holder.priorityImportantButton.setOnClickListener(v -> togglePriorityButton(task, "Important", holder));
        holder.priorityUrgentButton.setOnClickListener(v -> togglePriorityButton(task, "Urgent", holder));

        // Xử lý sự kiện nhấn vào các nút trong Duration Card
        holder.duration15MinButton.setOnClickListener(v -> toggleDurationButton(task, "15 min", holder));
        holder.duration30MinButton.setOnClickListener(v -> toggleDurationButton(task, "30 min", holder));
        holder.duration60MinButton.setOnClickListener(v -> toggleDurationButton(task, "60 min", holder));
        holder.durationCustomButton.setOnClickListener(v -> handleCustomDurationButtonClick(task, holder, position));

        // Xử lý sự kiện khi thay đổi trạng thái checkbox
        holder.checkTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            if (task.getId() != null && !task.getId().isEmpty()) {
                ((TasksPageActivity) holder.itemView.getContext()).updateTaskInFirestore(task);
            } else {
                Log.e("TaskAdapter", "Task ID is null or empty. Cannot update Firestore.");
            }
        });

        // Xử lý sự kiện khi thay đổi tiêu đề task
        holder.taskTitle.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String newTitle = holder.taskTitle.getText().toString().trim();
                if (!newTitle.equals(task.getTitle())) {
                    task.setTitle(newTitle);
                    if (task.getId() != null && !task.getId().isEmpty()) {
                        ((TasksPageActivity) holder.itemView.getContext()).updateTaskInFirestore(task);
                    } else {
                        Log.e("TaskAdapter", "Task ID is null or empty. Cannot update Firestore.");
                    }
                }
            }
        });

        // Set OnClickListener for taskTitle
//        holder.taskTitle.setOnClickListener(v -> {
//            Intent intent = new Intent(holder.itemView.getContext(), ViewNoteActivity.class);
//            Note note = getNoteById(task.getNoteId()); // Implement this method to get the Note object by its ID
//            intent.putExtra("note", note);
//            holder.itemView.getContext().startActivity(intent);
//        });

        // Set OnClickListener for the delete button
        holder.deleteButton.setOnClickListener(v -> {
            if (task.getId() != null && !task.getId().isEmpty()) {
                ((TasksPageActivity) holder.itemView.getContext()).deleteTaskFromFirestore(task);
            } else {
                Log.e("TaskAdapter", "Task ID is null or empty. Cannot delete from Firestore.");
            }
        });
    }

    // Hàm cập nhật trạng thái giao diện của các nút trong Duration Card
    private void updateDurationButtonStates(Task task, TaskViewHolder holder) {
        int selectedDuration = task.getDuration();
        Context context = holder.itemView.getContext();

        int defaultBackgroundColor = ContextCompat.getColor(context, R.color.dark_blue_gray);
        int selectedBackgroundColor = ContextCompat.getColor(context, R.color.textBlue);
        int defaultTextColor = ContextCompat.getColor(context, R.color.textGray);
        int selectedTextColor = ContextCompat.getColor(context, R.color.white);

        String selectedDurationString = selectedDuration + " min";

        holder.duration15MinButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                selectedDurationString.equals("15 min") ? selectedBackgroundColor : defaultBackgroundColor));
        holder.duration15MinButton.setTextColor(
                selectedDurationString.equals("15 min") ? selectedTextColor : defaultTextColor);

        holder.duration30MinButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                selectedDurationString.equals("30 min") ? selectedBackgroundColor : defaultBackgroundColor));
        holder.duration30MinButton.setTextColor(
                selectedDurationString.equals("30 min") ? selectedTextColor : defaultTextColor);

        holder.duration60MinButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                selectedDurationString.equals("60 min") ? selectedBackgroundColor : defaultBackgroundColor));
        holder.duration60MinButton.setTextColor(
                selectedDurationString.equals("60 min") ? selectedTextColor : defaultTextColor);

        // Update Custom button with value if available
        String customText = "Custom...";
        if (!selectedDurationString.equals("0 min") && !selectedDurationString.equals("15 min") && !selectedDurationString.equals("30 min") && !selectedDurationString.equals("60 min")) {
            customText = "Custom: "+ selectedDurationString;
            holder.durationCustomButton.setText(customText);
            holder.durationCustomButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedBackgroundColor));
            holder.durationCustomButton.setTextColor(selectedTextColor);
        } else {
            holder.durationCustomButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(defaultBackgroundColor));
            holder.durationCustomButton.setTextColor(defaultTextColor);
        }
    }

    // Hàm hiển thị dialog cho nút "Custom..." dưới dạng PopupWindow
    private void showCustomDurationDialog(Task task, TaskViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_custom_duration, (ViewGroup) holder.itemView.getRootView(), false);

        int widthInDp = 280;
        float density = context.getResources().getDisplayMetrics().density;
        int widthInPx = (int) (widthInDp * density);

        // Tạo PopupWindow với chiều rộng cố định
        PopupWindow popupWindow = new PopupWindow(
                dialogView,
                widthInPx,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        // Đặt nền cho PopupWindow
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, android.R.color.transparent));
        popupWindow.setOutsideTouchable(true); // Cho phép đóng popup khi chạm bên ngoài

        // Lấy các thành phần trong dialog
        EditText customDurationInput = dialogView.findViewById(R.id.custom_duration_input);
        MaterialButton customDuration2Hours = dialogView.findViewById(R.id.custom_duration_2hours);
        MaterialButton customDuration4Hours = dialogView.findViewById(R.id.custom_duration_4hours);
        MaterialButton customDurationAllDay = dialogView.findViewById(R.id.custom_duration_allday);
        MaterialButton customDurationApply = dialogView.findViewById(R.id.custom_duration_apply);

        // Đặt giá trị mặc định cho ô nhập liệu nếu có giá trị custom hiện tại
        String currentDuration = task.getDuration() + " min";
        if (currentDuration.startsWith("Custom:")) {
            String customValue = currentDuration.replace("Custom:", "").trim();
            customDurationInput.setText(customValue);
        }

        // Xử lý chọn các tùy chọn có sẵn
        customDuration2Hours.setOnClickListener(v -> customDurationInput.setText("120 min"));
        customDuration4Hours.setOnClickListener(v -> customDurationInput.setText("240 min"));
        customDurationAllDay.setOnClickListener(v -> customDurationInput.setText("1440 min"));

        // Xử lý nút APPLY
        customDurationApply.setOnClickListener(v -> {
            String newDuration = customDurationInput.getText().toString().trim();
            if (!newDuration.isEmpty()) {
                String currentCustomValue = currentDuration.startsWith("Custom:") ? currentDuration.replace("Custom:", "").trim() : "";
                if (!newDuration.equals(currentCustomValue)) {
                    task.setDuration(Integer.parseInt(newDuration.replace(" min", "")));
                    updateDurationButtonStates(task, holder);
                    if (task.getId() != null && !task.getId().isEmpty()) {
                        ((TasksPageActivity) holder.itemView.getContext()).updateTaskInFirestore(task);
                    } else {
                        Log.e("TaskAdapter", "Task ID is null or empty. Cannot update Firestore.");
                    }
//                    notifyItemChanged(position);
                }
            }
            popupWindow.dismiss();
        });

        // Đo kích thước của dialogView để đặt vị trí chính xác
        dialogView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int popupWidth = dialogView.getMeasuredWidth();
        int popupHeight = dialogView.getMeasuredHeight();

        // Lấy vị trí của nút "Custom..." trên màn hình
        int[] location = new int[2];
        holder.durationCustomButton.getLocationOnScreen(location);
        int anchorX = location[0];
        int anchorY = location[1];

        // Tính toán vị trí để hiển thị PopupWindow (ngay dưới nút "Custom...")
        int xOffset = anchorX - (popupWidth - holder.durationCustomButton.getWidth()) / 2; // Căn giữa theo nút
        int yOffset = anchorY + holder.durationCustomButton.getHeight(); // Hiển thị ngay dưới nút

        // Hiển thị PopupWindow
        popupWindow.showAtLocation(holder.durationCustomButton, Gravity.NO_GRAVITY, xOffset, yOffset);
        popupWindow.update();
    }

    // Hàm xử lý sự kiện nhấn vào nút "Custom..." trong Duration Card
    private void handleCustomDurationButtonClick(Task task, TaskViewHolder holder, int position) {
        int currentDuration = task.getDuration();
        String customButtonText = holder.durationCustomButton.getText().toString();

        // Nếu duration là "Custom..." (chưa có giá trị cụ thể), hiển thị dialog để nhập giá trị
        if (customButtonText.equals("Custom...")) {
            showCustomDurationDialog(task, holder, position);
            return;
        }

        // Nếu duration bắt đầu bằng "Custom:" (đã có giá trị cụ thể như "Custom: 4 hours")
        if (currentDuration != 0 && currentDuration != 15 && currentDuration != 30 && currentDuration != 60) {
            showCustomDurationDialog(task, holder, position);
        } else {
            String numericPart = customButtonText
                    .replace("Custom:", "")
                    .replace("min", "")
                    .trim();
            int customDuration = Integer.parseInt(numericPart);
            task.setDuration(customDuration);
            if (task.getId() != null && !task.getId().isEmpty()) {
                ((TasksPageActivity) holder.itemView.getContext()).updateTaskInFirestore(task);
            } else {
                Log.e("TaskAdapter", "Task ID is null or empty. Cannot update Firestore.");
            }
            updateDurationButtonStates(task, holder);
        }
    }

    // Hàm xử lý chọn/bỏ chọn nút trong Duration Card
    private void toggleDurationButton(Task task, String duration, TaskViewHolder holder) {
        String currentDuration = String.valueOf(task.getDuration()) + " min";
        if (currentDuration.equals(duration)) {
            task.setDuration(0);
        } else {
            // Remove " min" and parse the integer value
            int newDuration = Integer.parseInt(duration.replace(" min", ""));
            task.setDuration(newDuration);
        }
        updateDurationButtonStates(task, holder);
        if (task.getId() != null && !task.getId().isEmpty()) {
            ((TasksPageActivity) holder.itemView.getContext()).updateTaskInFirestore(task);
        } else {
            Log.e("TaskAdapter", "Task ID is null or empty. Cannot update Firestore.");
        }
    }

    // Hàm cập nhật trạng thái giao diện của các nút trong Priority Card
    private void updatePriorityButtonStates(Task task, TaskViewHolder holder) {
        String selectedPriority = task.getPriority();
        Context context = holder.itemView.getContext();

        int defaultBackgroundColor = ContextCompat.getColor(context, R.color.dark_blue_gray);
        int selectedBackgroundColor = ContextCompat.getColor(context, R.color.textBlue);
        int defaultTextColor = ContextCompat.getColor(context, R.color.textGray);
        int selectedTextColor = ContextCompat.getColor(context, R.color.white);

        holder.priorityImportantButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                selectedPriority != null && selectedPriority.equals("Important") ? selectedBackgroundColor : defaultBackgroundColor));
        holder.priorityImportantButton.setTextColor(
                selectedPriority != null && selectedPriority.equals("Important") ? selectedTextColor : defaultTextColor);

        holder.priorityUrgentButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                selectedPriority != null && selectedPriority.equals("Urgent") ? selectedBackgroundColor : defaultBackgroundColor));
        holder.priorityUrgentButton.setTextColor(
                selectedPriority != null && selectedPriority.equals("Urgent") ? selectedTextColor : defaultTextColor);
    }

    // Hàm xử lý chọn/bỏ chọn nút trong Priority Card
    private void togglePriorityButton(Task task, String priority, TaskViewHolder holder) {
        String currentPriority = task.getPriority();

        if (currentPriority != null && currentPriority.equals(priority)) {
            task.setPriority(null);
        } else {
            task.setPriority(priority);
        }

        updatePriorityButtonStates(task, holder);
        if (task.getId() != null && !task.getId().isEmpty()) {
            ((TasksPageActivity) holder.itemView.getContext()).updateTaskInFirestore(task);
        } else {
            Log.e("TaskAdapter", "Task ID is null or empty. Cannot update Firestore.");
        }
    }

    // Hàm cập nhật trạng thái giao diện của các nút trong Start Noti Card
    private void updateNotificationButtonStates(Task task, TaskViewHolder holder) {
        int selectedTime = task.getStartNoti();
        Context context = holder.itemView.getContext();

        int defaultBackgroundColor = ContextCompat.getColor(context, R.color.dark_blue_gray);
        int selectedBackgroundColor = ContextCompat.getColor(context, R.color.textBlue);
        int defaultTextColor = ContextCompat.getColor(context, R.color.textGray);
        int selectedTextColor = ContextCompat.getColor(context, R.color.white);

        String selectedTimeString = selectedTime == 1440 ? "1 day" : selectedTime + " min";

        holder.startNoti5MinButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                selectedTimeString.equals("5 min") ? selectedBackgroundColor : defaultBackgroundColor));
        holder.startNoti5MinButton.setTextColor(
                selectedTimeString.equals("5 min") ? selectedTextColor : defaultTextColor);

        holder.startNoti15MinButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                selectedTimeString.equals("15 min") ? selectedBackgroundColor : defaultBackgroundColor));
        holder.startNoti15MinButton.setTextColor(
                selectedTimeString.equals("15 min") ? selectedTextColor : defaultTextColor);

        holder.startNoti60MinButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                selectedTimeString.equals("60 min") ? selectedBackgroundColor : defaultBackgroundColor));
        holder.startNoti60MinButton.setTextColor(
                selectedTimeString.equals("60 min") ? selectedTextColor : defaultTextColor);

        holder.startNoti1DayButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                selectedTimeString.equals("1 day") ? selectedBackgroundColor : defaultBackgroundColor));
        holder.startNoti1DayButton.setTextColor(
                selectedTimeString.equals("1 day") ? selectedTextColor : defaultTextColor);
    }

    // Hàm xử lý chọn/bỏ chọn nút trong Start Noti Card
    private void toggleNotificationButton(Task task, String time, TaskViewHolder holder) {
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
                newNotificationTime = 1440; // 1 day = 1440 minutes
                break;
            default:
                newNotificationTime = 0;
                break;
        }

        if (currentNotificationTime == newNotificationTime) {
            task.setStartNoti(0);
        } else {
            task.setStartNoti(newNotificationTime);
        }

        updateNotificationButtonStates(task, holder);
        if (task.getId() != null && !task.getId().isEmpty()) {
            ((TasksPageActivity) holder.itemView.getContext()).updateTaskInFirestore(task);
        } else {
            Log.e("TaskAdapter", "Task ID is null or empty. Cannot update Firestore.");
        }
    }

    private void showHideUntilTimeOptionsMenu(Context context, View anchorView, TextView timeTextView,
                                              Task task, int position, TextView dateTextView) {
        Context themeContext = new ContextThemeWrapper(context, R.style.AppTheme);
        PopupMenu popupMenu = new PopupMenu(themeContext, anchorView);
        String[] times;
        times = new String[]{"8:00 am", "9:00 am", "10:00 am"};

        for (int i = 0; i < times.length; i++) {
            popupMenu.getMenu().add(0, i, i, times[i]);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            String selectedTime = item.getTitle().toString();
            timeTextView.setText(selectedTime);
            task.setHideUntilTime(selectedTime);

            Calendar calendar = Calendar.getInstance();
            String defaultDate = getCurDateSample();
            if (task.getHideUntilDate().equals(defaultDate) || task.getHideUntilDate().isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
                String formattedDate = dateFormat.format(calendar.getTime());
                dateTextView.setText(formattedDate);
                task.setHideUntilDate(formattedDate);
            }

            if (task.getId() != null && !task.getId().isEmpty()) {
                ((TasksPageActivity) context).updateTaskInFirestore(task);
            } else {
                Log.e("TaskAdapter", "Task ID is null or empty. Cannot update Firestore.");
            }
//            notifyItemChanged(position);
            return true;
        });

        popupMenu.show();
    }

    private void showHideUntilDateOptionsMenu(Context context, View anchorView, TextView dateTextView, Task task,
                                              int position, TextView timeTextView) {
        Context themeContext = new ContextThemeWrapper(context, R.style.AppTheme);
        PopupMenu popupMenu = new PopupMenu(themeContext, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.hide_date_options_menu, popupMenu.getMenu());

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEE, MMM dd 'at' h:mm a", Locale.getDefault());

        // Định dạng ngày và giờ cho "This afternoon" (giả sử 2pm)
        Calendar thisAfternoon = (Calendar) calendar.clone();
        thisAfternoon.set(Calendar.HOUR_OF_DAY, 14); // 2pm
        thisAfternoon.set(Calendar.MINUTE, 0);
        thisAfternoon.set(Calendar.SECOND, 0);
        String thisAfternoonDateTime = dateTimeFormat.format(thisAfternoon.getTime());
        MenuItem thisAfternoonItem = popupMenu.getMenu().findItem(R.id.option_hide_this_afternoon);
        thisAfternoonItem.setTitle("This afternoon (" + thisAfternoonDateTime + ")");

        // Định dạng ngày và giờ cho "Tomorrow" (giả sử 9:00 am)
        Calendar tomorrow = (Calendar) calendar.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 9); // 9am
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);
        String tomorrowDateTime = dateTimeFormat.format(tomorrow.getTime());
        MenuItem tomorrowItem = popupMenu.getMenu().findItem(R.id.option_hide_tomorrow);
        tomorrowItem.setTitle("Tomorrow (" + tomorrowDateTime + ")");

        // Định dạng ngày và giờ cho "This weekend" (thứ Bảy, giả sử 9:00 am)
        Calendar thisWeekend = (Calendar) calendar.clone();
        int dayOfWeek = thisWeekend.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek >= Calendar.SATURDAY) {
            thisWeekend.add(Calendar.WEEK_OF_YEAR, 1); // Chuyển sang cuối tuần tiếp theo nếu đã qua thứ Bảy
        }
        thisWeekend.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY); // Đặt sang thứ Bảy
        thisWeekend.set(Calendar.HOUR_OF_DAY, 9); // 9am
        thisWeekend.set(Calendar.MINUTE, 0);
        thisWeekend.set(Calendar.SECOND, 0);
        String weekendDateTime = dateTimeFormat.format(thisWeekend.getTime());
        MenuItem weekendItem = popupMenu.getMenu().findItem(R.id.option_hide_this_weekend);
        weekendItem.setTitle("This weekend (" + weekendDateTime + ")");

        // Định dạng ngày và giờ cho "One week" (giả sử 9:00 am)
        Calendar oneWeek = (Calendar) calendar.clone();
        oneWeek.add(Calendar.WEEK_OF_YEAR, 1);
        oneWeek.set(Calendar.HOUR_OF_DAY, 9); // 9am
        oneWeek.set(Calendar.MINUTE, 0);
        oneWeek.set(Calendar.SECOND, 0);
        String oneWeekDateTime = dateTimeFormat.format(oneWeek.getTime());
        MenuItem oneWeekItem = popupMenu.getMenu().findItem(R.id.option_hide_one_week);
        oneWeekItem.setTitle("One week (" + oneWeekDateTime + ")");

        // Định dạng ngày và giờ cho "Three weeks" (giả sử 9:00 am)
        Calendar threeWeeks = (Calendar) calendar.clone();
        threeWeeks.add(Calendar.WEEK_OF_YEAR, 3);
        threeWeeks.set(Calendar.HOUR_OF_DAY, 9); // 9am
        threeWeeks.set(Calendar.MINUTE, 0);
        threeWeeks.set(Calendar.SECOND, 0);
        String threeWeeksDateTime = dateTimeFormat.format(threeWeeks.getTime());
        MenuItem threeWeeksItem = popupMenu.getMenu().findItem(R.id.option_hide_three_weeks);
        threeWeeksItem.setTitle("Three weeks (" + threeWeeksDateTime + ")");

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            Calendar newDate = (Calendar) calendar.clone(); // Sao chép ngày hiện tại để tính toán
            if (itemId == R.id.option_hide_this_afternoon) {
                newDate.set(Calendar.HOUR_OF_DAY, 14); // 2pm
                newDate.set(Calendar.MINUTE, 0);
                newDate.set(Calendar.SECOND, 0);
            } else if (itemId == R.id.option_hide_tomorrow) {
                newDate.add(Calendar.DAY_OF_MONTH, 1);
                newDate.set(Calendar.HOUR_OF_DAY, 9); // 9am
                newDate.set(Calendar.MINUTE, 0);
                newDate.set(Calendar.SECOND, 0);
            } else if (itemId == R.id.option_hide_this_weekend) {
                int currentDayOfWeek = newDate.get(Calendar.DAY_OF_WEEK);
                if (currentDayOfWeek >= Calendar.SATURDAY) {
                    newDate.add(Calendar.WEEK_OF_YEAR, 1); // Chuyển sang cuối tuần tiếp theo
                }
                newDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY); // Đặt sang thứ Bảy
                newDate.set(Calendar.HOUR_OF_DAY, 9); // 9am
                newDate.set(Calendar.MINUTE, 0);
                newDate.set(Calendar.SECOND, 0);
            } else if (itemId == R.id.option_hide_one_week) {
                newDate.add(Calendar.WEEK_OF_YEAR, 1);
                newDate.set(Calendar.HOUR_OF_DAY, 9); // 9am
                newDate.set(Calendar.MINUTE, 0);
                newDate.set(Calendar.SECOND, 0);
            } else if (itemId == R.id.option_hide_three_weeks) {
                newDate.add(Calendar.WEEK_OF_YEAR, 3);
                newDate.set(Calendar.HOUR_OF_DAY, 9); // 9am
                newDate.set(Calendar.MINUTE, 0);
                newDate.set(Calendar.SECOND, 0);
            } else if (itemId == R.id.option_hide_choose_date) {
                showHideUntilDatePickerDialog(dateTextView, task, position, timeTextView);
                return true;
            }
            String formattedDateTime = dateTimeFormat.format(newDate.getTime());
            // Tách ngày và giờ để gán riêng
            String[] parts = formattedDateTime.split(" at ");
            if (parts.length == 2) {
                dateTextView.setText(parts[0]); // "Wed, Mar 19"
                task.setHideUntilDate(parts[0]);
                task.setHideUntilTime(parts[1]); // "2pm"
                timeTextView.setText(parts[1]);
            }

            if (task.getId() != null && !task.getId().isEmpty()) {
                ((TasksPageActivity) context).updateTaskInFirestore(task);
            } else {
                Log.e("TaskAdapter", "Task ID is null or empty. Cannot update Firestore.");
            }
//            notifyItemChanged(position);
            return true;
        });

        popupMenu.show();
    }

    private void showHideUntilDatePickerDialog(TextView dateTextView, Task task, int position,
                                               TextView timeTextView) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                dateTextView.getContext(),
                (view, year1, month1, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, month1, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
                    String formattedDate = dateFormat.format(selectedDate.getTime());

                    dateTextView.setText(formattedDate);
                    task.setHideUntilDate(formattedDate);

                    if (task.getHideUntilTime() == null || task.getHideUntilTime().isEmpty()) {
                        task.setHideUntilTime("9:00 am");
                        timeTextView.setText("9:00 am");
                    }

                    if (task.getId() != null && !task.getId().isEmpty()) {
                        ((TasksPageActivity) dateTextView.getContext()).updateTaskInFirestore(task);
                    } else {
                        Log.e("TaskAdapter", "Task ID is null or empty. Cannot update Firestore.");
                    }
//                    notifyItemChanged(position);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    // START AT
    private void showStartAtTimeOptionsMenu(Context context, View anchorView, TextView timeTextView,
                                            TextView periodTextView, Task task, int position, TextView dateTextView,
                                            ImageView clockIcon) {
        Context themeContext = new ContextThemeWrapper(context, R.style.AppTheme);
        PopupMenu popupMenu = new PopupMenu(themeContext, anchorView);
        String[] times;
        String currentPeriod = periodTextView.getText().toString();

        if (currentPeriod.equals("Early morning")) {
            times = new String[]{"3:00 am", "4:00 am", "5:00 am"};
        } else if (currentPeriod.equals("Morning")) {
            times = new String[]{"8:00 am", "9:00 am", "10:00 am"};
        } else if (currentPeriod.equals("Afternoon")) {
            times = new String[]{"1:00 pm", "2:00 pm", "3:00 pm"};
        } else if (currentPeriod.equals("Evening")) {
            times = new String[]{"4:00 pm", "5:00 pm", "6:00 pm"};
        } else if (currentPeriod.equals("Late night")) {
            times = new String[]{"8:00 pm", "9:00 pm", "10:00 pm"};
        } else if (currentPeriod.equals("Any time")) {
            times = new String[]{"10:00 am", "11:00 am", "12:00 pm"};
        } else {
            times = new String[]{"8:00 am", "9:00 am", "10:00 am"};
        }

        for (int i = 0; i < times.length; i++) {
            popupMenu.getMenu().add(0, i, i, times[i]);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            String selectedTime = item.getTitle().toString();
            timeTextView.setText(selectedTime);
            task.setStartAtTime(selectedTime);

            if (selectedTime.equals("8:00 am") || selectedTime.equals("9:00 am") || selectedTime.equals("10:00 am")) {
                periodTextView.setText("Morning");
                task.setStartAtPeriod("Morning");

                Calendar calendar = Calendar.getInstance();
                String defaultDate = getCurDateSample();
                if (task.getStartAtDate().equals(defaultDate) || task.getStartAtDate().isEmpty()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
                    String formattedDate = dateFormat.format(calendar.getTime());
                    dateTextView.setText(formattedDate);
                    task.setStartAtDate(formattedDate);
                }
            }

            updateStartAtComponentsColor(task, dateTextView, periodTextView, timeTextView, clockIcon, context);
            if (task.getId() != null && !task.getId().isEmpty()) {
                ((TasksPageActivity) context).updateTaskInFirestore(task);
            } else {
                Log.e("TaskAdapter", "Task ID is null or empty. Cannot update Firestore.");
            }
//            notifyItemChanged(position);
            return true;
        });

        popupMenu.show();
    }

    private void showPeriodOptionsMenu(Context context, View anchorView, TextView periodTextView, Task task,
                                       int position, TextView dateTextView, TextView timeTextView,
                                       ImageView clockIcon) {
        Context themeContext = new ContextThemeWrapper(context, R.style.AppTheme);
        PopupMenu popupMenu = new PopupMenu(themeContext, anchorView);
        String[] periods = {"Early morning", "Morning", "Afternoon", "Evening", "Late night", "Any time"};
        for (int i = 0; i < periods.length; i++) {
            popupMenu.getMenu().add(0, i, i, periods[i]);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            String selectedPeriod = item.getTitle().toString();
            periodTextView.setText(selectedPeriod);
            task.setStartAtPeriod(selectedPeriod);

            if (selectedPeriod.equals("Early morning")) {
                task.setStartAtTime("4:00 am");
            } else if (selectedPeriod.equals("Morning")) {
                task.setStartAtTime("9:00 am");
            } else if (selectedPeriod.equals("Afternoon")) {
                task.setStartAtTime("2:00 pm");
            } else if (selectedPeriod.equals("Evening")) {
                task.setStartAtTime("5:00 pm");
            } else if (selectedPeriod.equals("Late night")) {
                task.setStartAtTime("9:00 pm");
            } else if (selectedPeriod.equals("Any time")) {
                task.setStartAtTime("11:00 am");
            }
            timeTextView.setText(task.getStartAtTime());

            Calendar calendar = Calendar.getInstance();
            String defaultDate = getCurDateSample();
            if (task.getStartAtDate().equals(defaultDate) || task.getStartAtDate().isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
                String formattedDate = dateFormat.format(calendar.getTime());
                dateTextView.setText(formattedDate);
                task.setStartAtDate(formattedDate);
            }

            updateStartAtComponentsColor(task, dateTextView, periodTextView, timeTextView, clockIcon, context);
            if (task.getId() != null && !task.getId().isEmpty()) {
                ((TasksPageActivity) context).updateTaskInFirestore(task);
            } else {
                Log.e("TaskAdapter", "Task ID is null or empty. Cannot update Firestore.");
            }
//            notifyItemChanged(position);
            return true;
        });

        popupMenu.show();
    }

    private void showStartDateOptionsMenu(Context context, View anchorView, TextView dateTextView, Task task,
                                          int position, TextView periodTextView, TextView timeTextView,
                                          ImageView clockIcon) {
        Context themeContext = new ContextThemeWrapper(context, R.style.AppTheme);
        PopupMenu popupMenu = new PopupMenu(themeContext, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.start_date_options_menu, popupMenu.getMenu());

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());

        // Định dạng ngày cho "Today"
        String todayDate = dateFormat.format(calendar.getTime());
        MenuItem todayItem = popupMenu.getMenu().findItem(R.id.option_today);
        todayItem.setTitle("Today (" + todayDate + ")");

        // Định dạng ngày cho "Tomorrow"
        Calendar tomorrow = (Calendar) calendar.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        String tomorrowDate = dateFormat.format(tomorrow.getTime());
        MenuItem tomorrowItem = popupMenu.getMenu().findItem(R.id.option_tomorrow);
        tomorrowItem.setTitle("Tomorrow (" + tomorrowDate + ")");

        // Định dạng ngày cho "This weekend" (thứ Bảy gần nhất)
        Calendar thisWeekend = (Calendar) calendar.clone();
        int dayOfWeek = thisWeekend.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek >= Calendar.SATURDAY) {
            thisWeekend.add(Calendar.WEEK_OF_YEAR, 1); // Chuyển sang cuối tuần tiếp theo nếu đã qua thứ Bảy
        }
        thisWeekend.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY); // Đặt sang thứ Bảy
        String weekendDate = dateFormat.format(thisWeekend.getTime());
        MenuItem weekendItem = popupMenu.getMenu().findItem(R.id.option_this_weekend);
        weekendItem.setTitle("This weekend (" + weekendDate + ")");

        // Định dạng ngày cho "One week"
        Calendar oneWeek = (Calendar) calendar.clone();
        oneWeek.add(Calendar.WEEK_OF_YEAR, 1);
        String oneWeekDate = dateFormat.format(oneWeek.getTime());
        MenuItem oneWeekItem = popupMenu.getMenu().findItem(R.id.option_one_week);
        oneWeekItem.setTitle("One week (" + oneWeekDate + ")");

        // Định dạng ngày cho "Three weeks"
        Calendar threeWeeks = (Calendar) calendar.clone();
        threeWeeks.add(Calendar.WEEK_OF_YEAR, 3);
        String threeWeeksDate = dateFormat.format(threeWeeks.getTime());
        MenuItem threeWeeksItem = popupMenu.getMenu().findItem(R.id.option_three_weeks);
        threeWeeksItem.setTitle("Three weeks (" + threeWeeksDate + ")");

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            Calendar newDate = (Calendar) calendar.clone(); // Sao chép ngày hiện tại để tính toán
            if (itemId == R.id.option_today) {
                // Giữ nguyên ngày hiện tại
            } else if (itemId == R.id.option_tomorrow) {
                newDate.add(Calendar.DAY_OF_MONTH, 1);
            } else if (itemId == R.id.option_this_weekend) {
                int currentDayOfWeek = newDate.get(Calendar.DAY_OF_WEEK);
                if (currentDayOfWeek >= Calendar.SATURDAY) {
                    newDate.add(Calendar.WEEK_OF_YEAR, 1); // Chuyển sang cuối tuần tiếp theo
                }
                newDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY); // Đặt sang thứ Bảy
            } else if (itemId == R.id.option_one_week) {
                newDate.add(Calendar.WEEK_OF_YEAR, 1);
            } else if (itemId == R.id.option_three_weeks) {
                newDate.add(Calendar.WEEK_OF_YEAR, 3);
            } else if (itemId == R.id.option_choose_date) {
                showStartAtDatePickerDialog(dateTextView, task, position, periodTextView, timeTextView, clockIcon);
                return true;
            }
            String formattedDate = dateFormat.format(newDate.getTime());
            dateTextView.setText(formattedDate);
            task.setStartAtDate(formattedDate);

            // Nếu period hoặc time chưa được đặt, mặc định là "Morning" và "9:00 am"
            if ((task.getStartAtPeriod() == null || task.getStartAtPeriod().isEmpty()) &&
                    (task.getStartAtTime() == null || task.getStartAtTime().isEmpty())) {
                task.setStartAtPeriod("Morning");
                task.setStartAtTime("9:00 am");
                periodTextView.setText("Morning");
                timeTextView.setText("9:00 am");
            }

            updateStartAtComponentsColor(task, dateTextView, periodTextView, timeTextView, clockIcon, context);
            if (task.getId() != null && !task.getId().isEmpty()) {
                ((TasksPageActivity) context).updateTaskInFirestore(task);
            } else {
                Log.e("TaskAdapter", "Task ID is null or empty. Cannot update Firestore.");
            }
//            notifyItemChanged(position);
            return true;
        });

        popupMenu.show();
    }

    private void updateStartAtComponentsColor(Task task, TextView startAtDateText, TextView startAtPeriodText,
                                              TextView startAtTimeText, ImageView startAtClockIcon, Context context) {
        String defaultDate = getCurDateSample();
        String startAtDate = task.getStartAtDate();
        String startAtTime = task.getStartAtTime();

        if (startAtDate == null || startAtDate.isEmpty()) {
            startAtDateText.setTextColor(ContextCompat.getColor(context, R.color.textGray));
            startAtPeriodText.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            startAtTimeText.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            startAtClockIcon.setColorFilter(ContextCompat.getColor(context, android.R.color.black));
        } else if (startAtDate.equals(defaultDate) || isPastTime(startAtDate, startAtTime)) {
            startAtDateText.setTextColor(ContextCompat.getColor(context, R.color.textRed));
            startAtPeriodText.setTextColor(ContextCompat.getColor(context, R.color.textRed));
            startAtTimeText.setTextColor(ContextCompat.getColor(context, R.color.textRed));
            startAtClockIcon.setColorFilter(ContextCompat.getColor(context, R.color.textRed));
        } else {
            startAtDateText.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            startAtPeriodText.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            startAtTimeText.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            startAtClockIcon.setColorFilter(ContextCompat.getColor(context, android.R.color.black));
        }
    }
    private void showStartAtDatePickerDialog(TextView dateTextView, Task task, int position, TextView periodTextView,
                                             TextView timeTextView, ImageView clockIcon) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                dateTextView.getContext(),
                (view, year1, month1, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, month1, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
                    String formattedDate = dateFormat.format(selectedDate.getTime());

                    dateTextView.setText(formattedDate);
                    task.setStartAtDate(formattedDate);

                    if ((task.getStartAtPeriod() == null || task.getStartAtPeriod().isEmpty()) &&
                            (task.getStartAtTime() == null || task.getStartAtTime().isEmpty())) {
                        task.setStartAtPeriod("Morning");
                        task.setStartAtTime("9:00 am");
                        periodTextView.setText("Morning");
                        timeTextView.setText("9:00 am");
                    }

                    updateStartAtComponentsColor(task, dateTextView, periodTextView, timeTextView,
                            clockIcon, dateTextView.getContext());
                    if (task.getId() != null && !task.getId().isEmpty()) {
                        ((TasksPageActivity) dateTextView.getContext()).updateTaskInFirestore(task);
                    } else {
                        Log.e("TaskAdapter", "Task ID is null or empty. Cannot update Firestore.");
                    }
//                    notifyItemChanged(position);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    //check pass current time
    private boolean isPastTime(String date, String time) {
        if (date == null || time == null || date.isEmpty() || time.isEmpty()) {
            return false;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
            Calendar taskDateTime = Calendar.getInstance();
            taskDateTime.setTime(dateFormat.parse(date));
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            taskDateTime.set(Calendar.YEAR, currentYear);

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
            e.printStackTrace();
            return false;
        }
    }

    private String getFormattedDateWithSuffix(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Xử lý suffix
        String suffix;
        if (day >= 11 && day <= 13) {
            suffix = "th";
        } else {
            switch (day % 10) {
                case 1: suffix = "st"; break;
                case 2: suffix = "nd"; break;
                case 3: suffix = "rd"; break;
                default: suffix = "th"; break;
            }
        }

        // Format tháng và năm
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String monthYear = monthYearFormat.format(date);

        return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + day + suffix + ", " + calendar.get(Calendar.YEAR);
    }


    private String getCurDateSample() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
        String defaultDate = "e.g. Today, " + dateFormat.format(calendar.getTime());
        return defaultDate;
    }

    //popup icon
    private void showPopup(View anchorView, int layoutResId) {
        View popupView = LayoutInflater.from(anchorView.getContext()).inflate(layoutResId, null);
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

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

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkTask;
        EditText taskTitle;
        TextView createdDate;
        TextView createdTimeAgo1;
        TextView createdTimeAgo2;
        TextView taskScore;
        TextView noteTitle;
        ImageView expandButton;
        LinearLayout expandableLayout;
        ImageView repeatIcon;
        ImageView startAtIcon;
        ImageView startAtClockIcon;
        ImageView startNotiIcon;
        ImageView hideUntilIcon;
        ImageView priorityIcon;
        ImageView durationIcon;
        LinearLayout repeatCard;
        TextView repeatOptionText;
        LinearLayout startAtDateContainer;
        TextView startAtDateText;
        LinearLayout startAtPeriodContainer;
        TextView startAtPeriodText;
        LinearLayout startAtTimeContainer;
        TextView startAtTimeText;
        LinearLayout startNotiCard;
        MaterialButton startNoti5MinButton;
        MaterialButton startNoti15MinButton;
        MaterialButton startNoti60MinButton;
        MaterialButton startNoti1DayButton;
        MaterialButton priorityImportantButton;
        MaterialButton priorityUrgentButton;
        MaterialButton duration15MinButton;
        MaterialButton duration30MinButton;
        MaterialButton duration60MinButton;
        MaterialButton durationCustomButton;
        LinearLayout hideUntilDateContainer;
        TextView hideUntilDateText;
        LinearLayout hideUntilTimeContainer;
        TextView hideUntilTimeText;
        TextView deleteButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkTask = itemView.findViewById(R.id.check_task);
            taskTitle = itemView.findViewById(R.id.task_title);
            createdDate = itemView.findViewById(R.id.task_create_at_date);
            createdTimeAgo1 = itemView.findViewById(R.id.task_created_at_time_ago);
            createdTimeAgo2 = itemView.findViewById(R.id.task_created_time_ago);
            taskScore = itemView.findViewById(R.id.task_score);
            noteTitle = itemView.findViewById(R.id.note_title);expandButton = itemView.findViewById(R.id.expand_button);
            expandableLayout = itemView.findViewById(R.id.expandable_layout);
            repeatIcon = itemView.findViewById(R.id.repeat_icon);
            startAtIcon = itemView.findViewById(R.id.start_at_icon);
            startAtClockIcon = itemView.findViewById(R.id.start_at_clock_icon);
            startNotiIcon = itemView.findViewById(R.id.start_noti_icon);
            hideUntilIcon = itemView.findViewById(R.id.hide_until_icon);
            priorityIcon = itemView.findViewById(R.id.priority_icon);
            durationIcon = itemView.findViewById(R.id.duration_icon);
            repeatCard = itemView.findViewById(R.id.repeat_card);
            repeatOptionText = itemView.findViewById(R.id.repeat_option_text);
            startAtDateContainer = itemView.findViewById(R.id.start_at_date_container);
            startAtDateText = itemView.findViewById(R.id.start_at_date_text);
            startAtPeriodContainer = itemView.findViewById(R.id.start_at_period_container);
            startAtPeriodText = itemView.findViewById(R.id.start_at_period_text);
            startAtTimeContainer = itemView.findViewById(R.id.start_at_time_container);
            startAtTimeText = itemView.findViewById(R.id.start_at_time_text);
            startNotiCard = itemView.findViewById(R.id.start_noti_card);
            startNoti5MinButton = itemView.findViewById(R.id.start_noti_5min);
            startNoti15MinButton = itemView.findViewById(R.id.start_noti_15min);
            startNoti60MinButton = itemView.findViewById(R.id.start_noti_60min);
            startNoti1DayButton = itemView.findViewById(R.id.start_noti_1day);
            priorityImportantButton = itemView.findViewById(R.id.priority_important);
            priorityUrgentButton = itemView.findViewById(R.id.priority_urgent);
            duration15MinButton = itemView.findViewById(R.id.duration_15min);
            duration30MinButton = itemView.findViewById(R.id.duration_30min);
            duration60MinButton = itemView.findViewById(R.id.duration_60min);
            durationCustomButton = itemView.findViewById(R.id.duration_custom);
            hideUntilDateContainer = itemView.findViewById(R.id.hide_until_date_container);
            hideUntilDateText = itemView.findViewById(R.id.hide_until_date_text);
            hideUntilTimeContainer = itemView.findViewById(R.id.hide_until_time_container);
            hideUntilTimeText = itemView.findViewById(R.id.hide_until_time_text);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}