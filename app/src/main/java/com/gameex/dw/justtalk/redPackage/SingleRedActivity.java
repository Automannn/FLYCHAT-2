package com.gameex.dw.justtalk.redPackage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.automannn.commonUtils.security.Base64;
import com.automannn.commonUtils.security.MD5;
import com.automannn.commonUtils.security.RSA;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.GroupMemberInfo;
import okhttp3.Call;
import okhttp3.Response;

import static com.gameex.dw.justtalk.redPackage.SetYuanActivity.GET_TOKEN;
import static com.gameex.dw.justtalk.redPackage.SetYuanActivity.HAND_OUT_RED;

public class SingleRedActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "SingleRedActivity";
    /**
     * 返回箭头
     */
    private ImageView mBack;
    /**
     * 发送金额
     */
    private EditText mYuanNum;
    /**
     * 祝福语
     */
    private EditText mRedMessage;
    /**
     * 随机获取一个祝福语
     */
    private ImageView mRandomRedMsg;
    /**
     * 展示输入金额
     */
    private TextView mYuan;
    /**
     * 发送
     */
    private Button mSend;
    /**
     * 本地服务器用户id
     */
    private String userId;
    /**
     * pubKey
     */
    private String mPubKey;

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
        setContentView(R.layout.activity_single_red);
        mBack = findViewById(R.id.back);
        mYuanNum = findViewById(R.id.yuan_num);
        mRedMessage = findViewById(R.id.red_package_message);
        mRandomRedMsg = findViewById(R.id.update_message);
        mYuan = findViewById(R.id.money);
        mSend = findViewById(R.id.send_red_package);
    }

    /**
     * 初始化数据
     */
    @SuppressLint("SetTextI18n")
    private void initData() {

        mBack.setOnClickListener(this);

        mYuanNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
                mYuan.setText("￥" + mYuanNum.getText());
                if (!mYuanNum.getText().toString().equals("填写金额")) {
                    mSend.setEnabled(true);
                    mSend.setAlpha(1);
                }
            }
        });

        mRandomRedMsg.setOnClickListener(this);

        mSend.setOnClickListener(this);

        userId = getUserId();
        setPubKey(userId);
    }

    /**
     * 发送红包
     */
    private void handOutRed() {
        String secretString = getSecretString(mPubKey);
        final String token = MD5.generate(secretString, false);
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("userId", userId);
        paramsMap.put("token", token);
        paramsMap.put("secretString", secretString);

        OkHttpUtil.okHttpPost(HAND_OUT_RED, paramsMap, null
                , new CallBackUtil.CallBackDefault() {
                    @Override
                    public void onFailure(Call call, Exception e) {
                        LogUtil.d(TAG, "handOutRed-onFailure: ");
                        e.printStackTrace();
                        Toast.makeText(SingleRedActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Response response) {
                        if (response != null && response.isSuccessful()) {
                            try {
                                JSONObject object = null;
                                if (response.body() != null) {
                                    object = new JSONObject(response.body().string());
                                }
                                if (object != null) {
                                    String data = object.getString("data");
                                    boolean success = object.getBoolean("success");
                                    LogUtil.d(TAG, "handOutRed-onResponse: " +
                                            "data = " + data + " ;success = " + success);
                                    if (success) {
                                        Intent intent = new Intent();
                                        intent.putExtra("yuan", mYuan.getText().toString());
                                        intent.putExtra("token", token);
                                        intent.putExtra("blessings",mRedMessage.getText().toString());
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
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
     * 获取用户公匙
     *
     * @param userId 自家服务器的用户id
     */
    private void setPubKey(String userId) {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("userId", userId);
        OkHttpUtil.okHttpPost(GET_TOKEN, paramsMap, new CallBackUtil.CallBackDefault() {
            @Override
            public void onFailure(Call call, Exception e) {
                LogUtil.d(TAG, "setPubKey-onFailure: ");
                e.printStackTrace();
                Toast.makeText(SingleRedActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Response response) {
                if (response != null && response.isSuccessful()) {
                    try {
                        JSONObject object = null;
                        if (response.body() != null) {
                            object = new JSONObject(response.body().string());
                        }
                        if (object != null) {
                            String data = object.getString("data");
                            boolean success = object.getBoolean("success");
                            LogUtil.d(TAG, "setPubKey-onResponse: " +
                                    "data = " + data + " ;success = " + success);
                            if (success) {
                                mPubKey = data;
                            } else {
                                LogUtil.d(TAG, "setPubKey-onResponse-success=false: ");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    LogUtil.d(TAG, "setPubKey-response=false: ");
                }
            }
        });
    }

    /**
     * 获取secretString
     *
     * @param pubKey 用户公匙
     * @return string
     */
    private String getSecretString(String pubKey) {
        JSONObject secretJson = new JSONObject();
        try {
            secretJson.put("amount", Double.valueOf(mYuanNum.getText().toString()));
            secretJson.put("count", 1);
            secretJson.put("personCount", 1);
            secretJson.put("expireTime", 120);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String secretStr = secretJson.toString() + new Date().getTime();
        byte[] secretBytes = RSA.encryptByPublicKey4Android(secretStr.getBytes(), pubKey);
        return Base64.encode(secretBytes);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.update_message:
                Toast.makeText(this, "随机选取一句祝福语", Toast.LENGTH_SHORT).show();
                break;
            case R.id.send_red_package:
                handOutRed();
                break;
        }
    }
}
