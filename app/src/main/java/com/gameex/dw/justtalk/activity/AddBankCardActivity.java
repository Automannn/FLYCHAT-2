package com.gameex.dw.justtalk.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
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
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.util.BaseDialog;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;
import com.gameex.dw.justtalk.util.SharedPreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Response;

import static com.gameex.dw.justtalk.FlayChatApplication.bankNumMap;
import static com.gameex.dw.justtalk.FlayChatApplication.bankTypeMap;

/**
 * 添加银行卡界面
 */
public class AddBankCardActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "AddBankCardActivity";
    /**
     * ali查询银行卡归属
     */
    private static final String QUERY_BANK_URL1 =
            "https://ccdcapi.alipay.com/validateAndCacheCardInfo.json?_input_charset=utf-8&cardNo=";
    private static final String QUERY_BANK_URL2 = "&cardBinCheck=true";
    /**
     * 绑卡成功回调
     */
    private static final int REQUEST_SMS_PHONE_CODE=431;
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
     * 调用ali接口查询银行卡名称和类别
     *
     * @param cardNum 银行卡号
     */
    private void getBankInfo(String cardNum, Intent intent) {
        OkHttpUtil.okHttpGet(QUERY_BANK_URL1 + cardNum + QUERY_BANK_URL2, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                e.printStackTrace();
                Toasty.error(AddBankCardActivity.this, "网络连接失败").show();
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    boolean validated = object.getBoolean("validated");
                    if (validated) {
                        String bank = bankNumMap.get(object.getString("bank"));
                        String cardType = bankTypeMap.get(object.getString("cardType"));
                        intent.putExtra("bank", bank);
                        intent.putExtra("cardType", cardType);
                        startActivityForResult(intent,REQUEST_SMS_PHONE_CODE);
                    } else {
                        Toasty.info(AddBankCardActivity.this, "不支持该类银行卡").show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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
                intent.setClass(this, SMSPhoneActivity.class);
                Intent skyPayIntent = getIntent();
                if (skyPayIntent != null) {
                    //是否调用天付宝支付
                    intent.putExtra("goSkyPay", skyPayIntent.getBooleanExtra("goSkyPay", false));
                    //产品id
                    intent.putExtra("productCode", skyPayIntent.getStringExtra("productCode"));
                    //产品数量
                    intent.putExtra("number", skyPayIntent.getIntExtra("number",-1));
                }
                String cardNum = mCardNumber.getText().toString();
                intent.putExtra("IDcard", mIdNumber.getText().toString());  //身份证
                intent.putExtra("bankCard", cardNum);  //银行卡卡号
                intent.putExtra("userName", mName.getText().toString());    //持卡人真实姓名
                new Handler().post(() -> getBankInfo(cardNum, intent));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==REQUEST_SMS_PHONE_CODE&&resultCode==RESULT_OK){
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
