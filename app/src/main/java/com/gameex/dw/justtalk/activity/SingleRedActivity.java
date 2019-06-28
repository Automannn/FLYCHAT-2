package com.gameex.dw.justtalk.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.text.Editable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.automannn.commonUtils.security.Base64;
import com.automannn.commonUtils.security.MD5;
import com.automannn.commonUtils.security.RSA;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;
import com.gameex.dw.justtalk.util.WindowUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Response;

import static com.gameex.dw.justtalk.activity.SetYuanActivity.GET_TOKEN;
import static com.gameex.dw.justtalk.activity.SetYuanActivity.HAND_OUT_RED;


/**
 * 单聊红包设置金额界面
 */
public class SingleRedActivity extends BaseActivity implements View.OnClickListener {
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
     * 监听输入
     *
     * @param editable editable
     */
    @SuppressLint("SetTextI18n")
    @OnTextChanged(R.id.yuan_num)
    void afterTextChanged(Editable editable) {
        mYuan.setText("￥" + editable);
        if (!editable.toString().equals("填写金额")) {
            mSend.setEnabled(true);
            mSend.setAlpha(1);
        }
    }

    /**
     * 祝福语
     */
    private EditText mRedMessage;

    /**
     * 监听软键盘右下角，按下时承接光标
     *
     * @param view view
     * @return boolean
     */
    @OnEditorAction(R.id.yuan_num)
    boolean OnEditorAction(TextView view) {
        mRedMessage.requestFocus();
        return true;
    }

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
        ButterKnife.bind(this);
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

        mRandomRedMsg.setOnClickListener(this);

        mSend.setOnClickListener(this);

        userId = getUserId();
        new Handler().post(() -> setPubKey(userId));
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
                        Toasty.error(SingleRedActivity.this, "网络异常").show();
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
                                    boolean success = object.getBoolean("success");
                                    if (success) {
                                        Intent intent = new Intent();
                                        intent.putExtra("yuan", mYuan.getText().toString());
                                        intent.putExtra("token", token);
                                        intent.putExtra("blessings", mRedMessage.getText().toString());
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    } else {
                                        JSONObject data = object.getJSONObject("data");
                                        int code = data.getInt("code");
                                        String message = data.getString("message");
                                        LogUtil.d(TAG, "handOutRed-onResponse: " +
                                                "code = " + code + " ;data = " + data);
                                        Toasty.info(SingleRedActivity.this
                                                , message + "").show();
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
        OkHttpUtil.okHttpPost(GET_TOKEN, paramsMap, new CallBackUtil.CallBackString() {

            @Override
            public void onFailure(Call call, Exception e) {
                LogUtil.d(TAG, "setPubKey-onFailure: ");
                e.printStackTrace();
                Toasty.error(SingleRedActivity.this, "网络异常").show();
            }

            @Override
            public void onResponse(String response) {
                if (response != null) {
                    try {
                        JSONObject object = new JSONObject(response);
                        String data = object.getString("data");
                        boolean success = object.getBoolean("success");
                        LogUtil.d(TAG, "setPubKey-onResponse: " +
                                "data = " + data + " ;success = " + success);
                        if (success) {
                            mPubKey = data;
                        } else {
                            LogUtil.d(TAG, "setPubKey-onResponse-success=false: ");
                        }
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
                Toasty.info(this, "随机选取一句祝福语").show();
                break;
            case R.id.send_red_package:
                new Handler().post(this::handOutRed);
                break;
        }
    }

    /**
     * 点击软键盘外面的区域关闭软键盘
     *
     * @param ev motionEvent
     * @return boolean
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // 获得当前得到焦点的View，一般情况下就是EditText（特殊情况就是轨迹求或者实体案件会移动焦点）
            View v = getCurrentFocus();
            if (WindowUtil.isShouldHideInput(v, ev)) {
                WindowUtil.hideInput(this);
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
