// ViewTaskActivity.java
package com.example.amplenoteclone.tasks;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ViewTaskActivity extends AppCompatActivity {
    private TextView taskTitle;
    private TextView taskDetails;
    private String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_task);

        taskTitle = findViewById(R.id.task_title);
        taskDetails = findViewById(R.id.task_details);

        if (getIntent().hasExtra("taskId")) {
            taskId = getIntent().getStringExtra("taskId");
            loadTask(taskId);
        }
    }

    private void loadTask(String taskId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("tasks");
        collectionRef.document(taskId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Task task = documentSnapshot.toObject(Task.class);
                        if (task != null) {
                            taskTitle.setText(task.getTitle());
                  //          taskDetails.setText(task.getDetails());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                });
    }
}