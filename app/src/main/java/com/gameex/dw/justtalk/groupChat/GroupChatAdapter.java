package com.gameex.dw.justtalk.groupChat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.animation.AwardRotateAnimation;
import com.gameex.dw.justtalk.imgBrowse.PhotoBrowseActivity;
import com.gameex.dw.justtalk.redPackage.RedDetailActivity;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;
import com.gameex.dw.justtalk.util.WindowUtil;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.github.siyamed.shapeimageview.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import okhttp3.Call;
import okhttp3.Response;

import static com.gameex.dw.justtalk.groupChat.GroupChatActivity.sActivity;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.GroupChatHolder> {
    private static final String TAG = "GroupChatActivity";
    public static final String SNATCH_PACKAET = "account/snatchpacket";

    private Context mContext;
    private List<Message> mMessages;
    private String currentDate;

    GroupChatAdapter(Context context, List<Message> messages) {
        mContext = context;
        mMessages = messages;
        currentDate = DataUtil.msFormMMDD(System.currentTimeMillis());
    }

    @NonNull
    @Override
    public GroupChatHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.group_chat_item, viewGroup, false);
        return new GroupChatHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull GroupChatHolder holder, int position) {
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
    private void initLeftContent(final GroupChatHolder holder, Message message) {
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
                holder.leftMsg.setVisibility(View.VISIBLE);
                holder.leftMsg.setText(textContent.getText());
                holder.leftImg.setVisibility(View.GONE);
                holder.redMsgLeft.setVisibility(View.GONE);
                break;
            case image:
                ImageContent imageContent = (ImageContent) message.getContent();
                holder.leftMsg.setVisibility(View.GONE);
                holder.redMsgLeft.setVisibility(View.GONE);
                holder.leftImg.setVisibility(View.VISIBLE);
                Glide.with(mContext)
                        .load(imageContent.getLocalThumbnailPath())
                        .into(holder.leftImg);
                break;
            case custom:
                CustomContent customContent = (CustomContent) message.getContent();
                String blessings = customContent.getStringValue("blessings");
                holder.leftMsg.setVisibility(View.GONE);
                holder.leftImg.setVisibility(View.GONE);
                holder.redMsgLeft.setVisibility(View.VISIBLE);
                holder.redMessageLeft.setText(blessings);
                break;
        }
    }

    /**
     * 绑定接收的内容
     *
     * @param holder  ChatRecHolder
     * @param message 消息对象
     */
    private void initRightContent(final GroupChatHolder holder, Message message) {
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
                holder.rightMsg.setVisibility(View.VISIBLE);
                holder.rightMsg.setText(textContent.getText());
                holder.rightImg.setVisibility(View.GONE);
                holder.redMsgRight.setVisibility(View.GONE);
                break;
            case image:
                ImageContent imageContent = (ImageContent) message.getContent();
                holder.rightMsg.setVisibility(View.GONE);
                holder.rightImg.setVisibility(View.VISIBLE);
                holder.redMsgRight.setVisibility(View.GONE);
                Glide.with(mContext)
                        .load(imageContent.getLocalThumbnailPath())
                        .into(holder.rightImg);
                break;
            case custom:
                CustomContent customContent = (CustomContent) message.getContent();
                String blessings = customContent.getStringValue("blessings");
                holder.rightMsg.setVisibility(View.GONE);
                holder.rightImg.setVisibility(View.GONE);
                holder.redMsgRight.setVisibility(View.VISIBLE);
                holder.redMessageRight.setText(blessings);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mMessages == null ? 0 : mMessages.size();
    }

    class GroupChatHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout leftLayout, rightLayout, leftMsgLayout, rightMsgLayout, redMsgLeft, redMsgRight;
        CircularImageView leftCircle, rightCircle;
        TextView leftMsg, receiveTime, sendTime, rightMsg, redMessageLeft, redMessageRight;
        ImageView open;
        RoundedImageView leftImg, rightImg;

        private PopupWindow redPup;

        GroupChatHolder(@NonNull View itemView) {
            super(itemView);
            showRedPup();
            int[] ints = WindowUtil.getWH(GroupChatActivity.sActivity);
            leftLayout = itemView.findViewById(R.id.left_msg_linear);
            leftMsgLayout = itemView.findViewById(R.id.msg_receive_layout);
            rightLayout = itemView.findViewById(R.id.right_msg_linear);
            rightMsgLayout = itemView.findViewById(R.id.msg_send_layout);
            leftCircle = itemView.findViewById(R.id.user_icon_left);
            leftCircle.setOnClickListener(this);
            leftImg = itemView.findViewById(R.id.img_left);
            leftImg.setMaxWidth(ints[0] / 2);
            leftImg.setMaxHeight(ints[1] / 3);
            leftImg.setScaleType(ImageView.ScaleType.CENTER);
            leftImg.setOnClickListener(this);
            leftMsg = itemView.findViewById(R.id.user_msg_left);
            leftMsg.setOnClickListener(this);
            redMsgLeft = itemView.findViewById(R.id.red_msg_left);
            redMessageLeft = itemView.findViewById(R.id.red_message_left);
            receiveTime = itemView.findViewById(R.id.msg_time_receive);
            rightCircle = itemView.findViewById(R.id.user_icon_right);
            rightCircle.setOnClickListener(this);
            rightImg = itemView.findViewById(R.id.img_right);
            rightImg.setMaxWidth(ints[0] / 2);
            rightImg.setMaxHeight(ints[1] / 3);
            rightImg.setScaleType(ImageView.ScaleType.CENTER);
            rightImg.setOnClickListener(this);
            rightMsg = itemView.findViewById(R.id.user_msg_right);
            rightMsg.setOnClickListener(this);
            redMsgRight = itemView.findViewById(R.id.red_msg_right);
            redMessageRight = itemView.findViewById(R.id.red_message_right);
            sendTime = itemView.findViewById(R.id.msg_time_send);
        }

        @Override
        public void onClick(View view) {
            Message message = mMessages.get(getAdapterPosition());
            switch (view.getId()) {
                case R.id.user_icon_left:
                    Toast.makeText(mContext, "查看用户信息", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.red_msg_left:
                    CustomContent customContentLeft = (CustomContent) message.getContent();
                    if (customContentLeft != null &&
                            !TextUtils.isEmpty(customContentLeft.getStringValue("yuan"))) {
                        Toast.makeText(mContext, "打开钱包", Toast.LENGTH_SHORT).show();
                        if (redPup != null) {
                            redPup.showAtLocation(rightMsg
                                    , Gravity.CENTER, 0, 0);
                            WindowUtil.showBackgroundAnimator(sActivity, 0.5f);
                        }
                    }
                    break;
                case R.id.red_msg_right:
                    CustomContent customContentRight = (CustomContent) message.getContent();
                    if (customContentRight != null &&
                            !TextUtils.isEmpty(customContentRight.getStringValue("yuan"))) {
                        Toast.makeText(mContext, "查看钱包领取情况", Toast.LENGTH_SHORT).show();
                        if (redPup != null) {
                            redPup.showAtLocation(rightMsg
                                    , Gravity.CENTER, 0, 0);
                            WindowUtil.showBackgroundAnimator(sActivity, 0.5f);
                        }
                    }
                    break;
                case R.id.user_icon_right:
                    Toast.makeText(mContext, "查看自己的信息", Toast.LENGTH_SHORT).show();
                case R.id.close:
                    if (redPup != null && redPup.isShowing()) {
                        redPup.dismiss();
                    }
                    break;
                case R.id.open:
                    CustomContent customContentOpen = (CustomContent) message.getContent();
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
                    AwardRotateAnimation animation = new AwardRotateAnimation();
//                    animation.setRepeatCount(Animation.INFINITE);
//                    animation.setInterpolator(new OvershootInterpolator());
                    open.startAnimation(animation);
//                    getTimer();
                    snatchPacket(customContentOpen.getStringValue("token")
                            , pref.getString("userId", ""));
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

        /**
         * 红包弹出框
         */
        private void showRedPup() {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.popup_red_package, null);
            ImageView close = view.findViewById(R.id.close);
            close.setOnClickListener(this);
            open = view.findViewById(R.id.open);
            open.setOnClickListener(this);
            redPup = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            redPup.setFocusable(true);
            redPup.setOutsideTouchable(true);
            redPup.setTouchInterceptor(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_OUTSIDE) {
                        redPup.dismiss();
                        return true;
                    }
                    return false;
                }
            });
            redPup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    WindowUtil.setWindowBackgroundAlpha(sActivity, 1f);
                }
            });
            redPup.setAnimationStyle(R.style.translate_scale_alpha_style);
            redPup.update();
        }

        /**
         * 抢红包
         *
         * @param token  pubKey
         * @param userId 自家服务器上的userId
         */
        private void snatchPacket(String token, String userId) {
            HashMap<String, String> paramsMap = new HashMap<>();
            paramsMap.put("token", token);
            paramsMap.put("userId", userId);
            OkHttpUtil.okHttpPost(SNATCH_PACKAET, paramsMap, new CallBackUtil.CallBackDefault() {
                @Override
                public void onFailure(Call call, Exception e) {
                    LogUtil.d(TAG, "snatchPacket-onFailure: ");
                    e.printStackTrace();
                    Toast.makeText(mContext, "网络异常", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(Response response) {
                    if (response != null && response.isSuccessful()) {
                        try {
                            JSONObject object = null;
                            if (response.body() != null) {
                                object = new JSONObject(response.body().string());
                            }
                            if (object != null) {
                                Intent intent = new Intent(mContext, RedDetailActivity.class);
                                boolean success = object.getBoolean("success");
                                if (success) {
                                    String data = object.getString("data");
                                    Toast.makeText(mContext, data + "", Toast.LENGTH_SHORT).show();
                                    intent.putExtra("amount", data);
                                } else {
                                    JSONObject dataObject = object.getJSONObject("data");
                                    int code = dataObject.getInt("code");
                                    String message = dataObject.getString("message");
                                    LogUtil.d(TAG, "snatchPacket-onResponse-success=false: " +
                                            "code = " + code + " ;message = " + message);
                                    Toast.makeText(mContext, message + "", Toast.LENGTH_SHORT).show();
                                    intent.putExtra("amount", message);
                                }
                                if (redPup.isShowing()) {
                                    redPup.dismiss();
                                }
                                mContext.startActivity(intent);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        /**
         * 定时执行的动作
         */
        private void getTimer() {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    mContext.startActivity(new Intent(mContext, RedDetailActivity.class));
                    if (redPup.isShowing()) {
                        redPup.dismiss();
                    }
                }
            }, 850);
        }
    }
}
