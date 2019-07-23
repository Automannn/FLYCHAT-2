package com.gameex.dw.justtalk.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.activity.FlySpaceInfoActivity;
import com.gameex.dw.justtalk.soundController.VoiceSpeaker;
import com.gameex.dw.justtalk.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.cardview.widget.CardView;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import es.dmoral.toasty.Toasty;

/**
 * 飞聊空间适配器
 */
public class FlySpaceSwipeAdapter extends BaseAdapter {
    private static final String TAG = "FlySpaceSwipeAdapter";

    private Context mContext;
    private List<String> mData;
    private List<UserInfo> mUserInfos;

    public FlySpaceSwipeAdapter(Context context, List<String> data) {
        mContext = context;
        mData = data;
        mUserInfos = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"ViewHolder", "SetTextI18n"})
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null)
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.swipe_item_fly_space, viewGroup, false);
        ImageView img = view.findViewById(R.id.picture);    //图片预览
        img.setOnClickListener(view12 -> {
            Intent intent = new Intent(mContext, FlySpaceInfoActivity.class);
            intent.putExtra("userInfo_json", mUserInfos.get(i).toJson());
            mContext.startActivity(intent);
            Toasty.normal(mContext, "position = " + i).show();
        });
        TextView username = view.findViewById(R.id.username);   //用户名或用户昵称
        TextView gender = view.findViewById(R.id.gender_age);   //用户年龄
        TextView constellation = view.findViewById(R.id.constellation); //星座
        TextView career = view.findViewById(R.id.career); //职业
        Button play = view.findViewById(R.id.play);   //播放录音
        play.setOnClickListener(view1 -> Toasty.normal(mContext, "position = " + i
                + " ;username = " + mUserInfos.get(i).getUserName()).show());
        JMessageClient.getUserInfo(mData.get(i), new GetUserInfoCallback() {
            @Override
            public void gotResult(int i, String s, UserInfo userInfo) {
                if (i == 0) {
                    mUserInfos.add(userInfo);
                    Map<String, String> extras = userInfo.getExtras();
                    List<String> imgPath = JSONArray.parseArray(extras.get("picture"), String.class);
                    if (imgPath != null && imgPath.size() > 0)  //用户上传的图片的第一张
                        Glide.with(mContext).load(imgPath.get(0)).into(img);
                    else
                        Glide.with(mContext)
                                .load(mContext.getResources()
                                        .getDrawable(R.drawable.icon_img_load_fail))
                                .into(img);
                    username.setText(TextUtils.isEmpty(userInfo.getNickname()) ? userInfo.getUserName()
                            : userInfo.getNickname());  //昵称或用户名
                    switch (userInfo.getGender()) { //性别和年龄
                        case male:
                            gender.setText("♂" + " " + extras.get("age"));
                            break;
                        case female:
                            gender.setText("♀" + " " + extras.get("age"));
                            break;
                        default:
                            gender.setText("保密");
                            break;
                    }
                    constellation.setText(extras.get("constellation")); //星座
                    career.setText(extras.get("career"));   //职业
                } else LogUtil.d(TAG, "getView: " + "responseCode = " + i + " ;desc = " + s);
            }
        });
        return view;
    }
}
