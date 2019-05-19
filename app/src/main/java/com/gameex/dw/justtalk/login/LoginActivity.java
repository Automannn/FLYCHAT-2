package com.gameex.dw.justtalk.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.gameex.dw.justtalk.BottomBarActivity;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.dataStream.GetUserService;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.signUp.SignUpActivity;
import com.gameex.dw.justtalk.util.BarUtil;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;
import com.github.siyamed.shapeimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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
    private Button mLoginBtn;
    private CheckBox mAutoLogin;
    private TextView mQuickSign, mForgotPwd;

    private Intent mIntentGetUser;
    private ProgressDialog prosDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
        BarUtil.setFullTransBar(this);  //状态栏、虚拟键全透明
    }

    /**
     * 绑定id，设置监听
     */
    private void initView() {
        mCircularImg = findViewById(R.id.circle_img_login);
        mUsername = findViewById(R.id.username_text);
        mPassword = findViewById(R.id.pwd_text);
        mLoginBtn = findViewById(R.id.login_btn);
        mLoginBtn.setOnClickListener(this);
        mAutoLogin = findViewById(R.id.auto_login_box);
        mAutoLogin.setOnCheckedChangeListener(this);
        mQuickSign = findViewById(R.id.quick_sign_up);
        mQuickSign.setOnClickListener(this);
        mForgotPwd = findViewById(R.id.forgot_pwd);
        mForgotPwd.setOnClickListener(this);

        mIntentGetUser = new Intent(LoginActivity.this, GetUserService.class);
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
                JSONObject json = new JSONObject();
                try {
                    if (DataUtil.isMobileNumber(account)) {
                        json.put("phoneNumber", account);
                    } else {
                        json.put("username", account);
                    }
                    json.put("password", password);
                    OkHttpUtil.okHttpPostJson(LOGIN_PATH, json.toString(), null, new CallBackUtil.CallBackString() {
                        @Override
                        public void onFailure(Call call, Exception e) {
                            prosDialog.dismiss();
                            LogUtil.d(TAG, "checkToLogin-CallBackUtil-onFailure: " + "null");
                        }

                        @Override
                        public void onResponse(String response) {
                            if (response != null) {
                                LogUtil.d(TAG, "checkToLogin-CallBackUtil-onResponse: "
                                        + "response" + response);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    boolean isSuccess = jsonObject.getBoolean("success");
                                    if (isSuccess) {
                                        prosDialog.dismiss();
                                        editor = pref.edit();
                                        if (mAutoLogin.isChecked()) {
                                            editor.putBoolean("remember_password", true);
                                            editor.putString("account", account);
                                            editor.putString("password", password);
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
                                            }
                                        });
                                        Intent intentLogin = new Intent(LoginActivity.this, BottomBarActivity.class);
                                        finish();
                                        startActivity(intentLogin);
                                    } else {
                                        prosDialog.dismiss();
                                        JSONObject object = jsonObject.getJSONObject("data");
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
                                            Toast.makeText(LoginActivity.this, "请完善登陆信息", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
//                            OkHttpClient client = new OkHttpClient();
//                            RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));
//                            Request request = new Request.Builder()
//                                    .url(LOGIN_URL)
//                                    .post(requestBody)
//                                    .build();
//                            Response response = client.newCall(request).execute();
//                            if (response.isSuccessful()) {
//                                assert response.body() != null;
//                                String string = response.body().string();
//                                LogUtil.d("RESPONSE_STRING", string);
//                                JSONObject jsonObject = new JSONObject(string);
//                                boolean isSuccess = jsonObject.getBoolean("success");
//                                if (isSuccess) {
//                                    prosDialog.dismiss();
//                                    editor = pref.edit();
//                                    if (mAutoLogin.isChecked()) {
//                                        editor.putBoolean("remember_password", true);
//                                        editor.putString("account", account);
//                                        editor.putString("password", password);
//                                    } else {
//                                        editor.clear();
//                                    }
//                                    editor.apply();
//                                    JMessageClient.login(account, password, new BasicCallback() {
//                                        @Override
//                                        public void gotResult(int responseCode, String registerDesc) {
//                                            LogUtil.i("LOGIN_ACTIVITY_JMESSAGE_LOGIN",
//                                                    "JMessageClient.login " +
//                                                            ", responseCode = " + responseCode +
//                                                            " ; registerDesc = " + registerDesc);
//                                        }
//                                    });
//                                    Intent intentLogin = new Intent(LoginActivity.this, BottomBarActivity.class);
//                                    finish();
//                                    startActivity(intentLogin);
//                                } else {
//                                    prosDialog.dismiss();
//                                    JSONObject object = jsonObject.getJSONObject("data");
//                                    int code = object.getInt("code");
//                                    String message = object.getString("message");
//                                    if (code == 403) {
//                                        Looper.prepare();
//                                        YoYo.with(Techniques.Shake)
//                                                .duration(700)
//                                                .playOn(findViewById(R.id.username_layout));
//                                        Toast.makeText(LoginActivity.this, message + "", Toast.LENGTH_SHORT).show();
//                                        Looper.loop();
//                                    } else if (code == 404) {
//                                        Looper.prepare();
//                                        YoYo.with(Techniques.Shake)
//                                                .duration(700)
//                                                .playOn(findViewById(R.id.password_layout));
//                                        Toast.makeText(LoginActivity.this, message + "", Toast.LENGTH_SHORT).show();
//                                        Looper.loop();
//                                    } else {
//                                        Looper.prepare();
//                                        Toast.makeText(LoginActivity.this, "请完善登陆信息", Toast.LENGTH_SHORT).show();
//                                        Looper.loop();
//                                    }
//                                }
//                            } else {
//                                prosDialog.dismiss();
//                                LogUtil.d("RESPONSE_BODY", "null");
//                            }
                } catch (JSONException e) {
                    prosDialog.dismiss();
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(LoginActivity.this, "数据异常", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
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
