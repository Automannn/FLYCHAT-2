package com.gameex.dw.justtalk.service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import com.gameex.dw.justtalk.activity.SignUpActivity;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.RequiresApi;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.options.RegisterOptionalUserInfo;
import cn.jpush.im.api.BasicCallback;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Response;

/**
 * 开启服务，处理与服务器之间的数据交接
 */
public class PostJsonService extends Service {
    private static final String TAG = "PostJsonService";

    public PostJsonService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        String[] signInfo = Objects.requireNonNull(intent.getExtras()).getStringArray("sign_info");
        assert signInfo != null;
        final String phone = signInfo[0];
        final String pwd = signInfo[1];
        final JSONObject json = new JSONObject();
        //申明给服务端传递一个Json串
        try {
            json.put("phoneNumber", phone);
            json.put("password", pwd);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkHttpUtil.okHttpPostJson(signInfo[2], json.toString(), new CallBackUtil.CallBackDefault() {
            @Override
            public void onFailure(Call call, Exception e) {
                LogUtil.d(TAG, "onStartCommand-onFailure: ");
                e.printStackTrace();
                Toast.makeText(PostJsonService.this, "网络异常", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Response response) {
                LogUtil.d(TAG, "onStartCommand-onResponse: " + "response = " + response);
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        boolean isSuccess = jsonObject.getBoolean("success");
                        if (isSuccess) {
                            RegisterOptionalUserInfo optional = new RegisterOptionalUserInfo();
                            Map<String, String> extras = new HashMap<>();
                            extras.put("index", DataUtil.getPinYinFirstLetter(phone));
                            optional.setExtras(extras);
                            JMessageClient.register(phone, pwd, optional, new BasicCallback() {
                                @Override
                                public void gotResult(int responseCode, String registerDesc) {
                                    LogUtil.i(TAG, "JMessageClient.register " +
                                            ", responseCode = " + responseCode +
                                            " ; registerDesc = " + registerDesc);
                                    if (responseCode == 0) {
                                        Intent intentL = new Intent();
                                        intentL.putExtra("phone", phone);
                                        SignUpActivity.sSignUpActivity.setResult(Activity.RESULT_OK, intentL);
                                        SignUpActivity.sSignUpActivity.finish();
                                        Toasty.success(SignUpActivity.sSignUpActivity, "注册成功"
                                                , Toasty.LENGTH_SHORT).show();
                                    } else {
                                        Toasty.error(SignUpActivity.sSignUpActivity, registerDesc
                                                , Toasty.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            final JSONObject jObject = jsonObject.getJSONObject("data");
                            final String message = jObject.getString("message");
                            Toast.makeText(SignUpActivity.sSignUpActivity, message + "", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
