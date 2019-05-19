package com.gameex.dw.justtalk.chattingPack;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.gameex.dw.justtalk.ObjPack.Msg;
import com.gameex.dw.justtalk.ObjPack.MsgInfo;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.groupInfo.GroupInfoActivity;
import com.gameex.dw.justtalk.redPackage.SetYuanActivity;
import com.gameex.dw.justtalk.util.BarUtil;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.CustomContent;
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
    public static GroupChatActivity sActivity;
    private static final String TAG = "GroupChatActivity";
    /**
     * 群内红包
     */
    private static final int REQUEST_GROUP_RED_PACKAGE = 101;
    /**
     * 返回箭头
     */
    private ImageView mBack;
    /**
     * 群名称和群成员数
     */
    private TextView mNameNum;
    /**
     * 群信息入口
     */
    private ImageView mGroup;
    /**
     * 群成员头像展示RecyclerView
     */
    private RecyclerView mRecyclerView;
    /**
     * 群成员头像展示适配器
     */
    private GroupMemberAdapter mMemberAdapter;
    /**
     * 群消息RecyclerView
     */
    private RecyclerView mMsgRecycler;
    /**
     * 群消息适配器
     */
    private GroupChatAdapter mChatAdapter;
    /**
     * 发送语音按钮
     */
    private CircularImageView voiceImg;
    /**
     * 编辑框
     */
    private EditText mEdit;
    /**
     * 发送表情按钮
     */
    private CircularImageView mEmoji;
    /**
     * 发送及跟多功能按钮
     */
    private CircularImageView mSend;
    /**
     * 底部功能栏
     */
    private GridView mGridView;
    /**
     * 底部功能栏适配器
     */
    private SimpleAdapter mSimpleAdapter;
    /**
     * 群成员头像Uri
     */
    private List<Uri> mUris = new ArrayList<>();
    /**
     * 初始化消息列表
     */
    private List<Msg> mMsgs = new ArrayList<>();

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
    /**
     * 底部功能栏数据
     */
    private List<Map<String, Object>> mGridList = new ArrayList<>();
    private int[] icon = {R.drawable.icon_red_package, R.drawable.icon_photo};
    private String[] iconName = {"红包", "图片"};

    /**
     * 软键盘相关
     */
    private InputMethodManager mIMM;
    /**
     * 虚拟键高度，若没有/隐藏虚拟键，则为0
     */
    private int navigationBarHeight;
    /**
     * 软件盘高度，有虚拟键，则为软键盘高度+虚拟键高度
     */
    private int heightDifference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sActivity = this;
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
        mBack = findViewById(R.id.back);
        mNameNum = findViewById(R.id.group_name_member_num);
        mGroup = findViewById(R.id.group_info);
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
    @SuppressLint("SetTextI18n")
    private void initData() {
        mIMM = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        //获得虚拟键高度
        navigationBarHeight = BarUtil.getNavigationBarHeight(this);

        JMessageClient.registerEventReceiver(this);
        mGroupId = getIntent().getLongExtra("group_id", -1);
        mConversation = JMessageClient.getGroupConversation(mGroupId);

        mMsgInfo = getIntent().getParcelableExtra("msg_info");
        mGroupInfo = GroupInfo.fromJson(mMsgInfo.getGroupInfoJson());

        mGroupIcon = getIntent().getParcelableExtra("group_icon");

        mBack.setOnClickListener(this);

        mGroup.setOnClickListener(this);

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(300);
        animator.setChangeDuration(300);
        animator.setMoveDuration(300);
        animator.setRemoveDuration(300);
        mRecyclerView.setItemAnimator(animator);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mMemberAdapter = new GroupMemberAdapter(this, mUris);
        mRecyclerView.setAdapter(mMemberAdapter);

        DefaultItemAnimator animatorMsg = new DefaultItemAnimator();
        animatorMsg.setAddDuration(300);
        animatorMsg.setChangeDuration(300);
        animatorMsg.setMoveDuration(300);
        animatorMsg.setRemoveDuration(300);
        mMsgRecycler.setItemAnimator(animatorMsg);
        mMsgRecycler.setLayoutManager(new LinearLayoutManager(this));
        mChatAdapter = new GroupChatAdapter(this, mMsgs);
        mMsgRecycler.setAdapter(mChatAdapter);
        new Handler(this.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                getMsgs();
                getUris();
            }
        });

        voiceImg.setOnClickListener(this);
        mEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                LogUtil.d(TAG, "initData-beforeTextChange: " + "char = " + charSequence
                        + " ;i = " + i + " ;i1 = " + i1 + " ;i2 = " + i2);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                LogUtil.d(TAG, "initData-onTextChanged: " + "char = " + charSequence
                        + " ;i = " + i + " ;i1 = " + i1 + " ;i2 = " + i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                LogUtil.d(TAG, "initData-afterTextChanged: " + "editable = " + editable);
                if (TextUtils.isEmpty(editable)) {
                    mSend.setImageResource(R.drawable.more_send);
                } else {
                    mSend.setImageResource(R.drawable.send);
                }
            }
        });
        mEdit.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {  //当键盘弹出/隐藏时调用此方法
                Rect r = new Rect();
                //获取当前界面可视部分
                GroupChatActivity.this.getWindow()
                        .getDecorView()
                        .getWindowVisibleDisplayFrame(r);
                //获取屏幕的高度
                int screenHeight = GroupChatActivity.this.getWindow()
                        .getDecorView()
                        .getRootView()
                        .getHeight();
                //此处就是用来获取键盘的高度的， 在键盘没有弹出的时候 此高度为0 键盘弹出的时候为一个正数
                heightDifference = screenHeight - r.bottom;
                if (heightDifference > navigationBarHeight) {
                    mGridView.setVisibility(View.GONE);
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)
                            mMsgRecycler.getLayoutParams();
                    params.bottomMargin = DataUtil.dpToPx(GroupChatActivity.this, 57);
                    mMsgRecycler.setLayoutParams(params);
                    return;
                }
                LogUtil.d(TAG, "initView-onGlobalLayout: " + "Size = " + heightDifference);
            }
        });
        mEmoji.setOnClickListener(this);
        mSend.setOnClickListener(this);

        mGridView = findViewById(R.id.function_grid);
        mSimpleAdapter = new SimpleAdapter(this, initGridList()
                , R.layout.function_item, new String[]{"icon", "icon_name"}
                , new int[]{R.id.function_img, R.id.function_name});
        mGridView.setAdapter(mSimpleAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == 0) {
                    Intent intent = new Intent(GroupChatActivity.this
                            , SetYuanActivity.class);
                    intent.putExtra("group_info", mGroupInfo.toJson());
                    startActivityForResult(intent, REQUEST_GROUP_RED_PACKAGE);
                }
                LogUtil.d(TAG, "initData-onItemClick: " + "position = " + position);
            }
        });
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
            UserInfo userInfo = message.getFromUser();
            String username = userInfo.getUserName();
            long milliSecond = message.getCreateTime();
            String date = DataUtil.msFormMMDD(milliSecond);
            String time = DataUtil.msFormHHmmTime(milliSecond);
            Uri uri = TextUtils.isEmpty(userInfo.getExtra("icon_uri")) ? DataUtil.resourceIdToUri(getPackageName()
                    , R.drawable.icon_user) : Uri.parse(userInfo.getExtra("icon_uri"));
            if (groupInfo.getGroupID() == mGroupId) {
                switch (message.getContentType()) {
                    case text:  //处理文字信息
                        TextContent textContent = (TextContent) message.getContent();
                        String content = textContent.getText();
                        LogUtil.d("getMessageContent", username + ":" + content);
                        updateAdapter(new Msg(date, time, uri, content, Msg.Type.RECEIVED));
                        break;
                    case custom:
                        CustomContent customContent = (CustomContent) message.getContent();
                        String yuan = customContent.getStringValue("yuan");
                        LogUtil.d(TAG, "onEventMainThread-custom: " + "yuan = " + yuan);
                        Msg msg = new Msg(date, time, uri, yuan, Msg.Type.RECEIVED);
                        msg.setMsgType(Msg.MsgType.RED_PACKAGE);
                        updateAdapter(msg);
                        break;
                    default:
                        break;
                }
            } else {
                LogUtil.d("TAG", "not this group");
            }
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.group_info:
                intent.setClass(GroupChatActivity.this
                        , GroupInfoActivity.class);
                intent.putExtra("group_info", mGroupInfo.toJson());
                intent.putExtra("group_id", mGroupId);
                startActivity(intent);
                break;
            case R.id.voice_msg:
                Toast.makeText(this, "发送语音", Toast.LENGTH_SHORT).show();
                break;
            case R.id.emoji_circle:
                Toast.makeText(this, "发送表情", Toast.LENGTH_SHORT).show();
                break;
            case R.id.send_circle:
                String content = mEdit.getText().toString();
                if (content.isEmpty()) {
                    if (mIMM != null && heightDifference > navigationBarHeight) {
                        mIMM.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showFunction();
                            }
                        }, 50);
                    } else {
                        showFunction();
                    }
                    return;
                }
                sendTextMsg(content);
                updateAdapter(new Msg(DataUtil.msFormMMDD(System.currentTimeMillis()),
                        DataUtil.getCurrentTimeStr(), DataUtil.resourceIdToUri(
                        this.getPackageName(), R.drawable.icon_user)
                        , content, Msg.Type.SEND));
                mEdit.setText("");
                break;
        }
    }

    /**
     * 展示底部功能栏
     */
    private void showFunction() {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)
                mMsgRecycler.getLayoutParams();
        if (mGridView.getVisibility() == View.GONE) {
            mGridView.setVisibility(View.VISIBLE);
            params.bottomMargin = DataUtil.dpToPx(this, 207);
        } else {
            mGridView.setVisibility(View.GONE);
            params.bottomMargin = DataUtil.dpToPx(this, 57);
        }
        mMsgRecycler.setLayoutParams(params);
    }

    /**
     * 发送文本消息
     *
     * @param content 消息文本
     */
    private void sendTextMsg(String content) {
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
     * 创建自定义消息
     *
     * @param map 自定义消息键值对
     */
    private void sendCustomMsg(Map<String, String> map) {
        Message message = JMessageClient.createGroupCustomMessage(mGroupId, map);
        MessageSendingOptions options = new MessageSendingOptions();
        options.setRetainOffline(true);
        JMessageClient.sendMessage(message);
    }

    /**
     * 初始化标题栏头像图片
     */
    @SuppressLint("SetTextI18n")
    private void getUris() {
//        List<Uri> uris = new ArrayList<>();
        List<GroupMemberInfo> groupMemberInfos = mGroupInfo.getGroupMemberInfos();
        for (GroupMemberInfo groupMemberInfo : groupMemberInfos) {
            UserInfo userInfo = groupMemberInfo.getUserInfo();
            Uri uri = userInfo.getExtra("icon_uri") == null ? DataUtil.resourceIdToUri(getPackageName()
                    , R.drawable.icon_user) : Uri.parse(userInfo.getExtra("icon_uri"));
            mUris.add(uri);
        }
        mNameNum.setText(getIntent().getStringExtra("group_name")
                + "（" + mUris.size() + "）");
        mMemberAdapter.notifyDataSetChanged();
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
        UserInfo userInfo = message.getFromUser();
        Uri uri = TextUtils.isEmpty(userInfo.getExtra("icon_uri")) ? DataUtil.resourceIdToUri(getPackageName()
                , R.drawable.icon_user) : Uri.parse(userInfo.getExtra("icon_uri"));
        switch (message.getDirect()) {
            case send:
                addMsg(date, time, DataUtil.resourceIdToUri(this.getPackageName()
                        , R.drawable.icon_user), message, Msg.Type.SEND);
                break;
            case receive:
                addMsg(date, time, uri, message, Msg.Type.RECEIVED);
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
            case custom:
                CustomContent customContent = (CustomContent) message.getContent();
                String yuan = customContent.getStringValue("yuan");
                Msg msg = new Msg(date, time, uri, yuan, type);
                msg.setMsgType(Msg.MsgType.RED_PACKAGE);
                mMsgs.add(msg);
                break;
            default:
                break;
        }
        mChatAdapter.notifyItemInserted(mMsgs.size() - 1);
    }

    /**
     * 初始化底部功能栏布局
     *
     * @return List<Map                               <                               String                               ,                               Object>>
     */
    private List<Map<String, Object>> initGridList() {
        for (int i = 0; i < icon.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("icon", icon[i]);
            map.put("icon_name", iconName[i]);
            mGridList.add(map);
        }
        return mGridList;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_GROUP_RED_PACKAGE && resultCode == RESULT_OK) {
            if (data != null) {
                String[] yuan = data.getStringExtra("yuan").split("￥");
                Msg msg = new Msg(DataUtil.msFormMMDD(System.currentTimeMillis()),
                        DataUtil.getCurrentTimeStr(), DataUtil.resourceIdToUri(
                        this.getPackageName(), R.drawable.icon_user)
                        , data.getStringExtra("yuan"), Msg.Type.SEND);
                msg.setMsgType(Msg.MsgType.RED_PACKAGE);
                updateAdapter(msg);
                Map<String, String> map = new HashMap<>();
                map.put("yuan", yuan[1]);
                sendCustomMsg(map);
                LogUtil.d(TAG, "onActivityResult: " + "yuan.length = " + yuan.length
                        + " ;yuan[0] = " + yuan[0] + " ;yuan[1] = " + yuan[1]);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
