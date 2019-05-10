package com.gameex.dw.justtalk.chattingPack;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.gameex.dw.justtalk.ObjPack.Msg;
import com.gameex.dw.justtalk.ObjPack.MsgInfo;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.groupInfo.GroupInfoActivity;
import com.gameex.dw.justtalk.titleBar.OnViewClick;
import com.gameex.dw.justtalk.titleBar.TitleBarView;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.GroupMemberInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import cn.jpush.im.api.BasicCallback;

public class GroupChatActivity extends AppCompatActivity implements View.OnClickListener {

    private TitleBarView mTitleBarView;
    private RecyclerView mRecyclerView;
    private GroupMemberAdapter mMemberAdapter;
    private RecyclerView mMsgRecycler;
    private GroupChatAdapter mChatAdapter;
    private CircularImageView voiceImg;
    private EditText mEdit;
    private CircularImageView mEmoji;
    private CircularImageView mSend;

    private List<Uri> mUris;
    private List<Msg> mMsgs ;
    private MsgInfo mMsgInfo;
    /**
     * 群头像
     */
    private Uri mGroupIcon;
    /**
     * 群id
     */
    private long mGroupId;
    /**
     * 群组对象体
     */
    private GroupInfo mGroupInfo;
    private Conversation mConversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JMessageClient.unRegisterEventReceiver(this);
    }

    /**
     * 初始化布局
     */
    private void initView() {
        setContentView(R.layout.activity_group_chat);

        mTitleBarView = findViewById(R.id.title_bar);

        mRecyclerView = findViewById(R.id.group_member_title_rec);

        mMsgRecycler = findViewById(R.id.group_chat_recycler);

        voiceImg = findViewById(R.id.voice_msg);

        mEdit = findViewById(R.id.send_edit);

        mEmoji = findViewById(R.id.emoji_circle);

        mSend = findViewById(R.id.send_circle);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        JMessageClient.registerEventReceiver(this);
        mGroupId = getIntent().getLongExtra("group_id", -1);
        mConversation = JMessageClient.getGroupConversation(mGroupId);

        mMsgInfo = getIntent().getParcelableExtra("msg_info");
        mGroupInfo = GroupInfo.fromJson(mMsgInfo.getGroupInfoJson());

        mGroupIcon = getIntent().getParcelableExtra("group_icon");
//        mUris = getIntent().getParcelableArrayListExtra("group_member_icon");
//        String uri = JMessageClient.getMyInfo().getExtra("icon_uri");
//        mUris.add(0, TextUtils.isEmpty(uri) ? DataUtil.resourceIdToUri(getPackageName()
//                , R.drawable.icon_user) : Uri.parse(uri));

        mTitleBarView.setTitle(getIntent().getStringExtra("group_name")
                + "（" + mUris.size() + "）");
        mTitleBarView.setRightIVImg(R.drawable.icon_group);
        mTitleBarView.setSearchIVVisible(View.GONE);
        mTitleBarView.setOnViewClick(new OnViewClick() {
            @Override
            public void leftClick() {
                finish();
            }

            @Override
            public void searchClick() {

            }

            @Override
            public void rightClick() {
                Intent intent = new Intent(GroupChatActivity.this
                        , GroupInfoActivity.class);
                intent.putExtra("group_id", mGroupId);
                startActivity(intent);
            }
        });

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(500);
        animator.setChangeDuration(500);
        animator.setMoveDuration(500);
        animator.setRemoveDuration(500);
        mRecyclerView.setItemAnimator(animator);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mMemberAdapter = new GroupMemberAdapter(this, mUris);
        mRecyclerView.setAdapter(mMemberAdapter);

        mMsgRecycler.setItemAnimator(animator);
        mMsgRecycler.setLayoutManager(new LinearLayoutManager(this));
        mChatAdapter = new GroupChatAdapter(this, mMsgs);
        mMsgRecycler.setAdapter(mChatAdapter);
        new Handler(this.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                getMsgs();
            }
        });

        voiceImg.setOnClickListener(this);
        mEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    //当把编辑框聚焦时，改变右下角图片为发送
                } else {
                    //当把编辑框聚焦时，改变右下角图片为更多
                }
            }
        });
        mEmoji.setOnClickListener(this);
        mSend.setOnClickListener(this);
    }

    /**
     * 更新聊天数据，并滑到最后一条消息的位置
     *
     * @param msg 消息类
     */
    private void updateAdapter(Msg msg) {
        mMsgs.add(msg);
        //如果有新消息，则设置适配器的长度；通知适配器有新数据插入，并让RecyclerView定位到最后一行
        int newSize = mMsgs.size() - 1;
        mChatAdapter.notifyItemInserted(newSize);
        mMsgRecycler.scrollToPosition(newSize);
    }

    /**
     * 在线消息处理事件
     *
     * @param event messageEvent
     */
    public void onEventMainThread(MessageEvent event) {
        Message message = event.getMessage();
        if (message.getTargetType() == ConversationType.group) {
            GroupInfo groupInfo = (GroupInfo) message.getTargetInfo();
            String username = message.getFromUser().getUserName();
            long milliSecond = message.getCreateTime();
            String date = DataUtil.msFormMMDD(milliSecond);
            String time = DataUtil.msFormHHmmTime(milliSecond);
            switch (message.getContentType()) {
                case text:  //处理文字信息
                    TextContent textContent = (TextContent) message.getContent();
                    String content = textContent.getText();
                    LogUtil.d("getMessageContent", username + ":" + content);
                    if (groupInfo.getGroupID() == mGroupId) {
//                        updateAdapter(new Msg(date, time, mMsgInfo.getUri(), content, Msg.Type.RECEIVED));
                    } else {
                        LogUtil.d("TAG", "not this group");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.voice_msg:
                Toast.makeText(this, "发送语音", Toast.LENGTH_SHORT).show();
                break;
            case R.id.emoji_circle:
                Toast.makeText(this, "发送表情", Toast.LENGTH_SHORT).show();
                break;
            case R.id.send_circle:
                String content = mEdit.getText().toString();
                if (content.isEmpty()) {
                    Toast.makeText(this, "说点什么吧...", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendMsgCollector(content);
                updateAdapter(new Msg(DataUtil.msFormMMDD(System.currentTimeMillis()),
                        DataUtil.getCurrentTimeStr(), DataUtil.resourceIdToUri(
                        this.getPackageName(), R.drawable.icon_user)
                        , content, Msg.Type.SEND));
                mEdit.setText("");
                break;
        }
    }
    /**
     * 发送消息处理与监听
     *
     * @param content 消息文本
     */
    private void sendMsgCollector(String content) {
        if (mConversation == null) {
            mConversation = Conversation.createGroupConversation(mGroupId);
        }
        TextContent textContent = new TextContent(content);
        assert mConversation != null;
        Message message = mConversation.createSendMessage(textContent);
        message.setOnSendCompleteCallback(new BasicCallback() {
            @Override
            public void gotResult(int requestCode, String responseDesc) {
                if (requestCode == 0) {
                    LogUtil.d("requestCode = 0",
                            "发送成功" + "responseDesc = " + responseDesc);
                } else {
                    LogUtil.d("requestCode = " + requestCode,
                            "发送失败" + "responseDesc = " + responseDesc);
                }
            }
        });
        MessageSendingOptions options = new MessageSendingOptions();
        options.setRetainOffline(true);
        JMessageClient.sendMessage(message);
    }

    /**
     * 初始化标题栏头像图片
     * @return  uri集合
     */
    private List<Uri> getUris() {
        List<Uri> uris = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            uris.add(DataUtil.resourceIdToUri(getPackageName(), R.drawable.icon_user));
        }
        return uris;
    }

    /**
     * 初始化聊天内容
     */
    private void getMsgs() {
        Conversation conversation = JMessageClient.getGroupConversation(
                mGroupInfo.getGroupID());
        if (conversation == null) {
            return;
        }
        List<GroupMemberInfo> groupMemberInfos = mGroupInfo.getGroupMemberInfos();
        for (GroupMemberInfo groupMemberInfo : groupMemberInfos) {
            UserInfo userInfo = groupMemberInfo.getUserInfo();
            String uri = userInfo.getExtra("icon_uri");
            mUris.add(TextUtils.isEmpty(uri) ? DataUtil.resourceIdToUri(getPackageName()
                    , R.drawable.icon_user) : Uri.parse(uri));
            mMemberAdapter.notifyItemInserted(mUris.size() - 1);
        }
        List<Message> messages = conversation.getAllMessage();
        if (messages == null) {
            mMsgs.add(new Msg(mMsgInfo.getDate(), DataUtil.getCurrentTimeStr()
                    , Uri.parse(mMsgInfo.getUriPath()), mMsgInfo.getMsgLast(), Msg.Type.RECEIVED));
        } else {
            for (Message message : messages) {
                goAddMsgList(message);
            }
        }
    }

    /**
     * 判断message的发送状态，并做相应的处理
     *
     * @param message 消息体
     */
    private void goAddMsgList(Message message) {
        long milliSecond = message.getCreateTime();
        String date = DataUtil.msFormMMDD(milliSecond);
        String time = DataUtil.msFormHHmmTime(milliSecond);
        switch (message.getDirect()) {
            case send:
                addMsg(date, time, DataUtil.resourceIdToUri(this.getPackageName()
                        , R.drawable.icon_user), message, Msg.Type.SEND);
                break;
            case receive:
                addMsg(date, time, Uri.parse(mMsgInfo.getUriPath()), message, Msg.Type.RECEIVED);
                break;
            default:
                break;
        }
    }

    /**
     * 根据信息的类型，将获得的信息加入msg对象集合
     *
     * @param date    消息发送的日期
     * @param time    消息发送的时间
     * @param uri     消息发送方的头像
     * @param message 消息体
     * @param type    消息的类型（接收/发送）
     */
    private void addMsg(String date, String time, Uri uri,
                        Message message, Msg.Type type) {
        switch (message.getContentType()) {
            case text:
                TextContent textContent = (TextContent) message.getContent();
                mMsgs.add(new Msg(date, time, uri, textContent.getText(), type));
                break;
            case image:
                break;
            case voice:
                break;
            case video:
                break;
            default:
                break;
        }
        mChatAdapter.notifyItemInserted(mMsgs.size() - 1);
    }

}
