package com.gameex.dw.justtalk.dataStream;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.gameex.dw.justtalk.signUp.SignUpActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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

    private String phone, pwd, url;
    private Context mContext;

    public PostJson() {
    }

    public PostJson(String phone, String pwd, String url, Context context) {
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
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().toString();
                if (string != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(string);
                        int status = jsonObject.getInt("status");
                        if (status == 0) {
                            Toast.makeText(SignUpActivity.sSignUpActivity, "status=0", Toast.LENGTH_SHORT).show();
                        } else if (status == -2) {
                            final String message = jsonObject.getString("message");
                            Toast.makeText(SignUpActivity.sSignUpActivity, message + "", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUpActivity.sSignUpActivity, "status=" + status, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        Toast.makeText(mContext, "注册成功", Toast.LENGTH_SHORT).show();
    }
}
