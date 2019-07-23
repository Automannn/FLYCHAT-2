package com.gameex.dw.justtalk.asyncTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.Gravity;

import com.gameex.dw.justtalk.activity.AddBankCardActivity;
import com.gameex.dw.justtalk.activity.BankCardActivity;
import com.gameex.dw.justtalk.activity.ChargeActivity;
import com.gameex.dw.justtalk.entry.BankInfo;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;
import com.gameex.dw.justtalk.util.SharedPreferenceUtil;
import com.gameex.dw.justtalk.util.WindowUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Response;

import static com.gameex.dw.justtalk.activity.BankCardActivity.ACCOUNT_CARD_BIND_QUERY;

/**
 * 天付宝支付线程
 */
public class SkyPayTask extends AsyncTask {
    private static final String TAG = "SkyPayTask";

    private Activity mActivity;
    private List<BankInfo> mBankInfos = new ArrayList<>();
    private int mYuan;

    public SkyPayTask(Activity activity, int yuan) {
        mActivity = activity;
        mYuan = yuan;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String userId = (String) SharedPreferenceUtil.getData("userId", "");
        if (!TextUtils.isEmpty(userId))
            queryBindCard(userId);
        else Toasty.info(mActivity, "请稍后重试").show();
        return null;
    }


    /**
     * 查询已绑定的银行卡数据,成功后刷新列表
     *
     * @param userId 本地服务器的用户id
     */
    private void queryBindCard(String userId) {
        if (userId == null) {
            return;
        }
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("userId", userId);
        OkHttpUtil.okHttpPost(ACCOUNT_CARD_BIND_QUERY, paramsMap
                , new CallBackUtil.CallBackDefault() {
                    @Override
                    public void onFailure(Call call, Exception e) {
                        e.printStackTrace();
                        Toasty.error(mActivity, "网络异常", Toasty.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Response response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                JSONObject object = null;
                                boolean success = false;
                                try {
                                    object = new JSONObject(response.body().string());
                                    success = object.getBoolean("success");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if (success) {
                                    JSONArray data = null;
                                    try {
                                        data = object.getJSONArray("data");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    if (data == null || data.length() == 0) {
                                        Intent intent = new Intent(mActivity, AddBankCardActivity.class);
                                        intent.putExtra("goSkyPay", true);
                                        intent.putExtra("productCode", "fly_coin_" + mYuan);
                                        intent.putExtra("number",1);
                                        mActivity.startActivity(intent);
                                        mActivity.finish();
                                    } else {
                                        for (int i = 0; i < data.length(); i++) {
                                            JSONObject bank = null;
                                            try {
                                                bank = data.getJSONObject(i);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            BankInfo bankInfo = new BankInfo();
                                            assert bank != null;
                                            try {
                                                String bankNumber = bank.getString("bankCardNumber");
                                                if (!TextUtils.isEmpty(bankNumber))
                                                    bankInfo.setBankEndNum(bankNumber.substring(bankNumber.length() - 4));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            try {
                                                String name = bank.getString("bankName");
                                                if (!TextUtils.isEmpty(name))
                                                    bankInfo.setBankName(name);
                                            } catch (JSONException e) {
                                                bankInfo.setBankName("常用银行卡");
                                                e.printStackTrace();
                                            }
                                            try {
                                                String bankId = bank.getString("id");
                                                if (!TextUtils.isEmpty(bankId))
                                                    bankInfo.setBankId(bankId);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            mBankInfos.add(bankInfo);
                                        }
                                    }
                                } else {
                                    try {
                                        assert object != null;
                                        JSONObject data = object.getJSONObject("data");
                                        int code = data.getInt("code");
                                        String message = data.getString("message");
                                        LogUtil.d(TAG, "queryBindCard-onResponse: " +
                                                "code = " + code + " ;message = " + message);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toasty.error(mActivity, "服务器异常，请稍后再试").show();
                            LogUtil.d(TAG, "queryBindCard-onResponse: " + "response = " + response);
                        }
                    }
                });
    }
}
