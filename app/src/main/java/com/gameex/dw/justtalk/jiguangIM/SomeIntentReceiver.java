package com.gameex.dw.justtalk.jiguangIM;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.gameex.dw.justtalk.chattingPack.ChattingActivity;
import com.gameex.dw.justtalk.util.LogUtil;

import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;

public class SomeIntentReceiver extends BroadcastReceiver {
    private static final String TAG = "SOMEINTENTRECEIVER";
    private static final String TYPE_MAIN="";
    private static final String TYPE_CHAT ="";

    private NotificationManager mNm;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (null == mNm) {
            mNm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        Bundle bundle = intent.getExtras();
        assert bundle != null;
        LogUtil.d(TAG, "onReceive - " + intent.getAction() + ", extras: " + bundle.toString());
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            LogUtil.d(TAG, "JPush 用户注册成功");
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            LogUtil.d(TAG, "接受到推送下来的自定义消息");
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            LogUtil.d(TAG, "接受到推送下来的通知");
            receivingNotification(context, bundle);
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            LogUtil.d(TAG, "用户点击打开了通知");
            openNotification(context, bundle);
        } else {
            LogUtil.d(TAG, "Unhandled intent - " + intent.getAction());
        }
    }

    private void receivingNotification(Context context, Bundle bundle) {
        String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
        LogUtil.d(TAG, " title : " + title);
        String message = bundle.getString(JPushInterface.EXTRA_ALERT);
        LogUtil.d(TAG, "message : " + message);
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        LogUtil.d(TAG, "extras : " + extras);
    }

    private void openNotification(Context context, Bundle bundle) {
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        String valueBun;
        try {
            JSONObject extrasJson = new JSONObject(extras);
            valueBun = extrasJson.optString("myKey");
        } catch (Exception e) {
            LogUtil.w(TAG, "Unexpected: extras is not a valid json");
            e.printStackTrace();
            return;
        }
        LogUtil.d(TAG, "extrasJson: "+"" + valueBun);
        if (TYPE_MAIN.equals(valueBun)) {
//            Intent mIntent = new Intent(context, BottomBarActivity.class);
//            mIntent.putExtras(bundle);
//            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(mIntent);
            LogUtil.d(TAG,"Jump to MainActivity");
        } else if (TYPE_CHAT.equals(valueBun)) {
//            Intent mIntent = new Intent(context, ChattingActivity.class);
//            mIntent.putExtras(bundle);
//            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(mIntent);
            LogUtil.d(TAG,"Jump to ChattingActivity");
        }
    }
}
