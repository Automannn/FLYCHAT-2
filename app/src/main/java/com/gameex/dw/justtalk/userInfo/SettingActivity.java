package com.gameex.dw.justtalk.userInfo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.login.LoginActivity;
import com.gameex.dw.justtalk.managePack.ActivityCollector;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.titleBar.OnViewClick;
import com.gameex.dw.justtalk.titleBar.TitleBarView;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 设置界面activity
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener {
    @SuppressLint("StaticFieldLeak")
    public static SettingActivity sSettingActivity;

    private Integer mCurrentFontState;
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;

    private TitleBarView mBarView;
    private TextView mTest;
    private IndicatorSeekBar mSeekBar;

    /**
     * 切换账号
     */
    @OnClick(R.id.other_login)
    void changeLogin() {
        ActivityCollector.finishAll();  //销毁所有活动
        Intent intentLoginOut = new Intent(this, LoginActivity.class);
        intentLoginOut.putExtra("flag", "LoginOut");
        startActivity(intentLoginOut);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        sSettingActivity = this;

        initView();
    }

    /**
     * 绑定id，设置监听
     */
    private void initView() {
        mTest = findViewById(R.id.test_text);
        pref = PreferenceManager.getDefaultSharedPreferences(sSettingActivity);
        mCurrentFontState = pref.getInt("ui_state", 25);
        mSeekBar = findViewById(R.id.seek_bar_font_size);
        mSeekBar.setProgress(mCurrentFontState);
        mSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                switch (seekParams.progress) {
                    case 0:
                        mTest.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                        break;
                    case 25:
                        mTest.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        break;
                    case 50:
                        mTest.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                        break;
                    case 75:
                        mTest.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                        break;
                    case 100:
                        mTest.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
            }
        });
        mBarView = findViewById(R.id.title_bar_setting);
        mBarView.setRightIVVisible(View.INVISIBLE);
        mBarView.setSearchIVVisible(View.GONE);
        mBarView.setTitle("设置");
        mBarView.setOnViewClick(new OnViewClick() {
            @Override
            public void leftClick() {
                //若现在的fontState和app启动时不同，则显示弹窗
                if (mSeekBar.getProgress() != mCurrentFontState) {
                    showDialog();
                } else {
                    sSettingActivity.finish();
                }
            }

            @Override
            public void searchClick() {

            }

            @Override
            public void rightClick() {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                break;
        }
    }

    /**
     * 修改后是否保存的弹窗
     */
    private void showDialog() {
        final AlertDialog.Builder alertDog = new AlertDialog.Builder(sSettingActivity);
        //弹窗显示的内容
        alertDog.setMessage("字体大小要重启后才可生效，确定重启吗？");
        alertDog.setPositiveButton("确定", (dialogInterface, i) -> {
            //重启app
            BaseActivity.setState(mSeekBar.getProgress());
            ActivityCollector.finishAll();
            new Handler().postDelayed(() -> {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getApplication().getPackageName());
                assert launchIntent != null;
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(launchIntent);
            }, 50);
        });
        alertDog.setNegativeButton("取消", (dialogInterface, i) -> {
            //将字体大小设置回启动时的大小
            switch (mCurrentFontState) {
                case 0:
                    mTest.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    break;
                case 25:
                    mTest.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    break;
                case 50:
                    mTest.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    break;
                case 75:
                    mTest.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                    break;
                case 100:
                    mTest.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
                    break;
                default:
                    break;
            }
        });
        alertDog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //若现在的fontState和app启动时不同，则显示弹窗
            if (mSeekBar.getProgress() != mCurrentFontState) {
                showDialog();
            } else {
                sSettingActivity.finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
