package com.gameex.dw.justtalk.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.holder.SearchUserHodler;
import com.gameex.dw.justtalk.util.UserInfoUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.GroupMemberInfo;
import cn.jpush.im.android.api.model.UserInfo;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserHodler> {

    private Context mContext;
    private List<UserInfo> mUserInfos;
    private long mGroupId;
    private GroupInfo mGroupInfo;

    public SearchUserAdapter(Context context, List<UserInfo> userInfos, long grupId) {
        mContext = context;
        mUserInfos = userInfos;
        mGroupId = grupId;
        JMessageClient.getGroupInfo(mGroupId, new GetGroupInfoCallback() {
            @Override
            public void gotResult(int i, String s, GroupInfo groupInfo) {
                if (i == 0) {
                    mGroupInfo = groupInfo;
                }
            }
        });
    }

    @NonNull
    @Override
    public SearchUserHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_item_search_user, parent, false);
        return new SearchUserHodler(view, mUserInfos, mGroupId);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchUserHodler holder, int position) {
        UserInfo userInfo = mUserInfos.get(position);
        UserInfoUtils.initUserIcon(userInfo, mContext, holder.icon);
        holder.name.setText(TextUtils.isEmpty(userInfo.getNickname())
                ? userInfo.getUserName() : userInfo.getNickname());
        if (mGroupInfo != null) {
            GroupMemberInfo memberInfo = mGroupInfo.getGroupMember(userInfo.getUserName(), null);
            if (memberInfo == null) {
                holder.add.setText("已在本群");
                holder.add.setEnabled(false);
            } else {
                holder.add.setText("添加");
                holder.add.setEnabled(true);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mUserInfos == null ? 0 : mUserInfos.size();
    }
}
