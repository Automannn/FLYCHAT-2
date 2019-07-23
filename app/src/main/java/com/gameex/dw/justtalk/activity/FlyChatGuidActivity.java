package com.gameex.dw.justtalk.activity;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.tools.Glide4Engine;
import com.gameex.dw.justtalk.tools.GlideImageLoader;
import com.youth.banner.Banner;

import java.util.ArrayList;
import java.util.List;

/**
 * created by Dennil Wei 2019-7-17
 * app导航界面，只在安装后第一次点开app时启动
 */
public class FlyChatGuidActivity extends AppCompatActivity {
    /**
     * 图片轮播
     */
    @BindView(R.id.banner)
    Banner mBanner;

    /**
     * 立即登录、快速注册的点击监听事件
     *
     * @param view
     */
    @OnClick({R.id.login, R.id.sign_up})
    void doClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.login:    //立即登录
                intent.setClass(this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.sign_up:  //快速注册
                intent.setClass(this, SignUpActivity.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * 图片资源集合
     */
    private List<Integer> mImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fly_chat_guid);
        ButterKnife.bind(this);
        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mImages.add(R.mipmap.fly_chat_guid2);
        mImages.add(R.mipmap.fly_chat_guid3);
        mImages.add(R.mipmap.fly_chat_guid4);
        mBanner.setImageLoader(new GlideImageLoader()); //设置图片加载器
        mBanner.setImages(mImages); //设置图片资源集合
        mBanner.start();
    }
}
