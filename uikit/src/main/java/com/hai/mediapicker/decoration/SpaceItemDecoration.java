package com.hai.mediapicker.decoration;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Administrator on 2017/3/14.
 */

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public SpaceItemDecoration(Context context, int space) {
        this.space = (int) (space * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        outRect.left = space;
        outRect.right = space;
        outRect.bottom = space;

        //判断是GridLayoutManager还是LinearLayoutManager
        if (parent.getLayoutManager() instanceof GridLayoutManager) {
            int spanCount = ((GridLayoutManager) parent.getLayoutManager()).getSpanCount();
            outRect.top = parent.getChildLayoutPosition(view) < spanCount ? space : 0;
        } else {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) parent.getLayoutManager();
            outRect.top = linearLayoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL ||
                    parent.getChildLayoutPosition(view) == 0 ? space : 0;
        }
    }
}
