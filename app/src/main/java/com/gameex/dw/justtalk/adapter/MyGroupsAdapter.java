package com.gameex.dw.justtalk.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gameex.dw.justtalk.entry.MsgInfo;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.activity.GroupChatActivity;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.GroupInfoUtil;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.GroupInfo;

/**
 * 我的群组fragment
 */
public class MyGroupsAdapter extends RecyclerView.Adapter<MyGroupsAdapter.MyGroupsHolder> {
    private static final String TAG = "MyGroupsAdapter";

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
        View view = inflater.inflate(R.layout.recycler_item_my_group, viewGroup, false);
        return new MyGroupsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyGroupsHolder holder, int position) {
        GroupInfo groupInfo = mGroupInfos.get(position);
        GroupInfoUtil.initGroupIcon(groupInfo,mContext,holder.icon);
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

        MyGroupsHolder(@NonNull View itemView) {
            super(itemView);
            groups = itemView.findViewById(R.id.groups_layout);
            groups.setOnClickListener(view -> {
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
            });
            name = itemView.findViewById(R.id.name_group);
            icon = itemView.findViewById(R.id.icon_group);
        }
    }
}
