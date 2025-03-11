package com.example.amplenoteclone.tasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.R;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnItemClickListener listener;

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

        holder.checkTask.setChecked(task.isChecked());
        holder.taskTitle.setText(task.getTitle());
        holder.taskDate.setText(task.getDate());
        holder.taskCreatedTime.setText(task.getCreatedTime());

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
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkTask;
        EditText taskTitle;
        TextView taskDate;
        TextView taskCreatedTime;
        ImageView expandButton;
        LinearLayout expandableLayout;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkTask = itemView.findViewById(R.id.check_task);
            taskTitle = itemView.findViewById(R.id.task_title);
            taskDate = itemView.findViewById(R.id.task_date);
            taskCreatedTime = itemView.findViewById(R.id.task_created_time);
            expandButton = itemView.findViewById(R.id.expand_button);
            expandableLayout = itemView.findViewById(R.id.expandable_layout);
        }
    }
}