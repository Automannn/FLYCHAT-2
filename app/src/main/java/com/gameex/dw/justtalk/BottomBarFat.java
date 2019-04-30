package com.gameex.dw.justtalk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.ObjPack.Contact;
import com.gameex.dw.justtalk.userInfo.SettingActivity;
import com.gameex.dw.justtalk.userInfo.UserInfoActivity;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.gjiazhe.wavesidebar.WaveSideBar;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;
import com.yzq.zxinglibrary.encode.CodeCreator;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.UserInfo;

public class BottomBarFat extends Fragment implements View.OnClickListener {
    public static final int REQUEST_CODE_SCAN = 131;
    private static final String ARG_PARAM = "flag";
    private static final String UPDATE_USER_INFO = "com.gameex.dw.flychat.UPDATE_USER";
    private static String[] indexStr = new String[]{"↑", "A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};

    @SuppressLint("StaticFieldLeak")
    private static RecyclerView mRecView, mContactRec;
    @SuppressLint("StaticFieldLeak")
    private static DashAdapter mAdapter;
    @SuppressLint("StaticFieldLeak")
    private static ContactAdapter mContactAdapter;
    private String param;
    private View mView;
    private List<Object[]> mMsgInfo;
    private List<Contact> mContacts;
    private UpdateFragmentReceiver mFragmentReceiver;

    private static CircularImageView userIcon;
    @SuppressLint("StaticFieldLeak")
    private static TextView userName;

    private Dialog mQRCodeDialog;

    public BottomBarFat() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            param = getArguments().getString(ARG_PARAM);
        }
        mFragmentReceiver = new UpdateFragmentReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_USER_INFO);
        BottomBarActivity.sBottomBarActivity.registerReceiver(mFragmentReceiver, filter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        switch (param) {
            case "0":
                mView = inflater.inflate(R.layout.message_layout, container, false);
                return mView;
            case "1":
                mView = inflater.inflate(R.layout.contact_layout, container, false);
                return mView;
            case "2":
                mView = inflater.inflate(R.layout.mine_layout, container, false);
                return mView;
            default:
                return null;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    /**
     * 获取相机权限
     */
    private void requestPermission() {

        if (ContextCompat.checkSelfPermission(BottomBarActivity.sBottomBarActivity,
                Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(BottomBarActivity.sBottomBarActivity, new String[]{
                    Manifest.permission.CAMERA}, 1);
        } else {
            Intent intentScan = new Intent(BottomBarActivity.sBottomBarActivity, CaptureActivity.class);
            ZxingConfig zxingConfig = new ZxingConfig();    //配置扫一扫界面属性
            zxingConfig.setReactColor(R.color.colorAccent); //设置扫描框四个角颜色
            zxingConfig.setScanLineColor(R.color.colorAccent);  //设置扫描线的颜色
            intentScan.putExtra(Constant.INTENT_ZXING_CONFIG, zxingConfig);
            startActivityForResult(intentScan, REQUEST_CODE_SCAN);  //跳转到扫一扫界面
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(BottomBarActivity.sBottomBarActivity, "扫描", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intentScan = new Intent(BottomBarActivity.sBottomBarActivity, CaptureActivity.class);
                    ZxingConfig zxingConfig = new ZxingConfig();    //配置扫一扫界面属性
                    zxingConfig.setReactColor(R.color.colorAccent); //设置扫描框四个角颜色
                    zxingConfig.setScanLineColor(R.color.colorAccent);  //设置扫描线的颜色
                    intentScan.putExtra(Constant.INTENT_ZXING_CONFIG, zxingConfig);
                    startActivityForResult(intentScan, REQUEST_CODE_SCAN);  //跳转到扫一扫界面
                }
                break;
            default:
                break;
        }
    }

    /**
     * 根据param绑定id，设置监听，加载ui
     */
    private void initView() {
        switch (param) {
            case "0":
                DefaultItemAnimator animator = new DefaultItemAnimator();
                animator.setAddDuration(500);
                animator.setChangeDuration(500);
                animator.setMoveDuration(500);
                animator.setRemoveDuration(500);
                mRecView = mView.findViewById(R.id.recycler_dash);
                mRecView.setItemAnimator(animator);
                mRecView.setLayoutManager(new LinearLayoutManager(BottomBarActivity.sBottomBarActivity));
//                mRecView.addItemDecoration(new DividerItemDecoration(BottomBarActivity.sBottomBarActivity,
//                        DividerItemDecoration.VERTICAL));
                mMsgInfo = getMsgInfo();
                mAdapter = new DashAdapter(mMsgInfo, BottomBarActivity.sBottomBarActivity);
                mRecView.setAdapter(mAdapter);
                break;
            case "1":
                DefaultItemAnimator animatorContact = new DefaultItemAnimator();
                animatorContact.setAddDuration(500);
                animatorContact.setChangeDuration(500);
                animatorContact.setMoveDuration(500);
                animatorContact.setRemoveDuration(500);
                mContactRec = mView.findViewById(R.id.contact_recycler);
                mContactRec.setItemAnimator(animatorContact);
                mContactRec.setLayoutManager(new LinearLayoutManager(BottomBarActivity.sBottomBarActivity));
                mContacts = getContacts();
                mContactAdapter = new ContactAdapter(BottomBarActivity.sBottomBarActivity, mContacts);
                mContactRec.setAdapter(mContactAdapter);
                WaveSideBar indexBar = mView.findViewById(R.id.glide_side_bar);
                indexBar.setIndexItems(indexStr);
                indexBar.setOnSelectIndexItemListener(new WaveSideBar.OnSelectIndexItemListener() {
                    @Override
                    public void onSelectIndexItem(String index) {
                        for (int i = 0; i < mContacts.size(); i++) {
                            if (mContacts.get(i).getIndex().equals(index)) {
                                RecScrollHelper.scrollToPosition(mContactRec, i);
                                return;
                            }
                        }
                    }
                });
                break;
            case "2":
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
                break;
            default:
                break;
        }
    }

    /**
     * 飞聊界面模拟信息
     *
     * @return 未读信息类集合
     */
    private List<Object[]> getMsgInfo() {
        List<Object[]> msgInfo = new ArrayList<>();
        UserInfo myInfo = JMessageClient.getMyInfo();
        if (myInfo.getUserName().equals("17361060489")) {
            msgInfo.add(new Object[]{R.drawable.icon_user,
                    "18180027763", "最近的一条信息展示位", DataUtil.getCurrentDateStr()});
        } else {
            msgInfo.add(new Object[]{R.drawable.icon_user,
                    "17361060489", "最近的一条信息展示位", DataUtil.getCurrentDateStr()});
        }
        String[] name = new String[]{"德泽", "超海", "suhdanciakk**（jd", "郝世界第九才能哈光", "范明", "千帆", "怀曼", "香山", "了双", "吉萨嗲花",
                "蝴蝶卡", "德泽", "涉及到", "的哈韩*^%的jj的开始", "书店", "多说句", "江西", "大祭司", "的健康", "熽",};
        for (int i = 0; i < 20; i++) {
            msgInfo.add(new Object[]{R.drawable.icon_user,
                    name[i],
                    "最后&（&一条信息通知dsdads觉得垃圾呢&（&*",
                    "12月30号"});
        }
        return msgInfo;
    }

    /**
     * 加入基础信息，并添加模拟信息
     *
     * @return 联系人集合
     */
    private List<Contact> getContacts() {
        List<Contact> contacts = new ArrayList<>(Contact.getBasicContact());
        contacts.add(new Contact("A", R.drawable.icon_user, "联系人A1"));
        contacts.add(new Contact("A", R.drawable.icon_user, "联系人A2"));
        contacts.add(new Contact("A", R.drawable.icon_user, "联系人A3"));
        contacts.add(new Contact("B", R.drawable.icon_user, "联系人B1"));
        contacts.add(new Contact("B", R.drawable.icon_user, "联系人B2"));
        contacts.add(new Contact("B", R.drawable.icon_user, "联系人B3"));
        contacts.add(new Contact("C", R.drawable.icon_user, "联系人C1"));
        contacts.add(new Contact("D", R.drawable.icon_user, "联系人D1"));
        contacts.add(new Contact("D", R.drawable.icon_user, "联系人D2"));
        contacts.add(new Contact("D", R.drawable.icon_user, "联系人D3"));
        contacts.add(new Contact("E", R.drawable.icon_user, "联系人E1"));
        contacts.add(new Contact("E", R.drawable.icon_user, "联系人E2"));
        contacts.add(new Contact("E", R.drawable.icon_user, "联系人E3"));
        contacts.add(new Contact("E", R.drawable.icon_user, "联系人E4"));
        contacts.add(new Contact("E", R.drawable.icon_user, "联系人E5"));
        contacts.add(new Contact("E", R.drawable.icon_user, "联系人E6"));
        contacts.add(new Contact("E", R.drawable.icon_user, "联系人E7"));
        contacts.add(new Contact("G", R.drawable.icon_user, "联系人G1"));
        contacts.add(new Contact("J", R.drawable.icon_user, "联系人J1"));
        contacts.add(new Contact("O", R.drawable.icon_user, "联系人O1"));
        contacts.add(new Contact("Z", R.drawable.icon_user, "联系人Z1"));
        contacts.add(new Contact("#", R.drawable.icon_user, "联系人#1"));
        return contacts;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BottomBarActivity.sBottomBarActivity.unregisterReceiver(mFragmentReceiver);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mine_info_layout:
                Intent intentUserInfo = new Intent(BottomBarActivity.sBottomBarActivity, UserInfoActivity.class);
                startActivity(intentUserInfo);
                break;
            case R.id.qr_code_img:
                /*
                contentEtString:字符串内容
                w:图片宽
                h:图片高
                logo:不需要的话，直接传空
                 */
                Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.icon_user);
                Bitmap qrCodeBit = CodeCreator.createQRCode("www.baidu.com", 400, 400, logo);
                showQrDialog(qrCodeBit);
                break;
            case R.id.loose_change_layout:
                Toast.makeText(BottomBarActivity.sBottomBarActivity, "查看钱包信息", Toast.LENGTH_SHORT).show();
                break;
            case R.id.fly_chat_store_layout:
                Toast.makeText(BottomBarActivity.sBottomBarActivity, "进入商城", Toast.LENGTH_SHORT).show();
                break;
            case R.id.scan_layout:
                requestPermission();
                break;
            case R.id.my_favorite_layout:
                Toast.makeText(BottomBarActivity.sBottomBarActivity, "查看收藏条目", Toast.LENGTH_SHORT).show();
                break;
            case R.id.setting_layout:
                Intent intentSetting = new Intent(BottomBarActivity.sBottomBarActivity, SettingActivity.class);
                startActivity(intentSetting);
                break;
            case R.id.online_service_layout:
                Toast.makeText(BottomBarActivity.sBottomBarActivity, "请求客服", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    /**
     * 二维码展示图片
     *
     * @param bitmap 二维码
     */
    private void showQrDialog(Bitmap bitmap) {
        mQRCodeDialog = new Dialog(BottomBarActivity.sBottomBarActivity, R.style.qr_code_dialog_style);
        mQRCodeDialog.setContentView(R.layout.qr_code_dialog);
        ImageView qrImg = mQRCodeDialog.findViewById(R.id.qr_code_img_dialog);
        qrImg.setImageBitmap(bitmap);
        mQRCodeDialog.setCanceledOnTouchOutside(true);
        Window window = mQRCodeDialog.getWindow();
        assert window != null;
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 40;
        mQRCodeDialog.onWindowAttributesChanged(params);
        mQRCodeDialog.show();
    }

    /**
     * fragment刷新广播
     */
    class UpdateFragmentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UPDATE_USER_INFO:
                    try {
                        Uri uri = Uri.parse(intent.getStringExtra("icon_uri"));
                        Glide.with(BottomBarActivity.sBottomBarActivity)
                                .load(uri)
                                .into(userIcon);
                    } catch (NullPointerException npe) {
                        npe.printStackTrace();
                    }
                    String username = intent.getStringExtra("username");
                    userName.setText(username);
                    break;
                default:
                    break;
            }
        }
    }

}
