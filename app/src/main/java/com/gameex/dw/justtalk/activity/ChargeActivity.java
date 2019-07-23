package com.gameex.dw.justtalk.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.entry.BankInfo;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;
import com.gameex.dw.justtalk.util.SharedPreferenceUtil;
import com.gameex.dw.justtalk.util.WindowUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.Nullable;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Response;

import static com.gameex.dw.justtalk.activity.BankCardActivity.ACCOUNT_CARD_BIND_QUERY;

/**
 * 零钱界面
 */
public class ChargeActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "ChargeActivity";
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
    private TextView mMyCharge;
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
     * 绑定支付宝
     */
    private RelativeLayout mBindAli;
    /**
     * 父布局
     */
    private LinearLayout mLayout;
    /**
     * 没绑定银行卡时的popupWindow
     */
    private PopupWindow mNoBankCardPup;
    private List<BankInfo> mBankInfos = new ArrayList<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            LogUtil.i("mylog", "请求结果-->" + val);
        }
    };

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

        mLayout = findViewById(R.id.container);
        mBack = findViewById(R.id.back);
        mMore = findViewById(R.id.more);
        mMyCharge = findViewById(R.id.my_change);
        mReCharge = findViewById(R.id.recharge);
        mCashWithdrawal = findViewById(R.id.cash_withdrawal);
        mMyBankCard = findViewById(R.id.my_bank_card_layout);
        mMyRedRecord = findViewById(R.id.my_red_record_layout);
        mBindAli = findViewById(R.id.bind_alipay_layout);
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
        mBindAli.setOnClickListener(this);
        initBalance();
        initNoBankCardPup();
    }

    /**
     * 初始化我的零钱
     */
    private void initBalance() {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("userId", (String) SharedPreferenceUtil.getData("userId", ""));
        OkHttpUtil.okHttpPost(ACCOUNT_BALANCE, paramsMap, new CallBackUtil.CallBackDefault() {
            @Override
            public void onFailure(Call call, Exception e) {
                e.printStackTrace();
                Toasty.error(ChargeActivity.this, "网络异常", Toasty.LENGTH_SHORT).show();
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
                            mMyCharge.setText("￥" + data);
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
     * 绑定支付包
     * 此方法暂时无法使用，说是要等app正式上线了才可以用。不过我也不知道为什么要有这个方法
     */
    private void bindAli() {
        //支付宝沙盒模式
//        EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX);
        OkHttpUtil.okHttpGet(ALIPAY_GETAUTH, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                e.printStackTrace();
                Toasty.error(ChargeActivity.this, "网络错误", Toasty.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                LogUtil.d(TAG, "bindAli-onResponse: " + response);
                WindowUtil.openBrowser(ChargeActivity.this, response);
//                Message msg = new Message();
//                Bundle data = new Bundle();
//                data.putString("value","请求结果");
//                msg.setData(data);
//                handler.sendMessage(msg);
            }
        });
    }

    /**
     * 没有绑定银行卡时弹出
     */
    private void initNoBankCardPup() {
        @SuppressLint("InflateParams") View view = this.getLayoutInflater().inflate(R.layout.popup_no_bank_card, null);
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
                .setWindowBackgroundAlpha(this, 1f));
        mNoBankCardPup.setAnimationStyle(R.style.scale_alpha_style);
        mNoBankCardPup.update();
    }

    /**
     * 在银行卡数据集不为空的情况下，若其长度大于0，则跳选择支付方式；否则弹出提示框
     *
     * @param data 银行卡数据集
     */
    private void goChoosePayWay(List<BankInfo> data) {
        Intent intent = new Intent(this, WithdrawActivity.class);
        intent.putExtra("userId", (String) SharedPreferenceUtil.getData("userId", ""));
        intent.putExtra("balance", mMyCharge.getText());
        intent.putExtra("bankInfo", (Serializable) data);
        startActivityForResult(intent, RECHARGE_REQUEST_CODE);
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
                        Toasty.error(ChargeActivity.this, "网络异常", Toasty.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Response response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                JSONObject object = null;
                                boolean success = false;
                                try {
                                    object = new JSONObject(response.body().string());
                                    success = object.getBoolean("success");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if (success) {
                                    JSONArray data = null;
                                    try {
                                        data = object.getJSONArray("data");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    if (data == null || data.length() == 0) {
                                        if (mNoBankCardPup != null) {
                                            mNoBankCardPup.showAtLocation(mLayout, Gravity.CENTER, 0, 0);
                                            WindowUtil.showBackgroundAnimator(ChargeActivity.this, 0.5f);
                                        }
                                    } else {
                                        for (int i = 0; i < data.length(); i++) {
                                            JSONObject bank = null;
                                            try {
                                                bank = data.getJSONObject(i);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            BankInfo bankInfo = new BankInfo();
                                            assert bank != null;
                                            try {
                                                String bankNumber = bank.getString("bankCardNumber");
                                                if (!TextUtils.isEmpty(bankNumber))
                                                    bankInfo.setBankEndNum(bankNumber.substring(bankNumber.length() - 4));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            try {
                                                String name = bank.getString("bankName");
                                                if (!TextUtils.isEmpty(name))
                                                    bankInfo.setBankName(name);
                                            } catch (JSONException e) {
                                                bankInfo.setBankName("常用银行卡");
                                                e.printStackTrace();
                                            }
                                            try {
                                                String bankId = bank.getString("id");
                                                if (!TextUtils.isEmpty(bankId))
                                                    bankInfo.setBankId(bankId);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            mBankInfos.add(bankInfo);
                                        }
                                        goChoosePayWay(mBankInfos);
                                    }
                                } else {
                                    try {
                                        assert object != null;
                                        JSONObject data = object.getJSONObject("data");
                                        int code = data.getInt("code");
                                        String message = data.getString("message");
                                        LogUtil.d(TAG, "queryBindCard-onResponse: " +
                                                "code = " + code + " ;message = " + message);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toasty.error(ChargeActivity.this, "服务器异常，请稍后再试").show();
                            LogUtil.d(TAG, "queryBindCard-onResponse: " + "response = " + response);
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.back: //返回
                finish();
                break;
            case R.id.more: //更多功能
                Toasty.normal(this, "敬请期待").show();
                break;
            case R.id.recharge: //充值
                intent.setClass(this, RechargeActivity.class);
                startActivityForResult(intent, RECHARGE_REQUEST_CODE);
                break;
            case R.id.cash_withdrawal:  //提现
                queryBindCard((String) SharedPreferenceUtil.getData("userId", ""));
//                if (mNoBankCardPup != null) {
//                    mNoBankCardPup.showAtLocation(mLayout, Gravity.CENTER, 0, 0);
//                    WindowUtil.showBackgroundAnimator(this, 0.5f);
//                }
                break;
            case R.id.my_bank_card_layout:  //我的银行卡
                intent.setClass(this, BankCardActivity.class);
                startActivity(intent);
                break;
            case R.id.my_red_record_layout: //我的红包记录
                Toasty.normal(this, "我的红包记录").show();
                break;
            case R.id.bind_alipay_layout:   //绑定支付宝
                new Thread(this::bindAli).start();
                break;
            case R.id.cancel:   //取消
                if (mNoBankCardPup.isShowing()) {
                    mNoBankCardPup.dismiss();
                }
                break;
            case R.id.confirm:  //确认
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
                //订单完成，重新查询零钱并刷新界面
                initBalance();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
