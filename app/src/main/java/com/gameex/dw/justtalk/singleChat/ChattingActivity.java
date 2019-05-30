package com.gameex.dw.justtalk.singleChat;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

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
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiImageView;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import cn.jpush.im.api.BasicCallback;

import static com.gameex.dw.justtalk.main.BottomBarFat.UPDATE_MSG_INFO;

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

    private ViewGroup mRootView;
    private TitleBarView mTitleBar;
    private RecyclerView mRecycler;
    private ChatRecAdapter mRecAdapter;
    private LinearLayout mSendLayout;
    private EmojiEditText mSendText;
    private CircularImageView mVoiceCircle, mEmojiCircle, mCircleView;
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
        mSendText.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {  //当键盘弹出/隐藏时调用此方法
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
                        mGridView.setVisibility(View.GONE);
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
            }
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
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view
                    , int position, long id) {
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
            }
        });
    }

    /**
     * 初始化emoji表情框
     */
    private void initEmojiPopup() {
        mEmojiPopup = EmojiPopup.Builder.fromRootView(mRootView)
                .setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener() {
                    @Override
                    public void onEmojiBackspaceClick(View v) {
                        LogUtil.d(TAG, "Clicked on Backspace");
                    }
                })
                .setOnEmojiClickListener(new OnEmojiClickListener() {
                    @Override
                    public void onEmojiClick(@NonNull EmojiImageView emoji, @NonNull Emoji imageView) {
                        LogUtil.d(TAG, "Clicked on emoji");
                    }
                })
                .setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
                    @Override
                    public void onEmojiPopupShown() {
                        LogUtil.d(TAG, "Emoji popup id shown");
                    }
                })
                .setOnSoftKeyboardOpenListener(new OnSoftKeyboardOpenListener() {
                    @Override
                    public void onKeyboardOpen(int keyBoardHeight) {
                        LogUtil.d(TAG, "Opened soft keyboard");
                    }
                })
                .setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
                    @Override
                    public void onEmojiPopupDismiss() {
                        LogUtil.d(TAG, "Emoji popup id dismiss");
                    }
                })
                .setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener() {
                    @Override
                    public void onKeyboardClose() {
                        LogUtil.d(TAG, "Closed soft keyboard");
                    }
                })
                .setKeyboardAnimationStyle(R.style.emoji_fade_animation_style)
                .setPageTransformer(new PageTransformer())
                .build(mSendText);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.voice_msg:    //发送语音
                Toast.makeText(this, "发送语音", Toast.LENGTH_SHORT).show();
                break;
            case R.id.emoji_circle: //发送表情
                mEmojiPopup.toggle();
                break;
            case R.id.send_circle:  //发送文本
                String content = mSendText.getText().toString();
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
        } else {
            mGridView.setVisibility(View.GONE);
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
            mGridView.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }
}
