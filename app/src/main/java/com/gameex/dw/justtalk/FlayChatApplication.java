package com.gameex.dw.justtalk;

import android.annotation.SuppressLint;

import com.gameex.dw.justtalk.jiguangIM.GlobalEventListener;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.SharedPreferenceUtil;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.multidex.MultiDexApplication;
import cn.jiguang.share.android.api.JShareInterface;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.Message;

public class FlayChatApplication extends MultiDexApplication {
    private static final String TAG = "JGAPPLICATION";
    /**
     * 极光AppKey
     */
    public static final String APP_KEY = "fa964c46085d5543e75797c0";
    /**
     * 微信appId
     */
    public static final String WE_CHAT_APP_ID = "wx440625ae89c4244d";

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
//        JMessageClient.setDebugMode(true);  //极光im调式模式
        JMessageClient.init(getApplicationContext(), true); //极光im sdk初始化，并启动消息漫游
        JPushInterface.init(getApplicationContext());   //极光推送sdk初始化
        JShareInterface.init(getApplicationContext());  //极光社会化分享sdk初始化
//        JShareInterface.setDebugMode(true); //极光社会化分享调试模式
        JMessageClient.registerEventReceiver(
                new GlobalEventListener(getApplicationContext()));

        EmojiManager.install(new TwitterEmojiProvider());

        //初始化缓存工具类
        SharedPreferenceUtil.getInstance(this, null);
    }
}
