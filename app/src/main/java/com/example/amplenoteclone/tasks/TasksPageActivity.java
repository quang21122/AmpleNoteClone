package com.example.amplenoteclone.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.adapters.TaskAdapter;
import com.example.amplenoteclone.models.Task;
import com.example.amplenoteclone.note.NotesActivity;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.calendar.CalendarActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.amplenoteclone.DrawerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TasksPageActivity extends DrawerActivity {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent(R.layout.activity_tasks);

        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.recycler_view_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

//        // Khởi tạo danh sách tasks (có thể lấy từ API hoặc database)
//        taskList = new ArrayList<>();
//
//        // Thêm Task 1
//        taskList.add(new Task(false, "Test Task", new Date(), "task1", "Doesn't repeat",
//                "", "", "", "",
//                "Important", "30 min", "", ""));
//
//        // Thêm Task 2
//        taskList.add(new Task(false, "Test Task", new Date(), "task2", "Doesn't repeat",
//                "Monday, Mar 18", "Morning", "9:00 am", "5 min",
//                "Important", "30 min", "Saturday, Mar 22", "9:00 pm"));
//
//
//        taskAdapter = new TaskAdapter(taskList, new TaskAdapter.OnItemClickListener() {
//            @Override
//            public void onExpandClick(int position) {
//                // Xử lý khi click expand (nếu cần)
//            }
//        });

        taskAdapter = new TaskAdapter(new ArrayList<>(), position -> {
            // Handle expand click
        });
        recyclerView.setAdapter(taskAdapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;
        getTasksFromFirebase(userId, tasks -> runOnUiThread(() -> {
            taskAdapter.setTasks(tasks);
            taskAdapter.notifyDataSetChanged();
        }));

        recyclerView.setAdapter(this.taskAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_tasks, menu);
        return true;
    }
    @Override
    public String getToolbarTitle() {
        return "Tasks";
    }
    @Override
    protected int getCurrentPageId() {
        return R.id.action_tasks;
    }

    // In TasksPageActivity.java
    private void getTasksFromFirebase(String userId, FirestoreCallback firestoreCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("tasks");

        // Fetch only tasks for the given userId
        collectionRef.whereEqualTo("userId", userId)
                .get(Source.SERVER) // Force fetch from server
                .addOnCompleteListener(task -> {
                    ArrayList<Task> tasks = new ArrayList<>();

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task taskItem = new Task(
                                    document.getBoolean("isCompleted"),
                                    document.getString("title"),
                                    document.getDate("createAt"),
                                    document.getString("id"),
                                    document.getString("repeat"),
                                    document.getString("startAtDate"),
                                    document.getString("startAtPeriod"),
                                    document.getString("startAtTime"),
                                    document.getLong("startNoti") != null ? document.getLong("startNoti").intValue() : 0, // Null check added
                                    document.getString("priority"),
                                    document.getLong("duration") != null ? document.getLong("duration").intValue() : 0, // Changed to int
                                    document.getString("hideUntilDate"),
                                    document.getString("hideUntilTime")
                            );
                            taskItem.setUserId(document.getString("userId"));
                            taskItem.setNoteId(document.getString("noteId"));
                            tasks.add(taskItem);
                        }
                    } else {
                        Log.e("ERROR", "Firestore query failed: ", task.getException());
                    }

                    firestoreCallback.onCallback(tasks);
                });
    }

    public interface FirestoreCallback {
        void onCallback(ArrayList<Task> tasks);
    }

}