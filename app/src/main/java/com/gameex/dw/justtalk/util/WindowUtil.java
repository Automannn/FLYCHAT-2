package com.gameex.dw.justtalk.util;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

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
        animator.addUpdateListener(valueAnimator -> {
            float alpha = (float) animator.getAnimatedValue();
            setWindowBackgroundAlpha(activity, alpha);
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

    /**
     * 调用第三方浏览器打开
     *
     * @param context 上下文
     * @param url     要浏览的资源地址
     */
    public static void openBrowser(Context context, String url) {
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        // 注意此处的判断intent.resolveActivity()可以返回显示该Intent的Activity对应的组件名
        // 官方解释 : Name of the component implementing an activity that can display the intent
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            final ComponentName componentName = intent.resolveActivity(context.getPackageManager());
            // 打印Log   ComponentName到底是什么 L.d("componentName = " + componentName.getClassName());
            context.startActivity(Intent.createChooser(intent, "请选择浏览器"));
        } else {
            Toast.makeText(context.getApplicationContext(), "请下载浏览器", Toast.LENGTH_SHORT).show();
        }
    }
}
