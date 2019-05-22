package com.gameex.dw.justtalk.payPackage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;

public class AddBankCardActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AddBankCardActivity";
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
                mNext.setAlpha(1f);
                Toast.makeText(AddBankCardActivity.this, editable + "", Toast.LENGTH_SHORT).show();
            }
        });
        mNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.next_step:
                intent.setClass(this, VerifyIdentityActivity.class);
                startActivity(intent);
                break;
        }
    }
}
