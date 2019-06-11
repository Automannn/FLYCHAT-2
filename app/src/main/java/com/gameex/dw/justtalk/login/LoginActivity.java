package com.gameex.dw.justtalk.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.allenliu.versionchecklib.v2.builder.DownloadBuilder;
import com.cazaea.sweetalert.SweetAlertDialog;
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
import com.gameex.dw.justtalk.util.UpdateApkUtil;
import com.gameex.dw.justtalk.util.UserInfoUtils;
import com.github.siyamed.shapeimageview.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import es.dmoral.toasty.Toasty;
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

    private final Timer mTimer = new Timer();
    private TimerTask mTask;
    private int i = -1;
    private SweetAlertDialog prosDialog;

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
        mUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                JMessageClient.getUserInfo(editable.toString(), new GetUserInfoCallback() {
                    @Override
                    public void gotResult(int i, String s, UserInfo userInfo) {
                        if (i == 0) {
                            LogUtil.d(TAG, "initView-afterTextChanged: "
                                    + "userInfo = " + userInfo.toJson());
                            UserInfoUtils.initUserIcon(userInfo, LoginActivity.this
                                    , mCircularImg);
                        } else {
                            LogUtil.d(TAG, "initView-afterTextChanged: "
                                    + "responseCode = " + i + " ;desc = " + s);
                        }
                    }
                });
            }
        });
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

    /**
     * 初始化进度弹窗
     */
    private void initProsDialog() {
        prosDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        prosDialog.setTitleText("正在登录......");
        prosDialog.setCancelable(false);
        prosDialog.show();
        mTask = new TimerTask() {
            @Override
            public void run() {
                if (i > 6) {
                    i = 0;
                }
                i++;
                switch (i) {
                    case 0:
                        prosDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
                        break;
                    case 1:
                        prosDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorAccent));
                        break;
                    case 2:
                        prosDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorBlueDeep));
                        break;
                    case 3:
                        prosDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorGray));
                        break;
                    case 4:
                        prosDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorCoffee));
                        break;
                    case 5:
                        prosDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorRedDeep));
                        break;
                    case 6:
                        prosDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorBlue));
                        break;
                }
            }
        };
        mTimer.schedule(mTask, 800, 800);
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
            JMessageClient.login("18180586504", "123456dw", new BasicCallback() {
                @Override
                public void gotResult(int responseCode, String registerDesc) {
                    LogUtil.i("LOGIN_ACTIVITY_LMESSAGE_LOGIN",
                            "JMessageClient.register " +
                                    ", responseCode = " + responseCode +
                                    " ; registerDesc = " + registerDesc);
                }
            });
            Intent intentLogin = new Intent(LoginActivity.this, BottomBarActivity.class);
            startActivity(intentLogin);
            this.finish();
        }
        if (!TextUtils.isEmpty(mUsername.getText())) {
            if (!TextUtils.isEmpty(mPassword.getText())) {
                initProsDialog();
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
                        Toasty.error(LoginActivity.this, "网络异常").show();
                    }

                    @Override
                    public void onResponse(Response response) {
                        if (response != null) {
                            try {
                                assert response.body() != null;
                                String resBody = response.body().string();
                                LogUtil.d(TAG, "checkToLogin-CallBackUtil-onResponse: "
                                        + "response" + resBody);
                                JSONObject jsonObject = new JSONObject(resBody);
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
                                                mTimer.cancel();
                                            } else {
                                                prosDialog.dismiss();
                                                Toasty.error(LoginActivity.this, "登陆失败").show();
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
                                        Toasty.warning(LoginActivity.this, message).show();
                                    } else if (code == 404) {
                                        YoYo.with(Techniques.Shake)
                                                .duration(700)
                                                .playOn(findViewById(R.id.password_layout));
                                        Toasty.warning(LoginActivity.this, message).show();
                                    } else {
                                        LogUtil.d(TAG, "checkToLogin-CallBackUtil-onResponse: "
                                                + "code = " + code + " ;message = " + message);
                                        Toasty.error(LoginActivity.this, message).show();
                                    }
                                }
                            } catch (JSONException e) {
                                prosDialog.dismiss();
                                e.printStackTrace();
                            } catch (IOException e) {
                                prosDialog.dismiss();
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
                    Toasty.warning(this, "请输入密码").show();
                }
            }
        } else {
            if (flag == 1) {
                YoYo.with(Techniques.Shake)
                        .duration(700)
                        .playOn(findViewById(R.id.username_layout));
                Toasty.warning(this, "用户名不能为空").show();
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
                Toasty.info(this, "找回密码").show();
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
}
