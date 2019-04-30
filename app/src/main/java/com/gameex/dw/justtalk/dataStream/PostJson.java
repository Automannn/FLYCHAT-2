package com.gameex.dw.justtalk.dataStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.gameex.dw.justtalk.signUp.SignUpActivity;
import com.gameex.dw.justtalk.util.LogUtil;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 向服务器发送注册信息
 */
public class PostJson extends AsyncTask {
    private static final String TAG = "POST_JSON";

    private String phone, pwd, url;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;

    public PostJson() {
    }

    PostJson(String phone, String pwd, String url, Context context) {
        this.phone = phone;
        this.pwd = pwd;
        this.url = url;
        this.mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        JSONObject json = new JSONObject();
        //申明给服务端传递一个Json串
        try {
            json.put("phoneNumber", phone);
            json.put("password", pwd);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //创建一个OkhttpClient对象
        OkHttpClient client = new OkHttpClient();
        //json为String类型的Json数据；RequestBody(数据类型,传递的json串)
        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                String string = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(string);
                    boolean isSuccess = jsonObject.getBoolean("success");
                    if (isSuccess) {
                        Toast.makeText(SignUpActivity.sSignUpActivity, "注册成功", Toast.LENGTH_SHORT).show();
                        JMessageClient.register(phone, pwd, new BasicCallback() {
                            @Override
                            public void gotResult(int responseCode, String registerDesc) {
                                LogUtil.i("JMESSAGE", "JMessageClient.register " + ", responseCode = " + responseCode + " ; registerDesc = " + registerDesc);
                            }
                        });
                        Intent intent = new Intent();
                        intent.putExtra("phone", phone);
                        SignUpActivity.sSignUpActivity.setResult(Activity.RESULT_OK, intent);
                        SignUpActivity.sSignUpActivity.finish();
                    } else {
                        final JSONObject jObject = jsonObject.getJSONObject("data");
                        final String message = jObject.getString("message");
                        Toast.makeText(SignUpActivity.sSignUpActivity, message + "", Toast.LENGTH_SHORT).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        Toast.makeText(mContext, "注册成功", Toast.LENGTH_SHORT).show();
    }
}
