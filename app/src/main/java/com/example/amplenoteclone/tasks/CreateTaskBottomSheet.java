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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
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
    private Note selectedNote;
    private boolean isStartNewNote = false;

    private void initializeViews(View view) {
        textViewSelectNote = view.findViewById(R.id.text_select_note);

        // Set default selection to "Start a new note"
        selectedNote = null; // No note selected
        isStartNewNote = true; // Default to new note
        textViewSelectNote.setText("Start a new note");
        textViewSelectNote.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_task, container, false);
        initializeViews(view);
        // Khởi tạo các view
        layoutAddTaskContainer = view.findViewById(R.id.layout_add_task_container);
        textViewSelectNote = view.findViewById(R.id.text_select_note);
        layoutSelectStartTime = view.findViewById(R.id.layout_select_start_time);
        textViewSelectStartTime = view.findViewById(R.id.text_view_select_start_time);
        editTextQuickToDo = view.findViewById(R.id.edit_text_quick_to_do);
        buttonAddQuick = view.findViewById(R.id.button_add_quick);


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
            SelectNoteForTaskBottomSheet selectNoteForTaskBottomSheet = new SelectNoteForTaskBottomSheet(new SelectNoteForTaskBottomSheet.OnNoteSelectionListener() {
                @Override
                public void onNoteSelected(Note note) {
                    selectedNote = note;
                    isStartNewNote = false; // No longer using new note
                    textViewSelectNote.setText(note.getTitle());
                    textViewSelectNote.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                }

                @Override
                public void onStartNewNoteSelected() {
                    selectedNote = null;
                    isStartNewNote = true;
                    textViewSelectNote.setText("Start a new note");
                    textViewSelectNote.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                }
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
            String title = editTextQuickToDo.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a task title", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (isStartNewNote) {
                createNewNoteAndTask(userId, title);
            } else {
                createTaskWithExistingNote(userId, title);
            }
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

    private void createNewNoteAndTask(String userId, String taskTitle) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String noteId = db.collection("notes").document().getId();

        // Tạo note mới
        Note newNote = new Note();
        newNote.setTitle("Untitled note");
        newNote.setUserId(userId);
        newNote.setCreatedAt(Timestamp.now().toDate());
        newNote.setContent("");
        newNote.setUpdatedAt(Timestamp.now().toDate());
        newNote.setProtected(false);
        newNote.setTags(new ArrayList<>());
        newNote.setId(noteId);

        // Tạo task mới
        Task newTask = new Task(userId, noteId, taskTitle);
        newTask.setId(db.collection("tasks").document().getId());
        setTaskStartTime(newTask); // Cập nhật thời gian nếu có

        // Lưu note và thêm task ID
        ArrayList<String> tasks = new ArrayList<>();
        tasks.add(newTask.getId());
        newNote.setTasks(tasks);

        db.collection("notes")
                .document(noteId)
                .set(newNote)
                .addOnSuccessListener(aVoid -> {
                    newTask.createInFirestore(getContext().getApplicationContext(),
                            () -> {
                                dismiss();
                            },
                            e -> Toast.makeText(getContext(), "Error adding task: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error creating note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createTaskWithExistingNote(String userId, String taskTitle) {
        if (selectedNote != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String noteId = selectedNote.getId();
            Task newTask = new Task(userId, noteId, taskTitle);
            newTask.setId(db.collection("tasks").document().getId());
            setTaskStartTime(newTask);

            // Cập nhật note với task ID
            db.collection("notes").document(noteId)
                    .update("tasks", FieldValue.arrayUnion(newTask.getId()))
                    .addOnSuccessListener(aVoid -> {
                        newTask.createInFirestore(getContext().getApplicationContext(),
                                () -> {
                                    dismiss();
                                },
                                e -> Toast.makeText(getContext(), "Error adding task: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error updating note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void setTaskStartTime(Task task) {
        if (selectedStartAtDate != null && selectedStartAtTime != null) {
            task.setStartAtDate(selectedStartAtDate);
            task.setStartAtTime(selectedStartAtTime);
            task.setStartAtPeriod(selectedStartAtPeriod != null ? selectedStartAtPeriod : "");
        }
    }
}