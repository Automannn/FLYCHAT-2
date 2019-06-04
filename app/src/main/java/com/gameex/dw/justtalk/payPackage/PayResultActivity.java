package com.gameex.dw.justtalk.payPackage;

import android.animation.Animator;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.managePack.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PayResultActivity extends BaseActivity {

    @OnClick(R.id.back)
    public void back() {
        finish();
    }

    /**
     * 界面展示的图片
     */
    @BindView(R.id.check_circle) ImageView mCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_result);
        //绑定初始化butterKnife
        ButterKnife.bind(this);
        YoYo.with(Techniques.SlideInUp)
                .duration(700)
                .onEnd(animator -> new Handler().postDelayed(PayResultActivity.this::finish,100))
                .playOn(mCheck);
    }
}
