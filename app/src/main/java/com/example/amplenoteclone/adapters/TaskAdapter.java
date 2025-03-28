package com.example.amplenoteclone.adapters;

import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.ui.customviews.TaskCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<TaskCardView> taskList;
    private int expandedPosition = -1;
    private int lastKnownSize = 0;

    public TaskAdapter() {
        this.taskList = new ArrayList<>();
    }

    public TaskAdapter(List<TaskCardView> tasks) {
        this.taskList = tasks;
    }

    public void setTasks(List<TaskCardView> tasks) {
        Log.d("TaskAdapter", "setTasks called - Last known size: " + lastKnownSize + ", New size: " + tasks.size() + ", expandedPosition: " + expandedPosition);

        // Lưu task đang expand
        TaskCardView expandedTask = expandedPosition != -1 && expandedPosition < taskList.size() ? taskList.get(expandedPosition) : null;

        // Cập nhật danh sách mới
        this.taskList.clear();
        this.taskList.addAll(tasks);

        // Kiểm tra thay đổi
        if (lastKnownSize != tasks.size()) {
            // Nếu kích thước thay đổi (thêm hoặc xóa), reset expandedPosition
            expandedPosition = -1;
        } else if (expandedTask != null) {
            // Nếu kích thước không đổi, kiểm tra task cũ có còn không
            expandedPosition = -1; // Reset mặc định
            for (int i = 0; i < taskList.size(); i++) {
                if (taskList.get(i).getTask().getId().equals(expandedTask.getTask().getId())) {
                    expandedPosition = i;
                    break;
                }
            }
        }

        // Cập nhật lastKnownSize
        lastKnownSize = tasks.size();

        Log.d("TaskAdapter", "After update - expandedPosition: " + expandedPosition + ", taskList size: " + taskList.size());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TaskCardView taskCardView = new TaskCardView(parent.getContext());
        return new TaskViewHolder(taskCardView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskCardView taskCardView = taskList.get(position);
        holder.taskCardView.setTask(taskCardView.getTask());

        boolean isExpanded = position == expandedPosition;
        holder.taskCardView.setExpanded(isExpanded);

        holder.taskCardView.getExpandButton().setOnClickListener(v -> {
            int previousExpandedPosition = expandedPosition;
            if (expandedPosition == position) {
                expandedPosition = -1;
            } else {
                expandedPosition = position;
            }
            if (previousExpandedPosition != -1) {
                notifyItemChanged(previousExpandedPosition);
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TaskCardView taskCardView;

        public TaskViewHolder(@NonNull TaskCardView itemView) {
            super(itemView);
            this.taskCardView = itemView;
        }
    }

    public void resetExpand() {
        expandedPosition = -1;
        notifyDataSetChanged();
    }
}