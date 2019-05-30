package com.gameex.dw.justtalk.payPackage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.alipay.sdk.app.EnvUtils;
import com.alipay.sdk.app.PayTask;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

import static com.gameex.dw.justtalk.JGApplication.WE_CHAT_APP_ID;


public class PayOrderActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "PayOrderActivity";
    /**
     * 支付路径
     */
    private static final String PRODUCT_BUY = "product/buy";
    private static final int SDK_PAY_FLAG = 301;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            Resul result = new Result((String) msg.obj);
            Toast.makeText(PayOrderActivity.this, "支付成功",
                    Toast.LENGTH_LONG).show();
        }
    };
    private IWXAPI msgApi;
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
        //支付宝沙盒模式
        EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX);
        msgApi = WXAPIFactory.createWXAPI(this, null);
        //app注册到微信
        msgApi.registerApp(WE_CHAT_APP_ID);
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
        mPayType = "UNIONPAY";
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
     * 跳转支付
     */
    private void goPay() {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("userId", getUserId());
        paramsMap.put("payType", mPayType);
        paramsMap.put("productCode", "fly_coin_" + String.valueOf(mYuan));
        paramsMap.put("number", 1 + "");
        paramsMap.put("channel", "APP");
        OkHttpUtil.okHttpPost(PRODUCT_BUY, paramsMap, new CallBackUtil.CallBackDefault() {
            @Override
            public void onFailure(Call call, Exception e) {
                e.printStackTrace();
                Toast.makeText(PayOrderActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
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
                            //data 订单信息
                            Runnable payRunnable = new Runnable() {

                                @Override
                                public void run() {
                                    PayTask alipay = new PayTask(PayOrderActivity.this);
                                    Map<String, String> result = alipay.payV2(data, true);
                                    Message msg = new Message();
                                    msg.what = SDK_PAY_FLAG;
                                    msg.obj = result;
                                    mHandler.sendMessage(msg);
                                }
                            };
                            // 必须异步调用
                            Thread payThread = new Thread(payRunnable);
                            payThread.start();
                        } else {
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
                goPay();
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.union_pay:
                mPayType = "UNIONPAY";
                break;
            case R.id.alipay:
                mPayType = "ALIPAY";
                break;
            case R.id.wepay:
                mPayType = "WEPAY";
                break;
        }
    }

    /**
     * 接收微信支付返回结果
     *
     * @param resp baseResp
     */
    public void onResp(BaseResp resp) {
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            LogUtil.d(TAG, "onPayFinish,errCode=" + resp.errCode);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
        }
    }
}
