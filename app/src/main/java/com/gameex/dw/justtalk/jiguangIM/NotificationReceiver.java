package com.gameex.dw.justtalk.jiguangIM;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.login.LoginActivity;
import com.gameex.dw.justtalk.managePack.ActivityCollector;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.objPack.MsgInfo;
import com.gameex.dw.justtalk.userInfo.UserBasicInfoActivity;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;

import androidx.appcompat.app.AlertDialog;
import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.api.BasicCallback;

import static com.gameex.dw.justtalk.FlayChatApplication.APP_KEY;
import static com.gameex.dw.justtalk.main.ContactFragment.ADD_CONTACT;
import static com.gameex.dw.justtalk.main.MsgInfoFragment.UPDATE_MSG_INFO;

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
                dialogBuilder.setPositiveButton("确定", (dialogInterface, i) -> {
                    ActivityCollector.finishAll();  //销毁所有活动
                    Intent intentLoginOut = new Intent(context, LoginActivity.class);
                    intentLoginOut.putExtra("flag", "LoginOut");
                    context.startActivity(intentLoginOut);
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
                notifyToDo.setClass(context, UserBasicInfoActivity.class);
                notifyToDo.putExtra("user_info_json"
                        , intent.getStringExtra("user_info_json"));
                notifyToDo.putExtra("isInvite", true);
                context.startActivity(notifyToDo);
                break;
            case CONTACT_NOTIFY_ACCEPT:
                final String nameAccept = intent.getStringExtra("username");
                ContactManager.acceptInvitation(nameAccept, APP_KEY, new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {
                        if (i == 0) {
                            notifyToDo.setAction(UPDATE_MSG_INFO);
                            MsgInfo msgInfo=new MsgInfo(nameAccept,DataUtil.getCurrentDateStr()
                                    ,"你们已经是好友了，聊点什么吧...",true);
                            msgInfo.setSingle(true);
                            notifyToDo.putExtra("msg_info",msgInfo);
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
}
