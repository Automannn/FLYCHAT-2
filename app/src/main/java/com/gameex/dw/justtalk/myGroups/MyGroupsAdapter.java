package com.gameex.dw.justtalk.myGroups;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.ObjPack.MsgInfo;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.chattingPack.GroupChatActivity;
import com.gameex.dw.justtalk.util.DataUtil;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.GroupInfo;

public class MyGroupsAdapter extends RecyclerView.Adapter<MyGroupsAdapter.MyGroupsHolder> {

    private Context mContext;
    private List<GroupInfo> mGroupInfos;

    public MyGroupsAdapter(Context context, List<GroupInfo> groupInfos) {
        mContext = context;
        mGroupInfos = groupInfos;
    }

    @NonNull
    @Override
    public MyGroupsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.my_group_item, viewGroup, false);
        return new MyGroupsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyGroupsHolder holder, int position) {
        GroupInfo groupInfo = mGroupInfos.get(position);
        Glide.with(mContext)
                .load(DataUtil.resourceIdToUri(
                        mContext.getPackageName(), R.drawable.icon_group))
                .into(holder.icon);
        holder.name.setText(groupInfo.getGroupName());
    }

    @Override
    public int getItemCount() {
        return mGroupInfos == null ? 0 : mGroupInfos.size();
    }

    class MyGroupsHolder extends RecyclerView.ViewHolder {
        LinearLayout groups;
        TextView name;
        CircularImageView icon;

        public MyGroupsHolder(@NonNull View itemView) {
            super(itemView);
            groups = itemView.findViewById(R.id.groups_layout);
            groups.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, GroupChatActivity.class);
                    MsgInfo msgInfo = new MsgInfo();
                    GroupInfo groupInfo = mGroupInfos.get(getAdapterPosition());
                    msgInfo.setGroupInfoJson(groupInfo.toJson());
                    JMessageClient.enterGroupConversation(groupInfo.getGroupID());
                    intent.putExtra("msg_info", msgInfo);
                    intent.putExtra("group_id", groupInfo.getGroupID());
                    intent.putExtra("group_icon", DataUtil.resourceIdToUri(
                            mContext.getPackageName(), R.drawable.icon_group));
                    intent.putExtra("group_name", groupInfo.getGroupName());
                    mContext.startActivity(intent);
                }
            });
            name = itemView.findViewById(R.id.name_group);
            icon = itemView.findViewById(R.id.icon_group);
        }
    }
}
