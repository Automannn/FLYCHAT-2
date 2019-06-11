package com.gameex.dw.justtalk.singleChat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.gameex.dw.justtalk.soundController.RecordingService;
import com.gameex.dw.justtalk.emoji.PageTransformer;
import com.gameex.dw.justtalk.objPack.MsgInfo;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.imagePicker.GifSizeFilter;
import com.gameex.dw.justtalk.imagePicker.Glide4Engine;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.redPackage.SingleRedActivity;
import com.gameex.dw.justtalk.titleBar.OnViewClick;
import com.gameex.dw.justtalk.titleBar.TitleBarView;
import com.gameex.dw.justtalk.userInfo.UserBasicInfoActivity;
import com.gameex.dw.justtalk.util.BarUtil;
import com.gameex.dw.justtalk.util.DataUtil;
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
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import cn.jpush.im.api.BasicCallback;
import es.dmoral.toasty.Toasty;

import static com.gameex.dw.justtalk.main.MsgInfoFragment.UPDATE_MSG_INFO;

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
            "com.gameex.dw.justtalk.singleChat.ChattingActivity.RECORD_COMPLETE";

    private ViewGroup mRootView;
    private TitleBarView mTitleBar;
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
    private int[] icon = {R.drawable.icon_red_package, R.drawable.icon_photo};
    private String[] iconName = {"红包", "图片"};
    private List<Message> mMessages = new ArrayList<>();
    private MsgInfo mMsgInfo;
    private UserInfo mUserInfo;

    private Conversation mConversation;
    /**
     * 软键盘相关
     */
    private InputMethodManager mIMM;
    /**
     * 虚拟键高度，若没有/隐藏虚拟键，则为0
     */
    private int navigationBarHeight;
    /**
     * 软件盘高度，有虚拟键，则为软键盘高度+虚拟键高度 记录
     */
    private int mHeightDifference = -1;
    /**
     * 软件盘高度，有虚拟键，则为软键盘高度+虚拟键高度
     */
    private int heightDifference;
    /**
     * 记录键盘高度
     */
    private int mInt = -1;
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
                            if (mIMM != null && heightDifference > navigationBarHeight) {
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
    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        setContentView(R.layout.activity_chatting);
        mRootView = findViewById(R.id.view_single);
        mIMM = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        //获得虚拟键高度
        navigationBarHeight = BarUtil.getNavigationBarHeight(this);

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
                intent.putExtra("user_info_json", mMsgInfo.getUserInfoJson());
                intent.putExtra("from_single_chat", true);
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
        if (mConversation != null) {
            mMessages = mConversation.getAllMessage();
        }
        mRecAdapter = new ChatRecAdapter(this, mMessages);
        mRecycler.setAdapter(mRecAdapter);

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
        mSendText.getViewTreeObserver().addOnGlobalLayoutListener(() -> {  //当键盘弹出/隐藏时调用此方法
            Rect r = new Rect();
            //获取当前界面可视部分
            ChattingActivity.this.getWindow()
                    .getDecorView()
                    .getWindowVisibleDisplayFrame(r);
            //获取屏幕的高度
            int screenHeight = ChattingActivity.this.getWindow()
                    .getDecorView()
                    .getRootView()
                    .getHeight();
            //此处就是用来获取键盘(或键盘+虚拟键)的高度的， 在键盘没有弹出的时候 此高度为0 键盘弹出的时候为一个正数
            heightDifference = screenHeight - r.bottom;
            if (heightDifference <= navigationBarHeight) {
                mHeightDifference = heightDifference;   //没有虚拟键，记录此时的高度
            }
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                    mSendLayout.getLayoutParams();
            if (heightDifference > navigationBarHeight) {
                if (mGridView.getVisibility() == View.VISIBLE) {
                    showFunction();
                }
                if (mHeightDifference == 0 && params.bottomMargin != mInt) {
                    params.bottomMargin = heightDifference;
                    mSendLayout.setLayoutParams(params);
                    mInt = heightDifference;    //记录键盘高度
                } else if (mHeightDifference != 0 && params.bottomMargin != mInt) {
                    mInt = heightDifference - navigationBarHeight;
                    if (params.bottomMargin != mInt) {
                        params.bottomMargin = mInt;
                        mSendLayout.setLayoutParams(params);
                    }
                }
            } else {
                if (params.bottomMargin == mInt) {
                    params.bottomMargin = 0;
                    mSendLayout.setLayoutParams(params);
                }
            }
            LogUtil.d(TAG, "initView-onGlobalLayout: "
                    + "heightDifference = " + heightDifference
                    + " ;navigationBarHeight = " + navigationBarHeight);
        });
        mEmojiCircle = findViewById(R.id.emoji_circle);
        mEmojiCircle.setOnClickListener(this);
        initEmojiPopup();

        mCircleView = findViewById(R.id.send_circle);
        mCircleView.setOnClickListener(this);

        mGridView = findViewById(R.id.function_grid);
        mSimpleAdapter = new SimpleAdapter(this, initGridList()
                , R.layout.function_item, new String[]{"icon", "icon_name"}
                , new int[]{R.id.function_img, R.id.function_name});
        mGridView.setAdapter(mSimpleAdapter);
        mGridView.setOnItemClickListener((adapterView, view, position, id) -> {
            if (position == 0) {
                Intent intent = new Intent(ChattingActivity.this
                        , SingleRedActivity.class);
                startActivityForResult(intent, REQUEST_SINGLE_RED_PACKAGE);
            } else if (position == 1) {
                Matisse.from(ChattingActivity.this)
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
                .setOnSoftKeyboardOpenListener(keyBoardHeight -> LogUtil.d(TAG, "Opened soft keyboard"))
                .setOnEmojiPopupDismissListener(() -> LogUtil.d(TAG, "Emoji popup id dismiss"))
                .setOnSoftKeyboardCloseListener(() -> LogUtil.d(TAG, "Closed soft keyboard"))
                .setKeyboardAnimationStyle(R.style.emoji_fade_animation_style)
                .setPageTransformer(new PageTransformer())
                .build(mSendText);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.voice_msg:    //发送语音
                requestPermission();
                break;
            case R.id.emoji_circle: //发送表情
                mEmojiPopup.toggle();
                break;
            case R.id.send_circle:  //发送文本
                String content = Objects.requireNonNull(mSendText.getText()).toString();
                if (content.isEmpty()) {
                    if (mIMM != null && heightDifference > navigationBarHeight) {
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
        sendListener(message, true);
    }

    /**
     * 发送图片消息处理
     *
     * @param imgBitmap 图片文件
     */
    private void sendImgMsg(Bitmap imgBitmap) {
        if (mConversation == null) {
            mConversation = Conversation.createSingleConversation(mUserInfo.getUserName());
        }
        ImageContent.createImageContentAsync(imgBitmap, "flychat"
                , new ImageContent.CreateImageContentCallback() {
                    @Override
                    public void gotResult(int i, String s, ImageContent imageContent) {
                        if (i == 0) {
                            assert mConversation != null;
                            final Message message = mConversation.createSendMessage(imageContent);
                            sendListener(message, false);
                        } else {
                            Toast.makeText(ChattingActivity.this
                                    , "发送图片失败-" + s, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
            sendListener(message, false);
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
        sendListener(message, false);
    }

    /**
     * 消息发送监听
     *
     * @param message     消息对象
     * @param isClearText 是否清空文本编辑框
     */
    private void sendListener(final Message message, final boolean isClearText) {
        message.setOnSendCompleteCallback(new BasicCallback() {
            @Override
            public void gotResult(int requestCode, String responseDesc) {
                if (requestCode == 0) {
                    LogUtil.d("requestCode = 0",
                            "发送成功" + "responseDesc = " + responseDesc);
                    updateAdapter(message);
                    if (isClearText) {
                        mSendText.setText("");
                    }
                } else {
                    Toast.makeText(ChattingActivity.this, "发送失败，请重试"
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode
            , @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            assert data != null;
            List<Uri> path = Matisse.obtainResult(data);
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(
                        getContentResolver().openInputStream(path.get(0)));
                sendImgMsg(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_SINGLE_RED_PACKAGE && resultCode == RESULT_OK) {
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
