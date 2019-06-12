package com.gameex.dw.justtalk.inviteFriends;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.titleBar.OnViewClick;
import com.gameex.dw.justtalk.titleBar.TitleBarView;
import com.gameex.dw.justtalk.util.LogUtil;

import java.util.HashMap;

import cn.jiguang.share.android.api.JShareInterface;
import cn.jiguang.share.android.api.PlatActionListener;
import cn.jiguang.share.android.api.Platform;
import cn.jiguang.share.android.api.ShareParams;
import cn.jiguang.share.qqmodel.QQ;
import cn.jiguang.share.qqmodel.QZone;
import cn.jiguang.share.wechat.Wechat;
import cn.jiguang.share.wechat.WechatMoments;
import es.dmoral.toasty.Toasty;


public class InviteFriendsActivity extends BaseActivity
        implements View.OnClickListener, PlatActionListener {
    private static final String TAG = "InviteFriendsActivity";

    private TitleBarView mTitleBar;
    private LinearLayout mInviteByMsg, mInviteByWechat, mInviteByWezone, mInviteByQq, mInviteByQzone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);

        initView();
    }

    private void initView() {
        mTitleBar = findViewById(R.id.title_bar_invite);
        mTitleBar.setTitle("邀请好友");
        mTitleBar.setRightIVVisible(View.GONE);
        mTitleBar.setSearchIVVisible(View.GONE);
        mTitleBar.setOnViewClick(new OnViewClick() {
            @Override
            public void leftClick() {
                finish();
            }

            @Override
            public void searchClick() {

            }

            @Override
            public void rightClick() {

            }
        });
        mInviteByMsg = findViewById(R.id.invite_by_msg_layout);
        mInviteByMsg.setOnClickListener(this);
        mInviteByWechat = findViewById(R.id.invite_by_wechat_layout);
        mInviteByWechat.setOnClickListener(this);
        mInviteByWezone = findViewById(R.id.invite_by_wezone_layout);
        mInviteByWezone.setOnClickListener(this);
        mInviteByQq = findViewById(R.id.invite_by_qq_layout);
        mInviteByQq.setOnClickListener(this);
        mInviteByQzone = findViewById(R.id.invite_by_qzone_layout);
        mInviteByQzone.setOnClickListener(this);
    }

    /**
     * 调起系统发短信功能
     * @param phoneNumber  接收人手机号
     * @param message   短信内容
     */
    public void doSendSMSTo(String phoneNumber,String message){
        if(PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)){
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+phoneNumber));
            intent.putExtra("sms_body", message);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View view) {
        ShareParams params = new ShareParams();
        params.setShareType(Platform.SHARE_TEXT);
        params.setText("分享文本：嗨，我正在使用飞聊嗨/聊，来和我一起聊天吧~");
        switch (view.getId()) {
            case R.id.invite_by_msg_layout:
                doSendSMSTo("18180027763","嗨，我正在使用飞聊嗨/聊，来和我一起聊天吧~");
                Toasty.info(this, "发送短信", Toasty.LENGTH_SHORT).show();
                break;
            case R.id.invite_by_wechat_layout:
                JShareInterface.share(Wechat.Name, params,this);
                Toasty.info(this, "分享给微信好友", Toasty.LENGTH_SHORT).show();
                break;
            case R.id.invite_by_wezone_layout:
                JShareInterface.share(WechatMoments.Name, params,this);
                Toasty.info(this, "分享到朋友圈", Toasty.LENGTH_SHORT).show();
                break;
            case R.id.invite_by_qq_layout:
                ShareParams paramsQQ=new ShareParams();
                paramsQQ.setShareType(Platform.SHARE_WEBPAGE);
                paramsQQ.setUrl("https://www.baidu.com");
                paramsQQ.setTitle("分享给QQ好友");
                paramsQQ.setText("嗨，我正在使用飞聊嗨/聊，来和我一起聊天吧~");
                JShareInterface.share(QQ.Name, paramsQQ,this);
                Toasty.info(this, "分享给QQ好友", Toasty.LENGTH_SHORT).show();
                break;
            case R.id.invite_by_qzone_layout:
                JShareInterface.share(QZone.Name, params,this);
                Toasty.info(this, "分享到qq空间", Toasty.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
        LogUtil.i(TAG, "onComplete: " + "platform.getName = " + platform.getName()
                + " ;i = " + i + " ;hashMap = " + hashMap);
    }

    @Override
    public void onError(Platform platform, int i, int i1, Throwable throwable) {
        LogUtil.i(TAG, "onError: " + "platform.getName = " + platform.getName()
                + " ;i = " + i + " ;i1 = " + i1 + " ;throwable = " + throwable.getMessage());
    }

    @Override
    public void onCancel(Platform platform, int i) {
        LogUtil.i(TAG, "onComplete: " + "platform.getName = " + platform.getName()
                + " ;i = " + i);
    }
}
