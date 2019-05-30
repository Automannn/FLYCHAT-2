package com.gameex.dw.justtalk.jiguangIM;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.gameex.dw.justtalk.objPack.MsgInfo;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.singleChat.ChattingActivity;
import com.gameex.dw.justtalk.groupChat.GroupChatActivity;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;

import java.io.IOException;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.event.ContactNotifyEvent;
import cn.jpush.im.android.api.event.LoginStateChangeEvent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.event.NotificationClickEvent;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;

import static com.gameex.dw.justtalk.main.BottomBarFat.UPDATE_MSG_INFO;
import static com.gameex.dw.justtalk.jiguangIM.NotificationReceiver.CONTACT_NOTIFY_ACCEPT;
import static com.gameex.dw.justtalk.jiguangIM.NotificationReceiver.CONTACT_NOTIFY_CLICKED;
import static com.gameex.dw.justtalk.jiguangIM.NotificationReceiver.CONTACT_NOTIFY_DELETE;
import static com.gameex.dw.justtalk.jiguangIM.NotificationReceiver.CONTACT_NOTIFY_REFUSED;
import static com.gameex.dw.justtalk.jiguangIM.NotificationReceiver.LOGOUT_EVENT;
import static com.gameex.dw.justtalk.jiguangIM.NotificationReceiver.NOTIFY_Click_BIG;
import static com.gameex.dw.justtalk.jiguangIM.NotificationReceiver.NOTIFY_Click_NORMAL;
import static com.gameex.dw.justtalk.jiguangIM.NotificationReceiver.NOTIFY_TYPE_DEFAULT;
import static com.gameex.dw.justtalk.jiguangIM.NotificationReceiver.NOTIFY_TYPE_EXTRA;
import static com.gameex.dw.justtalk.jiguangIM.NotificationReceiver.NOTIFY_TYPE_ONE;

/**
 * 在demo中对于通知栏点击事件和在线消息接收事件，我们都直接在全局监听
 */
public class GlobalEventListener {
    private static final String TAG = "GLOBAL_EVENT_LISTENER";
    private Context appContext;
    private NotificationManager notifyManager;

    public GlobalEventListener(Context context) {
        appContext = context;
        JMessageClient.registerEventReceiver(this);
    }

    /**
     * 用户登陆状态变更事件
     *
     * @param lscEvent LoginStateChangeEvent
     */
    public void onEvent(LoginStateChangeEvent lscEvent) {
        final Intent intent = new Intent();
        switch (lscEvent.getReason()) {
            case user_logout:
                intent.setAction(LOGOUT_EVENT);
                appContext.sendBroadcast(intent);
                break;
            default:
                break;
        }
    }

    /**
     * 通知栏点击事件
     *
     * @param ncEvent NotificationClickEvent
     */
    public void onEvent(NotificationClickEvent ncEvent) {
        jumpToActivity(ncEvent.getMessage());
    }

    /**
     * 在线消息事件
     *
     * @param mEvent MessageEvent
     */
    public void onEvent(MessageEvent mEvent) {
        goUpdateMsgInfos(mEvent.getMessage());
    }

    /**
     * 好友相关事件
     *
     * @param cnEvent ContactNotifyEvent
     */
    public void onEvent(ContactNotifyEvent cnEvent) {
        LogUtil.d(TAG, "EventType = " + cnEvent.getType() +
                "reason = " + cnEvent.getReason() +
                "username = " + cnEvent.getFromUsername() +
                "AppKey = " + cnEvent.getfromUserAppKey());
        Uri ringtoneNotify = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(appContext, ringtoneNotify);
        Vibrator vibrator = (Vibrator) appContext
                .getSystemService(Context.VIBRATOR_SERVICE);
        switch (cnEvent.getType()) {
            case invite_received:   //收到好友添加请求
                //通知栏弹出通知提示,可查看请求信息;可进行“同意/拒绝”的处理;点击通知跳转至请求用户基本信息页，
                notifyInviteReceived(cnEvent.getFromUsername(), cnEvent.getReason());
                //若双方互发好友添加请求，则会自动成为好友，此处应作适当处理
                break;
            case invite_accepted:   //对方接受了你的好友请求
                /*播放提示音，刷新联系人列表，飞聊标签页增加新朋友item，
                  并在聊天界面加入“你们已经是好友了...”的聊天记录（提示）
                */
                ringtone.play();
                vibrator.vibrate(new long[]{0, 200, 200, 100}, -1);
                Intent intent = new Intent(UPDATE_MSG_INFO);
                MsgInfo msgInfo=new MsgInfo(cnEvent.getFromUsername(),DataUtil.getCurrentDateStr()
                        ,"我同意加你为好友了，你想和我聊点什么...",true);
                msgInfo.setSingle(true);
                intent.putExtra("msg_info",msgInfo);
                appContext.sendBroadcast(intent);
                break;
            case invite_declined:   //对方拒绝了你的好友请求
                //通知栏弹出通知提示，点击通知则跳转至拒绝用户的基本信息页
                break;
            case contact_deleted:   //对方将你从好友列表中删除
                //（通知栏弹出通知提示） 刷新联系人列表
                break;
            case contact_updated_by_dev_api:    //好友关系更新，由API管理员操作引起
                //待处理
                break;
            default:
                break;
        }
    }

    /**
     * 点击消息通知栏，跳转到对应聊天界面
     *
     * @param msg 消息体
     */
    private void jumpToActivity(Message msg) {
        Intent intent = new Intent();
        UserInfo fromUser = msg.getFromUser();
        String username = TextUtils.isEmpty(fromUser.getNickname())
                ? fromUser.getUserName() : fromUser.getNickname();
        String date = DataUtil.msFormMMDD(System.currentTimeMillis());
        String uriPath = fromUser.getExtra("user_icon") == null ? DataUtil
                .resourceIdToUri(appContext.getPackageName(), R.drawable.icon_user).toString()
                : fromUser.getExtra("user_icon");
        MsgInfo msgInfo = new MsgInfo(username, date, "", false);
        msgInfo.setUriPath(uriPath);
        msgInfo.setUserInfoJson(fromUser.toJson());
        switch (msg.getTargetType()) {
            case single:
                JMessageClient.enterSingleConversation(username);
                intent.setClass(appContext, ChattingActivity.class);
                intent.putExtra("msg_info", msgInfo);
                appContext.startActivity(intent);
                break;
            case group:
                GroupInfo groupInfo = (GroupInfo) msg.getTargetInfo();
                msgInfo.setGroupInfoJson(groupInfo.toJson());
                JMessageClient.enterGroupConversation(groupInfo.getGroupID());
                intent.setClass(appContext, GroupChatActivity.class);
                intent.putExtra("msg_info", msgInfo);
                intent.putExtra("group_id", groupInfo.getGroupID());
                intent.putExtra("group_icon", DataUtil.resourceIdToUri(
                        appContext.getPackageName(), R.drawable.icon_group));
                intent.putExtra("group_name", groupInfo.getGroupName());
                appContext.startActivity(intent);
                break;
        }
    }

    /**
     * 收到在线消息，判断消息类型，获取基本数据
     *
     * @param message 消息体
     */
    private void goUpdateMsgInfos(Message message) {
        String date = DataUtil.msFormMMDD(message.getCreateTime());
        MsgInfo msgInfo = new MsgInfo();
        msgInfo.setDate(date);
        msgInfo.setIsNotify(true);
        switch (message.getContentType()) {
            case text:
                TextContent content = (TextContent) message.getContent();
                msgInfo.setMsgLast(content.getText());
                updateMsgInfo(message, msgInfo);
                break;
            case image:
                msgInfo.setMsgLast("图片");
                updateMsgInfo(message, msgInfo);
                break;
            case custom:
                msgInfo.setMsgLast("红包");
                updateMsgInfo(message, msgInfo);
                break;
            case eventNotification:
                GroupInfo groupInfo = (GroupInfo) message.getTargetInfo();
                EventNotificationContent notificationContent = (EventNotificationContent) message.getContent();
                handleGroupEvent(notificationContent.getEventNotificationType());
                break;
        }
    }

    /**
     * 配置并弹出好友请求notification
     *
     * @param username 用于JMessage查询用户的username
     * @param content  上下下文参数
     */
    private void notifyInviteReceived(String username, final String content) {
        notifyManager = (NotificationManager) appContext.getSystemService(
                Context.NOTIFICATION_SERVICE);
        final Notification.Builder builder = new Notification.Builder(appContext)
                .setSmallIcon(R.drawable.logo)
                .setContentText("新的朋友")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 200, 200, 100})
                .setLights(Color.GREEN, 1000, 1000)
                .setDeleteIntent(getActivityPendingIntent(-2, null, null))
                .addAction(R.drawable.light_gray_rip_out, "同意",
                        getActivityPendingIntent(0, username, null))
                .addAction(R.drawable.light_gray_rip_out, "拒绝",
                        getActivityPendingIntent(-1, username, null))
                .setPriority(Notification.PRIORITY_MAX);
        JMessageClient.getUserInfo(username, new GetUserInfoCallback() {
            @Override
            public void gotResult(int responseCode, String getUserDesc, UserInfo userInfo) {
                if (responseCode == 0) {
                    builder.setContentTitle(userInfo.getExtra("username") == null
                            ? userInfo.getUserName() : userInfo.getExtra("username"));
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(appContext.getContentResolver()
                                , userInfo.getExtra("icon") == null ? DataUtil.resourceIdToUri(
                                        appContext.getPackageName(), R.drawable.icon_user)
                                        : Uri.parse(userInfo.getExtra("icon")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    builder.setLargeIcon(bitmap);
                    builder.setStyle(new Notification.BigTextStyle()
                            .bigText("\t\t" + content));
                    builder.setContentIntent(getActivityPendingIntent(1, null, userInfo));
                    notifyManager.notify(1, builder.build());
                } else {
                    LogUtil.i(TAG, "JMessageClient.getUserInfo() : " +
                            "responseCode = " + responseCode + " ; " +
                            "getUserDesc = " + getUserDesc);
                }
            }
        });
    }

    /**
     * 给notification设置事件
     *
     * @param flag     请求码
     * @param userInfo 用户信息体
     * @return pendingIntent
     */
    private PendingIntent getActivityPendingIntent(int flag, String username
            , UserInfo userInfo) {
        Intent intent = new Intent(appContext, NotificationReceiver.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra(NOTIFY_TYPE_EXTRA, NOTIFY_TYPE_ONE);
        if (flag == 3) {
            intent.setAction(NOTIFY_Click_BIG);
            intent.putExtra(NOTIFY_TYPE_EXTRA, NOTIFY_TYPE_DEFAULT);
        } else if (flag == 2) {
            intent.setAction(NOTIFY_Click_NORMAL);
            intent.putExtra(NOTIFY_TYPE_EXTRA, NOTIFY_TYPE_DEFAULT);
        } else if (flag == 1) {
            intent.setAction(CONTACT_NOTIFY_CLICKED);
            intent.putExtra(NOTIFY_TYPE_EXTRA, NOTIFY_TYPE_ONE);
            intent.putExtra("user_info_json", userInfo.toJson());
        } else if (flag == 0) {
            intent.setAction(CONTACT_NOTIFY_ACCEPT);
            intent.putExtra("username", username);
            intent.putExtra(NOTIFY_TYPE_EXTRA, NOTIFY_TYPE_ONE);
        } else if (flag == -1) {
            intent.setAction(CONTACT_NOTIFY_REFUSED);
            intent.putExtra("username", username);
            intent.putExtra(NOTIFY_TYPE_EXTRA, NOTIFY_TYPE_ONE);
        } else {
            intent.setAction(CONTACT_NOTIFY_DELETE);
            intent.putExtra(NOTIFY_TYPE_EXTRA, NOTIFY_TYPE_ONE);
        }
        return PendingIntent.getBroadcast(appContext, flag, intent,
                PendingIntent.FLAG_ONE_SHOT /*PendingIntent.FLAG_UPDATE_CURRENT*/);
    }

    /**
     * 判断是群组信息还是单聊信息，做最后处理，并发送广播更新飞聊标签页
     *
     * @param message 信息体
     * @param msgInfo 自定义信息体
     */
    private void updateMsgInfo(Message message, MsgInfo msgInfo) {
        Intent intent = new Intent(UPDATE_MSG_INFO);
        switch (message.getTargetType()) {
            case single:
                UserInfo userInfo = message.getFromUser();
                msgInfo.setUsername(userInfo.getUserName());
                msgInfo.setSingle(true);
                break;
            case group:
                GroupInfo groupInfo = (GroupInfo) message.getTargetInfo();
                msgInfo.setUsername(groupInfo.getGroupName());
                msgInfo.setSingle(false);
                msgInfo.setGroupInfoJson(groupInfo.toJson());
                break;
        }
        intent.putExtra("msg_info", msgInfo);
        appContext.sendBroadcast(intent);
    }

    /**
     * 处理群组事件
     *
     * @param type 群组事件类型
     */
    private void handleGroupEvent(EventNotificationContent.EventNotificationType type) {
        switch (type) {
            case group_member_added:    //群成员加群事件
                LogUtil.d(TAG, "handleGroupEvent-group_member_added: " + "新成员");
                break;
            case group_member_removed:  //群成员被踢事件
                LogUtil.d(TAG, "handleGroupEvent-group_member_removed: " + "群成员被踢");
                break;
            case group_member_exit:  //群成员退群事件
                LogUtil.d(TAG, "handleGroupEvent-group_member_exit: " + "群成员退群");
                break;
            case group_info_updated:    //群信息变更事件
                LogUtil.d(TAG, "handleGroupEvent-group_info_updated: " + "群信息变更");
                break;
        }
    }
}
