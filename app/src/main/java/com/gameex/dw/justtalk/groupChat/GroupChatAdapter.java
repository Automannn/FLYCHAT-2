package com.gameex.dw.justtalk.groupChat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import com.gameex.dw.justtalk.soundController.VoiceSpeaker;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;
import com.gameex.dw.justtalk.util.UserInfoUtils;
import com.gameex.dw.justtalk.util.WindowUtil;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.rey.material.widget.ProgressView;
import com.vanniktech.emoji.EmojiTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.enums.MessageStatus;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import es.dmoral.toasty.Toasty;
import jaygoo.widget.wlv.WaveLineView;
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
        View view = inflater.inflate(R.layout.recycler_item_group_chat, viewGroup, false);
        return new GroupChatHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull GroupChatHolder holder, int position) {
        Message message = mMessages.get(position);
        messageLinsten(message, holder.circlePros);
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
     * 判断消息状态，设置发送监听
     *
     * @param message 消息对象
     */
    private void messageLinsten(Message message, ProgressView circlePros) {
        if (message.getStatus() == MessageStatus.send_going) {
            circlePros.setVisibility(View.VISIBLE);
        }
        message.setOnSendCompleteCallback(new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                LogUtil.d(TAG, "messageLinsten: " + "responseCode = " + i
                        + " ;desc = " + s);
                if (i == 0) {
                    circlePros.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 绑定接收的内容
     *
     * @param holder  ChatRecHolder
     * @param message 消息对象
     */
    private void initLeftContent(final GroupChatHolder holder, Message message) {
        UserInfo userInfo = message.getFromUser();
        UserInfoUtils.initUserIcon(userInfo, mContext, holder.leftCircle);
        switch (message.getContentType()) {
            case text:
                TextContent textContent = (TextContent) message.getContent();
                holder.leftMsg.setVisibility(View.VISIBLE);
                holder.leftMsg.setText(textContent.getText());
                holder.leftImg.setVisibility(View.GONE);
                holder.redMsgLeft.setVisibility(View.GONE);
                holder.voiceMsgLeft.setVisibility(View.GONE);
                break;
            case image:
                ImageContent imageContent = (ImageContent) message.getContent();
                holder.leftMsg.setVisibility(View.GONE);
                holder.redMsgLeft.setVisibility(View.GONE);
                holder.voiceMsgLeft.setVisibility(View.GONE);
                holder.leftImg.setVisibility(View.VISIBLE);
                Glide.with(mContext)
                        .load(imageContent.getLocalThumbnailPath())
                        .into(holder.leftImg);
                break;
            case voice:
                VoiceContent voiceContent = (VoiceContent) message.getContent();
                holder.leftMsg.setVisibility(View.GONE);
                holder.leftImg.setVisibility(View.GONE);
                holder.redMsgLeft.setVisibility(View.GONE);
                holder.voiceMsgLeft.setVisibility(View.VISIBLE);
                holder.voiceDurationLeft.setText(voiceContent.getDuration() / 1000 + "");
                break;
            case custom:
                CustomContent customContent = (CustomContent) message.getContent();
                String blessings = customContent.getStringValue("blessings");
                holder.leftMsg.setVisibility(View.GONE);
                holder.leftImg.setVisibility(View.GONE);
                holder.voiceMsgLeft.setVisibility(View.GONE);
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
        UserInfoUtils.initUserIcon(userInfo, mContext, holder.rightCircle);
        switch (message.getContentType()) {
            case text:
                TextContent textContent = (TextContent) message.getContent();
                holder.rightMsg.setVisibility(View.VISIBLE);
                holder.rightMsg.setText(textContent.getText());
                holder.rightImg.setVisibility(View.GONE);
                holder.voiceMsgRight.setVisibility(View.GONE);
                holder.redMsgRight.setVisibility(View.GONE);
                break;
            case image:
                ImageContent imageContent = (ImageContent) message.getContent();
                holder.rightMsg.setVisibility(View.GONE);
                holder.rightImg.setVisibility(View.VISIBLE);
                holder.redMsgRight.setVisibility(View.GONE);
                holder.voiceMsgRight.setVisibility(View.GONE);
                Glide.with(mContext)
                        .load(imageContent.getLocalThumbnailPath())
                        .into(holder.rightImg);
                break;
            case voice:
                VoiceContent voiceContent = (VoiceContent) message.getContent();
                holder.rightMsg.setVisibility(View.GONE);
                holder.rightImg.setVisibility(View.GONE);
                holder.redMsgRight.setVisibility(View.GONE);
                holder.voiceMsgRight.setVisibility(View.VISIBLE);
                holder.voiceDurationRight.setText(voiceContent.getDuration() / 1000 + "");
                break;
            case custom:
                CustomContent customContent = (CustomContent) message.getContent();
                String blessings = customContent.getStringValue("blessings");
                holder.rightMsg.setVisibility(View.GONE);
                holder.rightImg.setVisibility(View.GONE);
                holder.voiceMsgRight.setVisibility(View.GONE);
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
        LinearLayout leftLayout, rightLayout, leftMsgLayout, rightMsgLayout, redMsgLeft, redMsgRight, voiceMsgLeft, voiceMsgRight;
        CircularImageView leftCircle, rightCircle;
        TextView receiveTime, sendTime, redMessageLeft, redMessageRight, voiceDurationLeft, voiceDurationRight;
        ImageView open;
        EmojiTextView leftMsg, rightMsg;
        RoundedImageView leftImg, rightImg;
        WaveLineView voiceLineLeft, voiceLineRight;
        ProgressView circlePros;

        private PopupWindow redPup;

        private boolean flag = true;
        private int counter;

        GroupChatHolder(@NonNull View itemView) {
            super(itemView);
            showRedPup();
            int[] ints = WindowUtil.getWH(GroupChatActivity.sActivity);
            //发送方消息ui
            leftLayout = itemView.findViewById(R.id.left_msg_linear);
            leftMsgLayout = itemView.findViewById(R.id.msg_receive_layout);
            //发送方头像
            leftCircle = itemView.findViewById(R.id.user_icon_left);
            leftCircle.setOnClickListener(this);
            //发送方发送的语音信息
            voiceMsgLeft = itemView.findViewById(R.id.voice_msg_left);
            voiceMsgLeft.setOnClickListener(this);
            voiceLineLeft = itemView.findViewById(R.id.voice_line_left);
            voiceDurationLeft = itemView.findViewById(R.id.voice_duration_left);
            //发送方图片消息
            leftImg = itemView.findViewById(R.id.img_left);
            leftImg.setMaxWidth(ints[0] / 2);
            leftImg.setMaxHeight(ints[1] / 3);
            leftImg.setScaleType(ImageView.ScaleType.CENTER);
            leftImg.setOnClickListener(this);
            //发送方文本消息
            leftMsg = itemView.findViewById(R.id.user_msg_left);
            leftMsg.setOnClickListener(this);
            //发送方红包消息
            redMsgLeft = itemView.findViewById(R.id.red_msg_left);
            redMsgLeft.setOnClickListener(this);
            redMessageLeft = itemView.findViewById(R.id.red_message_left);
            //发送方发送消息的时间
            receiveTime = itemView.findViewById(R.id.msg_time_receive);

            //用户消息ui
            rightLayout = itemView.findViewById(R.id.right_msg_linear);
            rightMsgLayout = itemView.findViewById(R.id.msg_send_layout);
            //用户头像
            rightCircle = itemView.findViewById(R.id.user_icon_right);
            rightCircle.setOnClickListener(this);
            //用户收到的语音信息
            voiceMsgRight = itemView.findViewById(R.id.voice_msg_right);
            voiceMsgRight.setOnClickListener(this);
            voiceLineRight = itemView.findViewById(R.id.voice_line_right);
            voiceDurationRight = itemView.findViewById(R.id.voice_duration_right);
            ////用户发送的图片消息
            rightImg = itemView.findViewById(R.id.img_right);
            rightImg.setMaxWidth(ints[0] / 2);
            rightImg.setMaxHeight(ints[1] / 3);
            rightImg.setScaleType(ImageView.ScaleType.CENTER);
            rightImg.setOnClickListener(this);
            //用户发送的文本消息
            rightMsg = itemView.findViewById(R.id.user_msg_right);
            rightMsg.setOnClickListener(this);
            //用户发送的红包消息
            redMsgRight = itemView.findViewById(R.id.red_msg_right);
            redMsgRight.setOnClickListener(this);
            redMessageRight = itemView.findViewById(R.id.red_message_right);
            //用户发送消息的时间
            sendTime = itemView.findViewById(R.id.msg_time_send);
            //进度展示
            circlePros = itemView.findViewById(R.id.progress);
        }

        @Override
        public void onClick(View view) {
            Message message = mMessages.get(getAdapterPosition());
            switch (view.getId()) {
                case R.id.user_icon_left:
                    Toast.makeText(mContext, "查看用户信息", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.voice_msg_left:   //发送方语音信息
                    VoiceContent voiceReceive = (VoiceContent) message.getContent();
                    voiceReceive.downloadVoiceFile(message, new DownloadCompletionCallback() {
                        @Override
                        public void onComplete(int i, String s, File file) {
                            if (i == 0) {
                                flag = true;
                                VoiceSpeaker.getInstance().speakSingle(file.getAbsolutePath());
                                startCountDown(voiceLineLeft, voiceDurationLeft, voiceReceive.getDuration());
                            } else {
                                Toasty.error(mContext, "文件拉取失败", Toasty.LENGTH_SHORT).show();
                                LogUtil.e(TAG, "onClick-voice_msg_left: "
                                        + "responseCode = " + i + " ;desc = " + s);
                            }
                        }
                    });
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
                    break;
                case R.id.voice_msg_right:  //接收到的语音信息
                    VoiceContent voiceSend = (VoiceContent) message.getContent();
                    voiceSend.downloadVoiceFile(message, new DownloadCompletionCallback() {
                        @Override
                        public void onComplete(int i, String s, File file) {
                            if (i == 0) {
                                flag = true;
                                VoiceSpeaker.getInstance().speakSingle(file.getAbsolutePath());
                                startCountDown(voiceLineRight, voiceDurationRight, voiceSend.getDuration());
                            } else {
                                Toasty.error(mContext, "文件拉取失败", Toasty.LENGTH_SHORT).show();
                                LogUtil.e(TAG, "onClick-voice_msg_right: "
                                        + "responseCode = " + i + " ;desc = " + s);
                            }
                        }
                    });
                    break;
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
        @SuppressLint("ClickableViewAccessibility")
        private void showRedPup() {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.popup_red_package, null);
            ImageView close = view.findViewById(R.id.close);
            close.setOnClickListener(this);
            open = view.findViewById(R.id.open);
            open.setOnClickListener(this);
            redPup = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            redPup.setFocusable(true);
            redPup.setOutsideTouchable(true);
            redPup.setTouchInterceptor((view1, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    redPup.dismiss();
                    return true;
                }
                return false;
            });
            redPup.setOnDismissListener(() -> WindowUtil.setWindowBackgroundAlpha(sActivity, 1f));
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
         * 倒计时
         */
        private void startCountDown(WaveLineView waveLine, TextView text
                , int duration) {
            waveLine.startAnim();
            counter = duration / 1000;
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    handler.postDelayed(this, 1000);
                    if (flag) {
                        counter--;
                        if (counter < 0) {
                            flag = false;
                            waveLine.stopAnim();
                            text.setText(duration / 1000 + "");
                            handler.removeCallbacks(this);
                            return;
                        }
                        text.setText(counter + "");
                    }
                }
            };
            handler.post(runnable);
        }
    }
}
