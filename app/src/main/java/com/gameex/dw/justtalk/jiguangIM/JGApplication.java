package com.gameex.dw.justtalk.jiguangIM;

import android.annotation.SuppressLint;
import android.app.Application;
import com.gameex.dw.justtalk.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.Message;

public class JGApplication extends Application {
    private static final String TAG = "JGAPPLICATION";
    /**
     * 极光AppKey
     */
    public static final String APP_KEY = "fa964c46085d5543e75797c0";

    public static final String CONV_TITLE = "conv_title";
    public static final int IMAGE_MESSAGE = 1;
    public static final int TAKE_PHOTO_MESSAGE = 2;
    public static final int TAKE_LOCATION = 3;
    public static final int FILE_MESSAGE = 4;
    public static final int TACK_VIDEO = 5;
    public static final int TACK_VOICE = 6;
    public static final int BUSINESS_CARD = 7;
    public static final int REQUEST_CODE_SEND_FILE = 26;

    public static final int RESULT_CODE_ALL_MEMBER = 22;
    @SuppressLint("UseSparseArrays")
    public static Map<Long, Boolean> isAtMe = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    public static Map<Long, Boolean> isAtAll = new HashMap<>();
    public static List<Message> forWardMsg = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.i("IMDebugApplication", "init");
        JMessageClient.setDebugMode(true);
        JMessageClient.init(getApplicationContext(), true);
        JPushInterface.init(getApplicationContext());
        JMessageClient.registerEventReceiver(
                new GlobalEventListener(getApplicationContext()));
    }
}
