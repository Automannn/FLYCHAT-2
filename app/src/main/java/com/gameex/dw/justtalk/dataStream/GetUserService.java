package com.gameex.dw.justtalk.dataStream;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import com.gameex.dw.justtalk.ObjPack.User;
import com.gameex.dw.justtalk.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetUserService extends Service {
    private static String url = "http://117.50.57.86:8060/user/queryAll";

    public GetUserService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new GetAllUserTask().execute();
        return super.onStartCommand(intent, flags, startId);
    }

    class GetAllUserTask extends AsyncTask {

        public GetAllUserTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Call call = client.newCall(request);
            try {
                Response response = call.execute();
                String userDataStr = response.body().string();
                JSONObject jsonObject = new JSONObject(userDataStr);
                int status = jsonObject.getInt("isStatus");
                if (status == 0) {
                    LogUtil.d("STATUS", "0");
                } else if (status == -2) {
                    final String message = jsonObject.getString("message");
                    LogUtil.d("MESSAGE", message + "");
                } else {
                    LogUtil.d("STATUS", status + "");
                }
                JSONArray array = jsonObject.getJSONArray("data");
                List<User> users = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    String username = object.getString("username");
                    String password = object.getString("password");
                    String phone = object.getString("phoneNumer");
                    users.add(new User(username, password, phone));
                    LogUtil.e("USER_INFO", "用户名" + username + "密码" + password + "电话" + phone);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
        }
    }
}
