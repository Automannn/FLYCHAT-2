package com.gameex.dw.justtalk.payOrder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;


public class PayOrderActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "PayOrderActivity";
    /**
     * 返回
     */
    private ImageView mBack;
    /**
     * 确认支付
     */
    private Button mPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        setContentView(R.layout.activity_pay_order);
        mBack=findViewById(R.id.back);
        mBack.setOnClickListener(this);
        mPay=findViewById(R.id.pay);
        mPay.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back:
                finish();
                break;
            case R.id.pay:
                Toast.makeText(this, "支付逻辑", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
