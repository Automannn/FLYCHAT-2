package com.gameex.dw.justtalk.chattingPack;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.gameex.dw.justtalk.ObjPack.Msg;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.titleBar.OnViewClick;
import com.gameex.dw.justtalk.titleBar.TitleBarView;
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
import cn.jpush.im.android.api.options.MessageSendingOptions;
import cn.jpush.im.api.BasicCallback;

public class ChattingActivity extends BaseActivity implements View.OnClickListener {

    private TitleBarView mTitleBar;
    private RecyclerView mRecycler;
    private ChatRecAdapter mRecAdapter;
    private EditText mSendText;
    private CircularImageView mVoiceCircle, mEmojiCircle, mCircleView;

    private List<Msg> mMsgs = new ArrayList<>();
    private String userPhone;

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
        mTitleBar = findViewById(R.id.title_bar_chatting);
        mTitleBar.setSearchIVVisible(View.GONE);
        mTitleBar.setRightIVImg(R.drawable.icon_user);
        mTitleBar.setTitleSize(14);
        try {
            String username = getIntent().getStringExtra("username");
            userPhone = username;
            mTitleBar.setTitle(username + "\n" + DataUtil.getCurrentDateStr());
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
                Toast.makeText(ChattingActivity.this, "更多操作", Toast.LENGTH_SHORT).show();
            }
        });

        JMessageClient.registerEventReceiver(this);
        mConversation = JMessageClient.getSingleConversation(userPhone);
        mMsgs = getMsgs();
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setRemoveDuration(500);
        animator.setAddDuration(500);
        mRecycler = findViewById(R.id.chat_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setItemAnimator(animator);
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
                updateAdapter(new Msg(content, Msg.Type.SEND));
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
                if (username.equals(userPhone)) {
                    updateAdapter(new Msg(date, time, R.drawable.icon_user, content, Msg.Type.RECEIVED));
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
        if (mConversation != null) {
            mConversation = Conversation.createSingleConversation(userPhone);
        }
        TextContent textContent = new TextContent(content);
        assert mConversation != null;
        Message message = mConversation.createSendMessage(textContent);
//        Message message = JMessageClient.createSingleTextMessage(userPhone, content);
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
    private List<Msg> getMsgs() {
        List<Msg> msgs = new ArrayList<>();
        if (mConversation != null) {
            mConversation = Conversation.createSingleConversation(userPhone);
        }
        assert mConversation != null;
        List<Message> messages = mConversation.getAllMessage();
        for (Message message : messages) {
            TextContent textContent = (TextContent) message.getContent();
            long milliSecond = message.getCreateTime();
            String date = DataUtil.msFormMMDD(milliSecond);
            String time = DataUtil.msFormHHmmTime(milliSecond);
            switch (message.getContentType()) {
                case text:
                    String content = textContent.getText();
                    msgs.add(new Msg(date, time, R.drawable.icon_user, content, Msg.Type.RECEIVED));
                    break;
                default:
                    break;
            }
        }
        return msgs;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JMessageClient.exitConversation();
        JMessageClient.unRegisterEventReceiver(this);
    }
}
