package com.gameex.dw.justtalk.util;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;

import java.util.Calendar;

/**
 * 数据处理工具类
 */
public class DataUtil {
    public static final String ANDROID_RESOURCE = "android.resource://";
    public static final String FOREWARD_SLASH = "/";

    /**
     * 目标项是否在最后一个可见项之后
     */
    public static boolean mShouldScroll;
    /**
     * 记录目标项位置
     */
    public static int mToPosition;

    /**
     * 将资源id转换成Uri
     *
     * @param packageName
     * @param resourceId
     * @return
     */
    public static Uri resourceIdToUri(String packageName, int resourceId) {
        Uri uri = Uri.parse(ANDROID_RESOURCE + "" + packageName + "" + FOREWARD_SLASH + "" + resourceId);
        return uri;
    }

    /**
     * dp转px
     *
     * @param context
     * @param dp
     * @return
     */
    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, context.getResources().getDisplayMetrics());
    }

    /**
     * 获取系统的日期
     *
     * @return
     */
    public static String getCurrentDateStr() {
        Calendar calendar = Calendar.getInstance();   //获取系统的日期
        int month = calendar.get(Calendar.MONTH) + 1;   //月
        int day = calendar.get(Calendar.DAY_OF_MONTH);    //日
        String date = month + "月" + day + "日";
        return date;
    }

    /**
     * 获取系统的时间
     *
     * @return
     */
    public static String getCurrentTimeStr() {
        Calendar calendar = Calendar.getInstance();   //获取系统的时间
        int hour = calendar.get(Calendar.HOUR_OF_DAY);   //小时
        int minute = calendar.get(Calendar.MINUTE);    //分钟
        String time = hour + ":" + minute;
        return time;
    }

    /**
     * 滚动到指定item，并置顶
     *
     * @param view
     * @param position
     */
    public static void smoothMovetoPosition(RecyclerView view, final int position) {
        //第一个可见位置
        int firstItem = view.getChildLayoutPosition(view.getChildAt(0));
        //最后一个可见位置
        int lastItem = view.getChildLayoutPosition(view.getChildAt(view.getChildCount() - 1));
        if (position < firstItem) {
            //第一种可能：跳转位置在第一个可见位置之前
            view.smoothScrollToPosition(position);
        } else if (position <= lastItem) {
            //第二种可能:跳转位置在第一个可见位置之后
            int movePosition = position - firstItem;
            if (movePosition >= 0 && movePosition < view.getChildCount()) {
                int top = view.getChildAt(movePosition).getTop();
                view.smoothScrollBy(0, top);
            }
        } else {
            //第三种可能：跳转位置在最后可见项之后
            view.smoothScrollToPosition(position);
            mToPosition = position;
            mShouldScroll = true;
        }
    }
}
