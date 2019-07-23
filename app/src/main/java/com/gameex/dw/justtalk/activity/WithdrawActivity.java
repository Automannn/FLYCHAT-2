package com.gameex.dw.justtalk.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.adapter.BottomBankDialogAdapter;
import com.gameex.dw.justtalk.entry.BankInfo;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.DialogUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;
import com.gameex.dw.justtalk.util.SharedPreferenceUtil;
import com.rey.material.app.BottomSheetDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;

/**
 * 提现界面
 */
public class WithdrawActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "WithdrawActivity";
    /**
     * 提现接口
     */
    private static final String WITHDRAW_PLATFORM = "account/withdrawPlatform";
    /**
     * 提现接口，天付宝
     */
    private static final String SINGLE_PAY_PATH = "tianfubao/singlePay";
    /**
     * 更新界面的bank信息action
     */
    public static final String RECEIVER_UPDATE_BANK =
            "com.gameex.dw.justtalk.activity.WithdrawActivity.RECEIVER_UPDATE_BANK";

    /**
     * 银行图标
     */
    @BindView(R.id.bank_icon)
    ImageView mBankIcon;
    /**
     * 金额输入框
     */
    @BindView(R.id.amount)
    EditText mAmount;
    /**
     * 银行名（银行尾号）、输入框下的提示
     */
    @BindViews({R.id.bank_name, R.id.notice})
    List<TextView> mTextViews;

    /**
     * 返回
     */
    @BindView(R.id.back)
    ImageView mBack;
    /**
     * 更多功能
     */
    @BindView(R.id.more)
    ImageView mMore;
    /**
     * 全部提现
     */
    @BindView(R.id.all_draw)
    TextView mAllDraw;
    /**
     * 提现
     */
    @BindView(R.id.withdraw)
    Button mWithdraw;

    /**
     * 体现银行卡的id
     */
    private String mCardId;
    /**
     * 我的零钱
     */
    private String mBalance;
    /**
     * 银行卡标识数据
     */
    private List<BankInfo> mBankInfos;

    private BottomSheetDialog dialog;

    private WithdrawReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);
        ButterKnife.bind(this);

        initData();
        initListener();
        mReceiver = new WithdrawReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(RECEIVER_UPDATE_BANK);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        mBalance = getIntent().getStringExtra("balance");
        mTextViews.get(1).setText("当前零钱余额" + mBalance + " , ");
        mBankInfos = (List<BankInfo>) getIntent().getSerializableExtra("bankInfo");
        initBank(mBankInfos.get(0));
    }

    @SuppressLint("SetTextI18n")
    public void initBank(BankInfo bankInfo) {
        switch (bankInfo.getBankName()) {
            case "建设银行":
                mBankIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_construction));
                break;
            case "农业银行":
                mBankIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_agricultural));
                break;
            case "工商银行":
                mBankIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_icbc));
                break;
            case "中兴银行":
                mBankIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_zte));
                break;
            default:
                break;
        }
        mBankIcon.setTag(bankInfo.getSignSn());
        mTextViews.get(0).setText(bankInfo.getBankName() + "(" + bankInfo.getBankEndNum() + ")");
        mTextViews.get(0).setTag(bankInfo.getBankId());
    }

    /**
     * 绑定输入框监听
     */
    private void initListener() {
        mBack.setOnClickListener(this);
        mMore.setOnClickListener(this);
        mTextViews.get(0).setOnClickListener(this);
        mAllDraw.setOnClickListener(this);
        mWithdraw.setOnClickListener(this);

        mAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                LogUtil.d(TAG, "initListener-beforeTextChanged: " +
                        "charSequence = " + charSequence + " ;i = " + i + " ;i1 = " + i1 + " ;i2 = " + i2);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                LogUtil.d(TAG, "initListener-onTextChanged: " +
                        "charSequence = " + charSequence + " ;i = " + i + " ;i1 = " + i1 + " ;i2 = " + i2);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable)) {
                    mWithdraw.setEnabled(false);
                    mWithdraw.setAlpha(0.5f);
                    return;
                }
                if (String.valueOf(editable).substring(0, 1).equals("."))
                    mAmount.setText("0" + editable);
                else if (Double.valueOf(editable.toString()) > Double.valueOf(mBalance.substring(1)))
                    mAmount.setText(mBalance.substring(1));
                mWithdraw.setEnabled(true);
                mWithdraw.setAlpha(1);
                LogUtil.d(TAG, "initListener-afterTextChanged: " +
                        "editable = " + editable);
            }
        });
    }

    /**
     * 提现方法
     *
     * @param amount 金额
     * @param userId 用户的本地服务器id
     * @param cardId 已绑定的银行卡id
     */
    private void withdraw(String amount, String userId, String cardId) {
        HashMap<String, String> param = new HashMap<>();
        param.put("amount", amount);
        param.put("userId", userId);
        param.put("cardId", cardId);
        OkHttpUtil.okHttpPost(WITHDRAW_PLATFORM, param, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                e.printStackTrace();
                Toasty.error(WithdrawActivity.this, "网络错误").show();
            }

            @Override
            public void onResponse(String response) {
                if (response != null) {
                    try {
                        JSONObject res = new JSONObject(response);
                        boolean isSuccess = res.getBoolean("success");
                        String data = res.getString("data");
                        Toasty.info(WithdrawActivity.this, data).show();
                        if (isSuccess) {
                            Intent intent = new Intent();
                            intent.putExtra("order_success", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    /**
     * 天付宝提现接口
     */
    private void withdtawByAtionPay() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("userId", SharedPreferenceUtil.getData("userId", ""));
        params.put("signSn", mBankIcon.getTag());
        params.put("money", Double.valueOf(mAmount.getText().toString()));
        HashMap<String, String> map = new HashMap<>();
        map.put("map", params.toString());
        OkHttpUtil.okHttpPost(SINGLE_PAY_PATH, map, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                e.printStackTrace();
                Toasty.error(WithdrawActivity.this, "网络连接异常").show();
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    boolean success = object.getBoolean("success");
                    if (success) {
                        JSONObject data = object.getJSONObject("data");
                        String spbillNo = data.getString("spbillNo");
                        //TODO: 跳转提现结果界面
                    } else {
                        Toasty.normal(WithdrawActivity.this, object.getString("data")).show();
                    }
                    LogUtil.d(TAG, "withdtawByAtionPay-onResponse: " + "response = " + response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 底部dialog显示已绑定的银行卡
     *
     * @param bankInfos 已绑定的银行卡数据集合
     */
    private void showBottom(List<BankInfo> bankInfos) {
        dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_bottom, null);
        RecyclerView recycler = view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        BottomBankDialogAdapter adapter = new BottomBankDialogAdapter(this, bankInfos);
        recycler.setAdapter(adapter);
        dialog.applyStyle(R.style.qr_code_dialog_style)
                .contentView(view)
                .heightParam(ViewGroup.LayoutParams.WRAP_CONTENT)
                .inDuration(300)
                .outDuration(300)
                .canceledOnTouchOutside(true)
                .show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back: //返回
                finish();
                break;
            case R.id.more: //更多功能
                Toasty.info(this, "敬请期待").show();
                break;
            case R.id.bank_name:
                showBottom(mBankInfos);
                break;
            case R.id.all_draw: //全部提现
                mAmount.setText(mBalance.substring(1));
                break;
            case R.id.withdraw: //提现
                new Handler().post(() -> withdraw(mAmount.getText().toString()
                        , getIntent().getStringExtra("userId")
                        , (String) mTextViews.get(0).getTag()));
                break;
        }
    }

    /**
     * 刷新bank信息
     */
    class WithdrawReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (RECEIVER_UPDATE_BANK.equals(intent.getAction())) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                BankInfo bankInfo = (BankInfo) intent.getSerializableExtra("bankInfo");
                initBank(bankInfo);
            }
        }
    }
}
