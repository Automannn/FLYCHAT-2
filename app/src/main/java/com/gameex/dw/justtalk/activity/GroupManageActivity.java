package com.gameex.dw.justtalk.activity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.GroupMemberInfo;
import cn.jpush.im.android.api.model.Message;
import es.dmoral.toasty.Toasty;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.util.LogUtil;

import java.util.List;

/**
 * 群管理界面，涉及群主权限转让、管理群管理员
 */
public class GroupManageActivity extends BaseActivity {
    private static final String TAG = "GroupManageActivity";

    @BindView(R.id.keeper_count)
    TextView mKeeperCount;

    @OnClick({R.id.back, R.id.set_keeper, R.id.change_owner})
    void doClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.set_keeper:
                intent.setClass(GroupManageActivity.this, SetKeeperActivity.class);
                intent.putExtra("groupId", mGroupInfo.getGroupID());
                startActivity(intent);
                break;
            case R.id.change_owner:
                intent.setClass(this,ChooseOwnerActivity.class);
                intent.putExtra("groupId",mGroupInfo.getGroupID());
                startActivity(intent);
                break;
        }
    }

    /**
     * 群组信息
     */
    private GroupInfo mGroupInfo;
    /**
     * 群管理员信息
     */
    private List<GroupMemberInfo> mKeeperInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_manage);
        ButterKnife.bind(this);
        initData();
        JMessageClient.registerEventReceiver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JMessageClient.unRegisterEventReceiver(this);
    }

    /**
     * 在线消息处理事件
     *
     * @param event messageEvent
     */
    public void onEventMainThread(MessageEvent event) {
        Message message = event.getMessage();
        GroupInfo groupInfo = (GroupInfo) message.getTargetInfo();
        if (groupInfo.getGroupID() != mGroupInfo.getGroupID()) {
            return;
        }
        if (message.getContentType() == ContentType.eventNotification) {
            EventNotificationContent content = (EventNotificationContent) message.getContent();
            updateInfo(content);
        }
    }

    /**
     * 接收到群信息变化后，更新当前页内的群信息
     *
     * @param content 群事件对象
     */
    private void updateInfo(EventNotificationContent content) {
        LogUtil.d(TAG, "updateInfo: " + "content = " + content.toJson());
        switch (content.getEventNotificationType()) {
            case group_keeper_added:    //新管理
            case group_keeper_removed: //撤销管理
                initData();
                break;
            default:
                break;
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        long groupId = getIntent().getLongExtra("groupId", -1);
        if (groupId != -1)
            JMessageClient.getGroupInfo(groupId, new GetGroupInfoCallback() {
                @Override
                public void gotResult(int i, String s, GroupInfo groupInfo) {
                    if (i == 0) {
                        mGroupInfo = groupInfo;
                        mKeeperInfos = mGroupInfo.getGroupKeeperMemberInfos();
                        mKeeperCount.setText(String.valueOf(mKeeperInfos.size()));
                    } else {
                        LogUtil.d(TAG, "initData: " + "responseCode = " + i + " ;desc = " + s);
                    }
                }
            });
    }
}
