package com.gameex.dw.justtalk.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.automannn.commonUtils.security.Base64;
import com.automannn.commonUtils.security.MD5;
import com.automannn.commonUtils.security.RSA;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.adapter.RandomDialogAdapter;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;
import com.gameex.dw.justtalk.util.WindowUtil;
import com.rey.material.app.BottomSheetDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.GroupMemberInfo;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Response;

import java.util.Date;

/**
 * 群聊红包设置金额界面
 */
public class SetYuanActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "SetYuanActivity";
    /**
     * 发送红包
     */
    public static final String HAND_OUT_RED = "account/sendpacket";
    /**
     * 获取pubkey
     */
    public static final String GET_TOKEN = "account/getpubkey";
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
     * 普通红包，单个金额
     */
    private TextView mNormalPack;
    /**
     * 总金额linear
     */
    private LinearLayout mPingLayout;
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
     * 监听软键盘右下角，按下时承接光标
     *
     * @param view view
     * @return boolean
     */
    @OnEditorAction({R.id.yuan_num, R.id.edit_red_package_num})
    boolean OnEditorAction(TextView view) {
        switch (view.getId()) {
            case R.id.yuan_num:
                mPackageNum.requestFocus();
                break;
            case R.id.edit_red_package_num:
                mRedMessage.requestFocus();
                break;
        }
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
     * 该群信息体
     */
    private GroupInfo mGroupInfo;

    private String userId;
    /**
     * pubKey
     */
    private String mPubKey;

    private List<String> mNum = new ArrayList<>();

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
        ButterKnife.bind(this);
        mBack = findViewById(R.id.back);
        mRedGotLayout = findViewById(R.id.who_get_layout);
        mGroupMemNum = findViewById(R.id.group_member_count);
        mRedGot = findViewById(R.id.who_get);
        mNormalPack = findViewById(R.id.normal_yuan_text);
        mPingLayout = findViewById(R.id.ping_yuan_layout);
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

        mChangetoPackage.setOnClickListener(this);

        mRandomRedMsg.setOnClickListener(this);

        mSend.setOnClickListener(this);

        userId = getUserId();
        new Handler().post(() -> setPubKey(userId));
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.who_get_layout:
                Toasty.info(this, "选择谁可以领取红包", Toasty.LENGTH_SHORT).show();
                break;
            case R.id.change_to_kind:
                if (mChangetoPackage.getText().equals("改为普通红包")) {
                    mNowPackage.setText("当前为普通红包，");
                    mChangetoPackage.setText("改为拼手气红包");
                    mPingLayout.setVisibility(View.GONE);
                    mNormalPack.setVisibility(View.VISIBLE);
                } else {
                    mNowPackage.setText("当前为拼手气红包，");
                    mChangetoPackage.setText("改为普通红包");
                    mPingLayout.setVisibility(View.VISIBLE);
                    mNormalPack.setVisibility(View.GONE);
                }
                break;
            case R.id.update_message:
                Toasty.info(this, "随机选取一句祝福语", Toasty.LENGTH_SHORT).show();
                break;
            case R.id.send_red_package:
                int packageNum = Integer.parseInt(mPackageNum.getText().toString());
                if (packageNum <= 0) return;
                if (mChangetoPackage.getText().equals("改为普通红包")) {
                    List<String> num = getStringList(packageNum);
                    randomDialog(num.size(), num);
                } else {
                    handOutRed(null, null);
                }
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

    /**
     * 初始化特殊红包
     *
     * @param n 特殊红包数
     * @return list-string
     */
    private List<String> getStringList(int n) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add("6");
        }
        return list;
    }

    /**
     * 设定特殊红包弹窗
     *
     * @param tail 总红包数
     * @param num  初始总红包
     */
    @SuppressLint("SetTextI18n")
    private void randomDialog(int tail, List<String> num) {
        mNum = num; //列表数据等于初始总红包，第一次弹出时加载初始总红包

        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.qr_code_dialog_style);
        dialog.setContentView(R.layout.dialog_random_red_packit);   //设定dialog布局文件
        dialog.heightParam(ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.inDuration(300);
        dialog.outDuration(300);
        TextView amount = dialog.findViewById(R.id.amount); //共多少个红包
        amount.setText("共" + tail + "个");
        EditText setAmount = dialog.findViewById(R.id.set_amount);  //特殊红包数
        if (mNum.size() == tail)
            mNum = getStringList(mNum.size() - 1);
        setAmount.setText(mNum.size() + "");
        DefaultItemAnimator animator = new DefaultItemAnimator();   //列表刷新动画
        animator.setChangeDuration(300);
        animator.setRemoveDuration(300);
        animator.setAddDuration(300);
        animator.setMoveDuration(300);
        RecyclerView recycler = dialog.findViewById(R.id.recycler); //列表
        recycler.setItemAnimator(animator); //设定刷新动画
        recycler.setLayoutManager(new LinearLayoutManager(this));   //布局管理器
        //分割线
        recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        RandomDialogAdapter adapter = new RandomDialogAdapter(this, mNum);  //适配器
        recycler.setAdapter(adapter);
        setAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(editable)) {
                    int n = Integer.parseInt(editable.toString());  //新输入的特殊红包数
                    if (n <= tail) {  //若输入不为null或“”，并且小于初始总红包数
                        dialog.dismiss();
                        randomDialog(tail, getStringList(n));
                    } else {
                        Toasty.info(SetYuanActivity.this, "不能超过总红包数").show();
                    }
                }
            }
        });
        Button button = dialog.findViewById(R.id.done);
        button.setOnClickListener(view -> new Handler().post(() -> handOutRed(setAmount.getText().toString(), adapter.getList())));
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    /**
     * 发送红包
     */
    private void handOutRed(String tailCount, String tailNumber) {
        String secretString = getSecretString(mPubKey, tailCount, tailNumber);
        if (secretString == null) {
            return;
        }
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
                        Toasty.error(SetYuanActivity.this, "网络异常", Toasty.LENGTH_SHORT).show();
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
                                        Toasty.info(SetYuanActivity.this, data.getString("message")).show();
                                        LogUtil.d(TAG, "handOutRed-onResponse: " +
                                                "data = " + data + " ;success = " + success);
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
                Toasty.error(SetYuanActivity.this, "网络异常", Toasty.LENGTH_SHORT).show();
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
    private String getSecretString(String pubKey, String tailCount, String tailNumber) {
        JSONObject secretJson = new JSONObject();
        String yuan = mYuanNum.getText().toString();
        String count = mPackageNum.getText().toString();
        if (!DataUtil.isLegleYuan(yuan)) {
            Toasty.info(this, "红包金额不正确", Toasty.LENGTH_SHORT).show();
            return null;
        }
        if (!TextUtils.isDigitsOnly(count)) {
            Toasty.info(this, "红包个数不正确", Toasty.LENGTH_SHORT).show();
            return null;
        }
        try {
            secretJson.put("amount", Double.valueOf(yuan));
            secretJson.put("count", Integer.parseInt(count));
            secretJson.put("personCount", Integer.parseInt(count));
            secretJson.put("expireTime", 120);
            if (tailCount != null && tailNumber != null) {
                secretJson.put("tailCount", tailCount);
                secretJson.put("tailNumber", tailNumber);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String secretStr = secretJson.toString() + new Date().getTime();
        byte[] secretBytes = RSA.encryptByPublicKey4Android(secretStr.getBytes(), pubKey);
        return Base64.encode(secretBytes);
    }

}
