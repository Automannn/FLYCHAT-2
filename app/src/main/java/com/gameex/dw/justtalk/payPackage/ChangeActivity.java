package com.gameex.dw.justtalk.payPackage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Response;


public class ChangeActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "ChangeActivity";
    /**
     * 查询用户余额路径
     */
    public static final String ACCOUNT_BALANCE = "account/balance";
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
        initBalance();
    }

    /**
     * 初始化我的零钱
     */
    private void initBalance() {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("userId", getUserId());
        OkHttpUtil.okHttpPost(ACCOUNT_BALANCE, paramsMap, new CallBackUtil.CallBackDefault() {
            @Override
            public void onFailure(Call call, Exception e) {
                e.printStackTrace();
                Toast.makeText(ChangeActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject object = new JSONObject(response.body().string());
                        boolean success = object.getBoolean("success");
                        if (success) {
                            String data = object.getString("data");
                            mMyChange.setText("￥" + data);
                        } else {
                            JSONObject data = object.getJSONObject("data");
                            int code = data.getInt("code");
                            String message = data.getString("message");
                            LogUtil.d(TAG, "initBalance-onResponse: " +
                                    "code" + code + " ;message = " + message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 获取自己服务器上的用户id
     *
     * @return string
     */
    private String getUserId() {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        return pref.getString("userId", null);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.more:
                Toast.makeText(this, "更多功能", Toast.LENGTH_SHORT).show();
                break;
            case R.id.recharge:
                intent.setClass(this, RechargeActivity.class);
                startActivity(intent);
                break;
            case R.id.cash_withdrawal:
                Toast.makeText(this, "提现", Toast.LENGTH_SHORT).show();
                break;
            case R.id.my_bank_card_layout:
                intent.setClass(this, BankCardActivity.class);
                startActivity(intent);
                break;
            case R.id.my_red_record_layout:
                Toast.makeText(this, "我的红包记录", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
