package com.gameex.dw.justtalk.chattingPack;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gameex.dw.justtalk.ObjPack.Msg;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.titleBar.OnViewClick;
import com.gameex.dw.justtalk.titleBar.TitleBarView;
import com.gameex.dw.justtalk.util.DataUtil;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

public class ChattingActivity extends BaseActivity implements View.OnClickListener {

    private TitleBarView mTitleBar;
    private RecyclerView mRecycler;
    private ChatRecAdapter mRecAdapter;
    private EditText mSendText;
    private CircularImageView mVoiceCircle, mEmojiCircle, mCircleView;

    private List<Msg> mMsgs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        initView();
    }

    /**
     * 绑定id，设置监听
     */
    private void initView() {
        mTitleBar = findViewById(R.id.title_bar_chatting);
        mTitleBar.setSearchIVVisible(View.GONE);
        mTitleBar.setRightIVImg(R.drawable.icon_user);
        mTitleBar.setTitleSize(14);
        try {
            String username = getIntent().getStringExtra("username");
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

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setRemoveDuration(500);
        animator.setAddDuration(500);
        mRecycler = findViewById(R.id.chat_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setItemAnimator(animator);
        mMsgs = getMsgs();
        mRecAdapter = new ChatRecAdapter(this, mMsgs, getIntent().getStringExtra("msg_first_uncheck_data"));
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
            case R.id.voice_msg:
                Toast.makeText(this, "发送语音", Toast.LENGTH_SHORT).show();
                break;
            case R.id.emoji_circle:
                Toast.makeText(this, "发送表情", Toast.LENGTH_SHORT).show();
                break;
            case R.id.send_circle:
                String content = mSendText.getText().toString();
                if (content.isEmpty()) {
                    Toast.makeText(this, "请编辑后发送...", Toast.LENGTH_SHORT).show();
                    return;
                }
                mMsgs.add(new Msg(content, Msg.Type.SEND));
                //如果有新消息，则设置适配器的长度；通知适配器有新数据插入，并让RecyclerView定位到最后一行
                int newSize = mMsgs.size() - 1;
                mRecAdapter.notifyItemInserted(newSize);
                mRecycler.scrollToPosition(newSize);
                mSendText.setText("");
                break;
            default:
                break;
        }
    }

    /**
     * 聊天界面测试参数
     */
    private List<Msg> getMsgs() {
        List<Msg> msgs = new ArrayList<>();
        Msg msg1 = new Msg("你好", Msg.Type.RECEIVED);
        msg1.setResourceId(R.drawable.icon_user);
        msg1.setTime(DataUtil.getCurrentTimeStr());
        msgs.add(msg1);
        Msg msg2 = new Msg("你好,请问你是？❓😕", Msg.Type.SEND);
        msg2.setResourceId(R.drawable.icon_user);
        msg2.setTime(DataUtil.getCurrentTimeStr());
        msgs.add(msg2);
        Msg msg3 = new Msg("我是Daniel,很高兴认识你！(*^_^*)😀", Msg.Type.RECEIVED);
        msg3.setResourceId(R.drawable.icon_user);
        msg3.setTime(DataUtil.getCurrentTimeStr());
        msgs.add(msg3);
        return msgs;
    }
}
