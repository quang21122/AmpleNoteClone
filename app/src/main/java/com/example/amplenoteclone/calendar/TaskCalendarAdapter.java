package com.example.amplenoteclone.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TaskCalendarAdapter extends RecyclerView.Adapter<TaskCalendarAdapter.TaskViewHolder> {
    private List<Task> tasks;
    private Context context;
    private FirebaseFirestore db;

    public TaskCalendarAdapter(Context context) {
        this.context = context;
        this.tasks = new ArrayList<>();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_add_task_calendar_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.taskTitle.setText(task.getTitle());
        holder.taskCheckbox.setChecked(task.isCompleted());

        holder.taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            // Update task completion status in Firestore
            db.collection("tasks")
                    .document(task.getId())
                    .update("completed", isChecked)
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to update task", Toast.LENGTH_SHORT).show()
                    );
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox taskCheckbox;
        TextView taskTitle;
        ImageButton addTaskButton;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskCheckbox = itemView.findViewById(R.id.task_checkbox);
            taskTitle = itemView.findViewById(R.id.task_title);
            addTaskButton = itemView.findViewById(R.id.add_task);
        }
    }
}
