package com.gameex.dw.justtalk.chattingPack;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.ObjPack.Msg;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.util.DataUtil;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.List;

public class ChatRecAdapter extends RecyclerView.Adapter<ChatRecAdapter.ChatRecHolder> {

    private Context mContext;
    private List<Msg> mList;

    ChatRecAdapter(Context context, List<Msg> list) {
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public ChatRecHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.chat_recycler_item, viewGroup, false);
        return new ChatRecHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRecHolder holder, int position) {
        Msg msg = mList.get(position);
        holder.msgData.setText(msg.getDate());
        if (position == 0 || DataUtil.isMoreThanOneDay(mList.get(position - 1).getDate(),
                msg.getDate())) {
            holder.msgData.setVisibility(View.VISIBLE);
        } else {
            holder.msgData.setVisibility(View.GONE);
        }
        switch (msg.getType()) {
            case RECEIVED:
                holder.leftLayout.setVisibility(View.VISIBLE);
                holder.rightLayout.setVisibility(View.GONE);
                Glide.with(mContext)
                        .load(msg.getResourceId())
                        .into(holder.leftCircle);
                holder.receiveTime.setText(msg.getTime());
                holder.leftMsg.setText(msg.getContent());
                break;
            case SEND:
                holder.leftLayout.setVisibility(View.GONE);
                holder.rightLayout.setVisibility(View.VISIBLE);
                Glide.with(mContext)
                        .load(R.drawable.icon_user)
                        .into(holder.rightCircle);
                holder.sendTime.setText(DataUtil.getCurrentTimeStr());
                holder.rightMsg.setText(msg.getContent());
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public class ChatRecHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout leftLayout, rightLayout, leftMsgLayout, rightMsgLayout;
        CircularImageView leftCircle, rightCircle;
        TextView leftMsg, receiveTime, sendTime, rightMsg, msgData;

        ChatRecHolder(@NonNull View itemView) {
            super(itemView);
            leftLayout = itemView.findViewById(R.id.left_msg_linear);
            leftMsgLayout = itemView.findViewById(R.id.msg_receive_layout);
            rightLayout = itemView.findViewById(R.id.right_msg_linear);
            rightMsgLayout = itemView.findViewById(R.id.msg_send_layout);
            leftCircle = itemView.findViewById(R.id.user_icon_left);
            leftCircle.setOnClickListener(this);
            leftMsg = itemView.findViewById(R.id.user_msg_left);
            receiveTime = itemView.findViewById(R.id.msg_time_receive);
            rightCircle = itemView.findViewById(R.id.user_icon_right);
            rightMsg = itemView.findViewById(R.id.user_msg_right);
            sendTime = itemView.findViewById(R.id.msg_time_send);
            msgData = itemView.findViewById(R.id.msg_data_chat);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.user_icon_left:
                    Toast.makeText(mContext, "查看用户信息", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }
}
