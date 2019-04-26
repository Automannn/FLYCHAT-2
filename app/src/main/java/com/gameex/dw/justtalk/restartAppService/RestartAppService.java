package com.gameex.dw.justtalk.restartAppService;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class RestartAppService extends Service {
    /**
     * 关闭应用后多久重新启动
     */
    private static long mStopDelayed = 50;
    private Handler mHandler;
    private String mPackageName;

    public RestartAppService() {
        mHandler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mStopDelayed = intent.getLongExtra("delayed", 50);
        mPackageName = intent.getStringExtra("package_name");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(mPackageName);
                startActivity(launchIntent);
                RestartAppService.this.stopSelf();
            }
        }, mStopDelayed);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
