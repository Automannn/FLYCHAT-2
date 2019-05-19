package com.gameex.dw.justtalk.myGroups;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gameex.dw.justtalk.BottomBarActivity;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.createGroup.CreateGroupActivity;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.titleBar.OnSearchListen;
import com.gameex.dw.justtalk.titleBar.OnSearchQueryListen;
import com.gameex.dw.justtalk.titleBar.OnViewClick;
import com.gameex.dw.justtalk.titleBar.TitleBarView;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupIDListCallback;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.model.GroupInfo;


public class MyGroupActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "MyGroupActivity";

    /**
     * 标题栏
     */
    private TitleBarView mBarView;
    /**
     * 发起群聊
     */
    private RelativeLayout mGreateGroup;
    /**
     * 群组列表
     */
    private RecyclerView mGroups;
    /**
     * 群组列表适配器
     */
    private MyGroupsAdapter mAdapter;
    /**
     * 群组列表集合
     */
    private List<GroupInfo> mGroupInfos = new ArrayList<>();
    private String mUserInfosStr;

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
        setContentView(R.layout.activity_my_group);

        mBarView = findViewById(R.id.title_bar);
        mGreateGroup = findViewById(R.id.create_group_layout);

        mGroups = findViewById(R.id.groups_rec);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mUserInfosStr = getIntent().getStringExtra("user_infos");

        mBarView.setTitle("我的群组");
        mBarView.setRightIVVisible(View.GONE);
        mBarView.setSearchIVVisible(View.VISIBLE);
        mBarView.setOnSearchListen(new OnSearchListen() {
            @Override
            public void onSearchShown() {

            }

            @Override
            public void onSearchClosed() {
                mBarView.setRightIVVisible(View.VISIBLE);
                mBarView.setToolbarVisible(View.GONE);
                mBarView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        DataUtil.dpToPx(MyGroupActivity.this, 74)));
            }
        });
        mBarView.setOnViewClick(new OnViewClick() {
            @Override
            public void leftClick() {
                finish();
            }

            @Override
            public void searchClick() {
                mBarView.setToolbarVisible(View.VISIBLE);
                mBarView.setSearchViewShow(true);
                mBarView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT
                        , LinearLayout.LayoutParams.MATCH_PARENT));
            }

            @Override
            public void rightClick() {

            }
        });
        mBarView.setToolbarBeActionBar(this);
        mBarView.setOnSearchListen(new OnSearchListen() {
            @Override
            public void onSearchShown() {

            }

            @Override
            public void onSearchClosed() {
                mBarView.setToolbarVisible(View.GONE);
                mBarView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        DataUtil.dpToPx(MyGroupActivity.this, 74)));
            }
        });
        mBarView.setQueryListen(new OnSearchQueryListen() {
            @Override
            public boolean onQuerySubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryChange(String newText) {
                return false;
            }
        });
        getSupportActionBar().setTitle("");

        mGreateGroup.setOnClickListener(this);

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(300);
        animator.setChangeDuration(300);
        animator.setMoveDuration(300);
        animator.setRemoveDuration(300);
        mGroups.setItemAnimator(animator);
        mGroups.setLayoutManager(new LinearLayoutManager(this));
        getGroups();
    }

    /**
     * 初始化群组列表
     */
    private void getGroups() {
        JMessageClient.getGroupIDList(new GetGroupIDListCallback() {
            @Override
            public void gotResult(int i, String s, List<Long> list) {
                if (i == 0) {
                    LogUtil.d(TAG, "getGroup: " + "list = " + list.toString());
                    for (long groupId : list) {
                        JMessageClient.getGroupInfo(groupId, new GetGroupInfoCallback() {
                            @Override
                            public void gotResult(int i, String s, GroupInfo groupInfo) {
                                if (i == 0) {
                                    LogUtil.d(TAG, "getGroups-getGroupIdList: " + groupInfo.toJson());
                                    mGroupInfos.add(groupInfo);
                                    mAdapter = new MyGroupsAdapter(
                                            MyGroupActivity.this, mGroupInfos);
                                    mGroups.setAdapter(mAdapter);
                                } else {
                                    LogUtil.d(TAG, "getGroups-getGroupIdList: " + "responseCode = "
                                            + i + " ;desc = " + s);
                                }
                            }
                        });
                    }
                } else {
                    LogUtil.d(TAG, "getGroup: " + "responseCode = "
                            + i + " ;desc = " + s);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mBarView.isSearchViewOpening()) {
            mBarView.setSearchViewShow(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.create_group_layout:
                intent.setClass(BottomBarActivity.sBottomBarActivity, CreateGroupActivity.class);
                intent.putExtra("user_infos", mUserInfosStr);
                startActivity(intent);
                break;
        }
    }
}
