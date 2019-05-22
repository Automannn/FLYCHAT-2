package com.gameex.dw.justtalk.chattingPack;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.ObjPack.Msg;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.animation.AwardRotateAnimation;
import com.gameex.dw.justtalk.redPackage.RedDetailActivity;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;
import com.gameex.dw.justtalk.util.WindowUtil;
import com.github.siyamed.shapeimageview.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Response;

import static com.gameex.dw.justtalk.chattingPack.GroupChatActivity.sActivity;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.GroupChatHolder> {
    private static final String TAG = "GroupChatActivity";
    private static final String SNATCH_PACKAET = "account/snatchpacket";

    private Context mContext;
    private List<Msg> mList;
    private String currentDate;

    GroupChatAdapter(Context context, List<Msg> list) {
        mContext = context;
        mList = list;
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
        ImageView open;

        private PopupWindow redPup;

        GroupChatHolder(@NonNull View itemView) {
            super(itemView);
            showRedPup();
            leftLayout = itemView.findViewById(R.id.left_msg_linear);
            leftMsgLayout = itemView.findViewById(R.id.msg_receive_layout);
            rightLayout = itemView.findViewById(R.id.right_msg_linear);
            rightMsgLayout = itemView.findViewById(R.id.msg_send_layout);
            leftCircle = itemView.findViewById(R.id.user_icon_left);
            leftCircle.setOnClickListener(this);
            leftMsg = itemView.findViewById(R.id.user_msg_left);
            leftMsg.setOnClickListener(this);
            receiveTime = itemView.findViewById(R.id.msg_time_receive);
            rightCircle = itemView.findViewById(R.id.user_icon_right);
            rightCircle.setOnClickListener(this);
            rightMsg = itemView.findViewById(R.id.user_msg_right);
            rightMsg.setOnClickListener(this);
            sendTime = itemView.findViewById(R.id.msg_time_send);
        }

        @Override
        public void onClick(View view) {
            Msg msg = mList.get(getAdapterPosition());
            Msg.MsgType msgType = msg.getMsgType();
            switch (view.getId()) {
                case R.id.user_icon_left:
                    Toast.makeText(mContext, "查看用户信息", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.user_msg_left:
                    if (msgType != null && msgType.equals(Msg.MsgType.RED_PACKAGE)) {
                        Toast.makeText(mContext, "打开钱包", Toast.LENGTH_SHORT).show();
                        if (redPup != null) {
                            redPup.showAtLocation(rightMsg
                                    , Gravity.CENTER, 0, 0);
                            WindowUtil.showBackgroundAnimator(sActivity, 0.5f);
                        }
                    }
                    break;
                case R.id.user_msg_right:
                    if (msgType != null && msgType.equals(Msg.MsgType.RED_PACKAGE)) {
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
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
                    AwardRotateAnimation animation = new AwardRotateAnimation();
//                    animation.setRepeatCount(Animation.INFINITE);
//                    animation.setInterpolator(new OvershootInterpolator());
                    open.startAnimation(animation);
//                    getTimer();
                    snatchPacket(msg.getRedToken(), pref.getString("userId", ""));
                    break;
                default:
                    break;
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
