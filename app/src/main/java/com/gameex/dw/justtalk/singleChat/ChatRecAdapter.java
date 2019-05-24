package com.gameex.dw.justtalk.singleChat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.imgBrowse.PhotoBrowseActivity;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.github.siyamed.shapeimageview.RoundedImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;

public class ChatRecAdapter extends RecyclerView.Adapter<ChatRecAdapter.ChatRecHolder> {
    private static final String TAG = "ChatRecAdapter";

    private Context mContext;
    private List<Message> mMessages;
    private String currentDate;

    ChatRecAdapter(Context context, List<Message> messages) {
        mContext = context;
        mMessages = messages;
        currentDate = DataUtil.msFormMMDD(System.currentTimeMillis());
    }

    @NonNull
    @Override
    public ChatRecHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.chat_recycler_item, viewGroup, false);
        return new ChatRecHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ChatRecHolder holder, int position) {
        Message message = mMessages.get(position);
        long milliSecond = message.getCreateTime();
        String date = DataUtil.msFormMMDD(milliSecond);
        String time = DataUtil.msFormHHmmTime(milliSecond);
        if (position == 0 || DataUtil.isMoreThanOneDay(DataUtil.msFormMMDD(
                mMessages.get(position - 1).getCreateTime()), date)) {
            if (currentDate.equals(date)) {
                holder.receiveTime.setText("今天  " + time);
                holder.sendTime.setText("今天  " + time);
            } else if (DataUtil.isMoreThanOneDay(date, currentDate)) {
                holder.receiveTime.setText("昨天  " + time);
                holder.sendTime.setText("昨天  " + time);
            } else {
                holder.receiveTime.setText(date + "  " + time);
                holder.sendTime.setText(date + "  " + time);
            }
        } else {
            holder.receiveTime.setText(time);
            holder.sendTime.setText(time);
        }
        switch (message.getDirect()) {
            case receive:
                holder.leftLayout.setVisibility(View.VISIBLE);
                holder.rightLayout.setVisibility(View.GONE);
                initLeftContent(holder, message);
                break;
            case send:
                holder.leftLayout.setVisibility(View.GONE);
                holder.rightLayout.setVisibility(View.VISIBLE);
                initRightContent(holder, message);
                break;
            default:
                break;
        }
    }

    /**
     * 绑定接收的内容
     *
     * @param holder  ChatRecHolder
     * @param message 消息对象
     */
    private void initLeftContent(final ChatRecHolder holder, Message message) {
        UserInfo userInfo = message.getFromUser();
        userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
            @Override
            public void gotResult(int i, String s, Bitmap bitmap) {
                if (i == 0) {
                    Glide.with(mContext)
                            .load(bitmap)
                            .into(holder.leftCircle);
                } else {
                    LogUtil.d(TAG, "initLeftContent: " + "responseCode = " + i
                            + " ;desc = " + s);
                    Glide.with(mContext)
                            .load(R.drawable.icon_user)
                            .into(holder.leftCircle);
                }
            }
        });
        switch (message.getContentType()) {
            case text:
                TextContent textContent = (TextContent) message.getContent();
                holder.leftMsg.setText(textContent.getText());
                holder.leftImg.setVisibility(View.GONE);
                break;
            case image:
                ImageContent imageContent = (ImageContent) message.getContent();
                holder.leftMsg.setVisibility(View.GONE);
                holder.leftImg.setVisibility(View.VISIBLE);
                Glide.with(mContext)
                        .load(imageContent.getLocalThumbnailPath())
                        .placeholder(R.drawable.icon_img_reload)
                        .error(R.drawable.icon_img_load_fail)
                        .into(holder.leftImg);
                break;
        }
    }

    /**
     * 绑定接收的内容
     *
     * @param holder  ChatRecHolder
     * @param message 消息对象
     */
    private void initRightContent(final ChatRecHolder holder, Message message) {
        UserInfo userInfo = message.getFromUser();
        userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
            @Override
            public void gotResult(int i, String s, Bitmap bitmap) {
                if (i == 0) {
                    Glide.with(mContext)
                            .load(bitmap)
                            .into(holder.rightCircle);
                } else {
                    LogUtil.d(TAG, "initLeftContent: " + "responseCode = " + i
                            + " ;desc = " + s);
                    Glide.with(mContext)
                            .load(R.drawable.icon_user)
                            .into(holder.rightCircle);
                }
            }
        });
        switch (message.getContentType()) {
            case text:
                TextContent textContent = (TextContent) message.getContent();
                holder.rightMsg.setText(textContent.getText());
                holder.rightImg.setVisibility(View.GONE);
                break;
            case image:
                ImageContent imageContent = (ImageContent) message.getContent();
                holder.rightMsg.setVisibility(View.GONE);
                holder.rightImg.setVisibility(View.VISIBLE);
                Glide.with(mContext)
                        .load(imageContent.getLocalThumbnailPath())
                        .placeholder(R.drawable.icon_img_reload)
                        .error(R.drawable.icon_img_load_fail)
                        .into(holder.rightImg);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mMessages == null ? 0 : mMessages.size();
    }

    public class ChatRecHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        LinearLayout leftLayout, rightLayout, leftMsgLayout, rightMsgLayout;
        CircularImageView leftCircle, rightCircle;
        TextView leftMsg, receiveTime, sendTime, rightMsg;
        RoundedImageView leftImg, rightImg;

        ChatRecHolder(@NonNull View itemView) {
            super(itemView);
            leftLayout = itemView.findViewById(R.id.left_msg_linear);
            leftMsgLayout = itemView.findViewById(R.id.msg_receive_layout);
            rightLayout = itemView.findViewById(R.id.right_msg_linear);
            rightMsgLayout = itemView.findViewById(R.id.msg_send_layout);
            leftCircle = itemView.findViewById(R.id.user_icon_left);
            leftCircle.setOnClickListener(this);
            leftImg = itemView.findViewById(R.id.img_left);
            leftImg.setOnClickListener(this);
            leftMsg = itemView.findViewById(R.id.user_msg_left);
            receiveTime = itemView.findViewById(R.id.msg_time_receive);
            rightCircle = itemView.findViewById(R.id.user_icon_right);
            rightImg = itemView.findViewById(R.id.img_right);
            rightImg.setOnClickListener(this);
            rightMsg = itemView.findViewById(R.id.user_msg_right);
            sendTime = itemView.findViewById(R.id.msg_time_send);
        }

        @Override
        public void onClick(View view) {
            Message message = mMessages.get(getAdapterPosition());
            switch (view.getId()) {
                case R.id.user_icon_left:
                    Toast.makeText(mContext, "查看用户信息", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.img_left:
                    browseImg(message);
                    break;
                case R.id.img_right:
                    browseImg(message);
                    break;
                default:
                    break;
            }
        }

        /**
         * 获得原图的本地地址并跳转前往展示
         *
         * @param message 消息体
         */
        private void browseImg(Message message) {
            ImageContent imgContent = (ImageContent) message.getContent();
            final Intent intent = new Intent(mContext, PhotoBrowseActivity.class);
            final ArrayList<String> imgPath = new ArrayList<>();
            if (imgContent.getLocalPath() != null) {
                imgPath.add(imgContent.getLocalPath());
                intent.putStringArrayListExtra("photo_browse", imgPath);
                mContext.startActivity(intent);
            } else {
                imgContent.downloadOriginImage(message, new DownloadCompletionCallback() {
                    @Override
                    public void onComplete(int i, String s, File file) {
                        if (i == 0) {
                            LogUtil.d(TAG, "browseImg-onComplete: " + file.toString());
                            imgPath.add(file.getPath());
                            intent.putStringArrayListExtra("photo_browse", imgPath);
                            mContext.startActivity(intent);
                        } else {
                            LogUtil.d(TAG, "browseImg-onComplete: "
                                    + "responseCode = " + i + " ;desc = " + s);
                            Toast.makeText(mContext, "图片下载失败，请重试", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
}
