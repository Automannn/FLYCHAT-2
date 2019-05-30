package com.gameex.dw.justtalk.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.objPack.MsgInfo;
import com.gameex.dw.justtalk.singleChat.ChattingActivity;
import com.gameex.dw.justtalk.groupChat.GroupChatActivity;
import com.gameex.dw.justtalk.util.DataUtil;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * 飞聊item的RecyclerView的adapter
 */
public class DashAdapter extends RecyclerView.Adapter<DashAdapter.DashHolder> {

    private List<MsgInfo> mMsgInfos;
    private Context mContext;

    DashAdapter(List<MsgInfo> msgInfos, Context context) {
        mMsgInfos = msgInfos;
        mContext = context;
    }

    @NonNull
    @Override
    public DashHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.message_item, viewGroup, false);
        return new DashHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DashHolder holder, int position) {
        MsgInfo msgInfo = mMsgInfos.get(position);
        Glide.with(mContext)
                .load(Uri.parse(msgInfo.getUriPath()))
                .into(holder.userIcon);
        holder.userName.setText(msgInfo.getUsername());
        String lastMsg = msgInfo.getMsgLast();
        if (lastMsg.length() > 16) {
            lastMsg = lastMsg.substring(0, 15) + "......";
        }
        holder.msgLast.setText(lastMsg);
        holder.msgTime.setText(msgInfo.getDate());
        if (msgInfo.isNotify()) {
            holder.notifyOff.setVisibility(View.GONE);
        } else {
            holder.notifyOff.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mMsgInfos.size();
    }

    class DashHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        CardView msgCard;
        CircularImageView userIcon;
        ImageView notifyOff;
        TextView userName, msgLast, msgTime;

        DashHolder(@NonNull final View itemView) {
            super(itemView);
            msgCard = itemView.findViewById(R.id.message_card);
            msgCard.setOnClickListener(this);
            msgCard.setOnLongClickListener(this);
            userIcon = itemView.findViewById(R.id.user_icon);
            notifyOff = itemView.findViewById(R.id.notify_off);
            userName = itemView.findViewById(R.id.user_name);
            msgLast = itemView.findViewById(R.id.msg_last);
            msgTime = itemView.findViewById(R.id.msg_time);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.message_card:
                    MsgInfo msgInfo = mMsgInfos.get(getAdapterPosition());
                    Intent intent = new Intent();
                    if (msgInfo.isSingle()) {
                        JMessageClient.enterSingleConversation(
                                UserInfo.fromJson(msgInfo.getUserInfoJson()).getUserName());
                        intent.setClass(mContext, ChattingActivity.class);
                    } else {
                        GroupInfo groupInfo = GroupInfo.fromJson(msgInfo.getGroupInfoJson());
                        JMessageClient.enterGroupConversation(GroupInfo.fromJson(
                                msgInfo.getGroupInfoJson()).getGroupID());
                        intent.setClass(mContext, GroupChatActivity.class);
                        intent.putExtra("group_id", groupInfo.getGroupID());
                        intent.putExtra("group_icon", DataUtil.resourceIdToUri(
                                mContext.getPackageName(), R.drawable.icon_group));
                        intent.putExtra("group_name", groupInfo.getGroupName());
                    }
                    intent.putExtra("msg_info", msgInfo);
                    intent.putExtra("last_msg", msgInfo.getMsgLast());
                    mContext.startActivity(intent);
                    break;
            }
        }

        @Override
        public boolean onLongClick(View view) {
            switch (view.getId()) {
                case R.id.message_card:
                    Toast.makeText(mContext, "删除此条", Toast.LENGTH_SHORT).show();
                    mMsgInfos.remove(mMsgInfos.get(getAdapterPosition()));
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor editor = pref.edit();
                    Gson gson = new Gson();
                    String msgsStr = gson.toJson(mMsgInfos);
                    editor.putString("msg_list", msgsStr);
                    editor.apply();
                    notifyItemRemoved(getAdapterPosition());
                    break;
            }
            return true;
        }
    }
}
