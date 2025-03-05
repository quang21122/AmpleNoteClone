package com.example.amplenoteclone;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class TasksActivity extends AppCompatActivity {

    // Khai báo các thành phần trong layout
    private ImageView expandButton;
    private LinearLayout expandableLayout;

    private boolean isExpanded = false; // Trạng thái của phần mở rộng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        // Ánh xạ các view từ XML
        expandButton = findViewById(R.id.expand_button);
        expandableLayout = findViewById(R.id.expandable_layout);

        // Đặt sự kiện khi nhấn vào nút Expand
        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpanded) {
                    // Nếu đang mở rộng thì thu gọn lại
                    expandableLayout.setVisibility(View.GONE);
                    expandButton.setImageResource(R.drawable.ic_arrow_expanded); // Đổi icon
                } else {
                    // Nếu đang thu gọn thì mở rộng ra
                    expandableLayout.setVisibility(View.VISIBLE);
                    expandButton.setImageResource(R.drawable.ic_arrow_collapsed); // Đổi icon
                }
                // Đổi trạng thái isExpanded
                isExpanded = !isExpanded;
            }
        });
    }
}

