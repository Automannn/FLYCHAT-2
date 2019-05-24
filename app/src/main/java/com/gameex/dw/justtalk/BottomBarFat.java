package com.gameex.dw.justtalk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.gameex.dw.justtalk.objPack.Contact;
import com.gameex.dw.justtalk.objPack.MsgInfo;
import com.gameex.dw.justtalk.payPackage.ChangeActivity;
import com.gameex.dw.justtalk.publicInterface.FragmentCallBack;
import com.gameex.dw.justtalk.userInfo.SettingActivity;
import com.gameex.dw.justtalk.userInfo.UserInfoActivity;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.UserInfoUtils;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.gjiazhe.wavesidebar.WaveSideBar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;
import com.yzq.zxinglibrary.encode.CodeCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.model.UserInfo;

public class BottomBarFat extends Fragment implements View.OnClickListener {
    public static final String UPDATE_MSG_INFO = "com.gameex.dw.flychat.BottomBarFat.update_MSG_INFO";
    public static final String REMOVE_CONTACT = "com.gameex.dw.flychat.BottomBarFat.CONTACT_ADAPTER_REMOVE";
    public static final String ADD_CONTACT = "com.gameex.dw.flychat.BottomBarFat.CONTACT_ADAPTER_ADD";
    public static final String UPDATE_USER_INFO = "com.gameex.dw.flychat.BottomBarFat.UPDATE_USER";

    private static final String TAG = "BOTTOM_BAR_FAT";
    public static final int REQUEST_CODE_SCAN = 131;
    private static final String ARG_PARAM = "flag";
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
    private List<MsgInfo> mMsgInfos = new ArrayList<>();
    private List<UserInfo> mContacts = new ArrayList<>();

    public List<UserInfo> getContacts() {
        return mContacts;
    }

    private UserInfo mUserInfo;
    private UpdateFragmentReceiver mFragmentReceiver;

    private static CircularImageView userIcon;
    @SuppressLint("StaticFieldLeak")
    private static TextView userName;

    private Dialog mQRCodeDialog;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;

    private FragmentCallBack mCallBack;

    public BottomBarFat() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallBack = (FragmentCallBack) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPref = PreferenceManager.getDefaultSharedPreferences(BottomBarActivity.sBottomBarActivity);
        if (getArguments() != null) {
            param = getArguments().getString(ARG_PARAM);
        }
        mFragmentReceiver = new UpdateFragmentReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_MSG_INFO);
        filter.addAction(UPDATE_USER_INFO);
        filter.addAction(REMOVE_CONTACT);
        filter.addAction(ADD_CONTACT);
        BottomBarActivity.sBottomBarActivity.registerReceiver(mFragmentReceiver, filter);
        mUserInfo = JMessageClient.getMyInfo();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater
            , @Nullable ViewGroup container
            , @Nullable Bundle savedInstanceState) {
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
                animator.setAddDuration(300);
                animator.setChangeDuration(300);
                animator.setMoveDuration(300);
                animator.setRemoveDuration(300);
                mRecView = mView.findViewById(R.id.recycler_dash);
                mRecView.setItemAnimator(animator);
                mRecView.setLayoutManager(new LinearLayoutManager(BottomBarActivity.sBottomBarActivity));
//                mRecView.addItemDecoration(new DividerItemDecoration(BottomBarActivity.sBottomBarActivity,
//                        DividerItemDecoration.VERTICAL));
                String msgsStr = mPref.getString("msg_list", "");
                if (!TextUtils.isEmpty(msgsStr)) {
                    mMsgInfos = new Gson().fromJson(msgsStr
                            , new TypeToken<List<MsgInfo>>() {
                            }.getType());
                }
                mAdapter = new DashAdapter(mMsgInfos, BottomBarActivity.sBottomBarActivity);
                mRecView.setAdapter(mAdapter);
                break;
            case "1":
                DefaultItemAnimator animatorContact = new DefaultItemAnimator();
                animatorContact.setAddDuration(300);
                animatorContact.setChangeDuration(300);
                animatorContact.setMoveDuration(300);
                animatorContact.setRemoveDuration(300);
                mContactRec = mView.findViewById(R.id.contact_recycler);
                mContactRec.setItemAnimator(animatorContact);
                mContactRec.setLayoutManager(new LinearLayoutManager(BottomBarActivity.sBottomBarActivity));
                String userInfosStr = mPref.getString("contact_list", "");
                if (!TextUtils.isEmpty(userInfosStr)) {
                    mContacts = (List<UserInfo>) UserInfo.fromJsonToCollection(userInfosStr);
                    mContactAdapter = new ContactAdapter(BottomBarActivity.sBottomBarActivity,
                            Contact.getBasicContact(), mContacts);
                    mContactRec.setAdapter(mContactAdapter);
                } else {
                    ContactManager.getFriendList(new GetUserInfoListCallback() {
                        @Override
                        public void gotResult(int responseCode, String friendListDesc, List<UserInfo> list) {
                            if (responseCode == 0) {
                                LogUtil.d(TAG, "ContactManager.getFriendList : " +
                                        "list = " + list.toString());
                                mContacts = list;
                                mContactAdapter = new ContactAdapter(BottomBarActivity.sBottomBarActivity,
                                        Contact.getBasicContact(), mContacts);
                                mContactRec.setAdapter(mContactAdapter);
                                mEditor = mPref.edit();
                                mEditor.putString("contact_list", UserInfo.collectionToJson(mContacts));
                                mEditor.apply();
                            } else {
                                LogUtil.d(TAG, "ContactManager.getFriendList : " +
                                        "responseCode = " + responseCode + " ; friendListDesc" +
                                        friendListDesc);
                            }
                        }
                    });
                }
                mCallBack.sendMessage(UserInfo.collectionToJson(mContacts));
                new Handler(getActivity().getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        updateContact();
                    }
                });
                WaveSideBar indexBar = mView.findViewById(R.id.glide_side_bar);
                indexBar.setIndexItems(indexStr);
                indexBar.setOnSelectIndexItemListener(new WaveSideBar.OnSelectIndexItemListener() {
                    @Override
                    public void onSelectIndexItem(String index) {
                        for (int i = 0; i < mContacts.size(); i++) {
                            String indexContact = mContacts.get(i).getExtra("index");
                            if (indexContact != null) {
                                if (indexContact.equals(index)) {
                                    RecScrollHelper.scrollToPosition(mContactRec, i);
                                    return;
                                }
                            }
                        }
                    }
                });
                break;
            case "2":
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
                initDataOfMine();
                break;
            default:
                break;
        }
    }

    /**
     * 初始化用户信息(我 标签页信息)
     */
    private void initDataOfMine() {
        switch (param) {
            case "0":
                break;
            case "1":
                break;
            case "2":
                userIcon = mView.findViewById(R.id.circle_img_user);
                userName = mView.findViewById(R.id.mine_name);
                if (mUserInfo == null) {
                    mUserInfo = JMessageClient.getMyInfo();
                }
                UserInfoUtils.initUserIcon(mUserInfo, BottomBarActivity.sBottomBarActivity
                        , userIcon);
                userName.setText(TextUtils.isEmpty(mUserInfo.getNickname())
                        ? mUserInfo.getUserName() : mUserInfo.getNickname());
                break;
            default:
                break;
        }
    }

    /**
     * 检查好友变更，并更新联系人列表
     */
    private void updateContact() {
        ContactManager.getFriendList(new GetUserInfoListCallback() {
            @Override
            public void gotResult(int responseCode, String friendListDesc, List<UserInfo> list) {
                if (responseCode == 0) {
                    LogUtil.d(TAG, "ContactManager.getFriendList : " +
                            "list = " + list.toString());
                    if (list.size() == 0) {
                        mContacts.clear();
                        mEditor = mPref.edit();
                        mEditor.putString("contact_list", UserInfo.collectionToJson(mContacts));
                        mEditor.apply();
                        mContactAdapter.notifyDataSetChanged();
                    } else {
                        for (int i = 0; i < list.size(); i++) {
                            UserInfo userInfo = list.get(i);
                            addContact(userInfo);
                        }
                    }
                } else {
                    LogUtil.d(TAG, "ContactManager.getFriendList : " +
                            "responseCode = " + responseCode + " ; friendListDesc" +
                            friendListDesc);
                }
            }
        });
    }

    /**
     * 添加联系人逻辑承接
     *
     * @param userInfo 极光联系人info
     */
    private void addContact(UserInfo userInfo) {
        if (!isUserExist(userInfo)) {
            String index = userInfo.getExtra("index");
            if (isIndexExist(index)) {
                addNewFromIndex(index, userInfo);
            } else {
                addNewNoIndex(index, userInfo);
            }
        }
    }

    /**
     * 索引值已存在，将新用户加入联系人集合中对应索引位置的第一位
     *
     * @param index    索引
     * @param userInfo 用户基本信息
     */
    private void addNewFromIndex(String index, UserInfo userInfo) {
        for (int j = 0; j < mContacts.size(); j++) {
            UserInfo contact = mContacts.get(j);
            if (contact.getExtra("index").equals(index)) {
                mContacts.add(j, userInfo);
                mEditor = mPref.edit();
                mEditor.putString("contact_list", UserInfo.collectionToJson(mContacts));
                mEditor.apply();
                mCallBack.sendMessage(UserInfo.collectionToJson(mContacts));
                mContactAdapter.notifyItemInserted(j);
                break;
            }
        }
    }

    /**
     * 索引不存在
     *
     * @param index    索引
     * @param userInfo 用户基本信息
     */
    private void addNewNoIndex(String index, UserInfo userInfo) {
        if (mContacts.size() == 0) {
            mContacts.add(userInfo);
            mEditor = mPref.edit();
            mEditor.putString("contact_list", UserInfo.collectionToJson(mContacts));
            mEditor.apply();
            mCallBack.sendMessage(UserInfo.collectionToJson(mContacts));
            mContactAdapter.notifyDataSetChanged();
            return;
        }
        for (int j = 0; j < mContacts.size(); j++) {
            UserInfo contact = mContacts.get(j);
            String indexContact = contact.getExtra("index");
            if (indexContact.equals("#") || Integer.parseInt(index) < Integer.parseInt(indexContact)) {
                mContacts.add(j, userInfo);
                mEditor = mPref.edit();
                mEditor.putString("contact_list", UserInfo.collectionToJson(mContacts));
                mEditor.apply();
                mCallBack.sendMessage(UserInfo.collectionToJson(mContacts));
                mContactAdapter.notifyItemInserted(j);
                break;
            }
        }
    }

    /**
     * 判断索引值是否已存在
     *
     * @param index 下拉的索引
     * @return boolean
     */
    private boolean isIndexExist(String index) {
        for (int j = 0; j < mContacts.size(); j++) {
            UserInfo contact = mContacts.get(j);
            if (contact.getExtra("index").equals(index)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断此用户是否已存在本地列表中
     *
     * @param userInfo 从极光的好友对象
     * @return boolean
     */
    private boolean isUserExist(UserInfo userInfo) {
        for (int j = 0; j < mContacts.size(); j++) {
            UserInfo contact = mContacts.get(j);
            if (contact.getUserName().equals(userInfo.getUserName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BottomBarActivity.sBottomBarActivity.unregisterReceiver(mFragmentReceiver);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.mine_info_layout:
                if (mUserInfo == null) {
                    mUserInfo = JMessageClient.getMyInfo();
                }
                intent.setClass(BottomBarActivity.sBottomBarActivity, UserInfoActivity.class);
                intent.putExtra("mine_info", mUserInfo.toJson());
                startActivity(intent);
                break;
            case R.id.qr_code_img:
                /*
                contentEtString:字符串内容
                w:图片宽
                h:图片高
                logo:不需要的话，直接传空
                 */
                Bitmap logo = BitmapFactory.decodeResource(getResources(),
                        R.drawable.icon_user);
                Bitmap qrCodeBit = CodeCreator.createQRCode("www.baidu.com",
                        400, 400, logo);
                showQrDialog(qrCodeBit);
                break;
            case R.id.loose_change_layout:
                intent.setClass(Objects.requireNonNull(getActivity()), ChangeActivity.class);
                startActivity(intent);
                break;
            case R.id.fly_chat_store_layout:
                Toast.makeText(BottomBarActivity.sBottomBarActivity,
                        "进入商城", Toast.LENGTH_SHORT).show();
                break;
            case R.id.scan_layout:
                requestPermission();
                break;
            case R.id.my_favorite_layout:
                Toast.makeText(BottomBarActivity.sBottomBarActivity,
                        "查看收藏条目", Toast.LENGTH_SHORT).show();
                break;
            case R.id.setting_layout:
                intent.setClass(BottomBarActivity.sBottomBarActivity, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.online_service_layout:
                Toast.makeText(BottomBarActivity.sBottomBarActivity,
                        "请求客服", Toast.LENGTH_SHORT).show();
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
     * 更新飞聊标签页
     *
     * @param context  上下文
     * @param name     极光上的username
     * @param date     信息发送的日期
     * @param msg      最后一条未读信息
     * @param isNotify 是否设置不提醒
     */
    private void updateMsgInfo(final Context context, String name, final String date
            , final String msg, final boolean isNotify
            , final boolean isSingle, final String groupInfoJson) {
        JMessageClient.getUserInfo(name, new GetUserInfoCallback() {
            @Override
            public void gotResult(int i, String s, UserInfo userInfo) {
                if (i == 0) {
                    LogUtil.d(TAG, "onReceiver: " + "userInfo = " + userInfo.toJson());
                    MsgInfo msgInfo = new MsgInfo(
                            userInfo.getExtra("username") == null
                                    ? userInfo.getUserName() : userInfo.getExtra("username")
                            , date, msg, isNotify);
                    msgInfo.setUriPath(userInfo.getExtra("icon_uri") == null ?
                            DataUtil.resourceIdToUri(context.getPackageName(), R.drawable.icon_user).toString()
                            : userInfo.getExtra("icon_uri"));
                    msgInfo.setSingle(isSingle);
                    msgInfo.setUserInfoJson(userInfo.toJson());
                    if (groupInfoJson != null) {
                        msgInfo.setGroupInfoJson(groupInfoJson);
                    }
                    int isExist = isMsgExist(userInfo);
                    if (isExist == -1) {
                        mMsgInfos.add(msgInfo);
                        mAdapter.notifyItemInserted(1);
                    } else {
                        mMsgInfos.set(isExist, msgInfo);
                        mAdapter.notifyItemChanged(isExist);
                    }
                    Gson gson = new Gson();
                    String msgsStr = gson.toJson(mMsgInfos);
                    mEditor = mPref.edit();
                    mEditor.putString("msg_list", msgsStr);
                    mEditor.apply();
                } else {
                    LogUtil.d(TAG, "onReceiver: " +
                            "responseCode = " + i + " ; desc = " + s);
                    Toast.makeText(context, "好友添加失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 判断新信息的来源是否已存在列表中
     *
     * @param userInfo 新信息来源的userInfo
     * @return 若存在列表中则返回所在位置，否则返回-1
     */
    private Integer isMsgExist(UserInfo userInfo) {
        for (int i = 0; i < mMsgInfos.size(); i++) {
            UserInfo user = UserInfo.fromJson(mMsgInfos.get(i).getUserInfoJson());
            if (user.getUserName().equals(userInfo.getUserName())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * fragment刷新广播
     */
    class UpdateFragmentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            switch (action) {
                case UPDATE_MSG_INFO:
                    String name = intent.getStringExtra("username");
                    String date = intent.getStringExtra("date");
                    String msg = intent.getStringExtra("msg_last");
                    boolean isNotify = intent.getBooleanExtra("is_notify", true);
                    boolean isSingle = intent.getBooleanExtra("is_single", true);
                    String groupInfoJson = intent.getStringExtra("group_json");
                    updateMsgInfo(context, name, date, msg, isNotify, isSingle, groupInfoJson);
                    break;
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
                case REMOVE_CONTACT:
                    String phone = intent.getStringExtra("phone");
                    for (UserInfo userInfo : mContacts) {
                        if (phone.equals(userInfo.getUserName())) {
                            mContacts.remove(userInfo);
                            mEditor = mPref.edit();
                            mEditor.putString("contact_list", UserInfo.collectionToJson(mContacts));
                            mEditor.apply();
                            break;
                        }
                    }
                    mContactAdapter.notifyDataSetChanged();
                    break;
                case ADD_CONTACT:
                    JMessageClient.getUserInfo(intent.getStringExtra("username"), new GetUserInfoCallback() {
                        @Override
                        public void gotResult(int i, String s, UserInfo userInfo) {
                            if (i == 0) {
                                LogUtil.d(TAG, "onReceive-addContact: " +
                                        "userInfo = " + userInfo.toJson());
                                addContact(userInfo);
                            } else {
                                LogUtil.d(TAG, "onReceive-addContact: " +
                                        "responseCode = " + i + "desc = " + s);
                            }
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    }

}
