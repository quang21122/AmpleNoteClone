package com.example.amplenoteclone.tasks;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amplenoteclone.adapters.TaskAdapter;
import com.example.amplenoteclone.models.Task;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.DrawerActivity;
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
    private List<Task> taskList;
    private FirebaseFirestore db;
    private ListenerRegistration taskListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent(R.layout.activity_tasks);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.recycler_view_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo danh sách tasks
        taskList = new ArrayList<>();

        taskAdapter = new TaskAdapter(taskList, position -> {
            // Handle expand click
        });
        recyclerView.setAdapter(taskAdapter);

        // Lấy userId từ FirebaseAuth
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;

        // Lắng nghe thay đổi từ Firestore
        if (userId != null) {
            listenToTasks(userId);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void listenToTasks(String userId) {
        CollectionReference collectionRef = db.collection("tasks");
        taskListener = collectionRef
                .whereEqualTo("userId", userId)
                .orderBy("createAt", Query.Direction.ASCENDING) // Sắp xếp theo createAt (tăng dần)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Error listening to tasks: ", error);
                        Toast.makeText(this, "Error loading tasks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    taskList.clear();
                    for (QueryDocumentSnapshot document : value) {
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
                                document.getString("priority"),
                                document.getLong("duration") != null ? document.getLong("duration").intValue() : 0,
                                document.getDouble("score") != null ? document.getDouble("score").floatValue() : 0.0f
                        );
                        taskItem.setId(document.getId());
                        taskList.add(taskItem);
                        Log.d("Task Debug", taskItem.toString());
                    }

                    taskAdapter.setTasks(new ArrayList<>(taskList));
                    taskAdapter.notifyDataSetChanged();
                });
    }

    public void updateTaskInFirestore(Task task) {
        CollectionReference collectionRef = db.collection("tasks");

        collectionRef.document(task.getId())
                .set(task)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Task successfully updated!");
                    // Không cần cập nhật giao diện vì listener sẽ tự động làm điều đó
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating task", e);
                    Toast.makeText(this, "Error updating task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void deleteTaskFromFirestore(Task task) {
        CollectionReference collectionRef = db.collection("tasks");

        collectionRef.document(task.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Task successfully deleted!");
                    taskList.remove(task);
                    taskAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error deleting task", e);
                    Toast.makeText(this, "Error deleting task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy lắng nghe khi activity bị hủy
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