package com.gameex.dw.justtalk.chattingPack;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.gameex.dw.justtalk.ObjPack.Msg;
import com.gameex.dw.justtalk.ObjPack.MsgInfo;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.titleBar.OnViewClick;
import com.gameex.dw.justtalk.titleBar.TitleBarView;
import com.gameex.dw.justtalk.userInfo.UserBasicInfoActivity;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import cn.jpush.im.api.BasicCallback;

public class ChattingActivity extends BaseActivity implements View.OnClickListener {

    private TitleBarView mTitleBar;
    private RecyclerView mRecycler;
    private ChatRecAdapter mRecAdapter;
    private EditText mSendText;
    private CircularImageView mVoiceCircle, mEmojiCircle, mCircleView;

    private List<Msg> mMsgs = new ArrayList<>();
    private MsgInfo mMsgInfo;
    private UserInfo mUserInfo;

    private Conversation mConversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    /**
     * 绑定id，设置监听
     */
    private void initView() {
        setContentView(R.layout.activity_chatting);

        mMsgInfo = getIntent().getParcelableExtra("msg_info");
        mUserInfo = UserInfo.fromJson(mMsgInfo.getUserInfoJson());

        mTitleBar = findViewById(R.id.title_bar_chatting);
        mTitleBar.setSearchIVVisible(View.GONE);
        mTitleBar.setRightIVImg(R.drawable.icon_user);
        mTitleBar.setTitleSize(14);
        try {
            mTitleBar.setTitle(mMsgInfo.getUsername() + "\n" + DataUtil.getCurrentDateStr());
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
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
                Intent intent = new Intent(ChattingActivity.this
                        , UserBasicInfoActivity.class);
                intent.putExtra("user_icon", mMsgInfo.getUriPath());
                intent.putExtra("username", mMsgInfo.getUsername());
                intent.putExtra("phone", mUserInfo.getUserName());
                startActivity(intent);
            }
        });

        JMessageClient.registerEventReceiver(this);
        mConversation = JMessageClient.getSingleConversation(mUserInfo.getUserName());
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setRemoveDuration(200);
        animator.setAddDuration(200);
        mRecycler = findViewById(R.id.chat_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setItemAnimator(animator);
        new Handler(this.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                getMsgs();
            }
        });
        mRecAdapter = new ChatRecAdapter(this, mMsgs);
        mRecycler.setAdapter(mRecAdapter);

        mVoiceCircle = findViewById(R.id.voice_msg);
        mVoiceCircle.setOnClickListener(this);
        mSendText = findViewById(R.id.send_edit);
        mEmojiCircle = findViewById(R.id.emoji_circle);
        mEmojiCircle.setOnClickListener(this);
        mCircleView = findViewById(R.id.send_circle);
        mCircleView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.voice_msg:    //发送语音
                Toast.makeText(this, "发送语音", Toast.LENGTH_SHORT).show();
                break;
            case R.id.emoji_circle: //发送表情
                Toast.makeText(this, "发送表情", Toast.LENGTH_SHORT).show();
                break;
            case R.id.send_circle:  //发送文本
                String content = mSendText.getText().toString();
                if (content.isEmpty()) {
                    Toast.makeText(this, "说点什么吧...", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendMsgCollector(content);
                updateAdapter(new Msg(DataUtil.msFormMMDD(System.currentTimeMillis()),
                        DataUtil.getCurrentTimeStr(), DataUtil.resourceIdToUri(
                        this.getPackageName(), R.drawable.icon_user)
                        , content, Msg.Type.SEND));
                mSendText.setText("");
                break;
            default:
                break;
        }
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
        mRecAdapter.notifyItemInserted(newSize);
        mRecycler.scrollToPosition(newSize);
    }

    /**
     * 在线消息处理事件
     *
     * @param event messageEvent
     */
    public void onEventMainThread(MessageEvent event) {
        Message message = event.getMessage();
        String username = message.getFromUser().getUserName();
        long milliSecond = message.getCreateTime();
        String date = DataUtil.msFormMMDD(milliSecond);
        String time = DataUtil.msFormHHmmTime(milliSecond);
        switch (message.getContentType()) {
            case text:  //处理文字信息
                TextContent textContent = (TextContent) message.getContent();
                String content = textContent.getText();
                LogUtil.d("getMessageContent", username + ":" + content);
                if (username.equals(mUserInfo.getUserName())) {
                    updateAdapter(new Msg(date, time, Uri.parse(mMsgInfo.getUriPath()), content, Msg.Type.RECEIVED));
                } else {
                    LogUtil.d("TAG", "username != userPhone");
                }
                break;
            default:
                break;
        }
    }

    /*
      离线消息处理事件

      @param event
     */
//    public void onEvent(OfflineMessageEvent event) {
//        List<Message> messages = event.getOfflineMessageList();
//        for (Message message : messages) {
//            String username = message.getFromUser().getUserName();
//            long time = message.getCreateTime();
//            switch (message.getContentType()) {
//                case text:  //处理文字信息
//                    TextContent textContent = (TextContent) message.getContent();
//                    String content = textContent.getText();
//                    LogUtil.d("getMessageContent", username + ":" + content);
//                    if (username == userPhone) {
//                        updateAdapter(new Msg(DataUtil.msFormTime(time), content, Msg.Type.RECEIVED));
//                    }
//                    break;
//                default:
//                    break;
//            }
//        }
//    }

    /**
     * 发送消息处理与监听
     *
     * @param content 消息文本
     */
    private void sendMsgCollector(String content) {
        if (mConversation == null) {
            mConversation = Conversation.createSingleConversation(mUserInfo.getUserName());
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
     * 聊天界面测试参数
     */
    private void getMsgs() {
        Conversation conversation = JMessageClient.getSingleConversation(
                mUserInfo.getUserName());
        if (conversation == null) {
            return;
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
        switch (message.getStatus()) {
            case receive_success:
                addMsg(date, time, Uri.parse(mMsgInfo.getUriPath()), message, Msg.Type.RECEIVED);
                break;
            case send_success:
                addMsg(date, time, DataUtil.resourceIdToUri(this.getPackageName()
                        , R.drawable.icon_user), message, Msg.Type.SEND);
                break;
            case send_going:
                break;
            case send_fail:
                break;
            case send_draft:
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
        mRecAdapter.notifyItemInserted(mMsgs.size() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JMessageClient.exitConversation();
        JMessageClient.unRegisterEventReceiver(this);
    }
}
