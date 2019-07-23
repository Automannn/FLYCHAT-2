package com.gameex.dw.justtalk.activity;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.im.android.api.model.UserInfo;
import es.dmoral.toasty.Toasty;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.donkingliang.labels.LabelsView;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.soundController.VoiceSpeaker;
import com.gameex.dw.justtalk.tools.GlideImageLoader;
import com.gameex.dw.justtalk.util.BarUtil;
import com.youth.banner.Banner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 飞聊空间信息展示界面
 */
public class FlySpaceInfoActivity extends BaseActivity {
    private static final String TAG = "FlySpaceInfoActivity";

    /**
     * 图集
     */
    @BindView(R.id.banner)
    Banner mBanner;
    /**
     * 用户名或昵称、性别和年龄、星座、职业或距离和最近在线时间、个性签名
     */
    @BindViews({R.id.username, R.id.gender_age, R.id.constellation, R.id.career, R.id.signature})
    TextView[] mTextViews;
    /**
     * 运动、美食、音乐、电影
     */
    @BindViews({R.id.sport, R.id.food, R.id.music, R.id.movies})
    LabelsView[] mLabelsViews;

    /**
     * 播放点击监听
     */
    @OnClick(R.id.play)
    void onClick() {
        String voice = extras.get("voice");
        if (TextUtils.isEmpty(voice)) Toasty.info(this, "此用户还未上传自述").show();
        else VoiceSpeaker.getInstance().speakSingle(voice);
    }

    private Map<String, String> extras;
    private List<String> mImgUrl = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fly_space_info);
        ButterKnife.bind(this);
        initData();
        BarUtil.setStatusBarColor(this, getResources().getColor(R.color.colorPrimary));
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        Intent intent = getIntent();
        UserInfo userInfo = UserInfo.fromJson(intent.getStringExtra("userInfo_json"));
        extras = userInfo.getExtras();
        mImgUrl = JSON.parseArray(extras.get("images"), String.class);
        mBanner.setImageLoader(new GlideImageLoader());
        mBanner.setImages(mImgUrl);
        //用户名或昵称
        mTextViews[0].setText(TextUtils.isEmpty(userInfo.getNickname()) ? userInfo.getUserName() : userInfo.getNickname());
        String gender;
        switch (userInfo.getGender()) {
            case male:
                gender = "男 ";
                break;
            case female:
                gender = "女 ";
                break;
            default:
                gender = "保密 ";
                break;
        }
        mTextViews[1].setText(gender + extras.get("age"));  //性别和年龄
        mTextViews[2].setText(extras.get("constellation")); //星座
        mTextViews[3].setText(extras.get("career"));    //职业或距离和最近在线时间
        mTextViews[4].setText(userInfo.getSignature()); //个性签名
        //运动标签
        List<String> sports = JSON.parseArray(extras.get("sports"), String.class);
        mLabelsViews[0].setLabels(sports);
        //美食标签
        List<String> foods = JSON.parseArray(extras.get("foods"), String.class);
        mLabelsViews[1].setLabels(foods);
        //音乐标签
        List<String> musics = JSON.parseArray(extras.get("musics"), String.class);
        mLabelsViews[2].setLabels(musics);
        //电影标签
        List<String> movies = JSON.parseArray(extras.get("movies"), String.class);
        mLabelsViews[0].setLabels(movies);
    }
}
