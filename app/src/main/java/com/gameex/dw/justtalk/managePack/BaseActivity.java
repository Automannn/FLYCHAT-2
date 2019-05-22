package com.gameex.dw.justtalk.managePack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.app.AppCompatActivity;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.login.LoginActivity;
import com.gameex.dw.justtalk.userInfo.UserInfoActivity;
import com.gameex.dw.justtalk.util.BarUtil;

/**
 * 与ActivityCollector管理其他activity
 */
public abstract class BaseActivity extends AppCompatActivity {
    public static BaseActivity sBaseActivity;
    public static final String LOGIN_OUT = "com.gameex.dw.justtalk.LOGIN_OUT";

    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    private static int mState = -1;

    public static void setState(int state) {
        mState = state;
    }

    private BaseBroadcastReceiver mBaseReceiver;

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
//        Configuration config = new Configuration();
//        config.setToDefaults();
        Configuration config = res.getConfiguration();
        if (config.fontScale != 1.0f) {
            config.fontScale = 1.0f;    //设置正常字体大小的倍数
        }
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    /**
     * 此方法会在onCreate方法之前被系统执行
     *
     * @param resid 主题资源id
     */
    @Override
    public void setTheme(@StyleRes int resid) {
        super.setTheme(resid);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
//        getSupportActionBar().hide();   //隐藏标题栏
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);    //隐藏状态栏
        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        BarUtil.setRootViewFitsSystemWindows(this, true);
        //设置状态栏透明
        BarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
//        if (!BarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
//            BarUtil.setStatusBarColor(this,0x55000000);
//        }

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        setUISize(mState);
        sBaseActivity = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册广播接收器
        mBaseReceiver = new BaseBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(LOGIN_OUT);
        registerReceiver(mBaseReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        unregisterReceiver(mBaseReceiver);
    }

    /**
     * 若sate=-1，则app还未设置过ui样式，使用缓存中的ui样式；反之使用设置的ui样式
     *
     * @param state ui样式代号
     */
    private void setUISize(int state) {
        if (state == -1) {
            state = mPref.getInt("ui_state", 25);
            setUiSizePref(state);
        } else {
            setUiSizePref(state);
        }
    }

    /**
     * 设置app UI样式，并更新sharedPreference中存贮的ui样式
     *
     * @param fontState ui样式代号
     */
    private void setUiSizePref(int fontState) {
        if (0 == fontState) {
            setTheme(R.style.AppTheme_SmallSuperUIStyle);
        } else if (25 == fontState) {
            setTheme(R.style.AppTheme_SmallUIStyle);
        } else if (50 == fontState) {
            setTheme(R.style.AppTheme_NormalUIStyle);
        } else if (75 == fontState) {
            setTheme(R.style.AppTheme_LargeUIStyle);
        } else {
            setTheme(R.style.AppTheme_LargeSuperUIStyle);
        }
        mEditor = mPref.edit();
        mEditor.putInt("ui_state", fontState);
        mEditor.apply();
    }

    public class BaseBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case LOGIN_OUT:
                    if (context == UserInfoActivity.sUserInfoActivity) {
                        ActivityCollector.finishAll();  //销毁所有活动
                        Intent intentLoginOut = new Intent(context, LoginActivity.class);
                        intentLoginOut.putExtra("flag", "LoginOut");
                        startActivity(intentLoginOut);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
