package com.gameex.dw.justtalk.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import com.github.siyamed.shapeimageview.CircularImageView;

/**
 * 登录activity
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private CircularImageView mCircularImg;
    private EditText mUsername, mPassword;
    private Button mLoginBtn;
    private CheckBox mAutoLogin;
    private TextView mQuickSign, mForgotPwd;

    private Intent mIntentGetUser;

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
            checkToLogin();
        } else {
            if (getIntent().getStringExtra("flag").equals("LoginOut")) {
                inputLogin();
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
        }
    }

    /**
     * 验证登录
     */
    private void checkToLogin() {
        if (!mUsername.getText().toString().isEmpty()) {
            String account = mUsername.getText().toString();
            String password = mPassword.getText().toString();
            if (mUsername.getText().toString().equals("小飞侠")) {
                if (mPassword.getText().toString().equals("flyxia.cn")) {
                    editor = pref.edit();
                    if (mAutoLogin.isChecked()) {
                        editor.putBoolean("remember_password", true);
                        editor.putString("account", account);
                        editor.putString("password", password);
                    } else {
                        editor.clear();
                    }
                    editor.apply();
                    Intent intentLogin = new Intent(this, BottomBarActivity.class);
                    startActivity(intentLogin);
                    finish();
                } else {
                    YoYo.with(Techniques.Shake)
                            .duration(500)
                            .playOn(findViewById(R.id.password_layout));
                    Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
                }
            } else {
                YoYo.with(Techniques.Shake)
                        .duration(700)
                        .playOn(findViewById(R.id.username_layout));
                Toast.makeText(this, "用户名不存在", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_btn:
                startService(mIntentGetUser);
                checkToLogin();
                break;
            case R.id.quick_sign_up:
                Intent intentQuickSign = new Intent(this, SignUpActivity.class);
                startActivity(intentQuickSign);
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
    protected void onDestroy() {
        super.onDestroy();
        stopService(mIntentGetUser);
    }
}
