package com.gameex.dw.justtalk.payPackage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Response;

public class BankCardActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "BankCardActivity";
    /**
     * 绑卡情况查询路径
     */
    public static final String ACCOUNT_CARD_BIND_QUERY = "account/cardbind/query";
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
    private JSONArray mBanks;

    private SharedPreferences mPref;

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
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        String data = mPref.getString("bind_card_list", "[]");
        JSONArray array = null;
        try {
            array = new JSONArray(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mBanks = array;
        mAdapter = new AddBankAdapter(this, mBanks);
        mRecView.setAdapter(mAdapter);
        queryBindCard(mPref.getString("userId", null));
    }

    /**
     * 刷新并缓存新数据
     *
     * @param data 数据集
     */
    private void updateRec(JSONArray data) {
        if (data == mBanks) {
            return;
        }
        mBanks = data;
        mAdapter.notifyDataSetChanged();
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString("bind_card_list", mBanks.toString());
        editor.apply();
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
                                    JSONArray data = object.getJSONArray("data");
                                    updateRec(data);
                                } else {
                                    JSONObject data = object.getJSONObject("data");
                                    int code = data.getInt("code");
                                    String message = data.getString("message");
                                    LogUtil.d(TAG, "queryBindCard-onResponse: " +
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
}
