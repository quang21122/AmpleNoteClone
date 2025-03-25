package com.example.amplenoteclone.calendar;

import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class TaskCalendarAdapter extends RecyclerView.Adapter<TaskCalendarAdapter.ViewHolder> {
    private List<Task> tasks = new ArrayList<>();
    private Set<String> uniqueNoteIds = new HashSet<>();
    private Set<String> uniqueTags = new HashSet<>();
    private Context context;
    private FirebaseFirestore db;
    private Date selectedDate;
    private Spinner durationSpinner;
    private View dialogView;

    public TaskCalendarAdapter(Context context, Spinner durationSpinner) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.durationSpinner = durationSpinner;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_add_task_calendar_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.taskTitle.setText(task.getTitle());

        holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));

        holder.itemView.setForeground(ContextCompat.getDrawable(context, task.calculateBorderTypeByScore()));

        holder.taskCheckbox.setChecked(task.isCompleted());
        setupCheckboxListener(holder, task, durationSpinner);
        holder.addTaskButton.setOnClickListener(v -> showTimePickerDialog(task));
    }

    private void setupCheckboxListener(ViewHolder holder, Task task, Spinner durationSpinner) {
        holder.taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Date currentTime = Calendar.getInstance().getTime();

                String durationStr = durationSpinner.getSelectedItem().toString();
                int duration = Integer.parseInt(durationStr.split(" ")[0]); // Extract number from "XX min"

                // Update task in Firestore
                FirebaseFirestore.getInstance().collection("tasks")
                        .document(task.getId())
                        .update(
                                "isCompleted", true,
                                "startAt", currentTime,
                                "duration", duration
                        )
                        .addOnSuccessListener(aVoid -> {
                            int pos = tasks.indexOf(task);
                            if (pos != -1) {
                                tasks.remove(pos);
                                notifyItemRemoved(pos);
                            }

                            // Notify fragments to reload
                            if (context instanceof CalendarActivity) {
                                ((CalendarActivity) context).refreshCurrentFragment();
                            }

                            Toast.makeText(context, "Task completed", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            holder.taskCheckbox.setChecked(false);
                            Toast.makeText(context, "Failed to complete task", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void showTimePickerDialog(Task task) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.layout_schedule_task_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        CheckBox taskCheckbox = bottomSheetView.findViewById(R.id.task_checkbox);
        TextView taskTitle = bottomSheetView.findViewById(R.id.task_title);
        View editDetailsContainer = bottomSheetView.findViewById(R.id.edit_details_container);

        taskCheckbox.setChecked(task.isCompleted());
        taskTitle.setText(task.getTitle());

        taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Date currentTime = Calendar.getInstance().getTime();

                String durationStr = durationSpinner.getSelectedItem().toString();
                int duration = Integer.parseInt(durationStr.split(" ")[0]);

                // Update task in Firestore
                db.collection("tasks")
                        .document(task.getId())
                        .update(
                                "isCompleted", true,
                                "startAt", currentTime,
                                "duration", duration
                        )
                        .addOnSuccessListener(aVoid -> {
                            int pos = tasks.indexOf(task);
                            if (pos != -1) {
                                tasks.remove(pos);
                                notifyItemRemoved(pos);
                            }

                            bottomSheetDialog.dismiss();

                            // Notify fragments to reload
                            if (context instanceof CalendarActivity) {
                                ((CalendarActivity) context).refreshCurrentFragment();
                            }

                            Toast.makeText(context, "Task completed", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            taskCheckbox.setChecked(false);
                            Toast.makeText(context, "Failed to complete task", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        View.OnClickListener timeButtonListener = v -> {
            Calendar calendar = Calendar.getInstance();
            if (selectedDate != null) {
                calendar.setTime(selectedDate);
            }
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            Random random = new Random();

            if (v.getId() == R.id.btn_9am) {
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.set(Calendar.MINUTE, 0);
            } else if (v.getId() == R.id.btn_12pm) {
                calendar.set(Calendar.HOUR_OF_DAY, 12);
                calendar.set(Calendar.MINUTE, 0);
            } else if (v.getId() == R.id.btn_3pm) {
                calendar.set(Calendar.HOUR_OF_DAY, 15);
                calendar.set(Calendar.MINUTE, 0);
            } else if (v.getId() == R.id.btn_5pm) {
                calendar.set(Calendar.HOUR_OF_DAY, 17);
                calendar.set(Calendar.MINUTE, 0);
            } else if (v.getId() == R.id.btn_7pm) {
                calendar.set(Calendar.HOUR_OF_DAY, 19);
                calendar.set(Calendar.MINUTE, 0);
            } else if (v.getId() == R.id.btn_early_morning) {
                calendar.set(Calendar.HOUR_OF_DAY, 4);
                calendar.set(Calendar.MINUTE, 0);
            } else if (v.getId() == R.id.btn_morning) {
                calendar.set(Calendar.HOUR_OF_DAY, 7);
                calendar.set(Calendar.MINUTE, 0);
            } else if (v.getId() == R.id.btn_afternoon) {
                calendar.set(Calendar.HOUR_OF_DAY, 14);
                calendar.set(Calendar.MINUTE, 0);
            } else if (v.getId() == R.id.btn_evening) {
                calendar.set(Calendar.HOUR_OF_DAY, 18);
                calendar.set(Calendar.MINUTE, 0);
            } else if (v.getId() == R.id.btn_late_night) {
                calendar.set(Calendar.HOUR_OF_DAY, 22);
                calendar.set(Calendar.MINUTE, 0);
            } else if (v.getId() == R.id.btn_any_time) {
                int hour = random.nextInt(17) + 6; // 6 to 22
                int minute = random.nextInt(60);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
            }

            updateTaskTime(task, calendar.getTime(), bottomSheetDialog);
        };

        int[] buttonIds = { R.id.btn_early_morning, R.id.btn_morning, R.id.btn_afternoon,
                R.id.btn_evening, R.id.btn_late_night, R.id.btn_any_time, R.id.btn_9am, R.id.btn_12pm, R.id.btn_3pm,
                R.id.btn_5pm, R.id.btn_7pm};
        for (int id : buttonIds) {
            bottomSheetView.findViewById(id).setOnClickListener(timeButtonListener);
        }

        bottomSheetView.findViewById(R.id.btn_custom).setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            if (selectedDate != null) {
                now.setTime(selectedDate);
            }
            new TimePickerDialog(context,
                    (view, hourOfDay, minute) -> {
                        Calendar calendar = Calendar.getInstance();
                        if (selectedDate != null) {
                            calendar.setTime(selectedDate);
                        }
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        updateTaskTime(task, calendar.getTime(), bottomSheetDialog);
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            ).show();
        });

        editDetailsContainer.setOnClickListener(v -> {
            Toast.makeText(context, "Edit details clicked", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void updateTaskTime(Task task, Date startAt, BottomSheetDialog dialog) {
        String durationStr = durationSpinner.getSelectedItem().toString();
        int duration = Integer.parseInt(durationStr.split(" ")[0]);

        db.collection("tasks")
                .document(task.getId())
                .update(
                        "startAt", startAt,
                        "duration", duration
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Task scheduled", Toast.LENGTH_SHORT).show();
                    int pos = tasks.indexOf(task);
                    if (pos != -1) {
                        tasks.remove(pos);
                        notifyItemRemoved(pos);
                    }
                    dialog.dismiss();

                    // Notify fragments to reload
                    if (context instanceof CalendarActivity) {
                        ((CalendarActivity) context).refreshCurrentFragment();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to schedule task", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        updateNotesAndTagsCounts();
        notifyDataSetChanged();
    }

    private void updateNotesAndTagsCounts() {
        uniqueNoteIds.clear();
        uniqueTags.clear();

        for (Task task : tasks) {
            if (task.getNoteId() != null) {
                uniqueNoteIds.add(task.getNoteId());
            }
        }

        for (String noteId : uniqueNoteIds) {
            FirebaseFirestore.getInstance()
                    .collection("notes")
                    .document(noteId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            ArrayList<String> tags = (ArrayList<String>) documentSnapshot.get("tags");
                            if (tags != null) {
                                uniqueTags.addAll(tags);
                                updateCountsInDialog();
                            }
                        }
                    });
        }
    }

    private void updateCountsInDialog() {
        if (dialogView != null) {
            TextView notesCount = dialogView.findViewById(R.id.notes_count);
            TextView tagsCount = dialogView.findViewById(R.id.tags_count);

            if (notesCount != null) {
                notesCount.setText(uniqueNoteIds.size() + " notes");
            }
            if (tagsCount != null) {
                tagsCount.setText(uniqueTags.size() + " tags");
            }
        }
    }

    public void setDialogView(View dialogView) {
        this.dialogView = dialogView;
        updateCountsInDialog();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle;
        CheckBox taskCheckbox;
        ImageButton addTaskButton;

        ViewHolder(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.task_title);
            taskCheckbox = itemView.findViewById(R.id.task_checkbox);
            addTaskButton = itemView.findViewById(R.id.add_task);
        }
    }
}
