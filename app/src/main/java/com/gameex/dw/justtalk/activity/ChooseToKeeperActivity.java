package com.gameex.dw.justtalk.activity;

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
import es.dmoral.toasty.Toasty;

import android.os.Bundle;
import android.view.View;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.adapter.ChooseToKeeperAdapter;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class ChooseToKeeperActivity extends BaseActivity {
    private static final String TAG = "ChooseToKeeperActivity";

    @BindView(R.id.recycler)
    RecyclerView mRecycler;

    @OnClick({R.id.back, R.id.add})
    void doClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.add:
                mGroupInfo.addGroupKeeper(mUserInfos, new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {
                        if (i == 0) {
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toasty.info(ChooseToKeeperActivity.this, "出错了").show();
                            LogUtil.d(TAG, "doClick: " + "responseCode = " + i + " ;desc = " + s);
                        }
                    }
                });
                break;
        }
    }

    /**
     * 群组信息
     */
    private GroupInfo mGroupInfo;
    /**
     * 群成员信息
     */
    private List<GroupMemberInfo> mMemberInfos;
    private List<UserInfo> mUserInfos = new ArrayList<>();
    private ChooseToKeeperAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_to_keeper);
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
        mAdapter = new ChooseToKeeperAdapter();
        mAdapter.setItemClick(userInfo -> {
            if (mUserInfos.contains(userInfo)) mUserInfos.remove(userInfo);
            else mUserInfos.add(userInfo);
        });

        long groupId = getIntent().getLongExtra("groupId", -1);
        if (groupId != -1)
            JMessageClient.getGroupInfo(groupId, new GetGroupInfoCallback() {
                @Override
                public void gotResult(int i, String s, GroupInfo groupInfo) {
                    if (i == 0) {
                        mGroupInfo = groupInfo;
                        GroupMemberInfo ownerInfo = mGroupInfo.getOwnerMemberInfo();
                        List<GroupMemberInfo> keeperInfo = mGroupInfo.getGroupKeeperMemberInfos();
                        mMemberInfos = mGroupInfo.getGroupMemberInfos();
                        mMemberInfos.remove(ownerInfo);
                        mMemberInfos.removeAll(keeperInfo);
                        mAdapter.setContext(ChooseToKeeperActivity.this);
                        mAdapter.setMemberInfos(mMemberInfos);
                        mRecycler.setAdapter(mAdapter);
                    } else LogUtil.d(TAG, "initData: " + "responseCode = " + i + " ;desc = " + s);
                }
            });
    }
}
