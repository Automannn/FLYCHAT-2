package com.gameex.dw.justtalk.jiguangIM;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.gameex.dw.justtalk.BottomBarActivity;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.login.LoginActivity;
import com.gameex.dw.justtalk.managePack.ActivityCollector;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.userInfo.UserBasicInfoActivity;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;

import java.lang.reflect.Method;

import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.api.BasicCallback;

import static com.gameex.dw.justtalk.BottomBarFat.ADD_CONTACT;
import static com.gameex.dw.justtalk.BottomBarFat.UPDATE_MSG_INFO;
import static com.gameex.dw.justtalk.jiguangIM.JGApplication.APP_KEY;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String TAG = "NOTIFICATION_RECEIVER";
    /**
     * action:用户在其它设备登录
     */
    public static final String LOGOUT_EVENT =
            "com.gameex.dw.justtalk.GlobalEventListener.LOGOUT_EVENT";
    /**
     * action:展开通知栏
     */
    public static final String NOTIFY_Click_BIG =
            "com.gameex.dw.justtalk.GlobalEventListener.NOTIFY_Click_BIG";
    /**
     * action:收起通知栏
     */
    public static final String NOTIFY_Click_NORMAL =
            "com.gameex.dw.justtalk.GlobalEventListener.NOTIFY_Click_NORMAL";
    /**
     * 点击了好友请求notify的action
     */
    public static final String CONTACT_NOTIFY_CLICKED =
            "com.gameex.dw.justtalk.GlobalEventListener.CONTACT_NOTIFY_CLICKED";
    /**
     * 点击了好友请求中接收event的action
     */
    public static final String CONTACT_NOTIFY_ACCEPT =
            "com.gameex.dw.justtalk.GlobalEventListener.CONTACT_NOTIFY_ACCEPT";
    /**
     * 点击了好友请求中拒绝event的action
     */
    public static final String CONTACT_NOTIFY_REFUSED =
            "com.gameex.dw.justtalk.GlobalEventListener.CONTACT_NOTIFY_REFUSED";
    /**
     * 滑动删除了notify的action
     */
    public static final String CONTACT_NOTIFY_DELETE =
            "com.gameex.dw.justtalk.GlobalEventListener.CONTACT_NOTIFY_DELETE";
    public static final String NOTIFY_TYPE_EXTRA = "NOTIFY_TYPE";
    public static final int NOTIFY_TYPE_DEFAULT = -1;
    public static final int NOTIFY_TYPE_ONE = 1;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        int type = intent.getIntExtra(NOTIFY_TYPE_EXTRA, NOTIFY_TYPE_DEFAULT);
        NotificationManager mManager;
        if (type != NOTIFY_TYPE_DEFAULT) {
            mManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            assert mManager != null;
            mManager.cancel(type);
        }
        final Intent notifyToDo = new Intent();
        assert action != null;
        switch (action) {
            case LOGOUT_EVENT:
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BaseActivity.sBaseActivity);
                dialogBuilder.setIcon(R.drawable.logo);
                dialogBuilder.setTitle("账号异常");
                dialogBuilder.setMessage("账号在其他设备登录！！！");
                dialogBuilder.setCancelable(false);
                dialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCollector.finishAll();  //销毁所有活动
                        Intent intentLoginOut = new Intent(context, LoginActivity.class);
                        intentLoginOut.putExtra("flag", "LoginOut");
                        context.startActivity(intentLoginOut);
                    }
                });
                dialogBuilder.show();
                break;
            case NOTIFY_Click_BIG:
//                expandNotification(context);  //展开通知栏->无效
                break;
            case NOTIFY_Click_NORMAL:
//                collapseStatusBar(context); //收起通知栏->无效
                break;
            case CONTACT_NOTIFY_CLICKED:
                String icon = intent.getStringExtra("user_icon");
                String username = intent.getStringExtra("username");
                String phone = intent.getStringExtra("phone");
                notifyToDo.setClass(context, UserBasicInfoActivity.class);
                notifyToDo.putExtra("user_icon", icon);
                notifyToDo.putExtra("username", username);
                notifyToDo.putExtra("phone", phone);
                notifyToDo.putExtra("isInvite",true);
                context.startActivity(notifyToDo);
                break;
            case CONTACT_NOTIFY_ACCEPT:
                final String nameAccept = intent.getStringExtra("username");
                ContactManager.acceptInvitation(nameAccept, APP_KEY, new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {
                        if (i == 0) {
                            notifyToDo.setAction(UPDATE_MSG_INFO);
                            notifyToDo.putExtra("username", nameAccept);
                            notifyToDo.putExtra("date", DataUtil.getCurrentDateStr());
                            notifyToDo.putExtra("msg_last", "你们已经是好友了，聊点什么吧...");
                            notifyToDo.putExtra("is_notify", true);
                            context.sendBroadcast(notifyToDo);
                            Intent addContact = new Intent(ADD_CONTACT);
                            addContact.putExtra("username", nameAccept);
                            context.sendBroadcast(addContact);
                        } else {
                            LogUtil.d(TAG, "onReceive-accept: " + "responseCode = " + i +
                                    "desc = " + s);
                            Toast.makeText(context, "添加失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case CONTACT_NOTIFY_REFUSED:
                final String nameDecline = intent.getStringExtra("username");
                ContactManager.declineInvitation(nameDecline, APP_KEY, "not my type", new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {
                        if (i == 0) {
                            Toast.makeText(context, "你拒绝了好友请求", Toast.LENGTH_SHORT).show();
                        } else {
                            LogUtil.d(TAG, "onReceive-refused: " + "responseCode = " + i +
                                    "desc = " + s);
                            Toast.makeText(context, "请重试", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case CONTACT_NOTIFY_DELETE:
                Toast.makeText(context, "delete notify", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    /**
     * 收起通知栏
     *
     * @param context 上下文参数
     */
    public void collapseStatusBar(Context context) {
        try {
            @SuppressLint("WrongConstant") Object statusBarManager = context.getSystemService("statusbar");
            Method collapse;
            if (Build.VERSION.SDK_INT <= 16) {
                collapse = statusBarManager.getClass().getMethod("collapse");
            } else {
                collapse = statusBarManager.getClass().getMethod("collapsePanels");
            }
            collapse.invoke(statusBarManager);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    /**
     * 展开通知栏
     *
     * @param context 上下文参数
     */
    public static void expandNotification(Context context) {
        @SuppressLint("WrongConstant") Object service = context.getSystemService("statusbar");
        if (null == service) {
            return;
        }
        try {
            @SuppressLint("PrivateApi") Class<?> clazz = Class.forName("android.app.StatusBarManager");
            int sdkVersion = android.os.Build.VERSION.SDK_INT;
            Method expand;
            if (sdkVersion <= 16) {
                expand = clazz.getMethod("expand");
            } else {
                /*
                * Android SDK 16之后的版本展开通知栏有两个接口可以处理
                * expandNotificationsPanel()
                * expandSettingsPanel()
                */
                expand = clazz.getMethod("expandNotificationsPanel");
//                expand = clazz.getMethod("expandSettingsPanel");
            }
            expand.setAccessible(true);
            expand.invoke(service);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
