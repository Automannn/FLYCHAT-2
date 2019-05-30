package com.gameex.dw.justtalk.restartAppService;

import android.content.Context;
import android.content.Intent;
import android.os.Process;

public class RestartAppTool {

    /**
     * 重启整个App
     *
     * @param context 上下文
     * @param delayed 延迟多少秒
     */
    public static void restartApp(Context context, long delayed) {
        //开启一个新的服务，来重启app
        Intent intent = new Intent(context, RestartAppService.class);
        intent.putExtra("package_name", context.getPackageName());
        intent.putExtra("delayed", delayed);
        context.startService(intent);

        //杀死整个进程
        Process.killProcess(Process.myPid());
    }

    /**
     * 重启整个App
     *
     * @param context 上下文
     */
    public static void restartApp(Context context) {
        restartApp(context, 2000);
    }
}
