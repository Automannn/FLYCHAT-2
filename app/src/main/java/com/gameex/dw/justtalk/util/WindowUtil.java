package com.gameex.dw.justtalk.util;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

public class WindowUtil {

    /**
     * 控制窗口背景的不透明度
     */
    public static void setWindowBackgroundAlpha(Activity activity, float alpha) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.alpha = alpha;
        window.setAttributes(layoutParams);
    }

    /**
     * 窗口显示，窗口背景透明度渐变动画
     */
    public static void showBackgroundAnimator(final Activity activity, float mAlpha) {
        if (mAlpha >= 1f) {
            return;
        }
        final ValueAnimator animator = ValueAnimator.ofFloat(1.0f, mAlpha);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float alpha = (float) animator.getAnimatedValue();
                setWindowBackgroundAlpha(activity, alpha);
            }
        });
        animator.setDuration(500);
        animator.start();
    }

    /**
     * 获得屏幕的宽高
     */
    public static int[] getWH(Activity activity) {
        int[] ints = new int[2];
        WindowManager windowManager = activity.getWindowManager();
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        ints[0] = metrics.widthPixels;
        ints[1] = metrics.heightPixels;
        return ints;
    }
}
