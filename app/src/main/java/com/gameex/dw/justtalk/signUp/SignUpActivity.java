package com.gameex.dw.justtalk.signUp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
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
import com.gameex.dw.justtalk.dataStream.PostJsonService;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.util.BarUtil;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 注册activity
 */
public class SignUpActivity extends BaseActivity implements View.OnClickListener {
    /**
     * 上下文参数
     */
    @SuppressLint("StaticFieldLeak")
    public static SignUpActivity sSignUpActivity;
    /**
     * 服务器注册接口
     */
    private static final String url = "http://117.50.57.86:8060/user/register";
    /**
     * 获取验证码接口
     */
    private static final String urlSms = "http://117.50.57.86:8060/code/sms";
    /**
     * 验证码验证接口
     */
    private static final String urlCheckSms = "http://117.50.57.86:8060/validate/mobile";

    private EditText mPhone, mVerifiCode, mPwd;
    private TextView mVerifiCodeText;
    private Button mSignUp;
    private Intent postJsonSer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        sSignUpActivity = this;
        BarUtil.setFullTransBar(this);  //设置全透明状态栏和虚拟键

        initView();
        initData();
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
        mVerifiCode = findViewById(R.id.verification_code_edit);
        mVerifiCodeText = findViewById(R.id.verification_code_text);
        mVerifiCodeText.setOnClickListener(this);
        mPwd = findViewById(R.id.pwd_edit);
        mSignUp = findViewById(R.id.sign_up_btn);
        mSignUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        final String phone = mPhone.getText().toString();
        String pwd = mPwd.getText().toString();
        switch (view.getId()) {
            case R.id.verification_code_text:   //获取验证码
                if (mVerifiCode.isEnabled()) {
                    if (DataUtil.isMobileNumber(phone)) {   //判断手机号格式是否正确
                        getSmsCodeThread(phone);
                    } else {
                        Toast.makeText(sSignUpActivity, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(sSignUpActivity, "点击获取验证码后需等待60秒", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.sign_up_btn:  //注册
                if (DataUtil.isMobileNumber(phone)) {   //验证手机号格式是否为空
                    if (!TextUtils.isEmpty(mVerifiCode.getText())) {    //判断验证码栏是否为空
                        if (DataUtil.isPWDCorrect(pwd)) {   //验证密码格式是否正确
                            //验证验证码是否正确。是，则进行注册；否，则Toast提示
//                            isSmsCorrectThread(mVerifiCode.getText().toString(), phone, pwd);
                            postJsonSer = new Intent(SignUpActivity.this, PostJsonService.class);
                            postJsonSer.putExtra("sign_info", new String[]{
                                    phone, pwd, url});
                            startService(postJsonSer);
                        } else {
                            //密码格式错误，动画+Toast提示
                            YoYo.with(Techniques.Shake)
                                    .duration(700)
                                    .playOn(findViewById(R.id.pwd_edit));
                            Toast.makeText(sSignUpActivity, "密码由字母和数字组成，且不少于8位", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        //验证码栏为空，动画+Toast提示
                        YoYo.with(Techniques.Shake)
                                .duration(700)
                                .playOn(findViewById(R.id.verification_code_edit));
                        Toast.makeText(sSignUpActivity, "请输入验证码", Toast.LENGTH_LONG).show();
                    }
                } else {
                    //手机号格式错误，动画+Toast提示
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .playOn(findViewById(R.id.phone_edit));
                    Toast.makeText(sSignUpActivity, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
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
        mVerifiCode.setEnabled(false);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mVerifiCode.setEnabled(true);
            }
        }, 60 * 1000);
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10 * 1000, TimeUnit.MILLISECONDS)
                        .readTimeout(5 * 10 * 1000, TimeUnit.MILLISECONDS)
                        .writeTimeout(5 * 10 * 1000, TimeUnit.MILLISECONDS)
                        .build();
                RequestBody formBody = new FormBody.Builder()
                        .add("phoneNumber", phone)
                        .build();
                Request request = new Request.Builder()
                        .url(urlSms)
                        .addHeader("accept", "application/json;charset=utf-8")
                        .addHeader("Content-Type", "text/plain")
                        .post(formBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        String string = response.body().string();
                        JSONObject jsonObject = new JSONObject(string);
                        boolean success = jsonObject.getBoolean("success");
                        if (success) {
                            String data = jsonObject.getString("data");
                            Looper.prepare();
                            Toast.makeText(SignUpActivity.this, data + "", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        } else {
                            Looper.prepare();
                            Toast.makeText(SignUpActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                            handler.removeMessages(0);
                            mVerifiCode.setEnabled(true);
                        }
                    } else {
                        assert response.body() != null;
                        LogUtil.d("VERIFICATION_CODE", response.body().string() + "response fail");
                    }

                } catch (IOException e) {
                    LogUtil.d("VERIFICATION_CODE", "IOException");
                    e.printStackTrace();
                } catch (JSONException e) {
                    LogUtil.d("VERIFICATION_CODE", "JSONException");
                    e.printStackTrace();
                }
            }
        }).start();
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10 * 1000, TimeUnit.MILLISECONDS)
                        .readTimeout(5 * 10 * 1000, TimeUnit.MILLISECONDS)
                        .writeTimeout(5 * 10 * 1000, TimeUnit.MILLISECONDS)
                        .build();
                RequestBody formBody = new FormBody.Builder()
                        .add("smsCode", sms)
                        .build();
                Request request = new Request.Builder()
                        .url(urlCheckSms)
                        .addHeader("accept", "application/json;charset=utf-8")
                        .addHeader("Content-Type", "text/plain")
                        .post(formBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        String string = response.body().string();
                        JSONObject jsonObject = new JSONObject(string);
                        boolean success = jsonObject.getBoolean("success");
                        String data = jsonObject.getString("data");
                        if (success) {
                            Looper.prepare();
                            LogUtil.d("VERIFICATION_CODE_CHECK_RESULT", data + "");
                            Looper.loop();
                            postJsonSer = new Intent(SignUpActivity.this, PostJsonService.class);
                            postJsonSer.putExtra("sign_info", new String[]{
                                    phone, pwd, url});
                            startService(postJsonSer);
                        } else {
                            Looper.prepare();
                            Toast.makeText(SignUpActivity.this, data + "", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    } else {
                        LogUtil.d("VERIFICATION_CODE_CHECK", "response fail");
                    }

                } catch (IOException e) {
                    LogUtil.d("VERIFICATION_CODE_CHECK", "IOException");
                    e.printStackTrace();
                } catch (JSONException e) {
                    LogUtil.d("VERIFICATION_CODE_CHECK", "JSONException");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (postJsonSer != null) {
            stopService(postJsonSer);
        }
    }
}
