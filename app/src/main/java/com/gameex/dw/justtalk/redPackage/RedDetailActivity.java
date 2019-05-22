package com.gameex.dw.justtalk.redPackage;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.managePack.BaseActivity;

public class RedDetailActivity extends BaseActivity implements View.OnClickListener {
    /**
     * 返回
     */
    private ImageView mBack;
    /**
     * 金额
     */
    private TextView mAmount;

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
        setContentView(R.layout.activity_red_detail);

        mBack = findViewById(R.id.back);
        mAmount = findViewById(R.id.amount);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mBack.setOnClickListener(this);
        mAmount.setText(getIntent().getStringExtra("amount"));
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
