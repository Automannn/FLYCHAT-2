package com.gameex.dw.justtalk.util;

import android.util.Log;

/**
 * @version 1.0
 * 日志工具类
 * Author Dennil Wei
 */

public class LogUtil {

    public static final int VERBOSE = 1;

    public static final int DEBUG = 2;

    public static final int INFO = 3;

    public static final int WARN = 4;

    public static final int ERROR = 5;

    public static final int NOTHING = 6;
    /**
     * 控制日志是否输出或仅输出某类日志
     * level=VErBOSE 时表示打印所有日志
     * level=NOTHING　时表示不打印日志
     */
    public static int level = VERBOSE;

    public static void v(String tag, String msg) {
        if (level <= VERBOSE) {
            Log.v(tag, "- - - - - - - - - - >>" + msg);
        }
    }

    public static void d(String tag, String msg) {
        if (level <= DEBUG) {
            Log.d(tag, "- - - - - - - - - - >>" + msg);
        }
    }

    public static void i(String tag, String msg) {
        if (level <= INFO) {
            Log.i(tag, "- - - - - - - - - - >>" + msg);
        }
    }

    public static void w(String tag, String msg) {
        if (level <= WARN) {
            Log.w(tag, "- - - - - - - - - - >>" + msg);
        }
    }

    public static void e(String tag, String msg) {
        if (level <= ERROR) {
            Log.e(tag, "- - - - - - - - - - >>" + msg);
        }
    }

}
