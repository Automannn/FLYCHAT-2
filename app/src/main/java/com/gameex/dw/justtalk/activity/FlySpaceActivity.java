package com.gameex.dw.justtalk.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.adapter.FlySpaceSwipeAdapter;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import link.fls.swipestack.SwipeStack;

/**
 * 飞聊空间界面
 */
public class FlySpaceActivity extends BaseActivity {
    private static final String TAG = "FlySpaceActivity";

    @BindView(R.id.swipe_stack)
    SwipeStack mStack;

    @OnClick({R.id.back, R.id.edit_myself, R.id.left, R.id.add, R.id.right})
    void doClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.edit_myself:
                requestPermission();
                break;
            case R.id.left:
                mStack.swipeTopViewToLeft();
                break;
            case R.id.add:
                mDatas.add("NEW");
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.right:
                mStack.swipeTopViewToRight();
                break;
        }
    }

    private FlySpaceSwipeAdapter mAdapter;
    private List<String> mDatas;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fly_space);
        ButterKnife.bind(this);
        initTestDatas();
        initData();
    }

    /**
     * 申请录音权限
     */
    @SuppressLint("CheckResult")
    private void requestPermission() {
        new RxPermissions(this)
                .request(Manifest.permission.RECORD_AUDIO)
                .subscribe(this::accept);
    }

    /**
     * 初始化SwipeStackView
     */
    private void initData() {
        mDatas = getIntent().getStringArrayListExtra("user_list");
        mAdapter = new FlySpaceSwipeAdapter(this, mDatas);
        mStack.setAdapter(mAdapter);
        mStack.setListener(new SwipeStack.SwipeStackListener() {    //卡片滑动监听
            @Override
            public void onViewSwipedToLeft(int position) {
                String leftToast = mDatas.get(position);
                Toasty.normal(FlySpaceActivity.this, "left = " + leftToast).show();
            }

            @Override
            public void onViewSwipedToRight(int position) {
                String rightToast = mDatas.get(position);
                Toasty.normal(FlySpaceActivity.this, "right = " + rightToast).show();
            }

            @Override
            public void onStackEmpty() {
                Toasty.normal(FlySpaceActivity.this, "没有数据").show();
            }
        });
    }

    /**
     * 初始化测试数据
     */
    private void initTestDatas() {
        mDatas = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            mDatas.add("Test" + ++i);
        }
    }

    /**
     * 同意授权后调用的方法
     *
     * @param granted boolean
     */
    private void accept(Boolean granted) {
        if (granted) {
            Intent intent = new Intent(this, EditSpaceInfoActivity.class);
            startActivity(intent);
        } else Toasty.normal(this, "授权失败").show();
    }
}
