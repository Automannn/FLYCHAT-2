package com.gameex.dw.justtalk.userInfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gameex.dw.justtalk.BottomBarActivity;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.managePack.ActivityCollector;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.restartAppService.RestartAppTool;
import com.gameex.dw.justtalk.titleBar.OnViewClick;
import com.gameex.dw.justtalk.titleBar.TitleBarView;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

/**
 * 设置界面activity
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener {
    public static SettingActivity sSettingActivity;

    private Integer mCurrentFontState;
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;

    private TitleBarView mBarView;
    private TextView mTest;
    private IndicatorSeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        sSettingActivity = this;

        initView();
    }

    /**
     * 绑定id，设置监听
     */
    private void initView() {
        mTest = findViewById(R.id.test_text);
        pref = PreferenceManager.getDefaultSharedPreferences(sSettingActivity);
        mCurrentFontState = pref.getInt("font_state", 25);
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
        alertDog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //重启app
                BaseActivity.setState(mSeekBar.getProgress());
                ActivityCollector.finishAll();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getApplication().getPackageName());
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(launchIntent);
                    }
                }, 50);
            }
        });
        alertDog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
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
