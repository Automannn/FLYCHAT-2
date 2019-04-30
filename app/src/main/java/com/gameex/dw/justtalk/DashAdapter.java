package com.gameex.dw.justtalk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.ObjPack.Msg;
import com.gameex.dw.justtalk.chattingPack.ChattingActivity;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.event.OfflineMessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;

/**
 * 飞聊item的RecyclerView的adapter
 */
public class DashAdapter extends RecyclerView.Adapter<DashAdapter.DashHolder> {

    private List<Object[]> mMsgInfo;
    private Context mContext;

    DashAdapter(List<Object[]> msgInfo, Context context) {
        mMsgInfo = msgInfo;
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
        Object[] obj = mMsgInfo.get(position);
        Glide.with(mContext)
                .load(obj[0])
                .into(holder.userIcon);
        holder.userName.setText(String.valueOf(obj[1]));
        String lastMsg = String.valueOf(obj[2]);
        if (lastMsg.length() > 16) {
            lastMsg = lastMsg.substring(0, 15);
        }
        holder.msgLast.setText(lastMsg + "......");
        holder.msgTime.setText(String.valueOf(obj[3]));
        if (position % 3 == 0) {
            holder.notifyOff.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mMsgInfo.size();
    }

    class DashHolder extends RecyclerView.ViewHolder {
        CardView msgCard;
        CircularImageView userIcon;
        ImageView notifyOff;
        TextView userName, msgLast, msgTime;

        DashHolder(@NonNull View itemView) {
            super(itemView);
            msgCard = itemView.findViewById(R.id.message_card);
            msgCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String usernameStr = String.valueOf(mMsgInfo.get(getAdapterPosition())[1]);
                    JMessageClient.enterSingleConversation(usernameStr);
                    Intent intent = new Intent(mContext, ChattingActivity.class);
                    intent.putExtra("username", usernameStr);
                    mContext.startActivity(intent);
                }
            });
            userIcon = itemView.findViewById(R.id.user_icon);
            notifyOff = itemView.findViewById(R.id.notify_off);
            userName = itemView.findViewById(R.id.user_name);
            msgLast = itemView.findViewById(R.id.msg_last);
            msgTime = itemView.findViewById(R.id.msg_time);
        }
    }
}
