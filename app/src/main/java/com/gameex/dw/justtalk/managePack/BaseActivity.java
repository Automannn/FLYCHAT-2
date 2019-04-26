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
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.login.LoginActivity;
import com.gameex.dw.justtalk.userInfo.SettingActivity;
import com.gameex.dw.justtalk.userInfo.UserInfoActivity;

/**
 * 与ActivityCollector管理其他activity
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final String LOGIN_OUT = "com.gameex.dw.justtalk.LOGIN_OUT";

    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    private static int mState = -1;

    public static int getState() {
        return mState;
    }

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
     * @param resid
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

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        setFontSize(mState);
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
     * 若sate=0，则app还未设置过字体大小，使用缓存中的字体大小；反之使用设置的字体大小
     *
     * @param state
     */
    private void setFontSize(int state) {
        if (state == -1) {
            state = mPref.getInt("font_state", 25);
            setFontSizePref(state);
        } else {
            setFontSizePref(state);
//            ActivityCollector.recreateAll();
        }
    }

    /**
     * 设置app字体大小，并更新sharedPreference中存贮的字体大小
     *
     * @param fontState
     */
    private void setFontSizePref(int fontState) {
        if (0 == fontState) {
            setTheme(R.style.AppTheme_SmallSuperFont);
        } else if (25 == fontState) {
            setTheme(R.style.AppTheme_SmallFont);
        } else if (50 == fontState) {
            setTheme(R.style.AppTheme_NormalFont);
        } else if (75 == fontState) {
            setTheme(R.style.AppTheme_LargeFont);
        } else {
            setTheme(R.style.AppTheme_LargeSuperFont);
        }
        mEditor = mPref.edit();
        mEditor.putInt("font_state", fontState);
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
