package com.gameex.dw.justtalk.groupInfo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.titleBar.OnViewClick;
import com.gameex.dw.justtalk.titleBar.TitleBarView;
import com.gameex.dw.justtalk.util.LogUtil;
import com.github.siyamed.shapeimageview.CircularImageView;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.model.GroupInfo;


public class GroupInfoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "GroupInfoActivity";

    private TitleBarView mTitleBarView;
    private RelativeLayout mIconLayout, mNickLayout, mQrCodeLayout, mPushLayout, mInviteMember;
    private CircularImageView mIcon;
    private TextView mName, mChangeIcon, mNick, mNotice, mManage, mPush, mMoneyGift, mChatRecord, mChatFile, mMemberCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        setContentView(R.layout.activity_group);
        mTitleBarView = findViewById(R.id.title_bar_group_info);
        mIconLayout = findViewById(R.id.group_icon_info_layout);
        mIcon = findViewById(R.id.group_icon_info);
        mName = findViewById(R.id.group_nick);
        mChangeIcon = findViewById(R.id.change_icon_text);
        mNickLayout = findViewById(R.id.mine_group_nick_layout);
        mNick = findViewById(R.id.mine_group_nick);
        mQrCodeLayout = findViewById(R.id.group_qr_code_layout);
        mNotice = findViewById(R.id.group_notice);
        mManage = findViewById(R.id.group_manage);
        mPushLayout = findViewById(R.id.group_push_layout);
        mPush = findViewById(R.id.group_push_nick);
        mMoneyGift = findViewById(R.id.money_gift_long_time_no_see);
        mChatRecord = findViewById(R.id.group_chat_record);
        mChatFile = findViewById(R.id.group_chat_file);
        mInviteMember = findViewById(R.id.invite_member_layout);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mTitleBarView.setTitle("群详情");
        mTitleBarView.setRightIVImg(R.drawable.more);
        mTitleBarView.setSearchIVVisible(View.GONE);
        mTitleBarView.setOnViewClick(new OnViewClick() {
            @Override
            public void leftClick() {
                finish();
            }

            @Override
            public void searchClick() {

            }

            @Override
            public void rightClick() {
                Toast.makeText(GroupInfoActivity.this, "更多", Toast.LENGTH_SHORT).show();
            }
        });

        mIconLayout.setOnClickListener(this);
        mChangeIcon.setOnClickListener(this);
        mNickLayout.setOnClickListener(this);
        mQrCodeLayout.setOnClickListener(this);
        mNotice.setOnClickListener(this);
        mManage.setOnClickListener(this);
        mPushLayout.setOnClickListener(this);
        mMoneyGift.setOnClickListener(this);
        mChatRecord.setOnClickListener(this);
        mChatFile.setOnClickListener(this);
        mInviteMember.setOnClickListener(this);

        JMessageClient.getGroupInfo(getIntent().getLongExtra("group_id", -1)
                , new GetGroupInfoCallback() {
                    @Override
                    public void gotResult(int i, String s, GroupInfo groupInfo) {
                        if (i == 0) {
                            LogUtil.d(TAG, "initData: " + "groupInfo = " + groupInfo.toJson());
                        } else {
                            LogUtil.d(TAG, "initData: " + "responseCode = " + i +
                                    " ; desc = " + s);
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.group_icon_info_layout:
                Toast.makeText(this, "修改群名称", Toast.LENGTH_SHORT).show();
                break;
            case R.id.change_icon_text:
                Toast.makeText(this, "修改群头像", Toast.LENGTH_SHORT).show();
                break;
            case R.id.mine_group_nick_layout:
                Toast.makeText(this, "修改我在本群的昵称", Toast.LENGTH_SHORT).show();
                break;
            case R.id.group_qr_code_layout:
                Toast.makeText(this, "查看群二维码", Toast.LENGTH_SHORT).show();
                break;
            case R.id.group_notice:
                Toast.makeText(this, "显示群公告", Toast.LENGTH_SHORT).show();
                break;
            case R.id.group_manage:
                Toast.makeText(this, "管理群成员", Toast.LENGTH_SHORT).show();
                break;
            case R.id.group_push_layout:
                Toast.makeText(this, "开启/关闭消息通知", Toast.LENGTH_SHORT).show();
                break;
            case R.id.money_gift_long_time_no_see:
                Toast.makeText(this, "查看长时间未领取的红包", Toast.LENGTH_SHORT).show();
                break;
            case R.id.group_chat_record:
                Toast.makeText(this, "查看聊天记录", Toast.LENGTH_SHORT).show();
                break;
            case R.id.group_chat_file:
                Toast.makeText(this, "查看聊天文件", Toast.LENGTH_SHORT).show();
                break;
            case R.id.invite_member_layout:
                Toast.makeText(this, "邀请群成员", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
