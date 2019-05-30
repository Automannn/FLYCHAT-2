package com.gameex.dw.justtalk.userInfo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.managePack.BaseActivity;

import static com.gameex.dw.justtalk.userInfo.UserInfoActivity.EDIT_NICK_REQUEST_CODE;

public class EditMyInfoActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "EditMyInfoActivity";

    private TextView mTitle;
    /**
     * 昵称
     */
    private EditText mNick;
    /**
     * 个性签名
     */
    private EditText mSignature;
    /**
     * 完成
     */
    private Button mDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initData();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        setContentView(R.layout.activity_edit_my_info);
        ImageView back = findViewById(R.id.back); //返回
        back.setOnClickListener(this);
        mTitle = findViewById(R.id.title);
        mNick = findViewById(R.id.nick_name);
        mSignature = findViewById(R.id.signature);
        mSignature.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Toast.makeText(EditMyInfoActivity.this, editable.length() + ""
                        , Toast.LENGTH_SHORT).show();
            }
        });
        mDone = findViewById(R.id.done);
        mDone.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        mTitle.setText(title);
        switch (title) {
            case "昵称":
                mNick.setVisibility(View.VISIBLE);
                mNick.setText(intent.getStringExtra("my_nick"));
                YoYo.with(Techniques.FadeInLeft)
                        .duration(700)
                        .playOn(mNick);
                break;
            case "个性签名":
                mSignature.setVisibility(View.VISIBLE);
                mSignature.setText(intent.getStringExtra("my_signature"));
                YoYo.with(Techniques.FadeInLeft)
                        .duration(700)
                        .playOn(mSignature);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.done:
                switch (mTitle.getText().toString()) {
                    case "昵称":
                        intent.putExtra("my_nick", mNick.getText().toString());
                        break;
                    case "个性签名":
                        intent.putExtra("my_signature", mSignature.getText().toString());
                        break;
                    default:
                        break;
                }
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }
}
