package com.gameex.dw.justtalk.groupInfo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.util.LogUtil;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.GroupMemberInfo;
import cn.jpush.im.api.BasicCallback;

/**
 * 群组详细信息展示
 */
public class GroupInfoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "GroupInfoActivity";
    /**
     * 返回箭头
     */
    private ImageView mBack;
    /**
     * 页面标题
     */
    private TextView mTitle;
    /**
     * 右上角功能键
     */
    private ImageView mMore;
    /**
     * 包含头像的layout
     */
    private RelativeLayout mIconLayout;
    /**
     * 我的群昵称的layout
     */
    private RelativeLayout mNickLayout;
    /**
     * 群二维码layout
     */
    private RelativeLayout mQrCodeLayout;
    /**
     * 消息推送开关layout
     */
    private RelativeLayout mPushLayout;
    /**
     * 邀请群成员layout
     */
    private RelativeLayout mInviteMember;
    /**
     * 群头像
     */
    private CircularImageView mIcon;
    /**
     * 群名称
     */
    private TextView mName;
    /**
     * 修改群头像
     */
    private TextView mChangeIcon;
    /**
     * 我的群昵称
     */
    private TextView mNick;
    /**
     * 群公告
     */
    private TextView mNotice;
    /**
     * 群管理
     */
    private TextView mManage;
    /**
     * 群消息通知
     */
    private TextView mPush;
    /**
     * 长时间未领取红包
     */
    private TextView mMoneyGift;
    /**
     * 群聊天记录
     */
    private TextView mChatRecord;
    /**
     * 群聊天文件
     */
    private TextView mChatFile;
    /**
     * 群成员数
     */
    private TextView mMemberCount;
    /**
     * 群组体
     */
    private GroupInfo mGroupInfo;

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
        mBack = findViewById(R.id.back);
        mTitle = findViewById(R.id.title);
        mMore = findViewById(R.id.more);
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
        mMemberCount = findViewById(R.id.group_member_count);
        mInviteMember = findViewById(R.id.invite_member_layout);
    }

    /**
     * 初始化数据
     */
    @SuppressLint("SetTextI18n")
    private void initData() {
        mGroupInfo = GroupInfo.fromJson(getIntent().getStringExtra("group_info"));

        mBack.setOnClickListener(this);
        mTitle.setText("群详情");
        mMore.setOnClickListener(this);

        mIconLayout.setOnClickListener(this);
        mName.setText(mGroupInfo.getGroupName());
        mChangeIcon.setOnClickListener(this);
        mNickLayout.setOnClickListener(this);
        mNick.setText(getGroupNick(JMessageClient.getMyInfo().getUserName()));
        mQrCodeLayout.setOnClickListener(this);
        mNotice.setOnClickListener(this);
        mManage.setOnClickListener(this);
        mPushLayout.setOnClickListener(this);
        if (mGroupInfo.isGroupBlocked() == 0) {
            mPush.setText("屏蔽");
            mPush.setTextColor(Color.RED);
        }
        mMoneyGift.setOnClickListener(this);
        mChatRecord.setOnClickListener(this);
        mChatFile.setOnClickListener(this);
        List<GroupMemberInfo> memberInfos = mGroupInfo.getGroupMemberInfos();
        if (memberInfos != null && memberInfos.size() > 0) {
            mMemberCount.setText(memberInfos.size() + "");
        } else {
            mMemberCount.setText("0");
        }
        mInviteMember.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.more:
                Toast.makeText(this, "更多", Toast.LENGTH_SHORT).show();
                break;
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
                if (mGroupInfo.isGroupBlocked() == 1) {
                    mPush.setText("屏蔽");
                    mPush.setTextColor(Color.RED);
                    mGroupInfo.setBlockGroupMessage(0, new BasicCallback() {
                        @Override
                        public void gotResult(int i, String s) {
                            LogUtil.d(TAG, "onClick-group_push_layout: " + "requestCode = " + i +
                                    " ;desc = " + s);
                            if (i == 0) {
                                Toast.makeText(GroupInfoActivity.this, "已开启"
                                        , Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    mPush.setText("开启");
                    mPush.setTextColor(getResources().getColor(R.color.colorLightGray));
                    mGroupInfo.setBlockGroupMessage(1, new BasicCallback() {
                        @Override
                        public void gotResult(int i, String s) {
                            LogUtil.d(TAG, "onClick-group_push_layout: " + "requestCode = " + i +
                                    " ;desc = " + s);
                            if (i == 0) {
                                Toast.makeText(GroupInfoActivity.this, "已屏蔽"
                                        , Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
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

    /**
     * 获取用户在该群组的群昵称
     *
     * @param username 用户名
     * @return string
     */
    private String getGroupNick(String username) {
        GroupMemberInfo memberInfo = mGroupInfo.getGroupMember(username, null);
        return memberInfo.getNickName();
    }
}
