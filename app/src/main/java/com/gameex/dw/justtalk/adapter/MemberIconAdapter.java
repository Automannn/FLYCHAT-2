package com.gameex.dw.justtalk.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.activity.UserBasicInfoActivity;
import com.gameex.dw.justtalk.util.UserInfoUtils;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.model.GroupMemberInfo;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * 群聊成员头像显示的适配器
 */
public class MemberIconAdapter extends RecyclerView.Adapter<MemberIconAdapter.GroupMemberHolder> {

    private Context mContext;
    private List<GroupMemberInfo> mMemberInfos;

    public MemberIconAdapter(Context context, List<GroupMemberInfo> memberInfos) {
        mContext = context;
        mMemberInfos = memberInfos;
    }

    @NonNull
    @Override
    public GroupMemberHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.recycler_item_group_icons, viewGroup, false);
        return new GroupMemberHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMemberHolder holder, int position) {
        if (position == 0) {
            holder.groupMaster.setVisibility(View.VISIBLE);
        } else {
            holder.groupMaster.setVisibility(View.GONE);
        }
        UserInfo userInfo = mMemberInfos.get(position).getUserInfo();
        UserInfoUtils.initUserIcon(userInfo, mContext, holder.icon);
    }

    @Override
    public int getItemCount() {
        return mMemberInfos == null ? 0 : mMemberInfos.size();
    }

    class GroupMemberHolder extends RecyclerView.ViewHolder {
        CircularImageView icon;
        TextView groupMaster;

        GroupMemberHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.group_member_circle);
            icon.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, UserBasicInfoActivity.class);
                intent.putExtra("user_info_json", mMemberInfos.get(getAdapterPosition())
                        .getUserInfo()
                        .toJson());
                mContext.startActivity(intent);
            });
            groupMaster = itemView.findViewById(R.id.group_master);
        }
    }
}
