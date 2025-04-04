package com.example.amplenoteclone.tag;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Note;
import com.example.amplenoteclone.models.Tag;
import com.example.amplenoteclone.note.ViewNoteActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BottomSheetTagMenu extends BottomSheetDialogFragment {

    private Tag tag;
    private OnTagActionListener tagActionListener;

    public interface OnTagActionListener {
        void onTagRemoved(Tag tag);
        void onTagEdited(Tag tag);
        void onTagDeleted(Tag tag);
    }

    public static BottomSheetTagMenu newInstance(Tag tag) {
        BottomSheetTagMenu fragment = new BottomSheetTagMenu();
        fragment.tag = tag;
        return fragment;
    }

    public void setOnTagActionListener(OnTagActionListener listener) {
        this.tagActionListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_tag_menu, container, false);

        // Cập nhật tiêu đề (tên tag)
        TextView tagNameTitle = view.findViewById(R.id.tag_menu_name);
        tagNameTitle.setText(tag.getName());

        // Cập nhật icon (nếu tag là "daily-jots" thì đổi màu icon)
        ImageView tagIcon = view.findViewById(R.id.tag_menu_icon);
        if ("daily-jots".equalsIgnoreCase(tag.getName())) {
            tagIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.textBlue));
        } else {
            tagIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.dark));
        }

        // Xử lý nút X để đóng dialog
        ImageView closeButton = view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> dismiss());

        // Xử lý các tùy chọn trong menu
        view.findViewById(R.id.option_remove_tag).setOnClickListener(v -> {
            removeTagFromNote();
            dismiss();
        });

        view.findViewById(R.id.option_edit_tag).setOnClickListener(v -> {
            editTag();
            dismiss();
        });

        view.findViewById(R.id.option_delete_tag).setOnClickListener(v -> {
            deleteTag();
            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                // Lấy chiều cao màn hình
                int screenHeight = getResources().getDisplayMetrics().heightPixels;

                // Điều chỉnh độ cao của BottomSheetDialog để chiếm toàn bộ màn hình
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                if (layoutParams == null) {
                    layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, screenHeight);
                } else {
                    layoutParams.height = screenHeight;
                }
                bottomSheet.setLayoutParams(layoutParams);

                // Cấu hình BottomSheetBehavior
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setPeekHeight(screenHeight); // Đặt peekHeight bằng chiều cao màn hình
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Mở rộng hoàn toàn
                behavior.setSkipCollapsed(true); // Bỏ qua trạng thái collapsed
                behavior.setFitToContents(false); // Không tự động điều chỉnh theo nội dung
            }
        }
    }

    private void removeTagFromNote() {
        if (getActivity() instanceof ViewNoteActivity) {
            ViewNoteActivity activity = (ViewNoteActivity) getActivity();
            Note currentNote = activity.getCurrentNote();
            if (currentNote != null) {
                List<String> updatedTagIds = new ArrayList<>(currentNote.getTags());
                updatedTagIds.remove(tag.getId());
                currentNote.setTags((ArrayList<String>) updatedTagIds);

                // Cập nhật Note trên Firestore
                FirebaseFirestore.getInstance().collection("notes").document(currentNote.getId())
                        .update("tags", updatedTagIds)
                        .addOnSuccessListener(aVoid -> {
                            if (tagActionListener != null) {
                                tagActionListener.onTagRemoved(tag);
                            }
                            Toast.makeText(getContext(), "Tag removed", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to remove tag", Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }

    private void editTag() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Tag");

        final EditText input = new EditText(requireContext());
        input.setText(tag.getName());
        input.setHint("Enter new tag name");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newTagName = input.getText().toString().trim();
            if (!newTagName.isEmpty() && !newTagName.equals(tag.getName())) {
                tag.editTagInFirestore(requireContext(),
                        newTagName,
                        () -> {
                            if (tagActionListener != null) {
                                tagActionListener.onTagEdited(tag);
                            }
                            Toast.makeText(getContext(), "Tag updated", Toast.LENGTH_SHORT).show();
                        },
                        error -> Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void deleteTag() {
        tag.deleteTagInFirestore(requireContext(),
                () -> {
                    if (tagActionListener != null) {
                        tagActionListener.onTagDeleted(tag);
                    }
                    Toast.makeText(getContext(), "Tag deleted", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());
    }
}