package com.gameex.dw.justtalk.groupChat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.gameex.dw.justtalk.soundController.RecordingService;
import com.gameex.dw.justtalk.emoji.PageTransformer;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.objPack.MsgInfo;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.groupInfo.GroupInfoActivity;
import com.gameex.dw.justtalk.imagePicker.GifSizeFilter;
import com.gameex.dw.justtalk.imagePicker.Glide4Engine;
import com.gameex.dw.justtalk.redPackage.SetYuanActivity;
import com.gameex.dw.justtalk.util.BarUtil;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.FileUtil;
import com.gameex.dw.justtalk.util.GroupInfoUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.GroupMemberInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import es.dmoral.toasty.Toasty;

import static com.gameex.dw.justtalk.main.BottomBarActivity.NEW_MSG;
import static com.gameex.dw.justtalk.main.MsgInfoFragment.UPDATE_MSG_INFO;
import static com.gameex.dw.justtalk.singleChat.ChattingActivity.RECORD_COMPLETE;

public class GroupChatActivity extends BaseActivity implements View.OnClickListener {
    @SuppressLint("StaticFieldLeak")
    public static GroupChatActivity sActivity;
    private static final String TAG = "GroupChatActivity";
    /**
     * 群内红包
     */
    private static final int REQUEST_GROUP_RED_PACKAGE = 101;
    /**
     * ZhiHu`s image picker`s request code
     */
    private static final int REQUEST_CODE_CHOOSE = 23;
    /**
     * 更新群头像
     */
    private static final int REQUEST_CODE_UPDATE_ICON = 102;
    private ViewGroup mRootView;
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
    private CircularImageView mGroup;
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
     * 底部编辑栏
     */
    private LinearLayout mSendLinear;
    /**
     * 发送语音按钮
     */
    private CircularImageView voiceImg;
    /**
     * 按住说话
     */
    private Button mRecord;
    /**
     * 编辑框
     */
    private EmojiEditText mEdit;
    /**
     * 发送表情按钮
     */
    private CircularImageView mEmoji;
    /**
     * 表情弹出窗
     */
    private EmojiPopup mEmojiPopup;
    /**
     * 发送及跟多功能按钮
     */
    private CircularImageView mSend;
    /**
     * 底部功能栏
     */
    private GridView mGridView;
    /**
     * 群成员头像Uri
     */
    private List<UserInfo> mUserInfos = new ArrayList<>();
    private List<Message> mMessages = new ArrayList<>();

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
     * 软键盘高度
     */
    private int mKeyBoardHeight = 0;
    private float posY, curY;
    private boolean isSendVoice = false;
    private GroupReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sActivity = this;
        initView();
        initData();
        mReceiver = new GroupReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(RECORD_COMPLETE);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JMessageClient.exitConversation();
        JMessageClient.unRegisterEventReceiver(this);
        unregisterReceiver(mReceiver);
        Intent intent = new Intent(NEW_MSG);
        sendBroadcast(intent);
    }

    /**
     * 初始化布局
     */
    private void initView() {
        setContentView(R.layout.activity_group_chat);
        mRootView = findViewById(R.id.container);
        mBack = findViewById(R.id.back);
        mNameNum = findViewById(R.id.group_name_member_num);
        mGroup = findViewById(R.id.group_info);
        mRecyclerView = findViewById(R.id.group_member_title_rec);
        mSendLinear = findViewById(R.id.send_linear);
        mMsgRecycler = findViewById(R.id.group_chat_recycler);
        voiceImg = findViewById(R.id.voice_msg);
        mRecord = findViewById(R.id.record_voice);
        mEdit = findViewById(R.id.send_edit);
        mEmoji = findViewById(R.id.emoji_circle);
        mSend = findViewById(R.id.send_circle);
    }

    /**
     * 初始化数据
     */
    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    private void initData() {
        mIMM = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        //获得虚拟键高度
//        navigationBarHeight = BarUtil.getNavigationBarHeight(this);

        JMessageClient.registerEventReceiver(this);
        mGroupId = getIntent().getLongExtra("group_id", -1);
        mConversation = JMessageClient.getGroupConversation(mGroupId);

        MsgInfo msgInfo = getIntent().getParcelableExtra("msg_info");
        mGroupInfo = GroupInfo.fromJson(msgInfo.getGroupInfoJson());

        mBack.setOnClickListener(this);

        mGroup.setOnClickListener(this);
        GroupInfoUtil.initGroupIcon(mGroupInfo, this, mGroup);

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(300);
        animator.setChangeDuration(300);
        animator.setMoveDuration(300);
        animator.setRemoveDuration(300);
        mRecyclerView.setItemAnimator(animator);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mMemberAdapter = new GroupMemberAdapter(this, mUserInfos);
        mRecyclerView.setAdapter(mMemberAdapter);
        getUserInfos();

        DefaultItemAnimator animatorMsg = new DefaultItemAnimator();
        animatorMsg.setAddDuration(300);
        animatorMsg.setChangeDuration(300);
        animatorMsg.setMoveDuration(300);
        animatorMsg.setRemoveDuration(300);
        mMsgRecycler.setItemAnimator(animatorMsg);
        mMsgRecycler.setLayoutManager(new LinearLayoutManager(this));
        if (mConversation != null) {
            mMessages = mConversation.getAllMessage();
        }
        mChatAdapter = new GroupChatAdapter(this, mMessages);
        mMsgRecycler.setAdapter(mChatAdapter);
//        new Handler(this.getMainLooper()).post(this::getBitmaps);

        voiceImg.setOnClickListener(this);
        mRecord.setOnTouchListener((view, motionEvent) -> {
            Intent intent = new Intent(GroupChatActivity.this
                    , RecordingService.class);
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startService(intent);
                    posY = motionEvent.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    curY = motionEvent.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    stopService(intent);
                    if (posY > curY) {
                        isSendVoice = true;
                    } else {
                        Toasty.normal(GroupChatActivity.this, "取消发送", Toasty.LENGTH_SHORT).show();
                    }
                    break;
            }
            return true;
        });
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
        mEmoji.setOnClickListener(this);
        initEmojiPopup();
        mSend.setOnClickListener(this);

        mGridView = findViewById(R.id.function_grid);

        //底部功能栏适配器
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, initGridList()
                , R.layout.grid_item_function, new String[]{"icon", "icon_name"}
                , new int[]{R.id.function_img, R.id.function_name});
        mGridView.setAdapter(simpleAdapter);
        mGridView.setOnItemClickListener((adapterView, view, position, id) -> {
            if (position == 0) {
                Intent intent = new Intent(GroupChatActivity.this
                        , SetYuanActivity.class);
                intent.putExtra("group_info", mGroupInfo.toJson());
                startActivityForResult(intent, REQUEST_GROUP_RED_PACKAGE);
            } else if (position == 1) {
                Matisse.from(GroupChatActivity.this)
                        .choose(MimeType.ofAll())
                        .countable(true)
                        .maxSelectable(9)
                        .addFilter(new GifSizeFilter(320, 320
                                , 5 * Filter.K * Filter.K))
                        .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.dp_120))
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                        .thumbnailScale(0.85f)
                        .imageEngine(new Glide4Engine())
                        .theme(com.zhihu.matisse.R.style.Matisse_Dracula)
                        .forResult(REQUEST_CODE_CHOOSE);
            }
            LogUtil.d(TAG, "initData-onItemClick: " + "position = " + position);
        });
    }

    /**
     * 初始化emoji表情框
     */
    private void initEmojiPopup() {
        mEmojiPopup = EmojiPopup.Builder.fromRootView(mRootView)
                .setOnEmojiBackspaceClickListener(v -> LogUtil.d(TAG, "Clicked on Backspace"))
                .setOnEmojiClickListener((emoji, imageView) -> LogUtil.d(TAG, "Clicked on emoji"))
                .setOnEmojiPopupShownListener(() -> LogUtil.d(TAG, "Emoji popup id shown"))
                .setOnSoftKeyboardOpenListener(keyBoardHeight -> {
                    LogUtil.d(TAG, "Opened soft keyboard: " + "keyBoardHeight = " + keyBoardHeight);
                    mKeyBoardHeight = keyBoardHeight;
                    if (mGridView.getVisibility() == View.VISIBLE) {
                        mGridView.setVisibility(View.GONE);
                    }
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                            mSendLinear.getLayoutParams();
                    params.bottomMargin = keyBoardHeight;
                    mSendLinear.setLayoutParams(params);
                })
                .setOnEmojiPopupDismissListener(() -> LogUtil.d(TAG, "Emoji popup id dismiss"))
                .setOnSoftKeyboardCloseListener(() -> {
                    LogUtil.d(TAG, "Closed soft keyboard");
                    if (mKeyBoardHeight > 0) {
                        mKeyBoardHeight = 0;
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                                mSendLinear.getLayoutParams();
                        params.bottomMargin = mKeyBoardHeight;
                        mSendLinear.setLayoutParams(params);
                    }
                })
                .setKeyboardAnimationStyle(R.style.emoji_fade_animation_style)
                .setPageTransformer(new PageTransformer())
                .build(mEdit);
    }

    @SuppressLint("CheckResult")
    private void requestPermission() {
        new RxPermissions(this)
                .request(Manifest.permission.RECORD_AUDIO)
                .subscribe(granted -> {
                    if (granted) {
//                        if (mGridView.getVisibility() == View.VISIBLE) {
//                            showFunction();
//                        }
                        if (mRecord.getVisibility() == View.VISIBLE) {
                            YoYo.with(Techniques.SlideOutDown)
                                    .duration(200)
                                    .onEnd(animator -> mRecord.setVisibility(View.GONE))
                                    .playOn(mRecord);
                            mEdit.setVisibility(View.VISIBLE);
                            YoYo.with(Techniques.SlideInDown)
                                    .duration(200)
                                    .playOn(mEdit);
                        } else {
                            if (mIMM != null && mKeyBoardHeight > 0) {
                                mIMM.hideSoftInputFromWindow(voiceImg.getWindowToken(), 0);
                            }
                            mRecord.setVisibility(View.VISIBLE);
                            YoYo.with(Techniques.SlideInUp)
                                    .duration(200)
                                    .playOn(mRecord);
                            YoYo.with(Techniques.SlideOutUp)
                                    .duration(200)
                                    .onEnd(animator -> mEdit.setVisibility(View.GONE))
                                    .playOn(mEdit);
                        }
                    } else {
                        Toast.makeText(this
                                , "无法开启录音功能", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 更新聊天数据，并滑到最后一条消息的位置
     *
     * @param message 消息对象
     */
    private void updateAdapter(Message message) {
        if (message == null) {
            return;
        }
        mMessages.add(message);
        //如果有新消息，则设置适配器的长度；通知适配器有新数据插入，并让RecyclerView定位到最后一行
        final int newSize = mMessages.size() - 1;
        mChatAdapter.notifyItemInserted(newSize);
        mMsgRecycler.scrollToPosition(newSize);
        goUpdateMsgInfos(message);
    }

    /**
     * 准备刷主界面消息列表
     *
     * @param message 消息体
     */
    private void goUpdateMsgInfos(Message message) {
        String data = DataUtil.msFormMMDD(message.getCreateTime());
        MsgInfo msgInfo = new MsgInfo();
        msgInfo.setUsername(mGroupInfo.getGroupName());
        msgInfo.setDate(data);
        msgInfo.setIsNotify(true);
        msgInfo.setSingle(false);
        msgInfo.setGroupInfoJson(mGroupInfo.toJson());
        switch (message.getContentType()) {
            case text:
                TextContent textContent = (TextContent) message.getContent();
                msgInfo.setMsgLast(textContent.getText());
                break;
            case image:
                msgInfo.setMsgLast("图片");
                break;
            case voice:
                msgInfo.setMsgLast("语音");
                break;
            case custom:
                msgInfo.setMsgLast("红包");
                break;
        }
        Intent intent = new Intent(UPDATE_MSG_INFO);
        intent.putExtra("msg_info", msgInfo);
        sendBroadcast(intent);
    }

    /**
     * 在线消息处理事件
     *
     * @param event messageEvent
     */
    public void onEventMainThread(MessageEvent event) {
        Message message = event.getMessage();
        GroupInfo groupInfo = (GroupInfo) message.getTargetInfo();
        if (groupInfo.getGroupID() != mGroupId) {
            return;
        }
        updateAdapter(message);
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
                startActivityForResult(intent, REQUEST_CODE_UPDATE_ICON);
                break;
            case R.id.voice_msg:    //发送语音
                requestPermission();
                break;
            case R.id.emoji_circle:
                if (mRecord.getVisibility() == View.VISIBLE) {
                    YoYo.with(Techniques.FadeOutLeft)
                            .duration(100)
                            .onEnd(animator -> {
                                mRecord.setVisibility(View.GONE);
                                mEdit.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.FadeIn)
                                        .duration(100)
                                        .onEnd(animator1 -> mEmojiPopup.toggle())
                                        .playOn(mEdit);
                            })
                            .playOn(mRecord);
                } else mEmojiPopup.toggle();
                break;
            case R.id.send_circle:
                String content = Objects.requireNonNull(mEdit.getText()).toString();
                if (content.isEmpty()) {
                    if (mIMM != null && mKeyBoardHeight > 0) {
                        mIMM.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        new Handler().postDelayed(this::showFunction, 50);
                    } else {
                        showFunction();
                    }
                    return;
                }
                sendTextMsg(content);
                break;
        }
    }

    /**
     * 展示底部功能栏
     */
    private void showFunction() {
        if (mGridView.getVisibility() == View.GONE) {
            mGridView.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.SlideInUp)
                    .duration(200)
                    .playOn(mGridView);
        } else {
            YoYo.with(Techniques.SlideOutDown)
                    .duration(200)
                    .onEnd(animator -> mGridView.setVisibility(View.GONE))
                    .playOn(mGridView);
        }
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
        final Message message = mConversation.createSendMessage(textContent);
        goUpdateAdapter(message, true);
    }

    /**
     * 发送图片消息处理
     *
     * @param file 图片文件
     */
    private void sendImgMsg(File file) {
        if (mConversation == null) {
            mConversation = Conversation.createGroupConversation(mGroupId);
        }
        try {
            Message message = mConversation.createSendImageMessage(file, "flychat");
            goUpdateAdapter(message, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送语音消息
     *
     * @param voiceFile 语音文件
     * @param duration  语音时长
     * @param name      保存的名字
     */
    private void sendVoiceMsg(File voiceFile, int duration, String name) {
        if (mConversation == null) {
            mConversation = Conversation.createGroupConversation(mGroupId);
        }
        try {
            Message message = mConversation.createSendVoiceMessage(voiceFile, duration, name);
            goUpdateAdapter(message, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建自定义消息
     *
     * @param map 自定义消息键值对
     */
    private void sendCustomMsg(Map<String, String> map) {
        if (mConversation == null) {
            mConversation = Conversation.createGroupConversation(mGroupId);
        }
        Message message = mConversation.createSendCustomMessage(map, "红包");
        goUpdateAdapter(message, false);
    }

    /**
     * 消息发送监听
     *
     * @param message     消息对象
     * @param isClearText 是否清空文本编辑框
     */
    private void goUpdateAdapter(final Message message, final boolean isClearText) {
        updateAdapter(message);
        if (isClearText) {
            mEdit.setText("");
        }
        MessageSendingOptions options = new MessageSendingOptions();
        options.setRetainOffline(true);
        JMessageClient.sendMessage(message);
    }

    /**
     * 初始化标题栏头像图片
     */
    @SuppressLint("SetTextI18n")
    private void getUserInfos() {
        List<GroupMemberInfo> groupMemberInfos = mGroupInfo.getGroupMemberInfos();
        for (GroupMemberInfo groupMemberInfo : groupMemberInfos) {
            UserInfo userInfo = groupMemberInfo.getUserInfo();
            mUserInfos.add(userInfo);
            mMemberAdapter.notifyItemInserted(mUserInfos.size() - 1);
        }
        mNameNum.setText(getIntent().getStringExtra("group_name")
                + "（" + groupMemberInfos.size() + "）");
    }

    /**
     * 初始化底部功能栏布局
     *
     * @return list-map.string,object
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
                String token = data.getStringExtra("token");
                String blessings = data.getStringExtra("blessings");
                Map<String, String> map = new HashMap<>();
                map.put("yuan", yuan[1]);
                map.put("token", token);
                map.put("blessings", blessings);
                sendCustomMsg(map);
                LogUtil.d(TAG, "onActivityResult: " + "yuan.length = " + yuan.length
                        + " ;yuan[0] = " + yuan[0] + " ;yuan[1] = " + yuan[1]);
            }
        } else if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            assert data != null;
            List<Uri> path = Matisse.obtainResult(data);
            sendImgMsg(FileUtil.getFileByUri(GroupChatActivity.this
                    , path.get(0)));
        } else if (requestCode == REQUEST_CODE_UPDATE_ICON && resultCode == RESULT_OK) {
            GroupInfoUtil.initGroupIcon(mGroupInfo, GroupChatActivity.this, mGroup);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (mGridView.getVisibility() == View.VISIBLE) {
            showFunction();
        } else {
            super.onBackPressed();
        }
    }

    class GroupReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            switch (action) {
                case RECORD_COMPLETE:
                    if (isSendVoice) {
                        String audioPath = intent.getStringExtra("audio_path");
                        long audioDuration = intent.getLongExtra("elpased", 0);
                        LogUtil.d(TAG, "SingleReceiver: " + "audioPath = " + audioPath
                                + " ;audioDuration = " + DataUtil.msFormmmssTime(audioDuration));
                        sendVoiceMsg(new File(audioPath), (int) audioDuration
                                , System.currentTimeMillis() + "_" + "FCJMG");
                        isSendVoice = false;
                    }
                    break;
            }
        }
    }
}
