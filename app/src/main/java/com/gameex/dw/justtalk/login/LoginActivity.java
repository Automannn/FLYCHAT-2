package com.gameex.dw.justtalk.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.gameex.dw.justtalk.main.BottomBarActivity;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.signUp.SignUpActivity;
import com.gameex.dw.justtalk.util.BarUtil;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;
import com.github.siyamed.shapeimageview.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;
import okhttp3.Call;
import okhttp3.Response;

/**
 * 登录activity
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "LoginActivity";
    public static final String LOGIN_PATH = "user/login";
    private static final int SIGN_UP_REQUEST_CODE = 201;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private CircularImageView mCircularImg;
    private EditText mUsername, mPassword;
    private CheckBox mAutoLogin;

    private ProgressDialog prosDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
        if (!BarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
//            BarUtil.setStatusBarColor(this,0x55000000);
        }
    }

    /**
     * 绑定id，设置监听
     */
    private void initView() {
        mCircularImg = findViewById(R.id.circle_img_login);
        mUsername = findViewById(R.id.username_text);
        mPassword = findViewById(R.id.pwd_text);
        Button loginBtn = findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(this);
        mAutoLogin = findViewById(R.id.auto_login_box);
        mAutoLogin.setOnCheckedChangeListener(this);
        TextView quickSign = findViewById(R.id.quick_sign_up);
        quickSign.setOnClickListener(this);
        TextView forgotPwd = findViewById(R.id.forgot_pwd);
        forgotPwd.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getExtras() == null) {
            inputLogin();
        } else {
            if (getIntent().getStringExtra("flag").equals("LoginOut")) {
                pref = PreferenceManager.getDefaultSharedPreferences(this);
                String account = pref.getString("account", "");
                String password = pref.getString("password", "");
                mUsername.setText(account);
                mPassword.setText(password);
                mAutoLogin.setChecked(true);
                editor = pref.edit();
                editor.remove("account");
                editor.remove("password");
                editor.remove("userId");
                editor.remove("remember_password");
                editor.apply();
            }
        }
    }

    /**
     * 自动输入上一次的登陆信息
     */
    private void inputLogin() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRemember = pref.getBoolean("remember_password", false);
        if (isRemember) {
            //将账号和密码都设置到文本框中
            String account = pref.getString("account", "");
            String password = pref.getString("password", "");
            mUsername.setText(account);
            mPassword.setText(password);
            mAutoLogin.setChecked(true);
            if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(password)) {
                JMessageClient.login(account, password, new BasicCallback() {
                    @Override
                    public void gotResult(int responseCode, String registerDesc) {
                        if (responseCode == 0) {
                            Intent intentLogin = new Intent(LoginActivity.this, BottomBarActivity.class);
                            LoginActivity.this.finish();
                            startActivity(intentLogin);
                        } else {
                            LogUtil.i("LOGIN_ACTIVITY_JMESSAGE_LOGIN",
                                    "JMessageClient.login " +
                                            ", responseCode = " + responseCode +
                                            " ; registerDesc = " + registerDesc);
                        }
                    }
                });
            } else {
                checkToLogin(0);
            }
        }
    }

    /**
     * 验证登录
     */
    private void checkToLogin(int flag) {
        if (flag == -1) {
            JMessageClient.login("18180027763", "123456dw", new BasicCallback() {
                @Override
                public void gotResult(int responseCode, String registerDesc) {
                    LogUtil.i("LOGIN_ACTIVITY_LMESSAGE_LOGIN",
                            "JMessageClient.register " +
                                    ", responseCode = " + responseCode +
                                    " ; registerDesc = " + registerDesc);
                }
            });
            Intent intentLogin = new Intent(LoginActivity.this, BottomBarActivity.class);
            finish();
            startActivity(intentLogin);
        }
        if (!TextUtils.isEmpty(mUsername.getText())) {
            if (!TextUtils.isEmpty(mPassword.getText())) {
                prosDialog = new ProgressDialog(this);
                prosDialog.setTitle("提示");
                prosDialog.setMessage("正在登录...");
                prosDialog.setCancelable(false);
                prosDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                prosDialog.show();
                final String account = mUsername.getText().toString();
                final String password = mPassword.getText().toString();
                HashMap<String, String> paramsMap = new HashMap<>();
                if (DataUtil.isMobileNumber(account)) {
                    paramsMap.put("phoneNumber", account);
                } else {
                    paramsMap.put("username", account);
                }
                paramsMap.put("password", password);
                OkHttpUtil.okHttpPost(LOGIN_PATH, paramsMap, new CallBackUtil.CallBackDefault() {
                    @Override
                    public void onFailure(Call call, Exception e) {
                        prosDialog.dismiss();
                        e.printStackTrace();
                        LogUtil.d(TAG, "checkToLogin-CallBackUtil-onFailure: ");
                        Toast.makeText(LoginActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Response response) {
                        if (response != null) {
                            LogUtil.d(TAG, "checkToLogin-CallBackUtil-onResponse: "
                                    + "response" + response);
                            try {
                                assert response.body() != null;
                                JSONObject jsonObject = new JSONObject(response.body().string());
                                JSONObject object = jsonObject.getJSONObject("data");
                                boolean isSuccess = jsonObject.getBoolean("success");
                                if (isSuccess) {
                                    String userId = object.getString("id");
                                    prosDialog.dismiss();
                                    editor = pref.edit();
                                    if (mAutoLogin.isChecked()) {
                                        editor.putBoolean("remember_password", true);
                                        editor.putString("account", account);
                                        editor.putString("password", password);
                                        editor.putString("userId", userId);
                                    } else {
                                        editor.clear();
                                    }
                                    editor.apply();
                                    JMessageClient.login(account, password, new BasicCallback() {
                                        @Override
                                        public void gotResult(int responseCode, String registerDesc) {
                                            LogUtil.i("LOGIN_ACTIVITY_JMESSAGE_LOGIN",
                                                    "JMessageClient.login " +
                                                            ", responseCode = " + responseCode +
                                                            " ; registerDesc = " + registerDesc);
                                            if (responseCode == 0) {
                                                Intent intentLogin = new Intent(
                                                        LoginActivity.this, BottomBarActivity.class);
                                                finish();
                                                startActivity(intentLogin);
                                            } else {
                                                prosDialog.dismiss();
                                                Toast.makeText(LoginActivity.this,
                                                        "登陆失败……", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    prosDialog.dismiss();
                                    int code = object.getInt("code");
                                    String message = object.getString("message");
                                    if (code == 403) {
                                        YoYo.with(Techniques.Shake)
                                                .duration(700)
                                                .playOn(findViewById(R.id.username_layout));
                                        Toast.makeText(LoginActivity.this, message + "", Toast.LENGTH_SHORT).show();
                                    } else if (code == 404) {
                                        YoYo.with(Techniques.Shake)
                                                .duration(700)
                                                .playOn(findViewById(R.id.password_layout));
                                        Toast.makeText(LoginActivity.this, message + "", Toast.LENGTH_SHORT).show();
                                    } else {
                                        LogUtil.d(TAG, "checkToLogin-CallBackUtil-onResponse: "
                                                + "code = " + code + " ;message = " + message);
                                        Toast.makeText(LoginActivity.this, message + "", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            } else {
                if (flag == 1) {
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .playOn(findViewById(R.id.password_layout));
                    Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            if (flag == 1) {
                YoYo.with(Techniques.Shake)
                        .duration(700)
                        .playOn(findViewById(R.id.username_layout));
                Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_btn:
                checkToLogin(1);
//                checkToLogin(-1);
                break;
            case R.id.quick_sign_up:
                Intent intentQuickSign = new Intent(this, SignUpActivity.class);
                String mobile = mUsername.getText().toString();
                if (DataUtil.isMobileNumber(mobile)) {
                    intentQuickSign.putExtra("phone", mobile);
                }
                startActivityForResult(intentQuickSign, SIGN_UP_REQUEST_CODE);
                break;
            case R.id.forgot_pwd:
                Toast.makeText(this, "找回密码", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        if (!checked) {
            editor = pref.edit();
            editor.clear();
            editor.apply();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode
            , @Nullable Intent data) {
        if (requestCode == SIGN_UP_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String phone = data.getStringExtra("phone");
                mUsername.setText(phone);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        stopService(mIntentGetUser);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (prosDialog != null && prosDialog.isShowing()) {
                prosDialog.dismiss();
                LogUtil.d("KEYCODE_BACK", "dismiss dialog");
            } else {
                LoginActivity.this.finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
