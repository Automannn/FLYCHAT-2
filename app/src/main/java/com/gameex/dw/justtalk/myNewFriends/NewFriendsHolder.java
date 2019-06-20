package com.gameex.dw.justtalk.myNewFriends;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.objPack.MsgInfo;
import com.gameex.dw.justtalk.userInfo.UserBasicInfoActivity;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import es.dmoral.toasty.Toasty;

import static com.gameex.dw.justtalk.main.ContactFragment.ADD_CONTACT;
import static com.gameex.dw.justtalk.main.MsgInfoFragment.UPDATE_MSG_INFO;
import static com.gameex.dw.justtalk.myNewFriends.NewFriendsActivity.DELETE_RECEIVE;

public class NewFriendsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static final String TAG = "NewFriendsHolder";
    /**
     * 上下文
     */
    private Context mContext;
    /**
     * 收到好友添加请求的日期
     */
    TextView date;
    /**
     * relative layout
     */
    private RelativeLayout newFriend;
    /**
     * 头像
     */
    CircularImageView icon;
    /**
     * 用户昵称或手机号
     */
    TextView name;
    /**
     * 请求添加的理由
     */
    TextView reason;
    /**
     * 接收好友添加请求
     */
    private Button accept;
    /**
     * 发送方用户信息对象集合json串
     */
    private List<Map<String, String>> mDatas;

    public NewFriendsHolder(@NonNull View itemView, Context context, List<Map<String, String>> datas) {
        super(itemView);
        this.mContext = context;
        this.mDatas = datas;
        date = itemView.findViewById(R.id.date);
        newFriend = itemView.findViewById(R.id.new_friend);
        newFriend.setOnClickListener(this);
        icon = itemView.findViewById(R.id.icon);
        name = itemView.findViewById(R.id.name);
        reason = itemView.findViewById(R.id.reason);
        accept = itemView.findViewById(R.id.accept);
        accept.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        Map<String, String> map = mDatas.get(getAdapterPosition());
        switch (view.getId()) {
            case R.id.new_friend:   //进入基本信息界面
                intent.setClass(mContext, UserBasicInfoActivity.class);
                intent.putExtra("user_info_json"
                        , map.get("userInfo"));
                mContext.startActivity(intent);
                break;
            case R.id.accept:   //接收好友请求
                String username = map.get("username");
                String date = map.get("date");
                ContactManager.acceptInvitation(username, null, new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {
                        if (i == 0) {
                            Intent intentMsg = new Intent();
                            intentMsg.setAction(UPDATE_MSG_INFO);
                            MsgInfo msgInfo = new MsgInfo(username, date
                                    , "你们已经是好友了，聊点什么吧...", true);
                            msgInfo.setSingle(true);
                            intentMsg.putExtra("msg_info", msgInfo);
                            mContext.sendBroadcast(intentMsg);
                            intent.setAction(ADD_CONTACT);
                            intent.putExtra("username", username);
                            mContext.sendBroadcast(intent);
                            Intent removeReceive = new Intent(DELETE_RECEIVE);
                            removeReceive.putExtra("userInfo", map.get("userInfo"));
                            mContext.sendBroadcast(removeReceive);
                            accept.setEnabled(false);
                            Toasty.success(mContext, "添加成功").show();
                        } else {
                            LogUtil.d(TAG, "onClick-accept: " + "responseCode = " + i +
                                    "desc = " + s);
                            Toasty.error(mContext, "添加失败"
                                    , Toasty.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
        }
    }
}
