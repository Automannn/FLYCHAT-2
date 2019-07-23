package com.gameex.dw.justtalk.activity;

import android.os.Bundle;

import com.gameex.dw.justtalk.manage.BaseActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.widget.Toolbar;
import cn.jpush.im.android.api.JMessageClient;

import android.view.View;

import com.gameex.dw.justtalk.R;

/**
 * 群组基本信息界面，展示给群外用户的界面（有待完善）
 */
public class GroupBasicInfoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_basic_info);


    }

}
