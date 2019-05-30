package com.gameex.dw.justtalk.util;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;

/**
 * 使RecylerView滚动到指定位置并置顶
 */
public class RecScrollHelper {
    /**
     * @param recView  需要被滚动的RecyclerView
     * @param position 滚动到的位置
     */
    public static void scrollToPosition(RecyclerView recView, int position) {
        RecyclerView.LayoutManager managerRec = recView.getLayoutManager();
        if (managerRec instanceof LinearLayoutManager) {
            LinearLayoutManager manager = (LinearLayoutManager) managerRec;
            final TopSmoothScroller scroller = new TopSmoothScroller(recView.getContext());
            scroller.setTargetPosition(position);
            manager.startSmoothScroll(scroller);
        }
    }

    /**
     *
     */
    public static class TopSmoothScroller extends LinearSmoothScroller {

        TopSmoothScroller(Context context) {
            super(context);
        }

        @Override
        protected int getHorizontalSnapPreference() {
            return SNAP_TO_START;
        }

        @Override
        protected int getVerticalSnapPreference() {
            return SNAP_TO_START;
        }
    }
}
