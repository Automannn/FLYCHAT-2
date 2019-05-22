package com.gameex.dw.justtalk.redPackage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.automannn.commonUtils.security.Base64;
import com.automannn.commonUtils.security.MD5;
import com.automannn.commonUtils.security.RSA;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.GroupMemberInfo;
import okhttp3.Call;
import okhttp3.Response;

import java.util.Date;


public class SetYuanActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "SetYuanActivity";
    private static final String HAND_OUT_RED = "account/sendpacket";
    private static final String GET_TOKEN = "account/getpubkey";
    /**
     * 返回箭头
     */
    private ImageView mBack;
    /**
     * 选择谁可以领取红包
     */
    private RelativeLayout mRedGotLayout;
    /**
     * 群成员总数
     */
    private TextView mGroupMemNum;
    /**
     * 谁可以领红包
     */
    private TextView mRedGot;
    /**
     * 发送金额
     */
    private EditText mYuanNum;
    /**
     * 现在的红包种类
     */
    private TextView mNowPackage;
    /**
     * 目标红包种类
     */
    private TextView mChangetoPackage;
    /**
     * 填写红包个数
     */
    private EditText mPackageNum;
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
     * 该群信息体
     */
    private GroupInfo mGroupInfo;

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
        setContentView(R.layout.activity_set_yuan);
        mBack = findViewById(R.id.back);
        mRedGotLayout = findViewById(R.id.who_get_layout);
        mGroupMemNum = findViewById(R.id.group_member_count);
        mRedGot = findViewById(R.id.who_get);
        mYuanNum = findViewById(R.id.yuan_num);
        mNowPackage = findViewById(R.id.now_package_kind);
        mChangetoPackage = findViewById(R.id.change_to_kind);
        mPackageNum = findViewById(R.id.edit_red_package_num);
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
        mGroupInfo = GroupInfo.fromJson(getIntent().getStringExtra("group_info"));
        List<GroupMemberInfo> memberInfos = mGroupInfo.getGroupMemberInfos();

        mBack.setOnClickListener(this);
        mRedGotLayout.setOnClickListener(this);
        if (memberInfos != null && memberInfos.size() > 0) {
            mGroupMemNum.setText("本群共" + memberInfos.size() + "人");
        } else {
            mGroupMemNum.setText("本群共0人");
        }

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

        mChangetoPackage.setOnClickListener(this);

        mRandomRedMsg.setOnClickListener(this);

        mSend.setOnClickListener(this);

        userId = getUserId();
        setPubKey(userId);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.who_get_layout:
                Toast.makeText(this, "选择谁可以领取红包", Toast.LENGTH_SHORT).show();
                break;
            case R.id.change_to_kind:
                if (mChangetoPackage.getText().equals("改为普通红包")) {
                    mNowPackage.setText("当前为普通红包，");
                    mChangetoPackage.setText("改为拼手气红包");
                } else {
                    mNowPackage.setText("当前为拼手气红包，");
                    mChangetoPackage.setText("改为普通红包");
                }
                break;
            case R.id.update_message:
                Toast.makeText(this, "随机选取一句祝福语", Toast.LENGTH_SHORT).show();
                break;
            case R.id.send_red_package:
                handOutRed();
                break;
        }
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
                        Toast.makeText(SetYuanActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SetYuanActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
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
            secretJson.put("count", Integer.parseInt(mPackageNum.getText().toString()));
            secretJson.put("personCount", Integer.parseInt(mPackageNum.getText().toString()));
            secretJson.put("expireTime", 120);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String secretStr = secretJson.toString() + new Date().getTime();
        byte[] secretBytes = RSA.encryptByPublicKey4Android(secretStr.getBytes(), pubKey);
        return Base64.encode(secretBytes);
    }

}
