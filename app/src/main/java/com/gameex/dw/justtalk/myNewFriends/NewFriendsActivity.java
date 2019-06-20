package com.gameex.dw.justtalk.myNewFriends;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import es.dmoral.toasty.Toasty;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.util.GsonUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.SharedPreferenceUtil;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NewFriendsActivity extends BaseActivity {
    private static final String TAG = "NewFriendsActivity";
    /**
     * 增加新邀请
     */
    public static final String ADD_RECEIVE =
            "com.gameex.dw.justtalk.myNewFriends.NewFriendsActivity.UPDATE_RECEIVE_LIST";
    /**
     * 删除邀请
     */
    public static final String DELETE_RECEIVE =
            "com.gameex.dw.justtalk.myNewFriends.NewFriendsActivity.DELETE_RECEIVE";

    /**
     * 搜索输入框
     */
    @BindView(R.id.search_edit)
    EditText searchEdit;

    /**
     * 搜索提示框
     */
    @BindView(R.id.search_text)
    TextView searchText;
    /**
     * 未处理的新朋友列表
     */
    @BindView(R.id.recycler)
    RecyclerView mRecycler;
    /**
     * 适配器
     */
    private NewFriendsAdapter mAdapter;
    private List<Map<String, String>> mDatas = new ArrayList<>();
    private NewFriendsReceiver mReceiver;

    /**
     * 点击隐藏提示框，并显示输入框
     *
     * @param v view
     */
    @OnClick({R.id.back, R.id.search_text})
    void submit(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.search_text:
                searchText.setVisibility(View.GONE);
                searchEdit.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * 搜索输入框失去焦点时隐藏，并显示搜索提示框
     *
     * @param isFocus 是否获得焦点
     */
    @OnFocusChange(R.id.search_edit)
    void onFocusChanged(boolean isFocus) {
        if (!isFocus) {
            searchEdit.setVisibility(View.GONE);
            searchText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 监听搜索输入框的输入内容
     *
     * @param e 输入的内容对象
     */
    @OnTextChanged(R.id.search_edit)
    void afterTextChange(Editable e) {
        Toasty.info(this, e).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friends);
        ButterKnife.bind(this);

        initData();
        mReceiver = new NewFriendsReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ADD_RECEIVE);
        filter.addAction(DELETE_RECEIVE);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        SharedPreferenceUtil.putList("new_friends_receive_list", mDatas);
        super.onDestroy();
    }

    /**
     * 初始化view数据
     */
    private void initData() {
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(300);
        animator.setChangeDuration(300);
        animator.setMoveDuration(300);
        animator.setRemoveDuration(300);
        mRecycler.setItemAnimator(animator);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));

        mDatas = SharedPreferenceUtil.getList("new_friends_receive_list"
                , new TypeToken<List<Map<String, String>>>() {
                }.getType());
        mAdapter = new NewFriendsAdapter(this, mDatas);
        mRecycler.setAdapter(mAdapter);
    }

    /**
     * 收到好友请求时更新数据
     */
    class NewFriendsReceiver extends BroadcastReceiver {
        private static final String tag = "NewFriendsReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            switch (action) {
                case ADD_RECEIVE:
                    Map<String, String> map = new HashMap<>();
                    map.put("username", intent.getStringExtra("contact_username"));
                    map.put("date", intent.getStringExtra("contact_date"));
                    map.put("reason", intent.getStringExtra("contact_reason"));
                    map.put("userInfo", intent.getStringExtra("userInfo"));
                    mDatas.add(map);
                    mAdapter.notifyItemInserted(mDatas.size() - 1);
                    break;
                case DELETE_RECEIVE:
                    UserInfo userInfo = UserInfo.fromJson(intent.getStringExtra("userInfo"));
                    for (int i = 0; i < mDatas.size(); i++) {
                        UserInfo user = UserInfo.fromJson(mDatas.get(i).get("userInfo"));
                        if (user.getUserName().equals(userInfo.getUserName())) {
                            mDatas.remove(i);
                            mAdapter.notifyItemRemoved(i);
                        }
                    }
                    break;
            }
        }
    }
}
