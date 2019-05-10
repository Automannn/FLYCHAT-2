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

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.GroupChatHolder> {

    private Context mContext;
    private List<Msg> mList;
    private String currentDate;

    public GroupChatAdapter(Context context, List<Msg> list) {
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public GroupChatHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater=LayoutInflater.from(mContext);
        View view=inflater.inflate(R.layout.group_chat_item,viewGroup,false);
        return new GroupChatHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatHolder holder, int position) {
        Msg msg = mList.get(position);
        if (position == 0 || DataUtil.isMoreThanOneDay(mList.get(position - 1).getDate(),
                msg.getDate())) {
            if (currentDate.equals(msg.getDate())) {
                holder.receiveTime.setText("今天  " + msg.getTime());
                holder.sendTime.setText("今天  " + msg.getTime());
            } else if (DataUtil.isMoreThanOneDay(msg.getDate(), currentDate)) {
                holder.receiveTime.setText("昨天  " + msg.getTime());
                holder.sendTime.setText("昨天  " + msg.getTime());
            } else {
                holder.receiveTime.setText(msg.getDate() + "  " + msg.getTime());
                holder.sendTime.setText(msg.getDate() + "  " + msg.getTime());
            }
        } else {
            holder.receiveTime.setText(msg.getTime());
            holder.sendTime.setText(msg.getTime());
        }
        switch (msg.getType()) {
            case RECEIVED:
                holder.leftLayout.setVisibility(View.VISIBLE);
                holder.rightLayout.setVisibility(View.GONE);
                Glide.with(mContext)
                        .load(msg.getUri())
                        .into(holder.leftCircle);
                holder.leftMsg.setText(msg.getContent());
                break;
            case SEND:
                holder.leftLayout.setVisibility(View.GONE);
                holder.rightLayout.setVisibility(View.VISIBLE);
                Glide.with(mContext)
                        .load(msg.getUri())
                        .into(holder.rightCircle);
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

    class GroupChatHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout leftLayout, rightLayout, leftMsgLayout, rightMsgLayout;
        CircularImageView leftCircle, rightCircle;
        TextView leftMsg, receiveTime, sendTime, rightMsg;

        public GroupChatHolder(@NonNull View itemView) {
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
