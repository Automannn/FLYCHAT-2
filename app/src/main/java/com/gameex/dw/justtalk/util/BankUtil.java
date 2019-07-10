package com.gameex.dw.justtalk.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 银行卡，绑定情况查询工具类
 */
public class BankUtil {
    /**
     * 绑卡情况查询路径
     */
    private static final String ACCOUNT_CARD_BIND_QUERY = "/account/cardbind/query";

    /**
     * 查询已绑定的银行卡数据,成功后刷新列表
     *
     * @param userId 本地服务器的用户id
     */
    public static void queryBindCard(String userId) {
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
//                        LogUtil.d(TAG, "queryBindCard-onFailure: ");
                    }

                    @Override
                    public void onResponse(Response response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                JSONObject object = new JSONObject(response.body().string());
                                boolean success = object.getBoolean("success");
                                if (success) {
                                    JSONArray data = object.getJSONArray("data");
//                                    updateRec(data);
                                } else {
                                    JSONObject data = object.getJSONObject("data");
                                    int code = data.getInt("code");
                                    String message = data.getString("message");
//                                    LogUtil.d(TAG, "queryBindCard-onResponse: " +
//                                            "code = " + code + " ;message = " + message);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }
}
