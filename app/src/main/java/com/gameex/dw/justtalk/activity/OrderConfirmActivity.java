package com.gameex.dw.justtalk.activity;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.jpush.im.android.api.JMessageClient;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class OrderConfirmActivity extends BaseActivity {
    private static final String TAG = "OrderConfirmActivity";
    /**
     * 天付宝支付确认接口
     */
    private static final String CONFIRM_ATIONPAY_PATH = "tianfubao/confirmationPay";
    /**
     * 支付订单号，支付金额
     */
    @BindViews({R.id.order_number, R.id.money})
    List<TextView> mTextViews;
    /**
     * 验证码输入框
     */
    @BindView(R.id.verify_code)
    EditText mVerifyCode;

    /**
     * 验证码输入监听
     *
     * @param editable 输入的文本
     */
    @OnTextChanged(R.id.verify_code)
    void afterTextChanged(Editable editable) {
        if (!TextUtils.isEmpty(editable)) {
            mConfirm.setEnabled(true);
            mConfirm.setAlpha(1);
        } else {
            mConfirm.setEnabled(false);
            mConfirm.setAlpha(0.5f);
        }
    }

    /**
     * 确认支付
     */
    @BindView(R.id.confirm)
    Button mConfirm;

    @OnClick(R.id.confirm)
    void onClick() {
        new Handler().post(() -> {
            params.put("smsCode", mVerifyCode.getText().toString());
            confirmAtionPay(params.toString());
        });
    }

    private HashMap<String, Object> params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);
        ButterKnife.bind(this);
        try {
            initData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化数据
     *
     * @throws JSONException 抛出json异常
     */
    private void initData() throws JSONException {
        JSONObject data = new JSONObject(getIntent().getStringExtra("data"));
        String spbillNo = data.getString("spbillNo");
        String signSn = data.getString("signSn");
        double money = data.getDouble("money");
        params = new HashMap<>();
        params.put("signSn", signSn); //签约序列号(银行卡id)
        params.put("spbillNo", spbillNo); //支付订单号
        params.put("money", money); //交易金额
        params.put("mobile", JMessageClient.getMyInfo().getUserName());   //用户手机号
        params.put("busiType", 2);    //签约序列号类型
    }

    /**
     * 天付宝支付
     *
     * @param params 参数Map字符串
     */
    private void confirmAtionPay(String params) {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("map", params);
        OkHttpUtil.okHttpPost(CONFIRM_ATIONPAY_PATH, paramsMap, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                e.printStackTrace();
                Toasty.error(OrderConfirmActivity.this, "网络链接异常").show();
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    boolean success = object.getBoolean("success");
                    if (success) {
                        setResult(RESULT_OK);
                        finish();
                    }
                    String data = object.getString("data");
                    Toasty.normal(OrderConfirmActivity.this, data).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
