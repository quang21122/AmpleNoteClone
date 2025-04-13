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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class AddTagDialogFragment extends DialogFragment {

    private EditText tagNameInput;
    private ImageView backButton;
    private ImageView cancelButton;
    private ImageView doneButton;

    public interface OnTagAddedListener {
        void onTagAdded(Tag tag);
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

        tagNameInput = view.findViewById(R.id.tag_name_input);
        backButton = view.findViewById(R.id.back_button);
        cancelButton = view.findViewById(R.id.cancel_button);
        doneButton = view.findViewById(R.id.done_button);


        tagNameInput.requestFocus();
        tagNameInput.post(() -> {
            Window window = getDialog().getWindow();
            if (window != null) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

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

    private void setUpDialog() {
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

    private void setupTagNameWatcher() {
        tagNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isEmpty = s.toString().trim().isEmpty();
                doneButton.setColorFilter(ContextCompat.getColor(requireContext(),
                        isEmpty ? R.color.textGray : R.color.white));
                doneButton.setEnabled(!isEmpty);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Xử lý nút Done trên bàn phím
        tagNameInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                if (tagNameInput.getText().toString().trim().isEmpty()) {
                    dismiss();
                } else {
                    completeAddTag();
                }
                return true;
            }
            return false;
        });
    }

    private void setupListener() {
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotesActivity.class);
            startActivity(intent);
            getActivity().finish();
            dismiss();
        });

        cancelButton.setOnClickListener(v -> dismiss());

        doneButton.setOnClickListener(v -> {
            if (!tagNameInput.getText().toString().trim().isEmpty()) {
                completeAddTag();
            }
        });
    }

    private void isTagExists(String tagName, Note currentNote, Runnable onTagAlreadyInNote, Consumer<Tag> onTagExists, Runnable onTagNotExists) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tags")
                .whereEqualTo("name", tagName)
                .whereEqualTo("userId", currentNote.getUserId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Tag exists
                        Tag existingTag = queryDocumentSnapshots.getDocuments().get(0).toObject(Tag.class);
                        existingTag.setId(queryDocumentSnapshots.getDocuments().get(0).getId());

                        // Check if note already has this tag
                        if (currentNote.getTags().contains(existingTag.getId())) {
                            onTagAlreadyInNote.run();
                        } else {
                            onTagExists.accept(existingTag);
                        }
                    } else {
                        // Tag doesn't exist
                        onTagNotExists.run();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to check tag existence", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
    }

    private void completeAddTag() {
        String tagName = tagNameInput.getText().toString().trim();
        if (tagName.isEmpty()) {
            Toast.makeText(getContext(), "Tag name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (getActivity() instanceof ViewNoteActivity) {
            ViewNoteActivity activity = (ViewNoteActivity) getActivity();
            Note currentNote = activity.getCurrentNote();
            if (currentNote == null) {
                Toast.makeText(getContext(), "Error: No note selected", Toast.LENGTH_SHORT).show();
                dismiss();
                return;
            }

            isTagExists(
                    tagName,
                    currentNote,
                    () -> {
                        dismiss();
                    },
                    existingTag -> {
                        // Add existing tag to note
                        List<String> updatedTagIds = new ArrayList<>(currentNote.getTags());
                        updatedTagIds.add(existingTag.getId());
                        currentNote.setTags((ArrayList<String>) updatedTagIds);

                        FirebaseFirestore.getInstance().collection("notes").document(currentNote.getId())
                                .update(
                                        "tags", updatedTagIds,
                                        "updatedAt", new Date())
                                .addOnSuccessListener(aVoid -> {
                                    if (tagAddedListener != null) {
                                        tagAddedListener.onTagAdded(existingTag);
                                    }
                                    Toast.makeText(getContext(), "Tag added to note", Toast.LENGTH_SHORT).show();
                                    dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to add tag to note", Toast.LENGTH_SHORT).show();
                                    dismiss();
                                });
                    },
                    () -> {
                        // Create new tag
                        Tag.createTagInFirestore(
                                tagName,
                                newTag -> {
                                    List<String> updatedTagIds = new ArrayList<>(currentNote.getTags());
                                    updatedTagIds.add(newTag.getId());
                                    currentNote.setTags((ArrayList<String>) updatedTagIds);

                                    FirebaseFirestore.getInstance().collection("notes").document(currentNote.getId())
                                            .update(
                                                    "tags", updatedTagIds,
                                                    "updatedAt", new Date())
                                            .addOnSuccessListener(aVoid -> {
                                                if (tagAddedListener != null) {
                                                    tagAddedListener.onTagAdded(newTag);
                                                }
                                                Toast.makeText(getContext(), "Tag created and added to note", Toast.LENGTH_SHORT).show();
                                                dismiss();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(getContext(), "Failed to add tag to note", Toast.LENGTH_SHORT).show();
                                                dismiss();
                                            });
                                },
                                error -> {
                                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                    dismiss();
                                });
                    });
        }
    }
}
