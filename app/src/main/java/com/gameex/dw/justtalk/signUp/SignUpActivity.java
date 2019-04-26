package com.gameex.dw.justtalk.signUp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.dataStream.PostJsonService;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.util.BarUtil;

/**
 * 注册activity
 */
public class SignUpActivity extends BaseActivity implements View.OnClickListener {
    public static SignUpActivity sSignUpActivity; //上下文参数

    //服务器注册接口
    private static final String url = "http://117.50.57.86:8060/user/register";

    private EditText mPhone, mPwd;
    private Button mSignUp;
    private Intent postJsonSer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        sSignUpActivity = this;

//        BarUtil.setHalfTransBar(this);    //沉侵式状态栏和虚拟键->半透明
        BarUtil.setFullTransBar(this);  //设置全透明状态栏和虚拟键

        initView();
    }

    /**
     * ui绑定id，设置监听器
     */
    private void initView() {
        mPhone = findViewById(R.id.phone_edit);
        mPwd = findViewById(R.id.pwd_edit);
        mSignUp = findViewById(R.id.sign_up_btn);
        mSignUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_up_btn:
                postJsonSer = new Intent(this, PostJsonService.class);
                postJsonSer.putExtra("sign_info", new String[]{
                        mPhone.getText().toString(), mPwd.getText().toString(), url});
                startService(postJsonSer);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (postJsonSer != null) {
            stopService(postJsonSer);
        }
    }
}
