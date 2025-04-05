package com.example.amplenoteclone.tag;

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

import com.example.amplenoteclone.R;
import com.example.amplenoteclone.models.Tag;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EditTagFragment extends BottomSheetDialogFragment {

    public static final String EXTRA_TAG = "extra_tag";

    private Tag tag;
    private EditText tagNameInput;
    private ImageView backButton;
    private OnTagEditListener tagEditListener;

    public interface OnTagEditListener {
        void onTagEdited(String newTagName);
    }

    public static EditTagFragment newInstance(Tag tag, OnTagEditListener listener) {
        EditTagFragment fragment = new EditTagFragment();
        fragment.tagEditListener = listener;
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_tag, container, false);

        if (getArguments() != null) {
            tag = (Tag) getArguments().getSerializable(EXTRA_TAG);
        }

        if (tag == null) {
            dismiss();
            return view;
        }

        backButton = view.findViewById(R.id.back_button);
        TextView currentTagName = view.findViewById(R.id.current_tag_name);
        tagNameInput = view.findViewById(R.id.tag_name_input);
        TextView renameButton = view.findViewById(R.id.rename_button);
        TextView cancelButton = view.findViewById(R.id.cancel_button);

        currentTagName.setText("#" + tag.getName());
        tagNameInput.setText(tag.getName());

        backButton.setOnClickListener(v -> dismiss());
        cancelButton.setOnClickListener(v -> dismiss());

        renameButton.setOnClickListener(v -> {
            String newTagName = tagNameInput.getText().toString().trim();
            if (!newTagName.isEmpty() && !newTagName.equals(tag.getName())) {
                tag.editTagInFirestore(requireContext(),
                        newTagName,
                        () -> {
                            Toast.makeText(requireContext(), "Tag updated", Toast.LENGTH_SHORT).show();
                            if (tagEditListener != null) {
                                tagEditListener.onTagEdited(newTagName);
                            }
                            dismiss();
                        },
                        error -> {
                            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                            dismiss();
                        });
            } else {
                dismiss();
            }
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
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                if (layoutParams == null) {
                    layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, screenHeight);
                } else {
                    layoutParams.height = screenHeight;
                }
                bottomSheet.setLayoutParams(layoutParams);

                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setPeekHeight(screenHeight);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setFitToContents(false);
            }
        }
    }
}