package com.gameex.dw.justtalk.imgBrowse;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.managePack.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class PhotoBrowseActivity extends BaseActivity {
    public static PhotoBrowseActivity sPhotoBrowseActivity;
    /**
     * 要浏览的图片集合
     */
    private List<String> photoBrowseList;
    /**
     *
     */
    private int current;
    /**
     * 横滑布局
     */
    private ViewPager mViewPager;
    /**
     * 横滑布局适配器
     */
    private PhotoBrowsePagerAdapter mPhotoBrowsePagerAdapter;

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
        photoBrowseList = new ArrayList<>();
        if (getIntent() != null) {
            Intent intent = getIntent();
            photoBrowseList = intent.getStringArrayListExtra("photo_browse");
            current = intent.getIntExtra("current_browse", 0);
        }
        mPhotoBrowsePagerAdapter = new PhotoBrowsePagerAdapter(photoBrowseList, getApplicationContext());
        mViewPager.setAdapter(mPhotoBrowsePagerAdapter);
        mViewPager.setCurrentItem(current);
    }

    /*
    public static PhotoBrowseActivity getPhotoBrowseActivity() {
        return sPhotoBrowseActivity;
    }
    */
}
