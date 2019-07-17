package com.gameex.dw.justtalk.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
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
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.gameex.dw.justtalk.adapter.ChatRecAdapter;
import com.gameex.dw.justtalk.soundController.RecordingService;
import com.gameex.dw.justtalk.emoji.PageTransformer;
import com.gameex.dw.justtalk.entry.MsgInfo;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.tools.GifSizeFilter;
import com.gameex.dw.justtalk.tools.Glide4Engine;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.FileUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.RecScrollHelper;
import com.gameex.dw.justtalk.util.UserInfoUtils;
import com.gameex.dw.justtalk.util.WindowUtil;
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
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import es.dmoral.toasty.Toasty;

import static com.gameex.dw.justtalk.activity.BottomBarActivity.NEW_MSG;
import static com.gameex.dw.justtalk.fragment.MsgInfoFragment.UPDATE_MSG_INFO;

/**
 * 聊天界面
 */
public class ChattingActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "ChattingActivity";
    /**
     * ZhiHu`s image picker`s request code
     */
    private static final int REQUEST_CODE_CHOOSE = 23;
    /**
     * 单聊红包请求码
     */
    private static final int REQUEST_SINGLE_RED_PACKAGE = 102;
    /**
     * 调用系统的录音功能
     */
    private static final int REQUEST_RECORDER = 100;
    /**
     * 录音完成
     */
    public static final String RECORD_COMPLETE =
            "com.gameex.dw.justtalk..ChattingActivity.RECORD_COMPLETE";

    private ViewGroup mRootView;

    /**
     * 结束当前
     */
    @OnClick({R.id.back})
    void submit(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
        }
    }

    /**
     * 发送方用户名和第一条未读信息（当天）的日期
     */
    private TextView mTitle;
    /**
     * 发送方头像
     */
    private CircularImageView mIcon;
    private RecyclerView mRecycler;
    private ChatRecAdapter mRecAdapter;
    private LinearLayout mSendLayout;
    private EmojiEditText mSendText;
    private CircularImageView mVoiceCircle, mEmojiCircle, mCircleView;
    /**
     * 按住说话
     */
    private Button mRecord;
    private EmojiPopup mEmojiPopup;
    private GridView mGridView;
    private SimpleAdapter mSimpleAdapter;

    private List<Map<String, Object>> mGridList = new ArrayList<>();
    private int[] icon = {R.drawable.icon_image, R.drawable.icon_red_packit
            , R.drawable.icon_voice_chat, R.drawable.icon_shock, R.drawable.icon_location
            , R.drawable.icon_business_card, R.drawable.icon_collection, R.drawable.icon_file};
    private String[] iconName = {"图片", "红包", "语音聊天", "震", "位置", "名片", "收藏", "文件"};
    private List<Message> mMessages = new ArrayList<>();
    private MsgInfo mMsgInfo;
    private UserInfo mUserInfo;

    private Conversation mConversation;
    /**
     * 软键盘相关
     */
    private InputMethodManager mIMM;
    /**
     * 软键盘高度
     */
    private int mKeyBoardHeight = 0;
    /**
     * 用于点击按住录音时记录触点坐标
     */
    private float posY, curY;
    private boolean isSendVoice = false;
    private SingleReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        mReceiver = new SingleReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(RECORD_COMPLETE);
        registerReceiver(mReceiver, filter);
    }

    @SuppressLint("CheckResult")
    private void requestPermission() {
        new RxPermissions(this)
                .request(Manifest.permission.RECORD_AUDIO)
                .subscribe(granted -> {
                    if (granted) {
                        if (mRecord.getVisibility() == View.VISIBLE) {
                            YoYo.with(Techniques.SlideOutDown)
                                    .duration(200)
                                    .onEnd(animator -> mRecord.setVisibility(View.GONE))
                                    .playOn(mRecord);
                            mSendText.setVisibility(View.VISIBLE);
                            YoYo.with(Techniques.SlideInDown)
                                    .duration(200)
                                    .playOn(mSendText);
                        } else {
                            if (mIMM != null && mKeyBoardHeight > 0) {
                                mIMM.hideSoftInputFromWindow(mVoiceCircle.getWindowToken(), 0);
                            }
                            mRecord.setVisibility(View.VISIBLE);
                            YoYo.with(Techniques.SlideInUp)
                                    .duration(200)
                                    .playOn(mRecord);
                            YoYo.with(Techniques.SlideOutUp)
                                    .duration(200)
                                    .onEnd(animator -> mSendText.setVisibility(View.GONE))
                                    .playOn(mSendText);
                        }
                    } else {
                        Toast.makeText(this
                                , "无法开启录音功能", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 绑定id，设置监听
     */
    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    private void initView() {
        setContentView(R.layout.activity_chatting);
        ButterKnife.bind(this); //黄油刀
        mRootView = findViewById(R.id.view_single);
        mIMM = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        mMsgInfo = getIntent().getParcelableExtra("msg_info");
        mUserInfo = UserInfo.fromJson(mMsgInfo.getUserInfoJson());

        mTitle = findViewById(R.id.title);
        mIcon = findViewById(R.id.icon);
        mIcon.setOnClickListener(this);
        UserInfoUtils.initUserIcon(mUserInfo, this, mIcon);
        mTitle.setText((TextUtils.isEmpty(mUserInfo.getNickname()) ? mUserInfo.getUserName()
                : mUserInfo.getNickname()) + "\n" + DataUtil.getCurrentDateStr());

        JMessageClient.registerEventReceiver(this);
        mConversation = JMessageClient.getSingleConversation(mUserInfo.getUserName());
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setRemoveDuration(200);
        animator.setAddDuration(200);
        mRecycler = findViewById(R.id.chat_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setItemAnimator(animator);
        mRecycler.setHasFixedSize(true);    //来避免 requestLayout 浪费资源
        if (mConversation != null) {
            mMessages = mConversation.getAllMessage();
        }
        mRecAdapter = new ChatRecAdapter(this, mMessages);
        mRecycler.setAdapter(mRecAdapter);
        if (mMessages != null && mMessages.size() > 0)
            RecScrollHelper.scrollToPosition(mRecycler, mMessages.size() - 1);

        mSendLayout = findViewById(R.id.send_linear);
        mVoiceCircle = findViewById(R.id.voice_msg);
        mVoiceCircle.setOnClickListener(this);
        mRecord = findViewById(R.id.record_voice);
        mRecord.setOnTouchListener((view, motionEvent) -> {
            Intent intent = new Intent(this, RecordingService.class);
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
                        Toasty.normal(ChattingActivity.this, "取消发送", Toasty.LENGTH_SHORT).show();
                    }
                    break;
            }
            return true;
        });
        mSendText = findViewById(R.id.send_edit);
        mSendText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable)) {
                    mCircleView.setImageResource(R.drawable.more_send);
                } else {
                    mCircleView.setImageResource(R.drawable.send);
                }
            }
        });
        mEmojiCircle = findViewById(R.id.emoji_circle);
        mEmojiCircle.setOnClickListener(this);
        initEmojiPopup();

        mCircleView = findViewById(R.id.send_circle);
        mCircleView.setOnClickListener(this);

        mGridView = findViewById(R.id.function_grid);
        mSimpleAdapter = new SimpleAdapter(this, initGridList()
                , R.layout.grid_item_function, new String[]{"icon", "icon_name"}
                , new int[]{R.id.function_img, R.id.function_name});
        mGridView.setAdapter(mSimpleAdapter);
        mGridView.setOnItemClickListener((adapterView, view, position, id) -> {
            Intent intent = new Intent();
            switch (position) {
                case 0:
                    Matisse.from(this)
                            .choose(MimeType.ofImage())
                            .countable(true)
                            .maxSelectable(1)
                            .addFilter(new GifSizeFilter(320, 320
                                    , 5 * Filter.K * Filter.K))
                            .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.dp_120))
                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                            .thumbnailScale(0.85f)
                            .imageEngine(new Glide4Engine())
                            .theme(com.zhihu.matisse.R.style.Matisse_Dracula)
                            .forResult(REQUEST_CODE_CHOOSE);
                    break;
                case 1:
                    intent.setClass(ChattingActivity.this
                            , SingleRedActivity.class);
                    startActivityForResult(intent, REQUEST_SINGLE_RED_PACKAGE);
                    break;
                default:
                    Toasty.info(ChattingActivity.this, "敬请期待").show();
                    break;
            }
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
                            mSendLayout.getLayoutParams();
                    params.bottomMargin = keyBoardHeight;
                    mSendLayout.setLayoutParams(params);
                })
                .setOnEmojiPopupDismissListener(() -> LogUtil.d(TAG, "Emoji popup id dismiss"))
                .setOnSoftKeyboardCloseListener(() -> {
                    LogUtil.d(TAG, "Closed soft keyboard");
                    if (mKeyBoardHeight > 0) {
                        mKeyBoardHeight = 0;
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                                mSendLayout.getLayoutParams();
                        params.bottomMargin = mKeyBoardHeight;
                        mSendLayout.setLayoutParams(params);
                    }
                })
                .setKeyboardAnimationStyle(R.style.emoji_fade_animation_style)
                .setPageTransformer(new PageTransformer())
                .build(mSendText);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.icon:
                Intent intent = new Intent(ChattingActivity.this
                        , UserBasicInfoActivity.class);
                intent.putExtra("user_info_json", mMsgInfo.getUserInfoJson());
                intent.putExtra("from_single_chat", true);
                startActivity(intent);
                break;
            case R.id.voice_msg:    //发送语音
                requestPermission();
                break;
            case R.id.emoji_circle: //发送表情
                if (mRecord.getVisibility() == View.VISIBLE) {
                    YoYo.with(Techniques.FadeOutLeft)
                            .duration(100)
                            .onEnd(animator -> {
                                mRecord.setVisibility(View.GONE);
                                mSendText.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.FadeIn)
                                        .duration(100)
                                        .onEnd(animator1 -> mEmojiPopup.toggle())
                                        .playOn(mSendText);
                            })
                            .playOn(mRecord);
                } else mEmojiPopup.toggle();
                break;
            case R.id.send_circle:  //发送文本
                String content = Objects.requireNonNull(mSendText.getText()).toString();
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
            default:
                break;
        }
    }

    /**
     * 点击软键盘外面的区域关闭软键盘
     *
     * @param ev motionEvent
     * @return boolean
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
//            if (mGridView.getVisibility() == View.VISIBLE) showFunction();
            // 获得当前得到焦点的View，一般情况下就是EditText（特殊情况就是轨迹求或者实体案件会移动焦点）
            View v = getCurrentFocus();
            if (WindowUtil.isShouldHideInput(v, ev)) {
                WindowUtil.hideInput(this);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 展示底部功能栏
     */
    private void showFunction() {
        if (mGridView.getVisibility() == View.GONE) {
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
        int newSize = mMessages.size() - 1;
        mRecAdapter.notifyItemInserted(newSize);
        mRecycler.scrollToPosition(newSize);
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
        msgInfo.setUsername(mUserInfo.getUserName());
        msgInfo.setDate(data);
        msgInfo.setIsNotify(true);
        msgInfo.setSingle(true);
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
        String username = message.getFromUser().getUserName();
        if (!username.equals(mUserInfo.getUserName())) {
            return;
        }
        updateAdapter(message);
    }

    /**
     * 发送文字消息处理
     *
     * @param content 消息文本
     */
    private void sendTextMsg(String content) {
        if (mConversation == null) {
            mConversation = Conversation.createSingleConversation(mUserInfo.getUserName());
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
            mConversation = Conversation.createSingleConversation(mUserInfo.getUserName());
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
            mConversation = Conversation.createSingleConversation(mUserInfo.getUserName());
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
            mConversation = Conversation.createSingleConversation(mUserInfo.getUserName());
        }
        Message message = mConversation.createSendCustomMessage(map, "红包");
        goUpdateAdapter(message, false);
    }

    /**
     * 准备刷新界面
     *
     * @param message     消息对象
     * @param isClearText 是否清空文本编辑框
     */
    private void goUpdateAdapter(final Message message, final boolean isClearText) {
        updateAdapter(message);
        if (isClearText) {
            mSendText.setText("");
        }
        MessageSendingOptions options = new MessageSendingOptions();
        options.setRetainOffline(true);
        JMessageClient.sendMessage(message);
    }

    /**
     * 初始化底部功能栏布局
     *
     * @return List-Map:String,Object
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
    protected void onDestroy() {
        super.onDestroy();
        JMessageClient.exitConversation();
        JMessageClient.unRegisterEventReceiver(this);
        unregisterReceiver(mReceiver);
        Intent intent = new Intent(NEW_MSG);
        sendBroadcast(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode
            , @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            assert data != null;
            List<Uri> path = Matisse.obtainResult(data);
            sendImgMsg(FileUtil.getFileByUri(ChattingActivity.this
                    , path.get(0)));
        } else if (requestCode == REQUEST_SINGLE_RED_PACKAGE && resultCode == RESULT_OK) {
            if (data != null) {
                String[] yuan = data.getStringExtra("yuan").split("￥");
                String token = data.getStringExtra("token");
                String blessings = data.getStringExtra("blessings");
                Map<String, String> map = new HashMap<>();
                map.put("yuan", yuan[1]);
                map.put("token", token);
                map.put("userInfo", JMessageClient.getMyInfo().toJson());
                map.put("blessings", TextUtils.isEmpty(blessings)
                        ? getString(R.string.red_package_message_str)
                        : blessings);
                sendCustomMsg(map);
                LogUtil.d(TAG, "onActivityResult: " + "yuan.length = " + yuan.length
                        + " ;yuan[0] = " + yuan[0] + " ;yuan[1] = " + yuan[1]);
            }
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

    class SingleReceiver extends BroadcastReceiver {

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
                                , System.currentTimeMillis() + "_" + "FCJMS");
                        isSendVoice = false;
                    }
                    break;
            }
        }
    }
}
