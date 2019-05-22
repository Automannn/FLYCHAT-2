package com.gameex.dw.justtalk.userInfo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.BottomBarActivity;
import com.gameex.dw.justtalk.ObjPack.MsgInfo;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.chattingPack.ChattingActivity;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.titleBar.OnViewClick;
import com.gameex.dw.justtalk.titleBar.TitleBarView;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.UserInfoUtils;
import com.github.siyamed.shapeimageview.CircularImageView;

import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

import static com.gameex.dw.justtalk.BottomBarFat.ADD_CONTACT;
import static com.gameex.dw.justtalk.BottomBarFat.REMOVE_CONTACT;
import static com.gameex.dw.justtalk.BottomBarFat.UPDATE_MSG_INFO;
import static com.gameex.dw.justtalk.jiguangIM.JGApplication.APP_KEY;

public class UserBasicInfoActivity extends BaseActivity implements View.OnClickListener {
    /**
     * tag
     */
    private static final String TAG = "USER_BASIC_INFO_ACTIVITY";
    /**
     * 极光AppKey
     */
    private static final String AppKey = "fa964c46085d5543e75797c0";

    /**
     * 标题栏
     */
    private TitleBarView mTitleBar;
    /**
     * 用户头像
     */
    private CircularImageView mCircleView;
    /**
     * 用户名
     */
    private TextView mUsername;
    /**
     * 查找聊天记录
     */
    private TextView mSearchChat;
    /**
     * 添加好友
     */
    private TextView mAddFriends;
    /**
     * 该用户为登陆用户的好友时现实的layout
     */
    private LinearLayout mFriendAction;
    /**
     * 删除好友
     */
    private TextView mDeleteUser;
    /**
     * 上下两个功能块的分割线
     */
    private TextView mLine;
    /**
     * 发消息
     */
    private TextView mSendMsg;
    /**
     * 承接上个activity传过来的UserInfo
     */
    private UserInfo mUserInfo;
    /**
     * 如果不是好友，且此跳转来自好友添加申请通知，则展示此layout
     */
    private LinearLayout mInviteShow;
    /**
     * 接受和拒绝好友申请的button
     */
    private Button mAccept, mRefused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initData();
    }

    /**
     * 初始化组件
     */
    private void initView() {
        setContentView(R.layout.activity_user_basic_info);

        mTitleBar = findViewById(R.id.title_bar_basic_info);
        mTitleBar.setRightIVVisible(View.GONE);
        mTitleBar.setSearchIVVisible(View.GONE);
        mTitleBar.setTitle("用户详情");
        mCircleView = findViewById(R.id.user_icon_info);
        mUsername = findViewById(R.id.user_nick);
        mSearchChat = findViewById(R.id.search_chat_text);
        mAddFriends = findViewById(R.id.add_to_contact_text);
        mFriendAction = findViewById(R.id.already_friend_action_layout);
        mDeleteUser = findViewById(R.id.delete_contact);
        mLine = findViewById(R.id.line);
        mSendMsg = findViewById(R.id.send_msg);
        mInviteShow = findViewById(R.id.invite_show_layout);
        mAccept = findViewById(R.id.accept_btn);
        mRefused = findViewById(R.id.refused_btn);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mTitleBar.setOnViewClick(new OnViewClick() {
            @Override
            public void leftClick() {
                finish();
            }

            @Override
            public void searchClick() {

            }

            @Override
            public void rightClick() {

            }
        });

        mUserInfo = UserInfo.fromJson(getIntent().getStringExtra("user_info_json"));
        UserInfoUtils.initUserIcon(mUserInfo, BottomBarActivity.sBottomBarActivity
                , mCircleView);
        mUsername.setText(TextUtils.isEmpty(mUserInfo.getNickname())
                ? mUserInfo.getUserName() : mUserInfo.getNickname());

        mSearchChat.setOnClickListener(this);

        isMyFriend();

        mAddFriends.setOnClickListener(this);

        mDeleteUser.setOnClickListener(this);
        mSendMsg.setOnClickListener(this);

        mAccept.setOnClickListener(this);

        mRefused.setOnClickListener(this);
    }

    /**
     * 判断是否是朋友，是则修改界面参数
     */
    private void isMyFriend() {
        if (mUserInfo.isFriend()) {
            mFriendAction.setVisibility(View.VISIBLE);
            mAddFriends.setVisibility(View.GONE);
            if (getIntent().getBooleanExtra("from_single_chat", false)) {
                mSendMsg.setVisibility(View.GONE);
                mLine.setVisibility(View.GONE);
            }
        } else {
            if (getIntent().getBooleanExtra("isInvite", false)) {
                mAddFriends.setVisibility(View.GONE);
                mInviteShow.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 删除聊天记录
     */
    private void deleteChat() {
        Conversation conversation = JMessageClient.getSingleConversation(mUserInfo.getUserName());
        if (conversation.deleteAllMessage()) {
            Toast.makeText(this, "聊天记录删除成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "聊天记录删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.add_to_contact_text:
                ContactManager.sendInvitationRequest(mUserInfo.getUserName(), AppKey, "新的朋友", new BasicCallback() {
                    @Override
                    public void gotResult(int responseCode, String invitationDesc) {
                        LogUtil.i(TAG, "sendInvitation : " +
                                "responseCode = " + responseCode +
                                "invitationDesc = " + invitationDesc);
                        if (responseCode == 0) {
                            Toast.makeText(UserBasicInfoActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(UserBasicInfoActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case R.id.delete_contact:
                if (mUserInfo != null) {
                    mUserInfo.removeFromFriendList(new BasicCallback() {
                        @Override
                        public void gotResult(int i, String s) {
                            if (i == 0) {
                                Toast.makeText(UserBasicInfoActivity.this, "好友移除成功",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(REMOVE_CONTACT);
                                intent.putExtra("phone", mUserInfo.getUserName());
                                sendBroadcast(intent);
                                finish();
                            } else {
                                LogUtil.d(TAG, "onClick_delete: " + "responseCode = " + i +
                                        "Desc = " + s);
                            }
                        }
                    });
                } else {
                    Toast.makeText(this, "发生了意想不到的错误...", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.send_msg:
                JMessageClient.enterSingleConversation(mUserInfo.getUserName());
                intent.setClass(this, ChattingActivity.class);
                MsgInfo msgInfo = new MsgInfo();
                msgInfo.setUserInfoJson(mUserInfo.toJson());
                intent.putExtra("msg_info", msgInfo);
                startActivity(intent);
                break;
            case R.id.search_chat_text: //搜索聊天记录
                deleteChat();
                break;
            case R.id.accept_btn:   //接受好友添加请求
                ContactManager.acceptInvitation(mUserInfo.getUserName(), APP_KEY, new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {
                        if (i == 0) {
                            Intent intent = new Intent();
                            intent.setAction(UPDATE_MSG_INFO);
                            intent.putExtra("username", mUserInfo.getUserName());
                            intent.putExtra("date", DataUtil.getCurrentDateStr());
                            intent.putExtra("msg_last", "你们已经是好友了，聊点什么吧...");
                            intent.putExtra("is_notify", true);
                            sendBroadcast(intent);
                            Intent addContact = new Intent(ADD_CONTACT);
                            addContact.putExtra("username", mUserInfo.getUserName());
                            sendBroadcast(addContact);
                            finish();
                        } else {
                            LogUtil.d(TAG, "onClick-accept: " + "responseCode = " + i +
                                    "desc = " + s);
                            Toast.makeText(UserBasicInfoActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case R.id.refused_btn:  //拒绝好友添加请求
                ContactManager.declineInvitation(mUserInfo.getUserName(), APP_KEY, "not my type", new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {
                        if (i == 0) {
                            mInviteShow.setVisibility(View.GONE);
                            mAddFriends.setVisibility(View.VISIBLE);
                            Toast.makeText(UserBasicInfoActivity.this, "你拒绝了好友请求", Toast.LENGTH_SHORT).show();
                        } else {
                            LogUtil.d(TAG, "onClick-refused: " + "responseCode = " + i +
                                    "desc = " + s);
                            Toast.makeText(UserBasicInfoActivity.this, "请重试", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            default:
                break;
        }
    }
}
