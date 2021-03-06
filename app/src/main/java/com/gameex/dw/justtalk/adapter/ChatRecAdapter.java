package com.gameex.dw.justtalk.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
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

import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.activity.ChattingActivity;
import com.gameex.dw.justtalk.activity.RedDetailActivity;
import com.gameex.dw.justtalk.activity.UserBasicInfoActivity;
import com.gameex.dw.justtalk.activity.UserInfoActivity;
import com.gameex.dw.justtalk.animation.AwardRotateAnimation;
import com.gameex.dw.justtalk.activity.PhotoBrowseActivity;
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
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.enums.MessageStatus;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import es.dmoral.toasty.Toasty;
import jaygoo.widget.wlv.WaveLineView;
import okhttp3.Call;
import okhttp3.Response;

import static com.gameex.dw.justtalk.adapter.GroupChatAdapter.SNATCH_PACKAET;

/**
 * 单聊界面，信息显示适配器
 */
public class ChatRecAdapter extends RecyclerView.Adapter<ChatRecAdapter.ChatRecHolder> {
    private static final String TAG = "ChatRecAdapter";

    private Context mContext;
    private List<Message> mMessages;
    private String currentDate;

    public ChatRecAdapter(Context context, List<Message> messages) {
        mContext = context;
        mMessages = messages;
        currentDate = DataUtil.msFormMMDD(System.currentTimeMillis());
    }

    @NonNull
    @Override
    public ChatRecHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.recycler_item_chat, viewGroup, false);
        return new ChatRecHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ChatRecHolder holder, int position) {
        Message message = mMessages.get(position);
        messageLinsten(message, holder.circlePros);
        long milliSecond = message.getCreateTime();
        String date = DataUtil.msFormMMDD(milliSecond);
        String time = DataUtil.msFormHHmmTime(milliSecond);
        if (position == 0 || DataUtil.isMoreThanOneDay(DataUtil.msFormMMDD( //判断是否超过一天
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
    private void initLeftContent(final ChatRecHolder holder, Message message) {
        UserInfo userInfo = message.getFromUser();
        UserInfoUtils.initUserIcon(userInfo, mContext, holder.leftCircle);
        switch (message.getContentType()) {
            case text:
                TextContent textContent = (TextContent) message.getContent();
                holder.leftMsg.setText(textContent.getText());
                holder.leftImg.setVisibility(View.GONE);
                holder.leftMsg.setVisibility(View.VISIBLE);
                holder.voiceMsgLeft.setVisibility(View.GONE);
                holder.redMsgLeft.setVisibility(View.GONE);
                break;
            case image:
                ImageContent imageContent = (ImageContent) message.getContent();
                holder.redMsgLeft.setVisibility(View.GONE);
                holder.leftMsg.setVisibility(View.GONE);
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
    private void initRightContent(final ChatRecHolder holder, Message message) {
        UserInfo userInfo = message.getFromUser();
        UserInfoUtils.initUserIcon(userInfo, mContext, holder.rightCircle);
        switch (message.getContentType()) {
            case text:
                TextContent textContent = (TextContent) message.getContent();
                holder.rightMsg.setText(textContent.getText());
                holder.redMsgRight.setVisibility(View.GONE);
                holder.rightImg.setVisibility(View.GONE);
                holder.voiceMsgRight.setVisibility(View.GONE);
                holder.rightMsg.setVisibility(View.VISIBLE);
                break;
            case image:
                ImageContent imageContent = (ImageContent) message.getContent();
                holder.redMsgRight.setVisibility(View.GONE);
                holder.rightMsg.setVisibility(View.GONE);
                holder.voiceMsgRight.setVisibility(View.GONE);
                holder.rightImg.setVisibility(View.VISIBLE);
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

    public class ChatRecHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        LinearLayout leftLayout, rightLayout, leftMsgLayout, rightMsgLayout, redMsgLeft, redMsgRight, voiceMsgLeft, voiceMsgRight;
        CircularImageView leftCircle, rightCircle;
        EmojiTextView leftMsg, rightMsg;
        TextView receiveTime, sendTime, redMessageLeft, redMessageRight, voiceDurationLeft, voiceDurationRight;
        /**
         * 红包头像
         */
        CircularImageView icon;
        /**
         * 红包祝福语
         */
        TextView bless;
        ImageView open;
        RoundedImageView leftImg, rightImg;
        WaveLineView voiceLineLeft, voiceLineRight;
        ProgressView circlePros;

        private PopupWindow redPup;

        private boolean flag = true;
        private int counter;

        ChatRecHolder(@NonNull View itemView) {
            super(itemView);
            showRedPup();
            int[] ints = WindowUtil.getWH(ChattingActivity.sBaseActivity);
            //发送方发送的各种信息ui
            leftLayout = itemView.findViewById(R.id.left_msg_linear);
            leftMsgLayout = itemView.findViewById(R.id.msg_receive_layout);
            //发送方头像
            leftCircle = itemView.findViewById(R.id.user_icon_left);
            leftCircle.setOnClickListener(this);
            //发送方发送的语音信息
            voiceMsgLeft = itemView.findViewById(R.id.voice_msg_left);
            voiceMsgLeft.setOnClickListener(this);
            voiceMsgLeft.setOnLongClickListener(this);
            voiceLineLeft = itemView.findViewById(R.id.voice_line_left);
            voiceDurationLeft = itemView.findViewById(R.id.voice_duration_left);
            //发送方发送的图片信息
            leftImg = itemView.findViewById(R.id.img_left);
            leftImg.setMaxWidth(ints[0] / 2);
            leftImg.setMaxHeight(ints[1] / 3);
            leftImg.setOnClickListener(this);
            leftImg.setOnLongClickListener(this);
            //用户接收的文字信息
            leftMsg = itemView.findViewById(R.id.user_msg_left);
            //发送方发送的红包信息
            redMsgLeft = itemView.findViewById(R.id.red_msg_left);
            redMsgLeft.setOnClickListener(this);
            redMsgLeft.setOnLongClickListener(this);
            redMessageLeft = itemView.findViewById(R.id.red_message_left);
            //发送方发送的时间
            receiveTime = itemView.findViewById(R.id.msg_time_receive);

            //用户发送的各种信息ui
            rightLayout = itemView.findViewById(R.id.right_msg_linear);
            rightMsgLayout = itemView.findViewById(R.id.msg_send_layout);
            //用户头像
            rightCircle = itemView.findViewById(R.id.user_icon_right);
            rightCircle.setOnClickListener(this);
            //用户收到的语音信息
            voiceMsgRight = itemView.findViewById(R.id.voice_msg_right);
            voiceMsgRight.setOnClickListener(this);
            voiceMsgRight.setOnLongClickListener(this);
            voiceLineRight = itemView.findViewById(R.id.voice_line_right);
            voiceDurationRight = itemView.findViewById(R.id.voice_duration_right);
            //用户收到的图片信息
            rightImg = itemView.findViewById(R.id.img_right);
            rightImg.setMaxWidth(ints[0] / 2);
            rightImg.setMaxHeight(ints[1] / 3);
            rightImg.setOnClickListener(this);
            rightImg.setOnLongClickListener(this);
            //用户发送的文字信息
            rightMsg = itemView.findViewById(R.id.user_msg_right);
            rightMsg.setOnLongClickListener(this);
            //用户收到的红包信息
            redMsgRight = itemView.findViewById(R.id.red_msg_right);
            redMsgRight.setOnClickListener(this);
            redMsgRight.setOnLongClickListener(this);
            redMessageRight = itemView.findViewById(R.id.red_message_right);
            //用户收到信息的时间
            sendTime = itemView.findViewById(R.id.msg_time_send);
            //进度展示
            circlePros = itemView.findViewById(R.id.progress);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            Message message = mMessages.get(getAdapterPosition());
            UserInfo userInfo = message.getFromUser();
            switch (view.getId()) {
                case R.id.user_icon_left:   //发送方头像
                    intent.setClass(mContext, UserBasicInfoActivity.class);
                    intent.putExtra("user_info_json", userInfo.toJson());
                    mContext.startActivity(intent);
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
                case R.id.red_msg_left: //发送方红包信息
                    CustomContent customContentLeft = (CustomContent) message.getContent();
                    if (customContentLeft != null &&
                            !TextUtils.isEmpty(customContentLeft.getStringValue("yuan"))) {
                        UserInfo user = UserInfo.fromJson(customContentLeft.getStringValue("userInfo"));
                        if (redPup != null) {
                            if (user != null) {
                                UserInfoUtils.initUserIcon(user, mContext, icon);
                            }
                            bless.setText(customContentLeft.getStringValue("blessings"));
                            redPup.showAtLocation(rightMsg
                                    , Gravity.CENTER, 0, 0);
                            WindowUtil.showBackgroundAnimator(ChattingActivity.sBaseActivity
                                    , 0.5f);
                        }
                    }
                    break;
//                case R.id.red_msg_right:
//                    CustomContent customContentRight = (CustomContent) message.getContent();
//                    if (customContentRight != null &&
//                            !TextUtils.isEmpty(customContentRight.getStringValue("yuan"))) {
//                        Toast.makeText(mContext, "查看钱包领取情况", Toast.LENGTH_SHORT).show();
//                        if (redPup != null) {
//                            redPup.showAtLocation(rightMsg
//                                    , Gravity.CENTER, 0, 0);
//                            WindowUtil.showBackgroundAnimator(sActivity, 0.5f);
//                        }
//                    }
//                    break;
                case R.id.user_icon_right:  //用户头像
                    intent.setClass(mContext, UserInfoActivity.class);
                    intent.putExtra("mine_info", userInfo.toJson());
                    mContext.startActivity(intent);
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
                                Toasty.error(mContext, "音频拉取失败", Toasty.LENGTH_SHORT).show();
                                LogUtil.e(TAG, "onClick-voice_msg_right: "
                                        + "responseCode = " + i + " ;desc = " + s);
                            }
                        }
                    });
                    break;
                case R.id.close:    //关闭红包弹窗
                    if (redPup != null && redPup.isShowing()) {
                        redPup.dismiss();
                    }
                    break;
                case R.id.open: //打开红包
                    CustomContent customContentOpen = (CustomContent) message.getContent();
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
                    AwardRotateAnimation animation = new AwardRotateAnimation();
//                    animation.setRepeatCount(Animation.INFINITE);
//                    animation.setInterpolator(new OvershootInterpolator());
                    open.startAnimation(animation);
                    snatchPacket(customContentOpen.getStringValue("token")
                            , pref.getString("userId", ""));
                    break;
                case R.id.img_left: //发送方图片信息
                    browseImg(message); //查看图片
                    break;
                case R.id.img_right:    //接收到的图片信息
                    browseImg(message);
                    break;
                default:
                    break;
            }
        }

        /**
         * 对文本消息的长按操作
         */
        private void showMsgDialog() {
            Message message = mMessages.get(getAdapterPosition());
            UserInfo userInfo = (UserInfo) message.getTargetInfo();
            Conversation conversation = JMessageClient.getSingleConversation(userInfo.getUserName());

            Dialog dialog = new Dialog(mContext, R.style.qr_code_dialog_style);
            dialog.setContentView(R.layout.dialog_msg_work);
            TextView deleteLocal = dialog.findViewById(R.id.delete_local);    //从本地删除
            deleteLocal.setOnClickListener(view -> {
                if (conversation != null) {
                    boolean success = conversation.deleteMessage(message.getId());
                    if (success) {
                        mMessages.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                    } else {
                        Toasty.info(mContext, "删除失败").show();
                    }
                    dialog.dismiss();
                }
            });
            TextView deleteOnline = dialog.findViewById(R.id.delete_online);  //从服务器上删除
            deleteOnline.setOnClickListener(view -> {
                if (conversation != null) {
                    boolean success = conversation.deleteMessage(message.getId());
                    if (success) {
                        mMessages.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                    } else {
                        Toasty.info(mContext, "删除失败").show();
                    }
                    dialog.dismiss();
                }
            });
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
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
                            Toasty.error(mContext, "图片下载失败", Toasty.LENGTH_SHORT).show();
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
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.popup_red_package, null);
            ImageView close = view.findViewById(R.id.close);
            close.setOnClickListener(this);
            icon = view.findViewById(R.id.group_award_logo);
            bless = view.findViewById(R.id.group_award_object);
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
            redPup.setOnDismissListener(() -> WindowUtil.setWindowBackgroundAlpha(ChattingActivity.sBaseActivity
                    , 1f));
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
                    Toasty.error(mContext, "网络异常", Toasty.LENGTH_SHORT).show();
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
                                    Toasty.success(mContext, data + "", Toasty.LENGTH_SHORT).show();
                                    intent.putExtra("amount", data);
                                } else {
                                    JSONObject dataObject = object.getJSONObject("data");
                                    int code = dataObject.getInt("code");
                                    String message = dataObject.getString("message");
                                    LogUtil.d(TAG, "snatchPacket-onResponse-success=false: " +
                                            "code = " + code + " ;message = " + message);
                                    Toasty.error(mContext, message + "", Toasty.LENGTH_SHORT).show();
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

        @Override
        public boolean onLongClick(View view) {
            showMsgDialog();
            return true;
        }
    }
}
