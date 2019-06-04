package com.gameex.dw.justtalk.wxapi;

import android.app.Activity;

import com.gameex.dw.justtalk.util.LogUtil;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

import androidx.appcompat.app.AlertDialog;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
    private static final String TAG = "WXPayEntryActivity";

    @Override
    public void onReq(BaseReq req) {
    }

    @Override
    public void onResp(BaseResp resp) {
        LogUtil.d(TAG, "onResp: " + "resp = " + resp.toString());
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            LogUtil.d(TAG, "onPayFinish,errCode=" + resp.errCode);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            ;
            builder.setTitle("Title WXAPI");
        }
    }
}
