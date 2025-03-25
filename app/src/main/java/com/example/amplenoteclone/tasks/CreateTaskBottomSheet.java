package com.example.amplenoteclone.tasks;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Note;
import com.example.amplenoteclone.models.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class CreateTaskBottomSheet extends BottomSheetDialogFragment {

    private EditText editTextQuickToDo;
    private TextView textViewSelectNote;
    private LinearLayout layoutAddTaskContainer;
    private LinearLayout layoutSelectStartTime;
    private TextView textViewSelectStartTime;
    private Button buttonAddQuick;

    private String selectedStartAtDate;
    private String selectedStartAtTime;
    private String selectedStartAtPeriod;
    private boolean isAddTaskVisible = false;
    private Note selectedNote; // Lưu note đã chọn

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_task, container, false);

        // Khởi tạo các view
        layoutAddTaskContainer = view.findViewById(R.id.layout_add_task_container);
        textViewSelectNote = view.findViewById(R.id.text_select_note);
        layoutSelectStartTime = view.findViewById(R.id.layout_select_start_time);
        textViewSelectStartTime = view.findViewById(R.id.text_view_select_start_time);
        editTextQuickToDo = view.findViewById(R.id.edit_text_quick_to_do);
        buttonAddQuick = view.findViewById(R.id.button_add_quick);

        // Kiểm tra xem editTextSelectNote có được khởi tạo không
        if (textViewSelectNote == null) {
            Toast.makeText(getContext(), "edit_text_select_note not found", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Lắng nghe sự kiện nhập liệu trong Quick to-do
        editTextQuickToDo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Thay đổi màu nền của nút Add
                if (s.length() > 0) {
                    buttonAddQuick.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.textBlue));
                    // Hiển thị giao diện Add a Task nếu chưa hiển thị
                    if (!isAddTaskVisible) {
                        showAddTaskLayout();
                        isAddTaskVisible = true;
                    }
                } else {
                    buttonAddQuick.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), android.R.color.darker_gray));
                    // Ẩn giao diện Add a Task nếu đang hiển thị
                    if (isAddTaskVisible) {
                        hideAddTaskLayout();
                        isAddTaskVisible = false;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Xử lý sự kiện nhấn vào ô chọn note
        textViewSelectNote.setOnClickListener(v -> {
            SelectNoteForTaskBottomSheet selectNoteForTaskBottomSheet = new SelectNoteForTaskBottomSheet(note -> {
                selectedNote = note;
                textViewSelectNote.setText(note.getTitle()); // Cập nhật giao diện với tiêu đề note đã chọn
                textViewSelectNote.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            });
            selectNoteForTaskBottomSheet.show(requireActivity().getSupportFragmentManager(), selectNoteForTaskBottomSheet.getTag());
        });

        // Xử lý sự kiện nhấn vào "Select a start time"
        layoutSelectStartTime.setOnClickListener(v -> {
            ScheduleTaskBottomSheet scheduleTaskBottomSheet = new ScheduleTaskBottomSheet((date, time, period) -> {
                selectedStartAtDate = date;
                selectedStartAtTime = time;
                selectedStartAtPeriod = period;
                textViewSelectStartTime.setText(date + " at " + time);
                textViewSelectStartTime.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            });
            scheduleTaskBottomSheet.show(requireActivity().getSupportFragmentManager(), scheduleTaskBottomSheet.getTag());
        });

        // Xử lý sự kiện nhấn nút Add
        buttonAddQuick.setOnClickListener(v -> {
            String title = editTextQuickToDo.getText().toString().trim(); // Sử dụng trực tiếp nội dung từ Quick to-do
            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a task title", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo task mới
            Task newTask = new Task();
            newTask.setTitle(title);
            newTask.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
            newTask.setNoteId(selectedNote != null ? selectedNote.getId() : "default_note_id"); // Sử dụng noteId của note đã chọn
            newTask.setCreateAt(new Date());
            newTask.setCompleted(false);
            newTask.setRepeat("Doesn't repeat");
            newTask.setStartAtDate(selectedStartAtDate != null ? selectedStartAtDate : "");
            newTask.setStartAtTime(selectedStartAtTime != null ? selectedStartAtTime : "");
            newTask.setStartAtPeriod(selectedStartAtPeriod != null ? selectedStartAtPeriod : "");
            newTask.setStartNoti(0);
            newTask.setPriority("");
            newTask.setDuration(0);
            newTask.setScore(1.0f);

            // Lưu task vào Firestore
            saveNewTaskToFirestore(newTask);
        });

        return view;
    }

    private void showAddTaskLayout() {
        layoutAddTaskContainer.setVisibility(View.VISIBLE);
        Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        layoutAddTaskContainer.startAnimation(slideUp);
    }

    private void hideAddTaskLayout() {
        Animation slideDown = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                layoutAddTaskContainer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        layoutAddTaskContainer.startAnimation(slideDown);
    }

    private void saveNewTaskToFirestore(Task task) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String generatedId = db.collection("tasks").document().getId();
        task.setId(generatedId);

        // Cập nhật logic xử lý ngày giờ bắt đầu
        if (selectedStartAtDate != null && selectedStartAtTime != null) {
            task.setStartAtDate(selectedStartAtDate);
            task.setStartAtTime(selectedStartAtTime);
            task.setStartAtPeriod(selectedStartAtPeriod != null ? selectedStartAtPeriod : "");
        } else {
            // Nếu không chọn ngày giờ, đặt các giá trị mặc định là rỗng
            task.setStartAtDate("");
            task.setStartAtTime("");
            task.setStartAtPeriod("");
        }

        db.collection("tasks")
                .document(generatedId)
                .set(task)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Task added successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error adding task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}