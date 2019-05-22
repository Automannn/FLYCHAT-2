package com.gameex.dw.justtalk.chattingPack;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.gameex.dw.justtalk.ObjPack.Msg;
import com.gameex.dw.justtalk.ObjPack.MsgInfo;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.imagePicker.GifSizeFilter;
import com.gameex.dw.justtalk.imagePicker.Glide4Engine;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.titleBar.OnViewClick;
import com.gameex.dw.justtalk.titleBar.TitleBarView;
import com.gameex.dw.justtalk.userInfo.UserBasicInfoActivity;
import com.gameex.dw.justtalk.util.BarUtil;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import cn.jpush.im.api.BasicCallback;

public class ChattingActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "ChattingActivity";
    /**
     * ZhiHu`s image picker`s request code
     */
    private static final int REQUEST_CODE_CHOOSE = 23;

    private TitleBarView mTitleBar;
    private RecyclerView mRecycler;
    private ChatRecAdapter mRecAdapter;
    private LinearLayout mSendLayout;
    private EditText mSendText;
    private CircularImageView mVoiceCircle, mEmojiCircle, mCircleView;
    private GridView mGridView;
    private SimpleAdapter mSimpleAdapter;

    private List<Map<String, Object>> mGridList = new ArrayList<>();
    private int[] icon = {R.drawable.icon_red_package, R.drawable.icon_photo};
    private String[] iconName = {"红包", "图片"};
    private List<Msg> mMsgs = new ArrayList<>();
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
        new Handler(this.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                getMsgs();
            }
        });
        mRecAdapter = new ChatRecAdapter(this, mMsgs);
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
                if (position == 1) {
                    Matisse.from(ChattingActivity.this)
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
            }
        });
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<Uri> path = Matisse.obtainResult(data);
            Toast.makeText(this, path + "", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (mGridView.getVisibility()==View.VISIBLE){
            mGridView.setVisibility(View.GONE);
        }else{
            super.onBackPressed();
        }
    }
}
