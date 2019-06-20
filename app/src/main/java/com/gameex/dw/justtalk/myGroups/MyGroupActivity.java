package com.gameex.dw.justtalk.myGroups;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gameex.dw.justtalk.main.BottomBarActivity;
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
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupIDListCallback;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.model.GroupInfo;

/**
 * 我的群组列表
 */
public class MyGroupActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "MyGroupActivity";
    /**
     * 更新群组列表
     */
    private static final int REQUEST_CODE_UPDATE_GROUP_LIST = 201;

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    @SuppressLint("NewApi")
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
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        mGreateGroup.setOnClickListener(this);

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(300);
        animator.setChangeDuration(300);
        animator.setMoveDuration(300);
        animator.setRemoveDuration(300);
        mGroups.setItemAnimator(animator);
        mGroups.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MyGroupsAdapter(this, mGroupInfos);
        mGroups.setAdapter(mAdapter);
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
                                    mAdapter.notifyItemInserted(mGroupInfos.size() - 1);
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
                startActivityForResult(intent,REQUEST_CODE_UPDATE_GROUP_LIST);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_UPDATE_GROUP_LIST && resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            mGroupInfos.add(GroupInfo.fromJson(data.getStringExtra("group_info")));
            mAdapter.notifyItemInserted(mGroupInfos.size() - 1);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
