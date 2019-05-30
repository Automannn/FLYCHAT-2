package com.gameex.dw.justtalk.payPackage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;
import com.gameex.dw.justtalk.util.WindowUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

import static com.gameex.dw.justtalk.payPackage.BankCardActivity.ACCOUNT_CARD_BIND_QUERY;

public class RechargeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "RechargeActivity";
    /**
     * 父布局
     */
    private LinearLayout mLayout;
    /**
     * 金额面板数据
     */
    private int[] mYuanArray = {10, 30, 50, 100, 200, 500, 1000};
    /**
     * gridView适配器
     */
    private SimpleAdapter mAdapter;
    /**
     * 金额面板数据集合
     */
    private List<Map<String, String>> mMapList;
    /**
     * 记录每次选中的金额
     */
    private Integer mYuan;
    /**
     * 记录上一次选中的checkBox
     */
    private CheckBox mCheckBox;
    /**
     * 没绑定银行卡时的popupWindow
     */
    private PopupWindow mNoBankCardPup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGrid();
        initView();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        setContentView(R.layout.activity_recharge);

        mLayout = findViewById(R.id.linear);

        //返回
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(this);

        //金额面板
        GridView gridView = findViewById(R.id.grid);
        mAdapter = new SimpleAdapter(this, mMapList, R.layout.recharge_item
                , new String[]{"yuan"}, new int[]{R.id.yuan});
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                LinearLayout layout;
                if (mYuan == null) {
                    layout = (LinearLayout) mAdapter.getView(position, view, null);
                    mCheckBox = (CheckBox) layout.getChildAt(0);
                    mCheckBox.setChecked(true);
                    mYuan = mYuanArray[position];
                } else if (mYuan == mYuanArray[position]) {
                    mCheckBox.setChecked(false);
                    mYuan = null;
                } else {
                    mCheckBox.setChecked(false);
                    layout = (LinearLayout) mAdapter.getView(position, view, null);
                    mCheckBox = (CheckBox) layout.getChildAt(0);
                    mCheckBox.setChecked(true);
                    mYuan = mYuanArray[position];
                }
            }
        });

        //下一步
        Button next = findViewById(R.id.next_step);
        next.setOnClickListener(this);

        initNoBankCardPup();
    }

    /**
     * 初始化Grid数据
     */
    private void initGrid() {
        mMapList = new ArrayList<>();
        for (int yuan : mYuanArray) {
            Map<String, String> map = new HashMap<>();
            map.put("yuan", yuan + ".0元");
            mMapList.add(map);
        }
    }

    /**
     * 没有绑定银行卡时弹出
     */
    private void initNoBankCardPup() {
        @SuppressLint("InflateParams") View view = this.getLayoutInflater().inflate(R.layout.no_bank_card_pup, null);
        TextView cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
        TextView confirm = view.findViewById(R.id.confirm);
        confirm.setOnClickListener(this);
        mNoBankCardPup = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mNoBankCardPup.setFocusable(true);
        mNoBankCardPup.setOutsideTouchable(false);
        mNoBankCardPup.setTouchInterceptor(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if (motionEvent.getAction() == MotionEvent.ACTION_OUTSIDE) {
//                    mNoBankCardPup.dismiss();
//                    return true;
//                }
                return false;
            }
        });
        mNoBankCardPup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowUtil.setWindowBackgroundAlpha(RechargeActivity.this, 1f);
            }
        });
        mNoBankCardPup.setAnimationStyle(R.style.scale_alpha_style);
        mNoBankCardPup.update();
    }

    /**
     * 在银行卡数据集不为空的情况下，若其长度大于0，则跳选择支付方式；否则弹出提示框
     *
     * @param data 银行卡数据集
     */
    private void goChoosePayWay(JSONArray data) {
        if (data == null) {
            if (mNoBankCardPup != null) {
                mNoBankCardPup.showAtLocation(mLayout, Gravity.CENTER, 0, 0);
                WindowUtil.showBackgroundAnimator(this, 0.5f);
            }
            return;
        }
        if (data.length() > 0) {
            Intent intent = new Intent(this, PayOrderActivity.class);
            intent.putExtra("money_amount", mYuan);
            startActivity(intent);
        }
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
                        Toast.makeText(RechargeActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Response response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                JSONObject object = new JSONObject(response.body().string());
                                boolean success = object.getBoolean("success");
                                if (success) {
                                    JSONArray data = object.getJSONArray("data");
                                    goChoosePayWay(data);
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
                        } else {
                            LogUtil.d(TAG, "queryBindCard-onResponse: " + "response = " + response);
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
                if (mYuan == null) {
                    Toast.makeText(this, "请选择金额", Toast.LENGTH_SHORT).show();
                    return;
                }
//                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
//                queryBindCard(pref.getString("userId", null));

//                Intent intent = new Intent(this, PayOrderActivity.class);
                intent.setClass(this, PayOrderActivity.class);
                intent.putExtra("money_amount", mYuan);
                startActivity(intent);
                break;
            case R.id.cancel:
                if (mNoBankCardPup.isShowing()) {
                    mNoBankCardPup.dismiss();
                }
                break;
            case R.id.confirm:
                if (mNoBankCardPup.isShowing()) {
                    mNoBankCardPup.dismiss();
                }
                intent.setClass(this, AddBankCardActivity.class);
                startActivity(intent);
                break;
        }
    }
}
