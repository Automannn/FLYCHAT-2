package com.gameex.dw.justtalk.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 数据处理工具类
 */
public class DataUtil {
    private static final String ANDROID_RESOURCE = "android.resource://";
    private static final String FOREWARD_SLASH = "/";
    /**
     * 验证手机号码是否合法的正则表达式
     * 13[0-9],14[5/7],15[0-3]/[5-9],17[6-8],18[0-9]
     * "13"代表前两位数字
     * "[0-9]"代表第二位可以为0-9中的一个
     * "[^4]"代表除了4
     * "\\d{8}"代表后面可以是0-9的数字，有8位
     */
    private static final String TELEPHONE_REGEX = "((13[0-9])|(15[^4])|(18[0-9])|(17[0-8])|(147,145))\\d{8}$";

    /**
     * (?!^\d+$)不能全是数字
     * (?!^[a-zA-Z]+$)不能全是字母
     * .{8,}长度不能小于8
     */
    private static final String PASSWORD_REGEX = "(?!^\\d+$)(?!^[a-zA-Z]+$).{8,}";

    /**
     * 将资源id转换成Uri
     *
     * @param packageName app包名
     * @param resourceId  头像资源id
     * @return 返回uri
     */
    public static Uri resourceIdToUri(String packageName, int resourceId) {
        return Uri.parse(ANDROID_RESOURCE + "" + packageName + "" + FOREWARD_SLASH + "" + resourceId);
    }

    /**
     * dp转px
     *
     * @param context 上下文参数
     * @param dp      想要转换的dp值
     * @return 返回像素px
     */
    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, context.getResources().getDisplayMetrics());
    }

    /**
     * 正则表达式判断是否是手机号
     *
     * @param mobile 想要判断的字符串
     * @return 返回boolean
     */
    public static boolean isMobileNumber(String mobile) {
        return !TextUtils.isEmpty(mobile) && mobile.matches(TELEPHONE_REGEX);
    }

    /**
     * 正则表达式判断密码是否符合规范
     *
     * @param pwd 想要判断的字符串
     * @return 返回boolean
     */
    public static boolean isPWDCorrect(String pwd) {
        return !TextUtils.isEmpty(pwd) && pwd.matches(PASSWORD_REGEX);
    }

    /**
     * 获取系统的日期
     *
     * @return 返回日期字符串
     */
    public static String getCurrentDateStr() {
        Calendar calendar = Calendar.getInstance();   //获取系统的日期
        int month = calendar.get(Calendar.MONTH) + 1;   //月
        int day = calendar.get(Calendar.DAY_OF_MONTH);    //日
        return month + "月" + day + "日";
    }

    /**
     * 获取系统的时间
     *
     * @return 返回时间字符串
     */
    public static String getCurrentTimeStr() {
        Calendar calendar = Calendar.getInstance();   //获取系统的时间
        int hour = calendar.get(Calendar.HOUR_OF_DAY);   //小时
        int minute = calendar.get(Calendar.MINUTE);    //分钟
        return hour + ":" + minute;
    }

    /**
     * 将毫秒数转化为-月-日格式的字符串
     *
     * @param milliSecond 毫秒数
     * @return 字符串date
     */
    public static String msFormMMDD(long milliSecond) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("MM-dd");
        return format.format(milliSecond).replaceAll("-", "月") + "日";
    }

    /**
     * 将毫秒数转化位时:分格式的字符串
     *
     * @param milliSecond 毫秒数
     * @return 字符串time
     */
    public static String msFormHHmmTime(long milliSecond) {
        @SuppressLint("SimpleDateFormat") DateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(milliSecond);
    }

    /**
     * 判断消息的日期是否比上一条消息晚一天
     *
     * @param date     上一条消息的日期的字符串
     * @param thanDate 待比较的日期字符串
     * @return 返回boolean
     */
    public static boolean isMoreThanOneDay(String date, String thanDate) {
//        if (Integer.parseInt(date.substring()))
        int day = Integer.parseInt(date.substring(3, 5));
        int thanDay = Integer.parseInt(thanDate.substring(3, 5));
        return thanDay - day >= 1;
    }

    /**
     * 目标项是否在最后一个可见项之后
     */
    private static boolean mShouldScroll;

    public static boolean ismShouldScroll() {
        return mShouldScroll;
    }

    public static void setmShouldScroll(boolean mShouldScroll) {
        DataUtil.mShouldScroll = mShouldScroll;
    }

    /**
     * 记录目标项位置
     */
    private static int mToPosition;

    public static int getmToPosition() {
        return mToPosition;
    }

    public static void setmToPosition(int mToPosition) {
        DataUtil.mToPosition = mToPosition;
    }

    /**
     * 滚动到指定item，并置顶
     *
     * @param view     recyclerView
     * @param position 目标位置
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
