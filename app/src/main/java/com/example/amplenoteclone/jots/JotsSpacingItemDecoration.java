package com.example.amplenoteclone.jots;

import android.content.res.Resources;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class JotsSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int spacing;

    /**
     * Constructor to set the spacing between items in the RecyclerView
     * @param spacing Spacing in dp
     */
    public JotsSpacingItemDecoration(int spacing) {
        // Convert dp to pixels
        this.spacing = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                spacing,
                Resources.getSystem().getDisplayMetrics()
        );
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        // Add spacing to top of each item except the first one
        int position = parent.getChildAdapterPosition(view);
        if (position > 0) {
            outRect.top = spacing;
        }

        // You can also add bottom spacing if needed
        // outRect.bottom = spacing;
    }
}