package com.gameex.dw.justtalk.activity;


import android.os.Bundle;
import android.text.Editable;
import android.view.View;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.adapter.GroupMemberAdapter;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.GroupMemberInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import es.dmoral.toasty.Toasty;

/**
 * 群成员列表界面
 */
public class GroupMemberActivity extends BaseActivity {
    private static final String TAG = "GroupMemberActivity";

    /**
     * 返回
     */
    @OnClick({R.id.back, R.id.more})
     void submit(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.more:
                Toasty.info(this, "敬请期待").show();
                break;
        }
    }

    /**
     * 群成员列表
     */
    @BindView(R.id.recycler)
     RecyclerView mRecycler;
    /**
     * 适配器
     */
    private GroupMemberAdapter mAdapter;
    /**
     * 群信息对象
     */
    private GroupInfo mGroupInfo;
    /**
     * 群成员信息对象集合
     */
    private List<GroupMemberInfo> mMemberInfos;

    /**
     * 监听搜索框输入后
     */
    @OnTextChanged(R.id.search)
     void afterTextChanged(Editable e) {
        Toasty.info(this, e).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member);
        ButterKnife.bind(this);
        initData();
        initRecycler();
        JMessageClient.registerEventReceiver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JMessageClient.unRegisterEventReceiver(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mGroupInfo = GroupInfo.fromJson(getIntent().getStringExtra("groupInfo"));
        mMemberInfos = mGroupInfo.getGroupMemberInfos();
    }

    /**
     * 初始化recycler view
     */
    private void initRecycler() {
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setRemoveDuration(300);
        animator.setMoveDuration(300);
        animator.setChangeDuration(300);
        animator.setAddDuration(300);
        mRecycler.setItemAnimator(animator);    //设置recycler刷新动画
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new GroupMemberAdapter(this, mMemberInfos, mGroupInfo.toJson());
        mRecycler.setAdapter(mAdapter);
        //设置item随手指移动而移动
//        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(mAdapter);
//        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
//        mItemTouchHelper.attachToRecyclerView(mRecycler);
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
            case group_member_removed:  //群成员被移除
                List<String> usernames = content.getUserNames();
                for (int n = 0; n < usernames.size() - 1; n++) {
                    String username = usernames.get(n);
                    for (int i = 0; i < mMemberInfos.size(); i++) {
                        GroupMemberInfo memberInfo = mMemberInfos.get(i);
                        if (username.equals(memberInfo.getUserInfo().getUserName())) {
                            mMemberInfos.remove(memberInfo);
                            mAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                }
            case group_member_added:    //新成员加群
            case group_member_exit: //群成员退群
                //更新群成员列表,若退群的是自己，则结束此界面
                if (content.getUserNames().get(0).equals(JMessageClient.getMyInfo().getUserName()))
                    finish();
                else
                    JMessageClient.getGroupInfo(mGroupInfo.getGroupID(), new GetGroupInfoCallback() {
                        @Override
                        public void gotResult(int i, String s, GroupInfo groupInfo) {
                            if (i == 0) {
                                mGroupInfo = groupInfo;
                                mMemberInfos = mGroupInfo.getGroupMemberInfos();
                                mAdapter.notifyDataSetChanged();
                            } else {
                                LogUtil.d(TAG, "updateInfo-group_member_removed: "
                                        + "responseCode = " + i + " ;desc = " + s);
                                Toasty.error(GroupMemberActivity.this, "群成员更新失败").show();
                            }
                        }
                    });
                break;
            case group_keeper_added:    //增加群管理
            case group_keeper_removed:  //取消群管理
                JMessageClient.getGroupInfo(mGroupInfo.getGroupID(), new GetGroupInfoCallback() {
                    @Override
                    public void gotResult(int i, String s, GroupInfo groupInfo) {
                        if (i == 0) {
                            mGroupInfo = groupInfo;
                            mMemberInfos = mGroupInfo.getGroupMemberInfos();
                            List<String> usernames = content.getUserNames();
                            for (int n = 0; n < usernames.size() - 1; n++) {
                                String username = usernames.get(n);
                                for (int t = 0; t < mMemberInfos.size(); t++) {
                                    UserInfo userInfo = mMemberInfos.get(t).getUserInfo();
                                    if (username.equals(userInfo.getUserName())) {
                                        List<String> payloads=new ArrayList<>();
                                        payloads.add(mGroupInfo.toJson());
                                        payloads.add(GroupMemberInfo.collectionToJson(mMemberInfos));
                                        mAdapter.notifyItemChanged(t, payloads);
                                        break;
                                    }
                                }
                            }
                        } else {
                            LogUtil.d(TAG, "updateInfo-group_member_removed: "
                                    + "responseCode = " + i + " ;desc = " + s);
                            Toasty.error(GroupMemberActivity.this, "群成员更新失败").show();
                        }
                    }
                });
                break;
//            case group_owner_changed:   //移交群主
//                //TODO：刷新成员列表
//                break;
            default:
                JMessageClient.getGroupInfo(mGroupInfo.getGroupID(), new GetGroupInfoCallback() {
                    @Override
                    public void gotResult(int i, String s, GroupInfo groupInfo) {
                        if (i == 0) {
                            mGroupInfo = groupInfo;
                            mMemberInfos = mGroupInfo.getGroupMemberInfos();
                        } else {
                            LogUtil.d(TAG, "updateInfo-group_member_removed: "
                                    + "responseCode = " + i + " ;desc = " + s);
                            Toasty.error(GroupMemberActivity.this, "群成员更新失败").show();
                        }
                    }
                });
                break;
        }
    }
}
