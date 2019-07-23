package com.gameex.dw.justtalk.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.service.PostJsonService;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.util.BarUtil;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;
import com.gameex.dw.justtalk.util.TimeCounter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * 注册activity
 */
public class SignUpActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "SignUpActivity";
    /**
     * 上下文参数
     */
    @SuppressLint("StaticFieldLeak")
    public static SignUpActivity sSignUpActivity;
    /**
     * 服务器注册接口
     */
    private static final String SIGN_PATH = "user/register";
    /**
     * 获取验证码接口
     */
    private static final String GET_SMS = "code/sms";
    /**
     * 验证码验证接口
     */
    private static final String CHECK_SMS = "validate/mobile";
    /**
     * 手机号、验证码、密码
     */
    private EditText mPhone, mVerifyCode, mPwd;
    /**
     * 获取验证码
     */
    private TextView mVerifyCodeText;
    /**
     * 注册线程
     */
    private Intent postJsonSer;
    /**
     * 记录请求头
     */
    private Map<String, String> header = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        sSignUpActivity = this;

        initView();
        initData();
        if (!BarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
//            BarUtil.setStatusBarColor(this,0x55000000);
        }
    }

    /**
     * 预加载(处理)数据
     */
    private void initData() {
        if (getIntent().getStringExtra("phone") != null) {
            mPhone.setText(getIntent().getStringExtra("phone"));
        }
    }

    /**
     * ui绑定id，设置监听器
     */
    private void initView() {
        mPhone = findViewById(R.id.phone_edit);
        mVerifyCode = findViewById(R.id.verification_code_edit);
        mVerifyCodeText = findViewById(R.id.verification_code_text);
        mVerifyCodeText.setOnClickListener(this);
        mPwd = findViewById(R.id.pwd_edit);
        Button signUp = findViewById(R.id.sign_up_btn);
        signUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        final String phone = mPhone.getText().toString();
        String pwd = mPwd.getText().toString();
        switch (view.getId()) {
            case R.id.verification_code_text:   //获取验证码
                if (mVerifyCode.isEnabled()) {
                    if (DataUtil.isMobileNumber(phone)) {   //判断手机号格式是否正确
                        getSmsCodeThread(phone);
                    } else {
                        YoYo.with(Techniques.Shake)
                                .duration(700)
                                .playOn(findViewById(R.id.phone_edit));
                        Toast.makeText(sSignUpActivity, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    YoYo.with(Techniques.Swing)
                            .duration(700)
                            .playOn(findViewById(R.id.verification_code_text));
                    Toast.makeText(sSignUpActivity, "请等待" + mVerifyCodeText.getText(), Toast.LENGTH_SHORT).show();
                }
//                Toasty.info(this, "验证码随便输，直接注册").show();
                break;
            case R.id.sign_up_btn:  //注册
                if (DataUtil.isMobileNumber(phone)) {   //验证手机号格式是否为空: !TextUtils.isEmpty(phone)
                    if (!TextUtils.isEmpty(mVerifyCode.getText())) {    //判断验证码栏是否为空
                        if (DataUtil.isPWDCorrect(pwd)) {   //验证密码格式是否正确
                            //验证验证码是否正确。是，则进行注册；否，则Toast提示
                            isSmsCorrectThread(mVerifyCode.getText().toString(), phone, pwd);
                        } else {
                            //密码格式错误，动画+Toast提示
                            YoYo.with(Techniques.Shake)
                                    .duration(700)
                                    .playOn(findViewById(R.id.pwd_edit));
                            Toasty.error(this, "密码由字母和数字组成，且不少于8位"
                                    , Toasty.LENGTH_LONG).show();
                        }
                    } else {
                        //验证码栏为空，动画+Toast提示
                        YoYo.with(Techniques.Shake)
                                .duration(700)
                                .playOn(findViewById(R.id.verification_code_edit));
                        Toasty.error(sSignUpActivity, "请输入验证码", Toasty.LENGTH_LONG).show();
                    }
                } else {
                    //手机号格式错误，动画+Toast提示
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .playOn(findViewById(R.id.phone_edit));
                    Toasty.error(this, "请输入正确的手机号", Toasty.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 获取验证码
     *
     * @param phone 手机号
     */
    private void getSmsCodeThread(final String phone) {
        final TimeCounter timeCounter = new TimeCounter(60000, 100, mVerifyCodeText);
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("phoneNumber", phone);
        OkHttpUtil.okHttpPost(GET_SMS, paramsMap, new CallBackUtil.CallBackDefault() {
            @Override
            public void onFailure(Call call, Exception e) {
                LogUtil.d(TAG, "getSmsCodeThread-onFailure: ");
                e.printStackTrace();
                Toast.makeText(SignUpActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Response response) {
                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            String string = response.body().string();
                            LogUtil.d(TAG, "getSmsCodeThread-onResponse: " + "response string = " + string);
                            JSONObject jsonObject = new JSONObject(string);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                Headers headers = response.headers();
                                List<String> cookies = headers.values("Set-cookie");
                                String s = cookies.get(0);
                                header.put("cookie", TextUtils.isEmpty(s) ? "" : s);
                                LogUtil.d(TAG, "getSmsCodeThread: " + "cookies(0) = " + s);
                                String data = jsonObject.getString("data");
                                timeCounter.start();
                                Toast.makeText(SignUpActivity.this, data + "", Toast.LENGTH_SHORT).show();
                            } else {
                                JSONObject data = jsonObject.getJSONObject("data");
                                int code = data.getInt("code");
                                String message = data.getString("message");
                                LogUtil.i(TAG, "getSmsCodeThread-onResponse-successFalse: "
                                        + "code = " + code + " ; message = " + message);
                                Toast.makeText(SignUpActivity.this, message + "", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            LogUtil.i(TAG, "getSmsCodeThread-response.bodyNull: ");
                        }
                    } else {
                        LogUtil.i(TAG, "getSmsCodeThread-responseFalse: ");
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (JSONException JSONe) {
                    JSONe.printStackTrace();
                }
            }
        });
    }

    /**
     * 验证验证码是否正确。是，则进行注册；否，则Toast提示
     *
     * @param sms   验证码
     * @param phone 手机号
     * @param pwd   密码
     */
    private void isSmsCorrectThread(final String sms, final String phone,
                                    final String pwd) {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("smsCode", sms);
        OkHttpUtil.okHttpPost(CHECK_SMS, paramsMap, header, new CallBackUtil.CallBackDefault() {
            @Override
            public void onFailure(Call call, Exception e) {
                LogUtil.d(TAG, "isSmsCorrectThread-onFailure: ");
                e.printStackTrace();
                Toast.makeText(SignUpActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Response response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String string;
                        try {
                            string = response.body().string();
                            JSONObject jsonObject = new JSONObject(string);
                            boolean success = jsonObject.getBoolean("success");
                            String data = jsonObject.getString("data");
                            if (success) {
                                header.clear();
                                LogUtil.d("VERIFICATION_CODE_CHECK_RESULT", data + "");
                                postJsonSer = new Intent(SignUpActivity.this
                                        , PostJsonService.class);
                                postJsonSer.putExtra("sign_info", new String[]{
                                        phone, pwd, SIGN_PATH});
                                startService(postJsonSer);
                            } else {
                                Toast.makeText(SignUpActivity.this, data + "", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        LogUtil.d(TAG, "isSmsCorrectThread-onResponse: " + "response.body = null");
                    }
                } else {
                    LogUtil.d(TAG, "isSmsCorrectThread-onResponse: " + "responseFalse");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (postJsonSer != null) {
            stopService(postJsonSer);
        }
    }
}
