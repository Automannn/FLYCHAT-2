package com.gameex.dw.justtalk.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.adapter.AddBankAdapter;
import com.gameex.dw.justtalk.entry.BankInfo;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;
import com.gameex.dw.justtalk.util.SharedPreferenceUtil;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Response;

/**
 * 已绑定的银行卡列表
 */
public class BankCardActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "BankCardActivity";
    /**
     * 刷新银行卡action
     */
    public static final String UPDATE_BANK_ACTION =
            "com.gameex.dw.justtalk.activity." + TAG + ".UPDATE_BANK_ACTION";
    /**
     * 绑卡情况查询路径
     */
    public static final String ACCOUNT_CARD_BIND_QUERY = "unionpay/cardbind/query";
    /**
     * 银行卡列表
     */
    private RecyclerView mRecView;
    /**
     * 银行卡列表适配器
     */
    private AddBankAdapter mAdapter;
    /**
     * 银行卡列表数据集合
     */
    private List<BankInfo> mBankInfos = new ArrayList<>();
    private BankCardRceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();

        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_BANK_ACTION);
        mReceiver = new BankCardRceiver();
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    /**
     * 初始化布局
     */
    private void initView() {
        setContentView(R.layout.activity_bank_card);

        //返回
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(this);

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setChangeDuration(300);
        mRecView = findViewById(R.id.recycler);
        mRecView.setLayoutManager(new LinearLayoutManager(this));
        mRecView.setItemAnimator(animator);

        //添加银行卡
        LinearLayout addBank = findViewById(R.id.add_bank_layout);
        addBank.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mBankInfos = SharedPreferenceUtil.getList("bind_card_list"
                , new TypeToken<List<BankInfo>>() {
                }.getType());
        if (mBankInfos == null) mBankInfos = new ArrayList<>();
        mAdapter = new AddBankAdapter(this, mBankInfos);
        mRecView.setAdapter(mAdapter);
        new Handler().post(() -> queryBindCard((String) SharedPreferenceUtil.getData("userId", "")));
    }

    /**
     * 刷新并缓存新数据
     *
     * @param bankInfos 数据集
     */
    private void updateRec(List<BankInfo> bankInfos) {
        if (bankInfos == mBankInfos) {
            return;
        }
        mBankInfos = bankInfos;
        mAdapter.notifyDataSetChanged();
        SharedPreferenceUtil.putList("bind_card_list", mBankInfos);
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
                        Toast.makeText(BankCardActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Response response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                JSONObject object = new JSONObject(response.body().string());
                                boolean success = object.getBoolean("success");
                                if (success) {
                                    List<BankInfo> bankInfos = new ArrayList<>();
                                    JSONArray data = object.getJSONArray("data");
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
                                            bankInfo.setSignSn(bank.getString("signSn"));
                                        } catch (JSONException jsone) {
                                            bankInfo.setSignSn("");
                                            jsone.printStackTrace();
                                        }
                                        try {
                                            String bankId = bank.getString("id");
                                            if (!TextUtils.isEmpty(bankId))
                                                bankInfo.setBankId(bankId);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        bankInfos.add(bankInfo);
                                    }
                                    updateRec(bankInfos);
                                } else {
                                    JSONObject data = object.getJSONObject("data");
                                    int code = data.getInt("code");
                                    String message = data.getString("message");
                                    LogUtil.d(TAG, "queryBindCard-onResponse: " +
                                            "code = " + code + " ;message = " + message);
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

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.add_bank_layout:
                intent.setClass(this, AddBankCardActivity.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * 广播接收器，用于刷新银行卡列表
     */
    class BankCardRceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UPDATE_BANK_ACTION)) {
                BankInfo bankInfo = (BankInfo) intent.getSerializableExtra("bank");
                if (mBankInfos.contains(bankInfo)) {
                    mBankInfos.remove(bankInfo);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}
