package com.example.amplenoteclone.tasks;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.DrawerActivity;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.adapters.TaskAdapter;
import com.example.amplenoteclone.models.Task;
import com.example.amplenoteclone.ui.customviews.TaskCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TasksPageActivity extends DrawerActivity {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<TaskCardView> taskCardList;
    private FirebaseFirestore db;
    private ListenerRegistration taskListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent(R.layout.activity_tasks);

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recycler_view_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskCardList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskCardList);
        recyclerView.setAdapter(taskAdapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;

        if (userId != null) {
            loadTasks(userId);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadTasks(String userId) {
        CollectionReference collectionRef = db.collection("tasks");
        taskListener = collectionRef
                .whereEqualTo("userId", userId)
                .orderBy("createAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Error listening to tasks: ", error);
                        Toast.makeText(this, "Error loading tasks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d("Firestore", "Snapshot received, size: " + (value != null ? value.size() : 0));
                    taskCardList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot document : value) {
                            Log.d("Firestore", "Task ID: " + document.getId() + ", Data: " + document.getData());
                            Task taskItem = new Task(
                                    document.getString("userId"),
                                    document.getString("noteId"),
                                    document.getString("title"),
                                    document.getDate("createAt"),
                                    document.getBoolean("isCompleted"),
                                    document.getString("repeat"),
                                    document.getDate("startAt"),
                                    document.getString("startAtDate"),
                                    document.getString("startAtPeriod"),
                                    document.getString("startAtTime"),
                                    document.getLong("startNoti") != null ? document.getLong("startNoti").intValue() : 0,
                                    document.getDate("hideUntil"),
                                    document.getString("hideUntilDate"),
                                    document.getString("hideUntilTime"),
                                    document.getString("priority"),
                                    document.getLong("duration") != null ? document.getLong("duration").intValue() : 0,
                                    document.getDouble("score") != null ? document.getDouble("score").floatValue() : 0.0f
                            );
                            taskItem.setId(document.getId());
                            if (taskItem.getTitle() != null) {
                                TaskCardView taskCard = new TaskCardView(this);
                                taskCard.setTask(taskItem);
                                taskCardList.add(taskCard);
                            } else {
                                Log.w("TasksPageActivity", "Task with null title ignored: " + document.getId());
                            }
                        }
                        taskAdapter.setTasks(new ArrayList<>(taskCardList));
                        taskAdapter.notifyDataSetChanged(); // Đảm bảo gọi notify để cập nhật giao diện
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (taskListener != null) {
            taskListener.remove();
        }
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
}