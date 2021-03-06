package com.gameex.dw.justtalk.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.activity.ChattingActivity;
import com.gameex.dw.justtalk.entry.MsgInfo;
import com.gameex.dw.justtalk.activity.GroupChatActivity;
import com.gameex.dw.justtalk.util.GroupInfoUtil;
import com.gameex.dw.justtalk.util.UserInfoUtils;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.gson.Gson;
import com.rey.material.app.Dialog;
import com.vanniktech.emoji.EmojiTextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import es.dmoral.toasty.Toasty;
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

/**
 * 飞聊item的RecyclerView的adapter
 */
public class DashAdapter extends RecyclerView.Adapter<DashAdapter.DashHolder> {

    private List<MsgInfo> mMsgInfos;
    private Context mContext;

    public DashAdapter(List<MsgInfo> msgInfos, Context context) {
        mMsgInfos = msgInfos;
        mContext = context;
    }

    @NonNull
    @Override
    public DashHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.recycler_item_message, viewGroup, false);
        return new DashHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DashHolder holder, int position) {
        MsgInfo msgInfo = mMsgInfos.get(position);
        if (msgInfo.getGroupInfoJson() == null) {
            UserInfo userInfo = UserInfo.fromJson(msgInfo.getUserInfoJson());
            UserInfoUtils.initUserIcon(userInfo, mContext, holder.userIcon);
            Conversation conversation = JMessageClient.getSingleConversation(
                    userInfo.getUserName());
            if (conversation != null)
                holder.badge.setBadgeNumber(conversation.getUnReadMsgCnt());
        } else {
            GroupInfo groupInfo = GroupInfo.fromJson(msgInfo.getGroupInfoJson());
            GroupInfoUtil.initGroupIcon(groupInfo, mContext, holder.userIcon);
            Conversation conversation = JMessageClient.getGroupConversation(
                    groupInfo.getGroupID());
            if (conversation != null)
                holder.badge.setBadgeNumber(conversation.getUnReadMsgCnt());
        }
        holder.userName.setText(msgInfo.getUsername());
        String lastMsg = msgInfo.getMsgLast();
        if (lastMsg != null && lastMsg.length() > 16) {
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
        TextView userName, msgTime;
        EmojiTextView msgLast;
        Badge badge;

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
            badge = new QBadgeView(mContext).bindTarget(msgCard);
            badge.setGravityOffset(22, 2, true);
            badge.setBadgeGravity(Gravity.BOTTOM | Gravity.END);
            //badge拖拽监听，加上则可已拖拽
//            badge.setOnDragStateChangedListener((dragState, badge, targetView) -> {
//                if (dragState == Badge.OnDragStateChangedListener.STATE_SUCCEED)
//                    Toasty.info(mContext, "标为已读", Toasty.LENGTH_SHORT).show();
//            });
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
                        intent.putExtra("msg_info", msgInfo);
                        intent.putExtra("last_msg", msgInfo.getMsgLast());
                        mContext.startActivity(intent);
                        notifyItemChanged(getAdapterPosition());
                    } else {
                        JMessageClient.getGroupInfo(GroupInfo.fromJson(msgInfo.getGroupInfoJson())
                                .getGroupID(), new GetGroupInfoCallback() {
                            @Override
                            public void gotResult(int i, String s, GroupInfo groupInfo) {
                                if (i == 0) {
                                    JMessageClient.enterGroupConversation(groupInfo.getGroupID());
                                    intent.setClass(mContext, GroupChatActivity.class);
                                    intent.putExtra("group_id", groupInfo.getGroupID());
                                    msgInfo.setGroupInfoJson(groupInfo.toJson());
                                    intent.putExtra("msg_info", msgInfo);
                                    intent.putExtra("last_msg", msgInfo.getMsgLast());
                                    mContext.startActivity(intent);
                                    notifyItemChanged(getAdapterPosition());
                                }
                            }
                        });
                    }
                    break;
            }
        }

        @Override
        public boolean onLongClick(View view) {
            switch (view.getId()) {
                case R.id.message_card:
                    showDialog();
                    break;
            }
            return true;
        }

        /**
         * 长按会话弹窗提示删除此会话
         */
        private void showDialog() {
            Dialog dialog = new Dialog(mContext);
            dialog.title("删除聊天")
                    .positiveAction("确定")
                    .positiveActionClickListener(view -> {
                        mMsgInfos.remove(mMsgInfos.get(getAdapterPosition()));
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
                        SharedPreferences.Editor editor = pref.edit();
                        Gson gson = new Gson();
                        String msgsStr = gson.toJson(mMsgInfos);
                        editor.putString("msg_list", msgsStr);
                        editor.apply();
                        notifyItemRemoved(getAdapterPosition());
                        dialog.dismiss();
                    })
                    .cancelable(true)
                    .show();
        }
    }
}
