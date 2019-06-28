package com.gameex.dw.justtalk.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.adapter.PhotoBrowsePagerAdapter;
import com.gameex.dw.justtalk.manage.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.viewpager.widget.ViewPager;

/**
 * 聊天界面图片查看activity
 */
public class PhotoBrowseActivity extends BaseActivity {
    @SuppressLint("StaticFieldLeak")
    public static PhotoBrowseActivity sPhotoBrowseActivity;
    /**
     *
     */
    private int current;
    /**
     * 横滑布局
     */
    private ViewPager mViewPager;

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
        setContentView(R.layout.activity_photo_browse);

        mViewPager = findViewById(R.id.browse_pager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    /**
     * 初始化数据
     */
    private void initData() {
        sPhotoBrowseActivity = this;

        //要浏览的图片集合
        List<String> photoBrowseList = new ArrayList<>();
        if (getIntent() != null) {
            Intent intent = getIntent();
            photoBrowseList = intent.getStringArrayListExtra("photo_browse");
            current = intent.getIntExtra("current_browse", 0);
        }

        //横滑布局适配器
        PhotoBrowsePagerAdapter photoBrowsePagerAdapter = new PhotoBrowsePagerAdapter(photoBrowseList, getApplicationContext());
        mViewPager.setAdapter(photoBrowsePagerAdapter);
        mViewPager.setCurrentItem(current);
    }
}
