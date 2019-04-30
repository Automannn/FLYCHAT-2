package com.gameex.dw.justtalk.jiguangIM;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.gameex.dw.justtalk.chattingPack.ChattingActivity;

import java.io.File;
import java.io.IOException;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.content.FileContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.LocationContent;
import cn.jpush.im.android.api.content.PromptContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VideoContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;

/**
 * Created by chenyn on 16/4/6.
 *
 * @desc :  极光IMG
 */
@SuppressLint("Registered")
public class ShowMessageActivity extends Activity {
    public static final String EXTRA_MSG_TYPE = "msg_type";

    public static final String EXTRA_IS_GROUP = "isGroup";

    public static final String EXTRA_FROM_USERNAME = "from_username";

    public static final String EXTRA_FROM_APPKEY = "from_appkey";

    public static final String EXTRA_GROUPID = "from_gid";

    public static final String EXTRA_MSGID = "msgid";

    private final String TAG = ShowMessageActivity.class.getSimpleName();

    private ContentType contentType;
    private Message message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        String msgTypeString = intent.getStringExtra(EXTRA_MSG_TYPE);
        contentType = ContentType.valueOf(msgTypeString);
        boolean isGroup = intent.getBooleanExtra(EXTRA_IS_GROUP, false);
        long gid = intent.getLongExtra(EXTRA_GROUPID, 0);
        String user = intent.getStringExtra(EXTRA_FROM_USERNAME);
        String appkey = intent.getStringExtra(EXTRA_FROM_APPKEY);
        int msgid = intent.getIntExtra(EXTRA_MSGID, 0);
        Conversation conversation;
        if (isGroup) {
            conversation = JMessageClient.getGroupConversation(gid);
        } else {
            conversation = JMessageClient.getSingleConversation(user, appkey);
        }
        if (conversation == null) {
            Toast.makeText(getApplicationContext(), "会话对象为null", Toast.LENGTH_SHORT).show();
            return;
        }
        message = conversation.getMessage(msgid);

        switch (contentType) {
            case text:
                TextContent textContent = (TextContent) message.getContent();
                break;
            case image:

                //缩略图在接收图片消息时由sdk自动下载。
                String thumbLocalPath = ((ImageContent) message.getContent()).getLocalThumbnailPath();
                if (!TextUtils.isEmpty(thumbLocalPath)) {
                    Bitmap bitmap = BitmapFactory.decodeFile(thumbLocalPath);
                }
                break;

            case voice:
                String voiceFilePath = ((VoiceContent) message.getContent()).getLocalPath();
                //语音文件在接收语音消息时由sdk自动下载。
                break;

            case location:
                LocationContent locationContent = (LocationContent) message.getContent();
                break;
            case file:
                message = conversation.getMessage(msgid);
                break;
            case custom:
                CustomContent content = (CustomContent) message.getContent();
                break;
            case eventNotification:
                EventNotificationContent eventNotificationContent = (EventNotificationContent) message.getContent();
                break;
            case prompt:
                PromptContent promptContent = (PromptContent) message.getContent();
                break;
            case video:
                //视频缩略图在接收视频消息时由sdk自动下载
                String videoThumbPath = ((VideoContent) message.getContent()).getThumbLocalPath();
                if (!TextUtils.isEmpty(videoThumbPath)) {
                    Bitmap bitmap = BitmapFactory.decodeFile(videoThumbPath);
                }
                break;
            default:
                break;
        }
    }

    private void cancelDownload() {
        if (message != null) {
            switch (contentType) {
                case image:
                    ImageContent imageContent = (ImageContent) message.getContent();
                    imageContent.cancelDownload(message);
                    break;
                case file:
                    FileContent fileContent = (FileContent) message.getContent();
                    fileContent.cancelDownload(message);
                    break;
                case video:
                    VideoContent videoContent = (VideoContent) message.getContent();
                    videoContent.cancelDownload(message);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
