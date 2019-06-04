package com.gameex.dw.justtalk.payPackage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.EnvUtils;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;
import com.gameex.dw.justtalk.util.WindowUtil;
import com.yzq.zxinglibrary.common.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import androidx.annotation.Nullable;
import okhttp3.Call;
import okhttp3.Response;

import static com.gameex.dw.justtalk.payPackage.BankCardActivity.ACCOUNT_CARD_BIND_QUERY;


public class ChangeActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "ChangeActivity";
    /**
     * 查询用户余额路径
     */
    public static final String ACCOUNT_BALANCE = "account/balance";
    /**
     * 绑定支付宝
     */
    public static final String ALIPAY_GETAUTH = "alipay/getauth";
    /**
     * 充值跳转code
     */
    private static final int RECHARGE_REQUEST_CODE = 101;
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
    /**
     * 父布局
     */
    private LinearLayout mLayout;
    /**
     * 没绑定银行卡时的popupWindow
     */
    private PopupWindow mNoBankCardPup;

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

        mLayout=findViewById(R.id.container);
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
//        initBalance();
        initNoBankCardPup();
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

    /**
     * 绑定支付包
     */
    private void bindAli() {
        //支付宝沙盒模式
        EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX);
        OkHttpUtil.okHttpGet(ALIPAY_GETAUTH, new CallBackUtil.CallBackDefault() {
            @Override
            public void onFailure(Call call, Exception e) {
                e.printStackTrace();
                Toast.makeText(ChangeActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String path = response.body().string();
                        LogUtil.d(TAG, "bindAli-onResponse: " + path);
                        WindowUtil.openBrowser(ChangeActivity.this, path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 没有绑定银行卡时弹出
     */
    private void initNoBankCardPup() {
        @SuppressLint("InflateParams") View view = this.getLayoutInflater().inflate(R.layout.no_bank_card_pup, null);
        TextView cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
        TextView confirm = view.findViewById(R.id.confirm);
        confirm.setOnClickListener(this);
        mNoBankCardPup = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mNoBankCardPup.setFocusable(true);
        mNoBankCardPup.setOutsideTouchable(false);
        mNoBankCardPup.setTouchInterceptor((view1, motionEvent) -> {
//                if (motionEvent.getAction() == MotionEvent.ACTION_OUTSIDE) {
//                    mNoBankCardPup.dismiss();
//                    return true;
//                }
            return false;
        });
        mNoBankCardPup.setOnDismissListener(() -> WindowUtil
                .setWindowBackgroundAlpha(ChangeActivity.this, 1f));
        mNoBankCardPup.setAnimationStyle(R.style.scale_alpha_style);
        mNoBankCardPup.update();
    }

    /**
     * 在银行卡数据集不为空的情况下，若其长度大于0，则跳选择支付方式；否则弹出提示框
     *
     * @param data 银行卡数据集
     */
    private void goChoosePayWay(JSONArray data) {
        if (data == null) {
            if (mNoBankCardPup != null) {
                mNoBankCardPup.showAtLocation(mLayout, Gravity.CENTER, 0, 0);
                WindowUtil.showBackgroundAnimator(this, 0.5f);
            }
            return;
        }
    }

    /**
     * 查询已绑定的银行卡数据,成功后刷新列表
     *
     * @param userId 本地服务器的用户id
     */
    private void queryBindCard(String userId) {
        if (userId == null) {
            return;
        }
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("userId", userId);
        OkHttpUtil.okHttpPost(ACCOUNT_CARD_BIND_QUERY, paramsMap
                , new CallBackUtil.CallBackDefault() {
                    @Override
                    public void onFailure(Call call, Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ChangeActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Response response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                JSONObject object = new JSONObject(response.body().string());
                                boolean success = object.getBoolean("success");
                                if (success) {
                                    JSONArray data = object.getJSONArray("data");
                                    goChoosePayWay(data);
                                } else {
                                    JSONObject data = object.getJSONObject("data");
                                    int code = data.getInt("code");
                                    String message = data.getString("message");
                                    LogUtil.d(TAG, "queryBindCard-onResponse: " +
                                            "code = " + code + " ;message = " + message);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            LogUtil.d(TAG, "queryBindCard-onResponse: " + "response = " + response);
                        }
                    }
                });
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
                startActivityForResult(intent, RECHARGE_REQUEST_CODE);
                break;
            case R.id.cash_withdrawal:
                if (mNoBankCardPup != null) {
                    mNoBankCardPup.showAtLocation(mLayout, Gravity.CENTER, 0, 0);
                    WindowUtil.showBackgroundAnimator(this, 0.5f);
                }
                Toast.makeText(this, "提现", Toast.LENGTH_SHORT).show();
                break;
            case R.id.my_bank_card_layout:
                intent.setClass(this, BankCardActivity.class);
                startActivity(intent);
                break;
            case R.id.my_red_record_layout:
                bindAli();
                Toast.makeText(this, "我的红包记录", Toast.LENGTH_SHORT).show();
                break;
            case R.id.cancel:
                if (mNoBankCardPup.isShowing()) {
                    mNoBankCardPup.dismiss();
                }
                break;
            case R.id.confirm:
                if (mNoBankCardPup.isShowing()) {
                    mNoBankCardPup.dismiss();
                }
                intent.setClass(this, AddBankCardActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RECHARGE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            if (data.getBooleanExtra("order_success", false)) {
                initBalance();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
