package com.gameex.dw.justtalk.fragment;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.activity.FlySpaceActivity;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.LocationUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;

public class FlySpaceFragment extends Fragment {
    private static final String TAG = "FlySpaceFragment";
    /**
     * 搜索用户
     */
    private static final String SEARCH_USER_PATH = "user/seacherUser";

    private DecimalFormat format = new DecimalFormat("#.000000");

    @OnClick(R.id.start_pitch)
    void onClick() {
        LocationUtil.getInstance(getContext()).getLngAndLat(new LocationUtil.OnLocationResultListener() {
            @Override
            public void onLocationResult(Location location) {
                String longitudeStr = format.format(location.getLongitude()); //将经度保留6位小数
                double longitude = Double.valueOf(longitudeStr);  //经度
                String latitudeStr = format.format(location.getLatitude());  //将纬度保留6位小数
                double latitude = Double.valueOf(latitudeStr);    //纬度
                JSONObject params = new JSONObject();
                try {
                    params.put("lng", longitude);
                    params.put("lat", latitude);
                    params.put("mobile", JMessageClient.getMyInfo().getUserName());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new Thread(() -> searchUser(params)).start();
            }

            @Override
            public void OnLocationChange(Location location) {

            }
        });
    }

    public static FlySpaceFragment newInstance() {
        FlySpaceFragment fragment = new FlySpaceFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fly_space, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    private void searchUser(JSONObject paramsMap) {
        OkHttpUtil.okHttpPostJson(SEARCH_USER_PATH, paramsMap.toString(), new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                e.printStackTrace();
                Toasty.error(Objects.requireNonNull(getContext()), "网络连接错误").show();
            }

            @Override
            public void onResponse(String response) {
                List<String> user = JSONArray.parseArray(response, String.class);
                Intent intent = new Intent(getContext(), FlySpaceActivity.class);
                intent.putStringArrayListExtra("user_list", (ArrayList<String>) user);
                startActivity(intent);
            }
        });
    }
}
