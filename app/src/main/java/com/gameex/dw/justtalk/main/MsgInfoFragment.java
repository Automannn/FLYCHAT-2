package com.gameex.dw.justtalk.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.objPack.MsgInfo;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import es.dmoral.toasty.Toasty;

public class MsgInfoFragment extends Fragment {
    private static final String TAG = "MsgInfoFragment";
    /**
     * 更新信息列表action
     */
    public static final String UPDATE_MSG_INFO =
            "com.gameex.dw.flychat.MsgInfoFragment.update_MSG_INFO";

    private View mView;
    private RecyclerView mRecView;
    private DashAdapter mAdapter;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    private List<MsgInfo> mMsgInfos;
    private UpdateMsgInfoReceiver mReceiver;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mMsgInfos = new ArrayList<>();
        mReceiver = new UpdateMsgInfoReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_MSG_INFO);
        Objects.requireNonNull(getActivity()).registerReceiver(mReceiver, filter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_message, container, false);
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initRecycler();
    }

    @Override
    public void onDestroy() {
        Objects.requireNonNull(getActivity()).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void initRecycler() {
        mRecView = mView.findViewById(R.id.recycler_dash);
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(300);
        animator.setChangeDuration(300);
        animator.setMoveDuration(300);
        animator.setRemoveDuration(300);
        mRecView.setItemAnimator(animator);
//                mRecView.addItemDecoration(new DividerItemDecoration(BottomBarActivity.sBottomBarActivity,
//                        DividerItemDecoration.VERTICAL));
        mRecView.setLayoutManager(new LinearLayoutManager(BottomBarActivity.sBottomBarActivity));
        String msgsStr = mPref.getString("msg_list", "");
        if (!TextUtils.isEmpty(msgsStr)) {
            mMsgInfos = new Gson().fromJson(msgsStr
                    , new TypeToken<List<MsgInfo>>() {
                    }.getType());
        }
        mAdapter = new DashAdapter(mMsgInfos, BottomBarActivity.sBottomBarActivity);
        mRecView.setAdapter(mAdapter);
    }

    /**
     * 判断新信息的来源是否已存在列表中,并执行对应刷新事件
     *
     * @param msgInfo 自定义信息体
     */
    private void updateMsgInfo(MsgInfo msgInfo) {
        if (mMsgInfos.size() == 0) {
            mMsgInfos.add(msgInfo);
            mAdapter.notifyDataSetChanged();
        } else {
            int i = 0;
            for (; i < mMsgInfos.size(); i++) {
                if (mMsgInfos.get(i).getId().equals(msgInfo.getId())) {
                    mMsgInfos.set(i, msgInfo);
                    mAdapter.notifyItemChanged(i);
                    break;
                }
            }
            if (i == mMsgInfos.size()) {
                mMsgInfos.add(0, msgInfo);
                mAdapter.notifyItemInserted(0);
            }
        }
        Gson gson = new Gson();
        String msgsStr = gson.toJson(mMsgInfos);
        mEditor = mPref.edit();
        mEditor.putString("msg_list", msgsStr);
        mEditor.apply();
    }

    /**
     * 准备更新信息列表
     *
     * @param msgInfo 自定义信息体
     */
    private void goUpdateMsgInfo(final MsgInfo msgInfo) {
        if (msgInfo.isSingle()) {
            JMessageClient.getUserInfo(msgInfo.getUsername(), new GetUserInfoCallback() {
                @Override
                public void gotResult(int i, String s, final UserInfo userInfo) {
                    if (i == 0) {
                        LogUtil.d(TAG, "onReceiver: " + "userInfo = " + userInfo.toJson());
                        msgInfo.setId(userInfo.getUserName());
                        msgInfo.setUsername(TextUtils.isEmpty(userInfo.getNickname())
                                ? userInfo.getUserName() : userInfo.getNickname());
                        userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                            @Override
                            public void gotResult(int i, String s, Bitmap bitmap) {
                                if (i == 0) {
                                    msgInfo.setUriPath(Uri.parse(MediaStore.Images.Media
                                            .insertImage(Objects.requireNonNull(getActivity()).getContentResolver()
                                                    , bitmap, null, null))
                                            .toString());
                                } else {
                                    msgInfo.setUriPath(DataUtil.resourceIdToUri(
                                            Objects.requireNonNull(getActivity()).getPackageName(), R.drawable.icon_user)
                                            .toString());
                                }
                                msgInfo.setUserInfoJson(userInfo.toJson());
                                updateMsgInfo(msgInfo);
                            }
                        });
                    } else {
                        LogUtil.d(TAG, "onReceiver: " +
                                "responseCode = " + i + " ; desc = " + s);
                        Toasty.error(Objects.requireNonNull(getContext()), "信息列表更新失败"
                                , Toasty.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            GroupInfo groupInfo = GroupInfo.fromJson(msgInfo.getGroupInfoJson());
            msgInfo.setId(String.valueOf(groupInfo.getGroupID()));
            groupInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                @Override
                public void gotResult(int i, String s, Bitmap bitmap) {
                    if (i == 0) {
                        msgInfo.setUriPath(Uri.parse(MediaStore.Images
                                .Media
                                .insertImage(Objects.requireNonNull(getActivity()).getContentResolver()
                                        , bitmap, null, null)).toString());
                    } else {
                        LogUtil.d(TAG, "updateMsgInfo-group: "
                                + "responseCode = " + i + " ;desc = " + s);
                        msgInfo.setUriPath(DataUtil.resourceIdToUri(Objects.requireNonNull(getActivity()).getPackageName()
                                , R.drawable.icon_group).toString());
                    }
                    updateMsgInfo(msgInfo);
                }
            });
        }
    }

    private class UpdateMsgInfoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            switch (action) {
                case UPDATE_MSG_INFO:
                    MsgInfo msgInfo = intent.getParcelableExtra("msg_info");
                    goUpdateMsgInfo(msgInfo);
                    break;
            }
        }
    }
}
