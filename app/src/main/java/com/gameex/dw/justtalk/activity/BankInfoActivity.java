package com.gameex.dw.justtalk.activity;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.im.android.api.JMessageClient;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.entry.BankInfo;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import static com.gameex.dw.justtalk.activity.BankCardActivity.UPDATE_BANK_ACTION;

public class BankInfoActivity extends BaseActivity {
    private static final String TAG = "BankInfoActivity";
    /**
     * 解绑接口
     */
    private static final String TERMINATION_PATH = "tianfubao/termination";
    /**
     * 图标
     */
    @BindView(R.id.icon)
    ImageView mIcon;
    /**
     * 名称、银行卡号
     */
    @BindViews({R.id.name, R.id.number})
    List<TextView> mTextViews;

    @OnClick({R.id.back, R.id.unbind})
    void doClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.unbind:
                new Handler().post(() -> unbind(mBankInfo.getSignSn()));
                break;
        }
    }

    private BankInfo mBankInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_info);
        ButterKnife.bind(this);
        initData();
    }

    /**
     * 初始化数据
     */
    @SuppressLint("SetTextI18n")
    private void initData() {
        mBankInfo = (BankInfo) getIntent().getSerializableExtra("bank");
        switch (mBankInfo.getBankName()) {
            case "建设银行":
                mIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_construction));
                break;
            case "农业银行":
                mIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_agricultural));
                break;
            case "工商银行":
                mIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_icbc));
                break;
            case "中兴银行":
                mIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_zte));
                break;
            default:
                break;
        }
        mTextViews.get(0).setText(mBankInfo.getBankName());
        mTextViews.get(1).setText("**** **** ****" + mBankInfo.getBankEndNum());
    }

    /**
     * 解绑银行卡
     *
     * @param signSn 签约序列号
     */
    private void unbind(String signSn) {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("signSn", signSn);
        paramsMap.put("mobile", JMessageClient.getMyInfo().getUserName());
        OkHttpUtil.okHttpPost(TERMINATION_PATH, paramsMap, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                e.printStackTrace();
                Toasty.error(BankInfoActivity.this, "网络连接异常").show();
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    boolean success = object.getBoolean("success");
                    if (success) {
                        Intent intent = new Intent(UPDATE_BANK_ACTION);
                        intent.putExtra("bank", mBankInfo);
                        sendBroadcast(intent);
                        finish();
                    }
                    Toasty.normal(BankInfoActivity.this, object.getString("data")).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
