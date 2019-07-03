package com.gameex.dw.justtalk.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;
import com.gameex.dw.justtalk.util.PayResult;
import com.gameex.dw.justtalk.util.PayUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Response;

/**
 * 支付订单界面
 */
public class PayOrderActivity extends BaseActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "PayOrderActivity";
    /**
     * 支付路径
     */
    private static final String PRODUCT_BUY = "product/buy";
    /**
     * 银联支付
     */
    private static final String UNION_PAY = "UNIONPAY";
    /**
     * 支付宝支付
     */
    private static final String ALI_PAY = "ALIPAY";
    /**
     * 微信支付
     */
    private static final String WE_PAY = "WEPAY";
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            PayResult payResult = new PayResult((Map<String, String>) msg.obj);
            if ("9000".equals(payResult.getResultStatus())) {
                Intent intent = new Intent();
                intent.putExtra("pay_success", true);
                setResult(RESULT_OK, intent);
                finish();
                Toasty.success(PayOrderActivity.this, "支付成功").show();
            } else {
                Toasty.normal(PayOrderActivity.this, payResult.getMemo()).show();
            }
        }
    };
    /**
     * 金额
     */
    private Integer mYuan;
    /**
     * 承接选择的支付方式
     */
    private String mPayType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        PayTask payTask = new PayTask(this);
//        String version = payTask.getVersion();
        initView();
        initData();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        setContentView(R.layout.activity_pay_order);

        // 返回
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(this);

        //单选按钮集
        RadioGroup group = findViewById(R.id.pay_group);
        group.setOnCheckedChangeListener(this);

        //确认支付
        Button pay = findViewById(R.id.pay);
        pay.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        Intent intent = getIntent();
        mYuan = intent.getIntExtra("money_amount", -1);
        mPayType = ALI_PAY;
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
     * 去支付
     *
     * @param data 支付参数
     */
    private void goPay(String data) {
        switch (mPayType) {
            case UNION_PAY:
                PayUtil.toUnionPay(this, data);
                break;
            case ALI_PAY:
                PayUtil.toAlipay(PayOrderActivity.this, mHandler, data);
                break;
            case WE_PAY:
                PayUtil.toWXPay(this, data);
                break;
        }
    }

    /**
     * 准备支付
     */
    private void getReadyToPay() {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("userId", getUserId());
        paramsMap.put("payType", mPayType);
        paramsMap.put("productCode", "fly_coin_" + mYuan);
        paramsMap.put("number", 1 + "");
        paramsMap.put("channel", "APP");
        OkHttpUtil.okHttpPost(PRODUCT_BUY, paramsMap, new CallBackUtil.CallBackDefault() {
            @Override
            public void onFailure(Call call, Exception e) {
                e.printStackTrace();
                Toasty.error(PayOrderActivity.this, "网络异常", Toasty.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject object = new JSONObject(response.body().string());
                        boolean success = object.getBoolean("success");
                        if (success) {
                            final String data = object.getString("data");
                            LogUtil.d(TAG, "goPay-onResponse: " + "data = " + data);
                            goPay(data);
                        } else {
                            LogUtil.d(TAG, "goPay-onResponse: " + object.toString());
                            JSONObject data = object.getJSONObject("data");
                            int code = data.getInt("code");
                            String message = data.getString("message");
                            LogUtil.d(TAG, "goPay-onResponse: " +
                                    "code = " + code + " ;message = " + message);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.pay:
                new Handler().post(this::getReadyToPay);
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.union_pay:
                mPayType = UNION_PAY;
                break;
            case R.id.alipay:
                mPayType = ALI_PAY;
                break;
            case R.id.wepay:
                mPayType = WE_PAY;
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null) {
            return;
        }
        String msg = "";
        /*
         * 支付控件返回字符串:success、fail、cancel 分别代表支付成功，支付失败，支付取消
         */
        String str = data.getExtras().getString("pay_result");
        if (str.equalsIgnoreCase("success")) {
            // 支付成功后，extra中如果存在result_data，取出校验
            // result_data结构见c）result_data参数说明
            if (data.hasExtra("result_data")) {
                String result = data.getExtras().getString("result_data");
                //                try {
                //                    JSONObject resultJson = new JSONObject(result);
                //                    String sign = resultJson.getString("sign");
                //                    String dataOrg = resultJson.getString("data");
                //                    // 验签证书同后台验签证书
                //                    // 此处的verify，商户需送去商户后台做验签
                //                    boolean ret = verify(dataOrg, sign, mMode);
                //                    if (ret) {
                //                        // 验证通过后，显示支付结果
                //                        msg = "支付成功！";
                //                    } else {
                //                        // 验证不通过后的处理
                //                        // 建议通过商户后台查询支付结果
                //               msg = "支付失败！";
                //                    }
                //                } catch (JSONException e) {
                //                }
                //            } else {
                // 未收到签名信息
                // 建议通过商户后台查询支付结果
                //               msg = "支付成功！";
                //            }
                msg = "支付成功！";
                Intent intent = new Intent();
                intent.putExtra("pay_success", true);
                setResult(RESULT_OK, intent);
                finish();
            } else if (str.equalsIgnoreCase("fail")) {
                msg = "支付失败！";
            } else if (str.equalsIgnoreCase("cancel")) {
                msg = "用户取消了支付";
            }

            Toasty.info(this, msg, Toasty.LENGTH_SHORT).show();
//            System.out.println("支付结果通知" + msg);

//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle("支付结果通知");
//            builder.setMessage(msg);
//            builder.setInverseBackgroundForced(true);
//            // builder.setCustomTitle();
//            builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            });
//            builder.create().show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
