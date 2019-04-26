package com.gameex.dw.justtalk.titleBar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

/**
 * 自定义TitleBar
 */
public class TitleBarView extends RelativeLayout implements View.OnClickListener, MaterialSearchView.SearchViewListener, MaterialSearchView.OnQueryTextListener {
    private LinearLayout leftLayout, searchLayout, rightLayout;
    private TextView titleTV;
    private Toolbar mToolbar;
    private MaterialSearchView mSearchView;
    private ImageView rightIV;
    private OnViewClick mClick;
    private OnSearchListen mSearchListen;
    private OnSearchQueryListen mQueryListen;


    public TitleBarView(Context context) {
        this(context, null);
    }

    public TitleBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.title_bar_layout, this);
        initView();
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.titleBarView, defStyleAttr, 0);
        int count = array.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = array.getIndex(i);
            switch (attr) {
                case R.styleable.titleBarView_centerTextColor:
                    titleTV.setTextColor(array.getColor(attr, Color.WHITE));
                    break;
                case R.styleable.titleBarView_centerText:
                    titleTV.setText(array.getString(attr));
                    break;
                default:
                    break;
            }
        }
        array.recycle();
    }

    private void initView() {
        leftLayout = findViewById(R.id.layout_left);
        leftLayout.setOnClickListener(this);
        titleTV = findViewById(R.id.tv_title);
        rightIV = findViewById(R.id.iv_right);
        searchLayout = findViewById(R.id.layout_search);
        searchLayout.setOnClickListener(this);
        mToolbar = findViewById(R.id.toolbar);
        mSearchView = findViewById(R.id.search_view);
        mSearchView.setVoiceSearch(false);
        mSearchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));
        mSearchView.setOnSearchViewListener(this);
        mSearchView.setOnQueryTextListener(this);
        rightLayout = findViewById(R.id.layout_right);
        rightLayout.setOnClickListener(this);
    }

    /**
     * 接口回调监听点击
     *
     * @param click
     */
    public void setOnViewClick(OnViewClick click) {
        this.mClick = click;
    }

    /**
     * 接口回调监听searchView的展示状态
     *
     * @param listen
     */
    public void setOnSearchListen(OnSearchListen listen) {
        this.mSearchListen = listen;
    }

    /**
     * 接口回调searchView搜索状态
     *
     * @param queryListen
     */
    public void setQueryListen(OnSearchQueryListen queryListen) {
        mQueryListen = queryListen;
    }

    /**
     * 设置标题
     *
     * @param title
     */
    public void setTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            titleTV.setText(title);
        }
    }

    /**
     * 设置标题大小
     *
     * @param size
     */
    public void setTitleSize(int size) {
        if (titleTV != null) {
            titleTV.setTextSize(TypedValue.COMPLEX_UNIT_SP,size);
        }
    }

    /**
     * 设置左图标可见否
     *
     * @param visible
     */
    public void setLeftIVVisible(int visible) {
        leftLayout.setVisibility(visible);
    }

    /**
     * 设置搜索circular可见否
     *
     * @param visible
     */
    public void setSearchIVVisible(int visible) {
        searchLayout.setVisibility(visible);
    }

    /**
     * 将自定义toolBar设置成app标题栏
     *
     * @param activity
     */
    public void setToolbarBeActionBar(AppCompatActivity activity) {
        activity.setSupportActionBar(mToolbar);
    }

    /**
     * 设置工具栏是否可见
     *
     * @param visible
     */
    public void setToolbarVisible(int visible) {
        mToolbar.setVisibility(visible);
    }

    /**
     * 判断searchView是否展示
     *
     * @return
     */
    public boolean isSearchViewOpening() {
        if (mSearchView.isSearchOpen()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 设置searView是否展示
     *
     * @param isShow
     */
    public void setSearchViewShow(boolean isShow) {
        if (isShow) {
            mSearchView.showSearch();
        } else {
            mSearchView.closeSearch();
        }
    }

    /**
     * 设置右图标可见否
     *
     * @param visible
     */
    public void setRightIVVisible(int visible) {
        rightIV.setVisibility(visible);
    }

    /**
     * 设置右图资源
     *
     * @param resourceId
     */
    public void setRightIVImg(int resourceId) {
        rightIV.setImageResource(resourceId);
    }

    /**
     * 点击监听事件
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_left:
                mClick.leftClick();
                break;
            case R.id.layout_search:
                mClick.searchClick();
                break;
            case R.id.layout_right:
                mClick.rightClick();
                break;
            default:
                break;
        }
    }

    /**
     * searchView展示监听事件
     */
    @Override
    public void onSearchViewShown() {
        mSearchListen.onSearchShown();
    }

    /**
     * searchView关闭监听事件
     */
    @Override
    public void onSearchViewClosed() {
        mSearchListen.onSearchClosed();
    }

    /**
     * 搜索文本提交监听
     *
     * @param query
     * @return
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return mQueryListen.onQuerySubmit(query);
    }

    /**
     * 搜索文本改变监听
     *
     * @param newText
     * @return
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        return mQueryListen.onQueryChange(newText);
    }
}
