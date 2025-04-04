package com.example.amplenoteclone.tag;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private ActivityResultLauncher<Intent> editTagLauncher;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo ActivityResultLauncher để xử lý kết quả từ EditTagActivity
        editTagLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Tag updatedTag = (Tag) data.getSerializableExtra(EditTagActivity.EXTRA_NEW_TAG_NAME);
                            if (updatedTag != null && tagActionListener != null) {
                                tagActionListener.onTagEdited(updatedTag);
                            }
                        }
                    }
                }
        );
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
            tagIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.textGray));
        } else {
            tagIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.textBlue));
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
            // Khởi động EditTagActivity
            Intent intent = new Intent(getContext(), EditTagActivity.class);
            intent.putExtra(EditTagActivity.EXTRA_TAG, tag);
            editTagLauncher.launch(intent);
            dismiss(); // Đóng BottomSheetTagMenu
        });

        view.findViewById(R.id.option_delete_tag).setOnClickListener(v -> {
            dismiss();
            showDeleteConfirmationDialog();
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

    private void showDeleteConfirmationDialog() {
        Context context = getContext();
        if (context == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_delete_tag, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        TextView deleteMessage = dialogView.findViewById(R.id.delete_message);
        String message = "Deleting #" + tag.getName() + " will remove it from all notes it is applied to and remove all descendant tags. You cannot undo this action.";
        deleteMessage.setText(message);

        TextView cancelButton = dialogView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        TextView deleteButton = dialogView.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(v -> {
            dialog.dismiss();
            tag.deleteTagInFirestore(context,
                    () -> {
                        if (tagActionListener != null) {
                            tagActionListener.onTagDeleted(tag);
                        }
                        Toast.makeText(context, "Tag deleted", Toast.LENGTH_SHORT).show();
                    },
                    error -> Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            );
        });

        dialog.show();
    }
    private void removeTagFromNote() {
        // Lưu context an toàn
        Context context = getContext();
        if (context == null) return;

        if (getActivity() instanceof ViewNoteActivity) {
            ViewNoteActivity activity = (ViewNoteActivity) getActivity();
            Note currentNote = activity.getCurrentNote();
            if (currentNote != null) {
                List<String> updatedTagIds = new ArrayList<>(currentNote.getTags());
                updatedTagIds.remove(tag.getId());
                currentNote.setTags((ArrayList<String>) updatedTagIds);

                // Cập nhật Note trên Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("notes").document(currentNote.getId())
                        .update("tags", updatedTagIds)
                        .addOnSuccessListener(aVoid -> {
                            if (tagActionListener != null) {
                                tagActionListener.onTagRemoved(tag);
                            }
                            // Kiểm tra xem có note nào khác còn chứa tag này không
                            checkAndDeleteTagIfUnused(context, tag);
                            // Sử dụng context đã lưu
                            if (context != null) {
                                Toast.makeText(context.getApplicationContext(),
                                        "Tag removed", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Sử dụng context đã lưu
                            if (context != null) {
                                Toast.makeText(context.getApplicationContext(),
                                        "Failed to remove tag", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    private void checkAndDeleteTagIfUnused(Context context, Tag tag) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notes")
                .whereArrayContains("tags", tag.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Không có note nào chứa tag này, xóa tag
                        tag.deleteTagInFirestore(context.getApplicationContext(),
                                () -> {
                                    if (tagActionListener != null) {
                                        tagActionListener.onTagDeleted(tag);
                                    }
                                    if (context != null) {
                                        Toast.makeText(context.getApplicationContext(),
                                                "Tag deleted as it was not used in any notes", Toast.LENGTH_SHORT).show();
                                    }
                                },
                                error -> {
                                    if (context != null) {
                                        Toast.makeText(context.getApplicationContext(),
                                                error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    if (context != null) {
                        Toast.makeText(context.getApplicationContext(),
                                "Failed to check tag usage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}