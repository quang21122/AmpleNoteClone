package com.example.amplenoteclone.tag;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Note;
import com.example.amplenoteclone.models.Tag;
import com.example.amplenoteclone.note.NotesActivity;
import com.example.amplenoteclone.note.ViewNoteActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddTagDialogFragment extends DialogFragment {

    private EditText tagNameInput;
    private ImageView backButton;
    private ImageView cancelButton;
    private ImageView doneButton;

    public interface OnTagAddedListener {
        void onTagAdded(String tagName);
    }

    private OnTagAddedListener tagAddedListener;

    public void setOnTagAddedListener(OnTagAddedListener listener) {
        this.tagAddedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_tag, container, false);
        getDialog().setCanceledOnTouchOutside(false);

        // Khởi tạo các view
        tagNameInput = view.findViewById(R.id.tag_name_input);
        backButton = view.findViewById(R.id.back_button);
        cancelButton = view.findViewById(R.id.cancel_button);
        doneButton = view.findViewById(R.id.done_button);

        // Set màu xám ban đầu cho nút done
        doneButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.textGray));
        doneButton.setEnabled(false);

        setupTagNameWatcher();
        setupListener();


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setUpDialog();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Tự động focus vào EditText và hiển thị bàn phím
        if (tagNameInput != null) {
            tagNameInput.requestFocus();
            tagNameInput.post(() -> {
                Window window = getDialog().getWindow();
                if (window != null) {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            });
        }
    }

    private void setUpDialog(){
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(android.R.color.transparent);
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                // Đặt vị trí dialog ở phía trên màn hình
                WindowManager.LayoutParams params = window.getAttributes();
                params.gravity = Gravity.TOP;

                params.y = 0; // Đặt dialog ngay tại đỉnh màn hình

                // Thêm cờ để dialog hiển thị toàn màn hình, bỏ qua các phần tử giao diện của Activity
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
                window.setAttributes(params);
            }
        }
    }

    private void setupTagNameWatcher(){
        tagNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isEmpty = s.toString().trim().isEmpty();
                doneButton.setColorFilter(ContextCompat.getColor(requireContext(),
                        isEmpty ? R.color.textGray : R.color.white));
                doneButton.setEnabled(!isEmpty);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Sửa xử lý nút Done trên bàn phím
        tagNameInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                if (tagNameInput.getText().toString().trim().isEmpty()) {
                    dismiss(); // Hủy dialog nếu input rỗng
                } else {
                    completeAddTag(); // Thêm tag nếu input không rỗng
                }
                return true;
            }
            return false;
        });
    }

    private void setupListener(){
        // Xử lý nút Back
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotesActivity.class);
            startActivity(intent);
            getActivity().finish();
            dismiss();
        });

        // Xử lý nút X (Cancel)
        cancelButton.setOnClickListener(v -> dismiss());

        // Sửa xử lý nút Tick (Done)
        doneButton.setOnClickListener(v -> {
            if (!tagNameInput.getText().toString().trim().isEmpty()) {
                completeAddTag();
            }
        });
    }

    private boolean isTagExists(String tagName) {
        if (getActivity() instanceof ViewNoteActivity) {
            ViewNoteActivity activity = (ViewNoteActivity) getActivity();
            Note currentNote = activity.getCurrentNote();
            if (currentNote != null && currentNote.getTags() != null) {
                for (String tagId : currentNote.getTags()) {
                    FirebaseFirestore.getInstance().collection("tags")
                            .document(tagId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    Tag tag = documentSnapshot.toObject(Tag.class);
                                    if (tag != null && tag.getName().equalsIgnoreCase(tagName)) {
                                        System.out.println("Note already has the same tag");
                                        dismiss();
                                    }
                                }
                            });
                }
            }
        }
        return false;
    }

    private void completeAddTag() {
        String tagName = tagNameInput.getText().toString().trim();
        if (tagName.isEmpty()) {
            Toast.makeText(getContext(), "Tag name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isTagExists(tagName)) {
            if (tagAddedListener != null) {
                tagAddedListener.onTagAdded(tagName);
            }
        }
        dismiss();
    }
}