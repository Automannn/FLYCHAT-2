package com.gameex.dw.justtalk;

import android.annotation.SuppressLint;
import android.app.Application;
import android.support.multidex.MultiDexApplication;

import com.gameex.dw.justtalk.jiguangIM.GlobalEventListener;
import com.gameex.dw.justtalk.util.LogUtil;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

public class JGApplication extends MultiDexApplication {
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
        JMessageClient.setDebugMode(true);
        JMessageClient.init(getApplicationContext(), true);
        JPushInterface.init(getApplicationContext());
        JMessageClient.registerEventReceiver(
                new GlobalEventListener(getApplicationContext()));

        EmojiManager.install(new TwitterEmojiProvider());
//        upateUser();
    }

    private void upateUser() {
        JMessageClient.getUserInfo("13404081072", new GetUserInfoCallback() {
            @Override
            public void gotResult(int i, String s, UserInfo userInfo) {
                if (i == 0) {
                    userInfo.setUserExtras("index", "#");
                    JMessageClient.updateMyInfo(UserInfo.Field.extras, userInfo, new BasicCallback() {
                        @Override
                        public void gotResult(int i, String s) {
                            LogUtil.d(TAG, "updateUser: " + "responseCode = " + i
                                    + " ;desc = " + s);
                        }
                    });
                }
            }
        });
    }
}
