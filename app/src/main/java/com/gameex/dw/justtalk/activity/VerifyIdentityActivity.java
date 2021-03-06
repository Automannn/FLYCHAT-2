package com.gameex.dw.justtalk.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.payPasswordView.PayPasswordView;

import es.dmoral.toasty.Toasty;

/**
 * 自定义密码输入界面
 */
public class VerifyIdentityActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "VerifyIdentityActivity";
    /**
     * 返回
     */
    private ImageView mBack;
    /**
     * 输入支付密码的View，包含自定义数字键盘
     */
    private PayPasswordView mPayPwdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initListener();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        setContentView(R.layout.activity_verify_identity);
        mBack = findViewById(R.id.back);
        mPayPwdView = findViewById(R.id.pay_pwd_view);
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        mBack.setOnClickListener(this);
        mPayPwdView.setOnFinishInput(() -> Toasty.info(VerifyIdentityActivity.this
                , mPayPwdView.getStrPassword() + "", Toasty.LENGTH_SHORT).show());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
        }
    }
}
