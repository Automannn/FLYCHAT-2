package com.gameex.dw.justtalk;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gameex.dw.justtalk.jiguangIM.GlobalEventListener;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.titleBar.OnSearchListen;
import com.gameex.dw.justtalk.titleBar.OnSearchQueryListen;
import com.gameex.dw.justtalk.titleBar.OnViewClick;
import com.gameex.dw.justtalk.titleBar.TitleBarView;
import com.gameex.dw.justtalk.util.DataUtil;
import com.yzq.zxinglibrary.common.Constant;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.Conversation;

/**
 * 主界面activity
 */
public class BottomBarActivity extends BaseActivity implements OnViewClick, View.OnClickListener {
    @SuppressLint("StaticFieldLeak")
    public static BottomBarActivity sBottomBarActivity;

    private TitleBarView mTitleBarView;
    private LinearLayout mTitleRightLL;
    private ViewPager viewPager;
    private BottomNavigationView navigation;
    private MenuItem menuItem;

    private PopupWindow mTitlePup;

    private long exitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_bar);
        sBottomBarActivity = this;
        initView();
        getTitlePW();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 使用menu文件加载布局，类似底部导航栏
     *
     * @param menu UI表单
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //使用menu文件加载标题栏布局，类似底部导航栏
        /*getMenuInflater().inflate(R.menu.material_search, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        mSearchView.setMenuItem(item);
        return true;*/
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 绑定id，设置监听，完善相关属性
     */
    private void initView() {
        mTitleBarView = findViewById(R.id.title_bar);
        mTitleBarView.setLeftIVVisible(View.GONE);
        mTitleBarView.setOnViewClick(this);
        mTitleBarView.setToolbarBeActionBar(this);
        mTitleBarView.setOnSearchListen(new OnSearchListen() {
            @Override
            public void onSearchShown() {

            }

            @Override
            public void onSearchClosed() {
                mTitleBarView.setRightIVVisible(View.VISIBLE);
                mTitleBarView.setToolbarVisible(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
                navigation.setVisibility(View.VISIBLE);
                mTitleBarView.setLayoutParams(new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        DataUtil.dpToPx(sBottomBarActivity, 60)));
            }
        });
        mTitleBarView.setQueryListen(new OnSearchQueryListen() {
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
        mTitleRightLL = findViewById(R.id.layout_right);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        viewPager = findViewById(R.id.view_page);
        disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        viewPager.addOnPageChangeListener(mPageChangeListener);
        navigation.setSelectedItemId(R.id.navigation_home);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(2);
    }

    /**
     * @param navigationView 底部导航栏
     */
    @SuppressLint("RestrictedApi")
    public void disableShiftMode(BottomNavigationView navigationView) {

        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigationView.getChildAt(0);
        try {
//            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
//            shiftingMode.setAccessible(true);
//            shiftingMode.setBoolean(menuView, false);
//            shiftingMode.setAccessible(false);

            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(i);
//                itemView.setShiftingMode(false);
                itemView.setChecked(itemView.getItemData().isChecked());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    /**
     * 标题栏弹出框
     */
    private void getTitlePW() {
        @SuppressLint("InflateParams") View view = this.getLayoutInflater().inflate(R.layout.title_pup_layout, null);
        RelativeLayout talkGround, addFriend, SQ, helpBack;
        talkGround = view.findViewById(R.id.talk_ground_layout);
        talkGround.setOnClickListener(this);
        addFriend = view.findViewById(R.id.add_friends_layout);
        addFriend.setOnClickListener(this);
        SQ = view.findViewById(R.id.SQ_layout);
        SQ.setOnClickListener(this);
        helpBack = view.findViewById(R.id.help_back_up_layout);
        helpBack.setOnClickListener(this);
        mTitlePup = new PopupWindow(view, RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        mTitlePup.setFocusable(true);
        mTitlePup.setOutsideTouchable(true);
        mTitlePup.setAnimationStyle(R.style.pop_anim);
        mTitlePup.update();
    }

    /**
     * viewPage适配器
     */
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private Fragment[] mFragments = getFragment();

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    /**
     * 底部导航栏改变监听事件
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int itemId = item.getItemId();
            switch (itemId) {
                case R.id.navigation_home:
                    viewPager.setCurrentItem(0);
                    mTitleBarView.setTitle("飞聊");
                    mTitleBarView.setRightIVVisible(View.VISIBLE);
                    mTitleBarView.setSearchIVVisible(View.VISIBLE);
                    break;
                case R.id.navigation_dashboard:
                    viewPager.setCurrentItem(1);
                    mTitleBarView.setTitle("联系人");
                    mTitleBarView.setRightIVVisible(View.VISIBLE);
                    mTitleBarView.setSearchIVVisible(View.GONE);
                    break;
                case R.id.navigation_notifications:
                    viewPager.setCurrentItem(2);
                    mTitleBarView.setTitle("我");
                    mTitleBarView.setRightIVVisible(View.GONE);
                    mTitleBarView.setSearchIVVisible(View.GONE);
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    /**
     * viewPage滑动监听器
     */
    private ViewPager.OnPageChangeListener mPageChangeListener
            = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            menuItem = navigation.getMenu().getItem(position);
            menuItem.setChecked(true);
            switch (position) {
                case 0:
                    mTitleBarView.setTitle("飞聊");
                    mTitleBarView.setRightIVVisible(View.VISIBLE);
                    mTitleBarView.setSearchIVVisible(View.VISIBLE);
                    break;
                case 1:
                    mTitleBarView.setTitle("联系人");
                    mTitleBarView.setRightIVVisible(View.VISIBLE);
                    mTitleBarView.setSearchIVVisible(View.GONE);
                    break;
                case 2:
                    mTitleBarView.setTitle("我");
                    mTitleBarView.setRightIVVisible(View.GONE);
                    mTitleBarView.setSearchIVVisible(View.GONE);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    /**
     * @return fragment[]
     */
    private Fragment[] getFragment() {
        Fragment[] fats = new Fragment[3];
        for (int i = 0; i < 3; i++) {
            BottomBarFat fragment = new BottomBarFat();
            Bundle bundle = new Bundle();
            bundle.putString("flag", String.valueOf(i));
            fragment.setArguments(bundle);
            fats[i] = fragment;
        }
        return fats;
    }

    /**
     * 自定义状态栏左侧的监听事件
     */
    @Override
    public void leftClick() {

    }

    /**
     * 搜索Circular监听事件
     */
    @Override
    public void searchClick() {
        mTitleBarView.setRightIVVisible(View.GONE);
        mTitleBarView.setToolbarVisible(View.VISIBLE);
        viewPager.setVisibility(View.GONE);
        navigation.setVisibility(View.GONE);
        mTitleBarView.setSearchViewShow(true);
        mTitleBarView.setLayoutParams(new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
    }

    /**
     * 自定义状态栏右侧的监听事件
     */
    @Override
    public void rightClick() {
        mTitlePup.showAsDropDown(mTitleRightLL, 0, 0);
    }

    /**
     * 点击监听事件
     *
     * @param view 被点击的组件
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.talk_ground_layout:
                Toast.makeText(sBottomBarActivity, "发起群聊", Toast.LENGTH_SHORT).show();
                break;
            case R.id.add_friends_layout:
                Toast.makeText(sBottomBarActivity, "添加朋友", Toast.LENGTH_SHORT).show();
                break;
            case R.id.SQ_layout:
                Toast.makeText(sBottomBarActivity, "扫一扫", Toast.LENGTH_SHORT).show();
                break;
            case R.id.help_back_up_layout:
                Toast.makeText(sBottomBarActivity, "帮助和反馈", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mTitleBarView.isSearchViewOpening()) {
                mTitleBarView.setSearchViewShow(false);
            } else {
                if (System.currentTimeMillis() - exitTime > 2000) {
                    Toast.makeText(sBottomBarActivity, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    exitTime = System.currentTimeMillis();
                } else {
                    BottomBarActivity.this.finish();
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 处理activity返回的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //搜索栏若开启声音搜索模式，此处处理其返回的数据
        /*if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    mSearchView.setQuery(searchWrd, false);
                }
            }
            return;
       }*/

        //接收扫描结果
        if (requestCode == BottomBarFat.REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                Toast.makeText(sBottomBarActivity, content + "", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
