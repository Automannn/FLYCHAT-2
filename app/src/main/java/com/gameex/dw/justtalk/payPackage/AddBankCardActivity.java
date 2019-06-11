package com.gameex.dw.justtalk.payPackage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import okhttp3.Call;
import okhttp3.Response;

public class AddBankCardActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AddBankCardActivity";
    /**
     * 用户绑卡路径
     */
    private static final String ACCOUNT_CARD_BIND = "account/cardbind";
    /**
     * 返回
     */
    private ImageView mBack;
    /**
     * 持卡人姓名
     */
    private EditText mName;
    /**
     * 持卡人身份证号
     */
    private EditText mIdNumber;
    /**
     * 卡号
     */
    private EditText mCardNumber;
    /**
     * 所属银行
     */
    private Spinner mBelong;
    /**
     * 下一步
     */
    private Button mNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initListener();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        setContentView(R.layout.activity_add_bank_card);
        mBack = findViewById(R.id.back);
        mName = findViewById(R.id.name);
        mIdNumber = findViewById(R.id.id_number);
        mCardNumber = findViewById(R.id.card_number);
        mNext = findViewById(R.id.next_step);
        mBelong = findViewById(R.id.belong);
    }

    /**
     * 添加监听器
     */
    private void initListener() {
        mBack.setOnClickListener(this);
        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mIdNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mCardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable)) {
                    mNext.setEnabled(false);
                    mNext.setAlpha(0.5f);
                } else {
                    mNext.setEnabled(true);
                    mNext.setAlpha(1f);
                }
            }
        });
        mNext.setOnClickListener(this);
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
     * 开始绑卡
     */
    private void goBindCard() {
        UserInfo userInfo = JMessageClient.getMyInfo();
        if (userInfo == null) {
            Toast.makeText(this, "点击重试", Toast.LENGTH_SHORT).show();
            return;
        }
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("userId", getUserId());
        paramsMap.put("IDcard", mIdNumber.getText().toString());
        paramsMap.put("bankCard", mCardNumber.getText().toString());
        paramsMap.put("userName", mName.getText().toString());
        paramsMap.put("bankName", mBelong.getSelectedItem().toString());
        bindCard(paramsMap);
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
                        e.printStackTrace();
                        Toast.makeText(AddBankCardActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Response response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                JSONObject json = new JSONObject(response.body().string());
                                boolean success = json.getBoolean("success");
                                JSONObject data = json.getJSONObject("data");
                                if (success) {
                                    String bankCardNumber = data.getString("bankCardNumber");
                                    String id = data.getString("id");
                                    LogUtil.d(TAG, "bindCard-onResponse: "
                                            + "bankCardNumber = " + bankCardNumber + " ;id = " + id);
                                    finish();
                                } else {
                                    int code = data.getInt("code");
                                    String message = data.getString("message");
                                    LogUtil.d(TAG, "bindCard-onResponse: "
                                            + "code = " + code + " ;message = " + message);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            LogUtil.d(TAG, "bindCard-onResponse: " + "response = " + response);
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
            case R.id.next_step:
                intent.setClass(this, VerifyIdentityActivity.class);
                startActivity(intent);
//                goBindCard();
                break;
        }
    }
}
