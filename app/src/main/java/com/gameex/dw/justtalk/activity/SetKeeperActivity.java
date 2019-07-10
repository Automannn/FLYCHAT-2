package com.gameex.dw.justtalk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.adapter.SetKeeperAdapter;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.util.LogUtil;
import com.rey.material.app.Dialog;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
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
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

public class SetKeeperActivity extends BaseActivity {
    private static final String TAG = "SetKeeperActivity";

    private static final int RESULT_ADD_KEEPER = 101;

    @BindView(R.id.recycler)
    RecyclerView mRecycler;

    @OnClick({R.id.back, R.id.add_keeper})
    void doClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.add_keeper:
                Intent intent = new Intent(this, ChooseToKeeperActivity.class);
                intent.putExtra("groupId", mGroupInfo.getGroupID());
                startActivityForResult(intent, 101);
                break;
        }
    }

    /**
     * 群组信息
     */
    private GroupInfo mGroupInfo;
    /**
     * 管理员信息
     */
    private List<GroupMemberInfo> mKeeperInfos;
    private SetKeeperAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_keeper);
        ButterKnife.bind(this);
        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(300);
        animator.setRemoveDuration(300);
        mRecycler.setItemAnimator(animator);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mAdapter = new SetKeeperAdapter();
        mAdapter.setItemLongClick(this::showCancelDialog);

        long groupId = getIntent().getLongExtra("groupId", -1);
        initRecycler(groupId);
    }

    /**
     * 加载管理员列表
     *
     * @param groupId 群id
     */
    private void initRecycler(long groupId) {
        if (groupId != -1)
            JMessageClient.getGroupInfo(groupId, new GetGroupInfoCallback() {
                @Override
                public void gotResult(int i, String s, GroupInfo groupInfo) {
                    if (i == 0) {
                        mGroupInfo = groupInfo;
                        mKeeperInfos = mGroupInfo.getGroupKeeperMemberInfos();
                        if (mKeeperInfos != null && mKeeperInfos.size() > 0) {
                            mRecycler.setVisibility(View.VISIBLE);
                            mAdapter.setContext(SetKeeperActivity.this);
                            mAdapter.setKeeperInfo(mKeeperInfos);
                            mRecycler.setAdapter(mAdapter);
                        }
                    } else LogUtil.d(TAG, "initData: " + "responseCode = " + i + " ;desc = " + s);
                }
            });
    }

    /**
     * 长按管理员条目弹窗，用于撤销管理员身份
     *
     * @param position 长按位置
     */
    private void showCancelDialog(int position) {
        Dialog dialog = new Dialog(this);
        dialog.title("撤销管理员身份")
                .positiveAction("确定")
                .positiveActionClickListener(view -> {
                    List<UserInfo> userInfos = new ArrayList<>();
                    UserInfo userInfo = mKeeperInfos.get(position).getUserInfo();
                    userInfos.add(userInfo);
                    mGroupInfo.removeGroupKeeper(userInfos, new BasicCallback() {
                        @Override
                        public void gotResult(int i, String s) {
                            if (i == 0) {
                                mKeeperInfos.remove(position);
                                mAdapter.notifyItemRemoved(position);
                                dialog.dismiss();
                            } else {
                                LogUtil.d(TAG, "showCancelDialog: " + "responseCode = " + i + " ;desc = " + s);
                            }
                        }
                    });
                })
                .negativeAction("取消")
                .negativeActionClickListener(view -> dialog.dismiss())
                .cancelable(true)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RESULT_ADD_KEEPER && resultCode == RESULT_OK)
            initRecycler(mGroupInfo.getGroupID());
        super.onActivityResult(requestCode, resultCode, data);
    }
}
