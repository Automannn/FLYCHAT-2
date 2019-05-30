package com.gameex.dw.justtalk.addFriends;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.publicInterface.RecyclerItemClick;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.userInfo.UserBasicInfoActivity;
import com.gameex.dw.justtalk.util.LogUtil;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;

public class AddFriendsActivity extends BaseActivity {
    private static final String TAG = "ADD_FRIENDS_ACTIVITY";

    private Toolbar mToolbar;
    private MaterialSearchView mSearchView;
    private RecyclerView mView;
    private AddFriendsAdapter mAdapter;

    private List<UserInfo> mUserInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
        initData();
    }

    /**
     * 初始化组件
     */
    private void init() {
        setContentView(R.layout.activity_add_friends);
        mToolbar = findViewById(R.id.toolbar_add_friends);
        setSupportActionBar(mToolbar);
        mSearchView = findViewById(R.id.search_view_add_friends);
        mView = findViewById(R.id.recycler_add_friends);
        mView = mView.findViewById(R.id.recycler_add_friends);
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(300);
        animator.setChangeDuration(300);
        animator.setMoveDuration(300);
        animator.setRemoveDuration(300);
        mView.setItemAnimator(animator);
        mView.setLayoutManager(new LinearLayoutManager(this));
        mView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
    }

    /**
     * 初始化数据
     */
    @SuppressLint("NewApi")
    private void initData() {
        mToolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        Objects.requireNonNull(getSupportActionBar()).setTitle("搜索用户");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSearchView.showSearch();
            }
        }, 100);
        mSearchView.setVoiceSearch(false);
        mSearchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));
        mSearchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LogUtil.d(TAG, "you click the item " +
                        "int=" + i + " long=" + l);
            }
        });
        mSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                LogUtil.d(TAG, "submit query = " + query);
                JMessageClient.getUserInfo(query, new GetUserInfoCallback() {
                    @Override
                    public void gotResult(int responseCode, String getInfoDesc, UserInfo userInfo) {
                        if (responseCode == 0) {
                            LogUtil.i(TAG, " ; userInfo = " + userInfo.toJson());
                            mUserInfos.add(userInfo);
                            mAdapter.notifyItemInserted(0);
                        } else {
                            LogUtil.i(TAG, ", responseCode = " + responseCode +
                                    " ; registerDesc = " + getInfoDesc);
                            mSearchView.showSearch();
                            Toast.makeText(AddFriendsActivity.this, "用户不存在", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                LogUtil.d(TAG, "change query = " + newText);
                return false;
            }
        });
        mSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                mView.setVisibility(View.GONE);
                mUserInfos.clear();
            }

            @Override
            public void onSearchViewClosed() {
                mView.setVisibility(View.VISIBLE);
            }
        });
        mAdapter = new AddFriendsAdapter(mUserInfos, this);
        mAdapter.setItemClick(new RecyclerItemClick() {
            @Override
            public void onClick(int position) {
                UserInfo userInfo = mUserInfos.get(position);
                Intent intent = new Intent(AddFriendsActivity.this,
                        UserBasicInfoActivity.class);
                intent.putExtra("user_info_json", userInfo.toJson());
                startActivity(intent);
            }

            @Override
            public void onLongClick(int position) {
                LogUtil.d(TAG, position + " long click");
            }
        });
        mView.setAdapter(mAdapter);
    }

    /**
     * 初始化搜索栏布局
     *
     * @param menu 表单文件
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.material_search, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        mSearchView.setMenuItem(item);
        return true;
    }
}
