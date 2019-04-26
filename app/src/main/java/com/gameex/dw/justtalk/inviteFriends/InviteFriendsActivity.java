package com.gameex.dw.justtalk.inviteFriends;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.titleBar.OnViewClick;
import com.gameex.dw.justtalk.titleBar.TitleBarView;


public class InviteFriendsActivity extends BaseActivity implements View.OnClickListener {

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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.invite_by_msg_layout:
                Toast.makeText(this, "发送短信", Toast.LENGTH_SHORT).show();
                break;
            case R.id.invite_by_wechat_layout:
                Toast.makeText(this, "分享给微信好友", Toast.LENGTH_SHORT).show();
                break;
            case R.id.invite_by_wezone_layout:
                Toast.makeText(this, "分享到朋友圈", Toast.LENGTH_SHORT).show();
                break;
            case R.id.invite_by_qq_layout:
                Toast.makeText(this, "分享给QQ好友", Toast.LENGTH_SHORT).show();
                break;
            case R.id.invite_by_qzone_layout:
                Toast.makeText(this, "分享到qq空间", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}
