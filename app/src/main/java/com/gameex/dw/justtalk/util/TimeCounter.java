package com.gameex.dw.justtalk.util;

import android.os.CountDownTimer;
import android.widget.TextView;

public class TimeCounter extends CountDownTimer {

    private TextView mCountNum;

    public TimeCounter(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    public TimeCounter(long millisInFuture, long countDownInterval, TextView countNum) {
        super(millisInFuture, countDownInterval);
        this.mCountNum=countNum;
    }

    @Override
    public void onTick(long l) {
        mCountNum.setEnabled(false);
        mCountNum.setText(String.format("%.1f",l/1000.0)+" 秒");
    }

    @Override
    public void onFinish() {
        mCountNum.setEnabled(true);
        mCountNum.setText("获取验证码");
    }
}
