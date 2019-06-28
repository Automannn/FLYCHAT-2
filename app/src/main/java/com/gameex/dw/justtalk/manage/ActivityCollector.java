package com.gameex.dw.justtalk.manage;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * 与BaseActivity一起管理其他的activity
 */
public class ActivityCollector {
    public static List<Activity> sActivities = new ArrayList<>();

    public static void addActivity(Activity activity) {
        sActivities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        sActivities.remove(activity);
    }

    /**
     * 结束所有activity
     */
    public static void finishAll() {
        for (Activity activity : sActivities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        sActivities.clear();
    }

    /**
     *
     */
    public static void recreateAll() {
        for (Activity activity : sActivities) {
            activity.recreate();
        }
    }
}
