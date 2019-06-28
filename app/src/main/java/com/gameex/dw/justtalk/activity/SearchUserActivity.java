package com.gameex.dw.justtalk.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;

import android.os.Bundle;
import android.text.Editable;
import android.widget.EditText;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.adapter.SearchUserAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchUserActivity extends AppCompatActivity {

    @OnTextChanged(R.id.search)
    void afterTextChanged(Editable e) {
        JMessageClient.getUserInfo(e.toString(), new GetUserInfoCallback() {
            @Override
            public void gotResult(int i, String s, UserInfo userInfo) {
                if (i == 0) {
                    mUserInfos.clear();
                    mUserInfos.add(userInfo);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @BindView(R.id.recycler)
    RecyclerView mRecycler;
    private SearchUserAdapter mAdapter;
    private List<UserInfo> mUserInfos = new ArrayList<>();
    private long mGroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);
        ButterKnife.bind(this);
        initRecycler();
    }

    /**
     * 初始化recycler
     */
    private void initRecycler() {
        mGroupId = getIntent().getLongExtra("groupId", -1);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        mAdapter = new SearchUserAdapter(this, mUserInfos, mGroupId);
        mRecycler.setAdapter(mAdapter);
    }
}
