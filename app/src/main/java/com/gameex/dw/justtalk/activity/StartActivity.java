package com.gameex.dw.justtalk.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;

import com.gameex.dw.justtalk.util.LogUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;

import androidx.appcompat.app.AppCompatActivity;

/**
 * app启动时运行
 */
public class StartActivity extends AppCompatActivity {
    private static final String TAG = "StartActivity";

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);    //隐藏状态栏
        requestPermission();
    }

    /**
     * 获取存储权限
     */
    @SuppressLint("CheckResult")
    private void requestPermission() {

        new RxPermissions(this)
                .requestEachCombined(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(permission -> {
                    LogUtil.d(TAG, "requestPermission: " + "permission = " + permission.name);
                    //1s后前往主页
                    mHandler.postDelayed(() -> gotoLogin(), 1000);
                    if (permission.shouldShowRequestPermissionRationale) {
                        Toast.makeText(StartActivity.this
                                , "您禁止了此权限，可能会影响那您的正常使用", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 启动结束，跳转到登陆界面
     */
    private void gotoLogin() {
        Intent intent = new Intent(StartActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 重写onKeyDown监听返回按钮
     *
     * @param keyCode keyCode
     * @param event   keyEvent
     * @return boolean
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }
}
