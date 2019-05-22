package com.gameex.dw.justtalk.payPackage;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.managePack.BaseActivity;


public class ChangeActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "ChangeActivity";
    /**
     * 返回
     */
    private ImageView mBack;
    /**
     * 更多
     */
    private ImageView mMore;
    /**
     * 我的零钱
     */
    private TextView mMyChange;
    /**
     * 充值
     */
    private TextView mReCharge;
    /**
     * 提现
     */
    private TextView mCashWithdrawal;
    /**
     * 我的银行卡
     */
    private RelativeLayout mMyBankCard;
    /**
     * 我的红包记录
     */
    private RelativeLayout mMyRedRecord;

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
        setContentView(R.layout.activity_change);

        mBack = findViewById(R.id.back);
        mMore = findViewById(R.id.more);
        mMyChange = findViewById(R.id.my_change);
        mReCharge = findViewById(R.id.recharge);
        mCashWithdrawal = findViewById(R.id.cash_withdrawal);
        mMyBankCard = findViewById(R.id.my_bank_card_layout);
        mMyRedRecord = findViewById(R.id.my_red_record_layout);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mBack.setOnClickListener(this);
        mMore.setOnClickListener(this);
        mReCharge.setOnClickListener(this);
        mCashWithdrawal.setOnClickListener(this);
        mMyBankCard.setOnClickListener(this);
        mMyRedRecord.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.more:
                Toast.makeText(this, "更多功能", Toast.LENGTH_SHORT).show();
                break;
            case R.id.recharge:
                Toast.makeText(this, "充值", Toast.LENGTH_SHORT).show();
                break;
            case R.id.cash_withdrawal:
                Toast.makeText(this, "提现", Toast.LENGTH_SHORT).show();
                break;
            case R.id.my_bank_card_layout:
                Toast.makeText(this, "我的银行卡", Toast.LENGTH_SHORT).show();
                break;
            case R.id.my_red_record_layout:
                Toast.makeText(this, "我的红包记录", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
