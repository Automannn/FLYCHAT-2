package com.gameex.dw.justtalk.groupChat;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.emoji.PageTransformer;
import com.gameex.dw.justtalk.objPack.MsgInfo;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.groupInfo.GroupInfoActivity;
import com.gameex.dw.justtalk.imagePicker.GifSizeFilter;
import com.gameex.dw.justtalk.imagePicker.Glide4Engine;
import com.gameex.dw.justtalk.redPackage.SetYuanActivity;
import com.gameex.dw.justtalk.util.BarUtil;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.GroupInfoUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.GroupMemberInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import cn.jpush.im.api.BasicCallback;

import static com.gameex.dw.justtalk.main.MsgInfoFragment.UPDATE_MSG_INFO;

public class GroupChatActivity extends AppCompatActivity implements View.OnClickListener {
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
     * 发送语音按钮
     */
    private CircularImageView voiceImg;
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
    private List<Uri> mUris = new ArrayList<>();
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
        JMessageClient.exitConversation();
        JMessageClient.unRegisterEventReceiver(this);
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

        MsgInfo msgInfo = getIntent().getParcelableExtra("msg_info");
        mGroupInfo = GroupInfo.fromJson(msgInfo.getGroupInfoJson());

        mBack.setOnClickListener(this);

        mGroup.setOnClickListener(this);
        GroupInfoUtil.initGroupIcon(mGroupInfo,this,mGroup);

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
        if (mConversation != null) {
            mMessages = mConversation.getAllMessage();
        }
        mChatAdapter = new GroupChatAdapter(this, mMessages);
        mMsgRecycler.setAdapter(mChatAdapter);
        new Handler(this.getMainLooper()).post(() -> getUris());

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
        mEdit.getViewTreeObserver().addOnGlobalLayoutListener(() -> {  //当键盘弹出/隐藏时调用此方法
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
            }
            LogUtil.d(TAG, "initView-onGlobalLayout: " + "Size = " + heightDifference);
        });
        mEmoji.setOnClickListener(this);
        initEmojiPopup();
        mSend.setOnClickListener(this);

        mGridView = findViewById(R.id.function_grid);

        //底部功能栏适配器
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, initGridList()
                , R.layout.function_item, new String[]{"icon", "icon_name"}
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
                .setOnSoftKeyboardOpenListener(keyBoardHeight -> LogUtil.d(TAG, "Opened soft keyboard"))
                .setOnEmojiPopupDismissListener(() -> LogUtil.d(TAG, "Emoji popup id dismiss"))
                .setOnSoftKeyboardCloseListener(() -> LogUtil.d(TAG, "Closed soft keyboard"))
                .setKeyboardAnimationStyle(R.style.emoji_fade_animation_style)
                .setPageTransformer(new PageTransformer())
                .build(mEdit);
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
                startActivity(intent);
                break;
            case R.id.voice_msg:
                Toast.makeText(this, "发送语音", Toast.LENGTH_SHORT).show();
                break;
            case R.id.emoji_circle:
                mEmojiPopup.toggle();
                break;
            case R.id.send_circle:
                String content = mEdit.getText().toString();
                if (content.isEmpty()) {
                    if (mIMM != null && heightDifference > navigationBarHeight) {
                        mIMM.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        new Handler().postDelayed(() -> showFunction(), 50);
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
        } else {
            mGridView.setVisibility(View.GONE);
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
        sendListener(message, true);
    }

    /**
     * 发送图片消息处理
     *
     * @param imgBitmap 图片文件
     */
    private void sendImgMsg(Bitmap imgBitmap) {
        if (mConversation == null) {
            mConversation = Conversation.createGroupConversation(mGroupId);
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
                            Toast.makeText(GroupChatActivity.this
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
            mConversation = Conversation.createGroupConversation(mGroupId);
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
                        mEdit.setText("");
                    }
                } else {
                    Toast.makeText(GroupChatActivity.this, "发送失败，请重试"
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });
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
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(
                        getContentResolver().openInputStream(path.get(0)));
                sendImgMsg(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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
