package com.gameex.dw.justtalk.activity;

import android.os.Bundle;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.adapter.ChooseOwnerAdapter;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.util.LogUtil;
import com.rey.material.app.Dialog;

import java.util.List;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.GroupMemberInfo;
import cn.jpush.im.api.BasicCallback;
import es.dmoral.toasty.Toasty;

public class ChooseOwnerActivity extends BaseActivity {
    private static final String TAG = "ChooseOwnerActivity";

    @OnClick(R.id.back)
    void onClick() {
        finish();
    }

    @BindView(R.id.recycler)
    RecyclerView mRecycler;

    private ChooseOwnerAdapter mAdapter;
    /**
     * 群信息
     */
    private GroupInfo mGroupInfo;
    /**
     * 群成员信息
     */
    private List<GroupMemberInfo> mMemberInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_owner);
        ButterKnife.bind(this);
        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mAdapter = new ChooseOwnerAdapter();
        mAdapter.setOnItemClick(this::showNewOwnerDialog);
        long groupId = getIntent().getLongExtra("groupId", -1);
        JMessageClient.getGroupInfo(groupId, new GetGroupInfoCallback() {
            @Override
            public void gotResult(int i, String s, GroupInfo groupInfo) {
                if (i == 0) {
                    mGroupInfo = groupInfo;
                    GroupMemberInfo ownerInfo = mGroupInfo.getOwnerMemberInfo();
                    mMemberInfos = mGroupInfo.getGroupMemberInfos();
                    mMemberInfos.remove(ownerInfo);
                    mAdapter.setContext(ChooseOwnerActivity.this);
                    mAdapter.setMemberInfos(mMemberInfos);
                    mRecycler.setAdapter(mAdapter);
                } else {
                    LogUtil.d(TAG, "initData: " + "responseCode = " + i + " ;desc = " + s);
                }
            }
        });
    }

    /**
     * 确认新群主弹窗
     *
     * @param username 用户名
     */
    private void showNewOwnerDialog(String username) {
        Dialog dialog = new Dialog(this);
        dialog.title("此用户即将成为新的群主")
                .positiveAction("确定")
                .negativeAction("取消")
                .positiveActionClickListener(view -> mGroupInfo.changeGroupAdmin(username, null, new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {
                        if (i == 0) {
                            Toasty.normal(ChooseOwnerActivity.this, "已移交群主权限").show();
                            dialog.dismiss();
                            finish();
                        } else {
                            Toasty.info(ChooseOwnerActivity.this, "只有群主可以进行此项操作").show();
                            LogUtil.d(TAG, "initData: " + "responseCode = " + i + " ;desc = " + s);
                        }
                    }
                }))
                .negativeActionClickListener(view -> dialog.dismiss())
                .cancelable(true)
                .show();

    }
}
