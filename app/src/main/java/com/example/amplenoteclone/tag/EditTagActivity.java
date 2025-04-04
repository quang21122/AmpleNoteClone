package com.example.amplenoteclone.tag;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Tag;

public class EditTagActivity extends AppCompatActivity {

    public static final String EXTRA_TAG = "extra_tag";
    public static final String EXTRA_NEW_TAG_NAME = "extra_new_tag_name";
    public static final int REQUEST_CODE_EDIT_TAG = 100;

    private Tag tag;
    private EditText tagNameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tag);

        // Làm cho thanh trạng thái trong suốt
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // Nhận dữ liệu tag từ Intent
        tag = (Tag) getIntent().getSerializableExtra(EXTRA_TAG);
        if (tag == null) {
            finish();
            return;
        }

        // Khởi tạo các view
        TextView currentTagName = findViewById(R.id.current_tag_name);
        tagNameInput = findViewById(R.id.tag_name_input);
        ImageView backButton = findViewById(R.id.back_button);
        TextView renameButton = findViewById(R.id.rename_button);
        TextView cancelButton = findViewById(R.id.cancel_button);

        // Cập nhật tên tag hiện tại
        currentTagName.setText("#" + tag.getName());
        tagNameInput.setText(tag.getName());

        // Xử lý nút Back
        backButton.setOnClickListener(v -> finish());

        // Xử lý nút Cancel
        cancelButton.setOnClickListener(v -> finish());

        // Xử lý nút Rename
        renameButton.setOnClickListener(v -> {
            String newTagName = tagNameInput.getText().toString().trim();
            if (!newTagName.isEmpty() && !newTagName.equals(tag.getName())) {
                tag.editTagInFirestore(this,
                        newTagName,
                        () -> {
                            Toast.makeText(this, "Tag updated", Toast.LENGTH_SHORT).show();
                            // Trả kết quả về BottomSheetTagMenu
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra(EXTRA_NEW_TAG_NAME, newTagName);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        },
                        error -> {
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                            finish();
                        });
            } else {
                finish();
            }
        });
    }
}