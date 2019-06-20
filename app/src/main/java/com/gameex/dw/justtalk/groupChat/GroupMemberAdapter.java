package com.gameex.dw.justtalk.groupChat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.util.UserInfoUtils;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.model.UserInfo;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.GroupMemberHolder> {

    private Context mContext;
    private List<UserInfo> mUserInfos;

    GroupMemberAdapter(Context context, List<UserInfo> userInfos) {
        mContext = context;
        mUserInfos = userInfos;
    }

    @NonNull
    @Override
    public GroupMemberHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.recycler_item_group_member, viewGroup, false);
        return new GroupMemberHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMemberHolder holder, int position) {
        if (position == 0) {
            holder.groupMaster.setVisibility(View.VISIBLE);
        }else{
            holder.groupMaster.setVisibility(View.GONE);
        }
        UserInfo userInfo = mUserInfos.get(position);
        UserInfoUtils.initUserIcon(userInfo,mContext,holder.icon);
    }

    @Override
    public int getItemCount() {
        return mUserInfos == null ? 0 : mUserInfos.size();
    }

    class GroupMemberHolder extends RecyclerView.ViewHolder {
        CircularImageView icon;
        TextView groupMaster;

        GroupMemberHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.group_member_circle);
            groupMaster = itemView.findViewById(R.id.group_master);
        }
    }
}
