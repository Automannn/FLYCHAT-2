package com.gameex.dw.justtalk.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.activity.BottomBarActivity;
import com.gameex.dw.justtalk.activity.ChargeActivity;
import com.gameex.dw.justtalk.activity.SettingActivity;
import com.gameex.dw.justtalk.activity.ShopWebActivity;
import com.gameex.dw.justtalk.activity.UserBasicInfoActivity;
import com.gameex.dw.justtalk.activity.UserInfoActivity;
import com.gameex.dw.justtalk.util.DialogUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.UpdateApkUtil;
import com.gameex.dw.justtalk.util.UserInfoUtils;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;

/**
 * 我的fragment
 */
public class MyInfoFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "MyInfoFragment";
    /**
     * 更新用户信息action
     */
    public static final String UPDATE_USER_INFO =
            "com.gameex.dw.flychat.MyInfoFragment.UPDATE_USER";
    /**
     * 扫码结果接收码
     */
    public static final int REQUEST_CODE_SCAN = 131;

    private View mView;
    private CircularImageView userIcon;
    private TextView userName;
    private UserInfo mUserInfo;
    private Dialog mQRCodeDialog;
    private UpdateMyInfoReceiver mReceiver;
    private UpdateApkUtil mApkUtil;

    public static MyInfoFragment getInstance(String userInfoJson) {
        Bundle bundle = new Bundle();
        bundle.putString("msg", userInfoJson);
        MyInfoFragment fragment = new MyInfoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Bundle bundle = getArguments();
        if (bundle != null) {
            JMessageClient.getUserInfo(UserInfo.fromJson(bundle.getString("msg")).getUserName()
                    , new GetUserInfoCallback() {
                        @Override
                        public void gotResult(int i, String s, UserInfo userInfo) {
                            if (i == 0) {
                                mUserInfo = userInfo;
                            } else {
                                LogUtil.d(TAG, "onAttach: " + "responseCode = " + i + " ;desc = " + s);
                            }
                        }
                    });
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new UpdateMyInfoReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_USER_INFO);
        Objects.requireNonNull(getActivity()).registerReceiver(mReceiver, filter);
        mApkUtil = new UpdateApkUtil(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container
            , @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_mine, container, false);
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(getActivity()).unregisterReceiver(mReceiver);
        mApkUtil.cancelUpdate();
    }

    /**
     * 获取相机权限
     */
    @SuppressLint("CheckResult")
    private void requestPermission() {
        new RxPermissions(Objects.requireNonNull(getActivity()))
                .request(Manifest.permission.CAMERA)
                .subscribe(granted -> {
                    if (granted) {
                        Intent intentScan = new Intent(BottomBarActivity.sBottomBarActivity, CaptureActivity.class);
                        ZxingConfig zxingConfig = new ZxingConfig();    //配置扫一扫界面属性
                        zxingConfig.setReactColor(R.color.colorAccent); //设置扫描框四个角颜色
                        zxingConfig.setScanLineColor(R.color.colorAccent);  //设置扫描线的颜色
                        intentScan.putExtra(Constant.INTENT_ZXING_CONFIG, zxingConfig);
                        startActivityForResult(intentScan, REQUEST_CODE_SCAN);  //跳转到扫一扫界面
                    } else {
                        Toast.makeText(getActivity()
                                , "Denied permission", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 初始化用户信息(我 标签页信息)
     */
    private void initDataOfMine() {
        if (mUserInfo == null)
            mUserInfo = JMessageClient.getMyInfo();
        UserInfoUtils.initUserIcon(mUserInfo, BottomBarActivity.sBottomBarActivity
                , userIcon);
        userName.setText(TextUtils.isEmpty(mUserInfo.getNickname())
                ? mUserInfo.getUserName() : mUserInfo.getNickname());
    }

    /**
     * 初始化布局
     */
    private void initView() {
        userIcon = mView.findViewById(R.id.circle_img_user);
        userName = mView.findViewById(R.id.mine_name);
        ImageView qrCode = mView.findViewById(R.id.qr_code_img);
        qrCode.setOnClickListener(this);
        RelativeLayout mineInfoLayout, flyChatStoreLayout, looseChangeLayout,
                scanLayout, favoriteLayout, settingLayout, onlineServiceLayout;
        mineInfoLayout = mView.findViewById(R.id.mine_info_layout);
        mineInfoLayout.setOnClickListener(this);
        flyChatStoreLayout = mView.findViewById(R.id.fly_chat_store_layout);
        flyChatStoreLayout.setOnClickListener(this);
        looseChangeLayout = mView.findViewById(R.id.loose_change_layout);
        looseChangeLayout.setOnClickListener(this);
        scanLayout = mView.findViewById(R.id.scan_layout);
        scanLayout.setOnClickListener(this);
        favoriteLayout = mView.findViewById(R.id.my_favorite_layout);
        favoriteLayout.setOnClickListener(this);
        settingLayout = mView.findViewById(R.id.setting_layout);
        settingLayout.setOnClickListener(this);
        onlineServiceLayout = mView.findViewById(R.id.online_service_layout);
        onlineServiceLayout.setOnClickListener(this);
        new Handler().postDelayed(this::initDataOfMine, 200);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.mine_info_layout://我的详细信息
                if (mUserInfo == null)
                    mUserInfo = JMessageClient.getMyInfo();
                intent.setClass(BottomBarActivity.sBottomBarActivity, UserInfoActivity.class);
                intent.putExtra("mine_info", mUserInfo.toJson());
                startActivity(intent);
                break;
            case R.id.qr_code_img://我的二维码
                if (mUserInfo == null) return;
                mUserInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int i, String s, Bitmap bitmap) {
                        if (i == 0) DialogUtil.showQrDialog(getActivity()
                                , mUserInfo.getUserName(), bitmap);
                        else DialogUtil.showQrDialog(getActivity()
                                , mUserInfo.getUserName(), null);
                    }
                });
                break;
            case R.id.loose_change_layout://零钱
                intent.setClass(Objects.requireNonNull(getActivity()), ChargeActivity.class);
                startActivity(intent);
                break;
            case R.id.fly_chat_store_layout://商城
//                mApkUtil.sendRequest(); //检测是否有新版本
                intent.setClass(Objects.requireNonNull(getContext()), ShopWebActivity.class);
                startActivity(intent);
                break;
            case R.id.scan_layout://扫一扫
                requestPermission();
                break;
            case R.id.my_favorite_layout://收藏
                Toasty.info(Objects.requireNonNull(getActivity()), "敬请期待"
                        , Toasty.LENGTH_SHORT).show();
                break;
            case R.id.setting_layout://设置
                intent.setClass(BottomBarActivity.sBottomBarActivity, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.online_service_layout://客服
                Toasty.info(Objects.requireNonNull(getActivity()), "敬请期待"
                        , Toasty.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //接收扫描结果
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                Intent intent = new Intent();
//                if (content.matches(TELEPHONE_REGEX)){
                intent.setClass(Objects.requireNonNull(getContext()), UserBasicInfoActivity.class);
                intent.putExtra("username", content);
//                }else{
//                    intent.setClass(BottomBarActivity.this,UserBasicInfoActivity.class);
//                    intent.putExtra("groupid",content);
//                }
                startActivity(intent);
//                WindowUtil.openBrowser(Objects.requireNonNull(getActivity()), content);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class UpdateMyInfoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            if (UPDATE_USER_INFO.equals(action)) {
                if (mUserInfo == null)
                    mUserInfo = JMessageClient.getMyInfo();
                UserInfoUtils.initUserIcon(mUserInfo, BottomBarActivity.sBottomBarActivity
                        , userIcon);
                userName.setText(TextUtils.isEmpty(mUserInfo.getNickname())
                        ? mUserInfo.getUserName() : mUserInfo.getNickname());
            }
        }
    }
}
