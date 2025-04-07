package com.example.amplenoteclone.adapters;

import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.ui.customviews.TaskCardView;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<TaskCardView> taskList;
    private int expandedPosition = -1;
    private int lastKnownSize = 0;
    private boolean showDeleteButton = true;
    private boolean isEditable = true;
    private boolean showTaskTitleDetails = true;

    public TaskAdapter() {
        this.taskList = new ArrayList<>();
    }

    public TaskAdapter(List<TaskCardView> tasks) {
        this.taskList = tasks;
    }

    public void setTasks(List<TaskCardView> tasks) {
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

        lastKnownSize = tasks.size();
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

        // Ẩn/hiện nút xóa dựa trên showDeleteButton
        holder.taskCardView.setShowDeleteButton(showDeleteButton);
        holder.taskCardView.setEditable(isEditable);

        // Ẩn/hiện tiêu đề chi tiết dựa trên showTaskTitleDetail
        holder.taskCardView.setShowTaskTitleDetails(showTaskTitleDetails);

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

    public void setExpandedPosition(int position) {
        if (position >= 0 && position < taskList.size()) {
            this.expandedPosition = position;
            notifyItemChanged(position); // Cập nhật item tại vị trí được expand
        } else {
            this.expandedPosition = -1;
            notifyDataSetChanged();
        }
    }
    public void setShowDeleteButton(boolean show) {
        this.showDeleteButton = show;
        notifyDataSetChanged();
    }
    public void setEditable(boolean editable) {
        this.isEditable = editable;
        notifyDataSetChanged();
    }
    public void setShowTaskTitleDetails(boolean show) {
        this.showTaskTitleDetails = show;
        notifyDataSetChanged();
    }
}