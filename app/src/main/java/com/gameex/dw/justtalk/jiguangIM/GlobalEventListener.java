package com.gameex.dw.justtalk.jiguangIM;

import android.content.Context;
import android.content.Intent;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.event.NotificationClickEvent;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;

public class GlobalEventListener {
    private Context appContext;

    public GlobalEventListener(Context context) {
        appContext = context;
        JMessageClient.registerEventReceiver(this);
    }

    public void onEvent(NotificationClickEvent event) {
        jumpToActivity(event.getMessage());
    }

    public void onEvent(MessageEvent event) {
        jumpToActivity(event.getMessage());
    }

    private void jumpToActivity(Message msg) {
        UserInfo fromUser = msg.getFromUser();
//        final Intent notificationIntent = new Intent(appContext, ShowMessageActivity.class);
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if (msg.getTargetType() == ConversationType.group) {
//            GroupInfo groupInfo = (GroupInfo) msg.getTargetInfo();
//            notificationIntent.putExtra(ShowMessageActivity.EXTRA_IS_GROUP, true);
//            notificationIntent.putExtra(ShowMessageActivity.EXTRA_GROUPID, groupInfo.getGroupID());
//        } else {
//            notificationIntent.putExtra(ShowMessageActivity.EXTRA_IS_GROUP, false);
//        }
//
//        notificationIntent.putExtra(ShowMessageActivity.EXTRA_FROM_USERNAME, fromUser.getUserName());
//        notificationIntent.putExtra(ShowMessageActivity.EXTRA_FROM_APPKEY, fromUser.getAppKey());
//        notificationIntent.putExtra(ShowMessageActivity.EXTRA_MSG_TYPE, msg.getContentType().toString());
//        notificationIntent.putExtra(ShowMessageActivity.EXTRA_MSGID, msg.getId());
//        appContext.startActivity(notificationIntent);
    }
}
