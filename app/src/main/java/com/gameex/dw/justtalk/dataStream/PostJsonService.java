package com.gameex.dw.justtalk.dataStream;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 开启服务，处理与服务器之间的数据交接
 */
public class PostJsonService extends Service {
    public PostJsonService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String[] signInfo = intent.getExtras().getStringArray("sign_info");
        assert signInfo != null;
        String phone = signInfo[0];
        String pwd = signInfo[1];
        String url = signInfo[2];
        new PostJson(phone, pwd, url, this).execute();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
