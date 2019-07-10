package com.gameex.dw.justtalk.activity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.jpush.im.android.api.JMessageClient;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Response;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.util.BaseDialog;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;
import com.gameex.dw.justtalk.util.PayUtil;
import com.gameex.dw.justtalk.util.SharedPreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class SMSPhoneActivity extends BaseActivity {
    private static final String TAG = "SMSPhoneActivity";
    /**
     * 用户绑卡路径
     */
    private static final String ACCOUNT_CARD_BIND = "account/cardbind";
    /**
     * 银行卡类型
     */
    @BindView(R.id.bank_type)
    TextView mType;
    /**
     * 银行预留手机号
     */
    @BindView(R.id.phone)
    EditText mPhone;

    @OnTextChanged(R.id.phone)
    void afterTextChanged(Editable editable) {
        if (!TextUtils.isEmpty(editable)) {
            mNext.setEnabled(true);
            mNext.setAlpha(1);
        } else {
            mNext.setEnabled(false);
            mNext.setAlpha(0.5f);
        }
    }

    @BindView(R.id.next_step)
    Button mNext;

    @OnClick({R.id.back, R.id.next_step})
    void doClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.next_step:
                if (getIntent().getBooleanExtra("goSkyPay", false)) {
                    paramsMap.put("mobile", JMessageClient.getMyInfo().getUserName());
                    paramsMap.put("trueMobile", mPhone.getText().toString());
                    PayUtil.toSkyPayWithoutBank(this, paramsMap.toString());
                } else {
                    new Handler().post(() -> bindCard(paramsMap));
                }
                break;
        }
    }

    /**
     * 天赋包支付（没有绑定银行卡）参数
     */
    private HashMap<String, Object> ationMap;
    /**
     * 绑定银行卡参数map
     */
    private HashMap<String, String> paramsMap;

    private BaseDialog mCirclePros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsphone);
        ButterKnife.bind(this);
        mCirclePros = new BaseDialog(this, R.style.CusVersionDialog, R.layout.dialog_progress);
        initData();
    }

    /**
     * 初始化数据
     */
    @SuppressLint("SetTextI18n")
    private void initData() {
        paramsMap = new HashMap<>();
        Intent intent = getIntent();
        String bankName = intent.getStringExtra("bank");
        String bankType = intent.getStringExtra("cardType");
        paramsMap.put("userId", (String) SharedPreferenceUtil.getData("userId", ""));
        paramsMap.put("IDcard", intent.getStringExtra("IDcard"));    //身份证
        paramsMap.put("bankCard", intent.getStringExtra("bankCard"));    //银行卡卡号
        paramsMap.put("userName", intent.getStringExtra("userName"));  //真实姓名
        if (intent.getBooleanExtra("goSkyPay", false)) {
            ationMap = new HashMap<>();
            ationMap.putAll(paramsMap);
            //产品id
            ationMap.put("productCode", intent.getStringExtra("productCode"));
            //产品数量
            ationMap.put("number", intent.getIntExtra("number", -1));
        } else paramsMap.put("bankName", bankName);   //银行名称
        mType.setText(bankName + "  " + bankType);
    }

    /**
     * 开始绑卡
     *
     * @param paramsMap 表单参数
     */
    private void bindCard(HashMap<String, String> paramsMap) {
        OkHttpUtil.okHttpPost(ACCOUNT_CARD_BIND, paramsMap
                , new CallBackUtil.CallBackDefault() {
                    @Override
                    public void onFailure(Call call, Exception e) {
                        mCirclePros.dismiss();
                        e.printStackTrace();
                        Toast.makeText(SMSPhoneActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Response response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                JSONObject json = new JSONObject(response.body().string());
                                boolean success = json.getBoolean("success");
                                String data = json.getString("data");
                                if (success) {
                                    setResult(RESULT_OK);
                                    finish();
                                }
                                mCirclePros.dismiss();
                                Toasty.info(SMSPhoneActivity.this, data).show();
                            } catch (JSONException e) {
                                mCirclePros.dismiss();
                                e.printStackTrace();
                            } catch (IOException e) {
                                mCirclePros.dismiss();
                                e.printStackTrace();
                            }
                        } else {
                            mCirclePros.dismiss();
                            LogUtil.d(TAG, "bindCard-onResponse: " + "response = " + response);
                        }
                    }
                });
    }
}
