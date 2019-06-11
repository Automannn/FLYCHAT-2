package com.gameex.dw.justtalk.groupInfo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.main.BottomBarActivity;
import com.gameex.dw.justtalk.userInfo.EditMyInfoActivity;
import com.gameex.dw.justtalk.userInfo.UserInfoActivity;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.GroupInfoUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;

import java.io.File;
import java.util.List;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.GroupMemberInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import es.dmoral.toasty.Toasty;

import static com.gameex.dw.justtalk.main.MyInfoFragment.REQUEST_CODE_SCAN;
import static com.gameex.dw.justtalk.util.DataUtil.CHOOSE_PHOTO;
import static com.gameex.dw.justtalk.util.DataUtil.CROP_PHOTO;
import static com.gameex.dw.justtalk.util.DataUtil.TAKE_PHOTO;

/**
 * 群组详细信息展示
 */
public class GroupInfoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "GroupInfoActivity";
    /**
     * 修改群昵称请求码
     */
    private static final int EDIT_GROUP_NICK_CODE = 101;
    /**
     * 修改我在本群的昵称请求码
     */
    private static final int EDIT_MY_GROUP_NICK_CODE = 102;
    /**
     * 返回箭头
     */
    private ImageView mBack;
    /**
     * 页面标题
     */
    private TextView mTitle;
    /**
     * 右上角功能键
     */
    private ImageView mMore;
    /**
     * 包含头像的layout
     */
    private RelativeLayout mIconLayout;
    /**
     * 我的群昵称的layout
     */
    private RelativeLayout mNickLayout;
    /**
     * 群二维码layout
     */
    private RelativeLayout mQrCodeLayout;
    /**
     * 消息推送开关layout
     */
    private RelativeLayout mPushLayout;
    /**
     * 邀请群成员layout
     */
    private RelativeLayout mInviteMember;
    /**
     * 群头像
     */
    private CircularImageView mIcon;
    /**
     * 群名称
     */
    private TextView mName;
    /**
     * 我的群昵称
     */
    private TextView mNick;
    /**
     * 群公告
     */
    private TextView mNotice;
    /**
     * 群管理
     */
    private TextView mManage;
    /**
     * 群消息通知
     */
    private TextView mPush;
    /**
     * 长时间未领取红包
     */
    private TextView mMoneyGift;
    /**
     * 群聊天记录
     */
    private TextView mChatRecord;
    /**
     * 群聊天文件
     */
    private TextView mChatFile;
    /**
     * 查看群成员
     */
    private RelativeLayout mMember;
    /**
     * 群成员数
     */
    private TextView mMemberCount;
    /**
     * 群组体
     */
    private GroupInfo mGroupInfo;

    private AlertDialog mDialog;
    private Uri mPhotoUri;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        setContentView(R.layout.activity_group);
        mBack = findViewById(R.id.back);
        mTitle = findViewById(R.id.title);
        mMore = findViewById(R.id.more);
        mIconLayout = findViewById(R.id.group_icon_info_layout);
        mIcon = findViewById(R.id.group_icon_info);
        mMember = findViewById(R.id.group_member);
        mName = findViewById(R.id.group_nick);
        mNickLayout = findViewById(R.id.mine_group_nick_layout);
        mNick = findViewById(R.id.mine_group_nick);
        mQrCodeLayout = findViewById(R.id.group_qr_code_layout);
        mNotice = findViewById(R.id.group_notice);
        mManage = findViewById(R.id.group_manage);
        mPushLayout = findViewById(R.id.group_push_layout);
        mPush = findViewById(R.id.group_push_nick);
        mMoneyGift = findViewById(R.id.money_gift_long_time_no_see);
        mChatRecord = findViewById(R.id.group_chat_record);
        mChatFile = findViewById(R.id.group_chat_file);
        mMemberCount = findViewById(R.id.group_member_count);
        mInviteMember = findViewById(R.id.invite_member_layout);
    }

    /**
     * 初始化数据
     */
    @SuppressLint("SetTextI18n")
    private void initData() {
        mGroupInfo = GroupInfo.fromJson(getIntent().getStringExtra("group_info"));

        mBack.setOnClickListener(this);
        mTitle.setText("群详情");
        mMore.setOnClickListener(this);

        mIconLayout.setOnClickListener(this);
        mIcon.setOnClickListener(this);
        GroupInfoUtil.initGroupIcon(mGroupInfo, this, mIcon);
        mMember.setOnClickListener(this);
        mName.setText(mGroupInfo.getGroupName());
        mNickLayout.setOnClickListener(this);
        mNick.setText(getGroupNick(JMessageClient.getMyInfo().getUserName()));
        mQrCodeLayout.setOnClickListener(this);
        mNotice.setOnClickListener(this);
        mManage.setOnClickListener(this);
        mPushLayout.setOnClickListener(this);
        if (mGroupInfo.isGroupBlocked() == 0) {
            mPush.setText("屏蔽");
            mPush.setTextColor(Color.RED);
        }
        mMoneyGift.setOnClickListener(this);
        mChatRecord.setOnClickListener(this);
        mChatFile.setOnClickListener(this);
        List<GroupMemberInfo> memberInfos = mGroupInfo.getGroupMemberInfos();
        if (memberInfos != null && memberInfos.size() > 0) {
            mMemberCount.setText(memberInfos.size() + "");
        } else {
            mMemberCount.setText("0");
        }
        mInviteMember.setOnClickListener(this);
    }

    /**
     * 获取相机权限
     */
    @SuppressLint("CheckResult")
    private void requestPermission() {
        new RxPermissions(this)
                .request(Manifest.permission.CAMERA)
                .subscribe(granted -> {
                    if (granted) {
                        Intent takePhotoIntent = new Intent("android.media.action.IMAGE_CAPTURE");
                        mPhotoUri = DataUtil.getPhotoUri(this, null, "take_photo.jpg");
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                        startActivityForResult(takePhotoIntent, TAKE_PHOTO);
                        mDialog.dismiss();
                    } else {
                        Toasty.warning(this, "Denied permission"
                                , Toasty.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * 改变图片方式Dialog
     */
    private void showTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        mDialog = builder.create();
        View view = View.inflate(this, R.layout.dialog_set_photo, null);
        TextView takePhoto = view.findViewById(R.id.take_photo);
        takePhoto.setOnClickListener(this);
        TextView choosePhoto = view.findViewById(R.id.choose_from_lib_text);
        choosePhoto.setOnClickListener(this);
        mDialog.setView(view);
        mDialog.show();
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.more:
                Toasty.custom(this, "更多", R.drawable.vector_hook_check
                        , R.color.colorBlack, Toasty.LENGTH_SHORT, true
                        , false).show();
                break;
            case R.id.group_icon_info_layout:   //修改群昵称
                intent.setClass(this, EditMyInfoActivity.class);
                intent.putExtra("title", "群昵称");
                intent.putExtra("my_nick", mName.getText());
                startActivityForResult(intent, EDIT_GROUP_NICK_CODE);
                break;
            case R.id.group_icon_info:
                showTypeDialog();   //修改群头像
                break;
            case R.id.group_member:
                Toasty.info(this, "查看群成员").show();
                break;
            case R.id.mine_group_nick_layout:   //修改我在本群的昵称
                intent.setClass(this, EditMyInfoActivity.class);
                intent.putExtra("title", "我在本群的昵称");
                intent.putExtra("my_nick", mNick.getText());
                startActivityForResult(intent, EDIT_MY_GROUP_NICK_CODE);
                break;
            case R.id.group_qr_code_layout: //展示群二维码
                Toasty.custom(this, "查看群二维码", R.drawable.vector_hook_check
                        , R.color.colorBlack, Toasty.LENGTH_SHORT, false, true).show();
                break;
            case R.id.group_notice: //操作群公告
                Toasty.Config.getInstance()
                        .setTextSize(16)
                        .allowQueue(true)
                        .apply();
                Toasty.custom(this, "查看群管理", R.drawable.vector_hook_check
                        , R.color.colorBlack, Toasty.LENGTH_SHORT, true, true).show();
                Toasty.Config.reset();
                break;
            case R.id.group_manage: //操作群管理
                Toasty.info(this, "查看群管理").show();
                break;
            case R.id.group_push_layout:
                if (mGroupInfo.isGroupBlocked() == 1) {
                    mPush.setText("屏蔽");
                    mPush.setTextColor(Color.RED);
                    mGroupInfo.setBlockGroupMessage(0, new BasicCallback() {
                        @Override
                        public void gotResult(int i, String s) {
                            LogUtil.d(TAG, "onClick-group_push_layout: " + "requestCode = " + i +
                                    " ;desc = " + s);
                            if (i == 0) {
                                Toast.makeText(GroupInfoActivity.this, "已开启"
                                        , Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    mPush.setText("开启");
                    mPush.setTextColor(getResources().getColor(R.color.colorLightGray));
                    mGroupInfo.setBlockGroupMessage(1, new BasicCallback() {
                        @Override
                        public void gotResult(int i, String s) {
                            LogUtil.d(TAG, "onClick-group_push_layout: " + "requestCode = " + i +
                                    " ;desc = " + s);
                            if (i == 0) {
                                Toast.makeText(GroupInfoActivity.this, "已屏蔽"
                                        , Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                break;
            case R.id.money_gift_long_time_no_see:
                Toasty.info(this, "查看长时间未领取的红包").show();
                break;
            case R.id.group_chat_record:
                Toasty.info(this, "查看聊天记录").show();
                break;
            case R.id.group_chat_file:
                Toasty.info(this, "查看聊天文件").show();
                break;
            case R.id.invite_member_layout: //邀请群成员
                Toasty.info(this, "邀请群成员").show();
                break;
            case R.id.take_photo:
                requestPermission();
                break;
            case R.id.choose_from_lib_text:
                Intent choosePhotoIntent = new Intent(Intent.ACTION_PICK, null);
                choosePhotoIntent.setDataAndType(MediaStore.Images
                        .Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(choosePhotoIntent, CHOOSE_PHOTO);
                mDialog.dismiss();
                break;
        }
    }

    /**
     * 获取用户在该群组的群昵称
     *
     * @param username 用户名
     * @return string
     */
    private String getGroupNick(String username) {
        GroupMemberInfo memberInfo = mGroupInfo.getGroupMember(username, null);
        return memberInfo.getNickName();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode
            , @Nullable Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    DataUtil.cropPhoto(this, null, mPhotoUri);
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    assert data != null;
                    DataUtil.cropPhoto(this, null, data.getData());
                }
                break;
            case CROP_PHOTO:
                if (data != null) {
                    Bundle extras = data.getExtras();
                    assert extras != null;
                    mBitmap = extras.getParcelable("data");
                    if (mBitmap != null) {
                        File file = DataUtil.setPicToView(this, mBitmap, "user_icon.jpg");
                        mGroupInfo.updateAvatar(file, "group_icon", new BasicCallback() {
                            @Override
                            public void gotResult(int i, String s) {
                                LogUtil.d(TAG, "onActivityResult-CROP_PHOTO: " +
                                        "responseCode = " + i + " ;desc = " + s);
                                Toasty.info(GroupInfoActivity.this, "正在上传头像"
                                        , Toasty.LENGTH_SHORT, true).show();
                                if (i == 0) {
                                    mIcon.setImageBitmap(mBitmap);
                                    Toasty.success(GroupInfoActivity.this, "头像上传成功"
                                            , Toasty.LENGTH_SHORT, true).show();
                                } else {
                                    LogUtil.d(TAG, "onActivityResult-CROP_PHOTO: "
                                            + "responseCode = " + i + " ;desc = " + s);
                                    Toasty.error(GroupInfoActivity.this, "头像上传失败"
                                            , Toasty.LENGTH_SHORT, true).show();
                                }
                            }
                        });
                    }
                }
                break;
            case EDIT_GROUP_NICK_CODE:
                if (data != null && resultCode == RESULT_OK) {
                    String nick = data.getStringExtra("my_nick");
                    mGroupInfo.updateName(nick, new BasicCallback() {
                        @Override
                        public void gotResult(int i, String s) {
                            if (i == 0) {
                                mName.setText(nick);
                            } else {
                                LogUtil.d(TAG, "onActivityResult-EDIT_GROUP_NICK_CODE: "
                                        + "responseCode = " + i + " ;desc = " + s);
                                Toasty.error(GroupInfoActivity.this, "群昵称更新失败"
                                        , Toasty.LENGTH_SHORT, true).show();
                            }
                        }
                    });
                }
                break;
            case EDIT_MY_GROUP_NICK_CODE:
                if (data != null && resultCode == RESULT_OK) {
                    String myNick = data.getStringExtra("my_nick");
                    UserInfo myInfo = JMessageClient.getMyInfo();
                    mGroupInfo.setMemNickname(myInfo.getUserName(), null, myNick, new BasicCallback() {
                        @Override
                        public void gotResult(int i, String s) {
                            if (i == 0) {
                                mNick.setText(myNick);
                            } else {
                                LogUtil.d(TAG, "onActivityResult-EDIT_MY_GROUP_NICK_CODE: "
                                        + "responseCode = " + i + " ;desc = " + s);
                                Toasty.error(GroupInfoActivity.this, "我的群昵称更新失败"
                                        , Toasty.LENGTH_SHORT, true).show();
                            }
                        }
                    });
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
