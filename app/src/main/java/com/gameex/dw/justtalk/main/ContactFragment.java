package com.gameex.dw.justtalk.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.objPack.Contact;
import com.gameex.dw.justtalk.publicInterface.FragmentCallBack;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.RecScrollHelper;
import com.gjiazhe.wavesidebar.WaveSideBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.model.UserInfo;

public class ContactFragment extends Fragment {
    private static final String TAG = "ContactFragment";
    /**
     * 移除联系人action
     */
    public static final String REMOVE_CONTACT =
            "com.gameex.dw.flychat.ContactFragment.CONTACT_ADAPTER_REMOVE";
    /**
     * 添加联系人action
     */
    public static final String ADD_CONTACT =
            "com.gameex.dw.flychat.ContactFragment.CONTACT_ADAPTER_ADD";

    private View mView;
    private RecyclerView mContactRec;
    private ContactAdapter mAdapter;
    /**
     * 缓存操作
     */
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    /**
     * 刷新联系人动态广播接收器
     */
    private UpdateContactReceiver mReceiver;
    /**
     * 接口回调
     */
    private FragmentCallBack mCallBack;
    /**
     * 联系人集合
     */
    private List<UserInfo> mContacts;
    /**
     * 索引值
     */
    private static String[] indexStr = new String[]{"↑", "A", "B", "C", "D", "E"
            , "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S"
            , "T", "U", "V", "W", "X", "Y", "Z", "#"};

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallBack = (FragmentCallBack) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mReceiver = new UpdateContactReceiver();
        mContacts = new ArrayList<>();
        IntentFilter filter = new IntentFilter();
        filter.addAction(REMOVE_CONTACT);
        filter.addAction(ADD_CONTACT);
        Objects.requireNonNull(getActivity()).registerReceiver(mReceiver, filter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.contact_layout, container, false);
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    @Override
    public void onDestroy() {
        Objects.requireNonNull(getActivity()).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    /**
     * 初始化布局
     */
    private void initView() {
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
            mAdapter = new ContactAdapter(BottomBarActivity.sBottomBarActivity,
                    Contact.getBasicContact(), mContacts);
            mContactRec.setAdapter(mAdapter);
        } else {
            ContactManager.getFriendList(new GetUserInfoListCallback() {
                @Override
                public void gotResult(int responseCode, String friendListDesc, List<UserInfo> list) {
                    if (responseCode == 0) {
                        LogUtil.d(TAG, "ContactManager.getFriendList : " +
                                "list = " + list.toString());
                        mContacts = list;
                        mAdapter = new ContactAdapter(BottomBarActivity.sBottomBarActivity,
                                Contact.getBasicContact(), mContacts);
                        mContactRec.setAdapter(mAdapter);
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
        new Handler(Objects.requireNonNull(getActivity()).getMainLooper()).post(this::updateContact);
        WaveSideBar indexBar = mView.findViewById(R.id.glide_side_bar);
        indexBar.setIndexItems(indexStr);
        indexBar.setOnSelectIndexItemListener(index -> {
            for (int i = 0; i < mContacts.size(); i++) {
                String indexContact = mContacts.get(i).getExtra("index");
                if (indexContact != null) {
                    if (indexContact.equals(index)) {
                        RecScrollHelper.scrollToPosition(mContactRec, i);
                        return;
                    }
                }
            }
        });
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
                mAdapter.notifyItemInserted(j);
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
            mAdapter.notifyDataSetChanged();
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
                mAdapter.notifyItemInserted(j);
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
                        mAdapter.notifyDataSetChanged();
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

    private class UpdateContactReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            switch (action) {
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
                    mAdapter.notifyDataSetChanged();
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
            }
        }
    }
}
