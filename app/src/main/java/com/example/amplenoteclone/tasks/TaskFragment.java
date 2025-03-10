package com.example.amplenoteclone.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.amplenoteclone.R;

public class TaskFragment extends Fragment {

    private CheckBox checkTask;
    private EditText taskTitle;
    private TextView taskDate;
    private TextView taskCreatedTime;
    private ImageView expandButton;
    private LinearLayout expandableLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout fragment_task.xml
        View view = inflater.inflate(R.layout.fragment_task, container, false);

        // Khởi tạo các thành phần giao diện
        checkTask = view.findViewById(R.id.check_task);
        taskTitle = view.findViewById(R.id.task_title);
        taskDate = view.findViewById(R.id.task_date);
        taskCreatedTime = view.findViewById(R.id.task_created_time);
        expandButton = view.findViewById(R.id.expand_button);
        expandableLayout = view.findViewById(R.id.expandable_layout);

        // Thiết lập sự kiện cho nút expand
        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expandableLayout.getVisibility() == View.GONE) {
                    expandableLayout.setVisibility(View.VISIBLE);
                    expandButton.setImageResource(R.drawable.ic_arrow_collapsed); // Thay bằng icon collapse
                } else {
                    expandableLayout.setVisibility(View.GONE);
                    expandButton.setImageResource(R.drawable.ic_arrow_expanded); // Thay bằng icon expand
                }
            }
        });

        // Thiết lập dữ liệu ban đầu (nếu cần)
        taskTitle.setText("Task Title");
        taskDate.setText("February 24th, 2025");
        taskCreatedTime.setText("Created 23 minutes ago");

        return view;
    }
}
