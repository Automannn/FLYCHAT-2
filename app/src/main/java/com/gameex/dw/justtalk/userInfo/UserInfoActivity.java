package com.gameex.dw.justtalk.userInfo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gameex.dw.justtalk.main.BottomBarActivity;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.titleBar.OnViewClick;
import com.gameex.dw.justtalk.titleBar.TitleBarView;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.UserInfoUtils;
import com.gameex.dw.justtalk.util.WindowUtil;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

import static com.gameex.dw.justtalk.main.MyInfoFragment.UPDATE_USER_INFO;
import static com.gameex.dw.justtalk.util.DataUtil.CHOOSE_PHOTO;
import static com.gameex.dw.justtalk.util.DataUtil.CROP_PHOTO;
import static com.gameex.dw.justtalk.util.DataUtil.TAKE_PHOTO;

/**
 * 用户详细信息activity
 */
public class UserInfoActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "UserInfoActivity";
    @SuppressLint("StaticFieldLeak")
    public static UserInfoActivity sUserInfoActivity;
    /**
     * 修改昵称请求
     */
    public static final int EDIT_NICK_REQUEST_CODE = 201;
    /**
     * 修改个性签名请求
     */
    public static final int EDIT_SIGNATURE_REQUEST_CODE = 202;

    private LinearLayout mLinearLayout;
    private TitleBarView mBarView;
    private RelativeLayout mIconInfo, mQRCode, mFlyCode, mFlySign, mNumInfo;
    private CircularImageView mIconImg;
    private TextView mNickName, mFlyCodeText, mFlySignText, mNumText, mShareCard, mLoginOut;

    private AlertDialog mDialog;
    private Uri mPhotoUri;
    private Bitmap mIcon;

    private PopupWindow mEditPup;

    private UserInfo mUserInfo;

    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initData();
        sUserInfoActivity = this;
    }

    /**
     * 绑定id，添加监听事件
     */
    private void initView() {
        setContentView(R.layout.activity_user_info);
        mLinearLayout = findViewById(R.id.user_info_constraint);
        mBarView = findViewById(R.id.title_bar_user_info);
        mBarView.setRightIVVisible(View.INVISIBLE);
        mBarView.setSearchIVVisible(View.GONE);
        mBarView.setTitle("个人信息");
        mBarView.setOnViewClick(new OnViewClick() {
            @Override
            public void leftClick() {
                finish();
            }

            @Override
            public void searchClick() {

            }

            @Override
            public void rightClick() {

            }
        });
        mIconInfo = findViewById(R.id.mine_icon_info_layout);
        mIconInfo.setOnClickListener(this);
        mIconImg = findViewById(R.id.mine_icon_info);
        mIconImg.setOnClickListener(this);
        mNickName = findViewById(R.id.mine_nick);
        mQRCode = findViewById(R.id.mine_qr_code_layout);
        mFlyCode = findViewById(R.id.fly_code_info_layout);
        mFlyCode.setOnClickListener(this);
        mFlyCodeText = findViewById(R.id.fly_code_info);
        mFlySign = findViewById(R.id.fly_sign_info_layout);
        mFlySign.setOnClickListener(this);
        mFlySignText = findViewById(R.id.fly_sign_info);
        mNumInfo = findViewById(R.id.mine_num_info_layout);
        mNumInfo.setOnClickListener(this);
        mNumText = findViewById(R.id.mine_num_info);
        mShareCard = findViewById(R.id.share_card_text);
        mShareCard.setOnClickListener(this);
        mLoginOut = findViewById(R.id.login_out_text);
        mLoginOut.setOnClickListener(this);

        //加载昵称修改弹出框
        editPupWin("飞聊号", mFlyCodeText.getText().toString(), mFlyCodeText);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mUserInfo = UserInfo.fromJson(getIntent().getStringExtra("mine_info"));
        UserInfoUtils.initUserIcon(mUserInfo, BottomBarActivity.sBottomBarActivity
                , mIconImg);
        mNickName.setText(TextUtils.isEmpty(mUserInfo.getNickname()) ? mUserInfo.getUserName()
                : mUserInfo.getNickname());
        mFlySignText.setText(TextUtils.isEmpty(mUserInfo.getSignature()) ? "未设置"
                : mUserInfo.getSignature());
        mNumText.setText(mUserInfo.getUserName());
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.mine_icon_info:
                showTypeDialog();
                break;
            case R.id.mine_icon_info_layout:
                intent.setClass(this, EditMyInfoActivity.class);
                intent.putExtra("title", "昵称");
                intent.putExtra("my_nick", mNickName.getText());
                startActivityForResult(intent, EDIT_NICK_REQUEST_CODE);
                break;
            case R.id.mine_qr_code_layout:
                Toast.makeText(this, "展示二维码", Toast.LENGTH_SHORT).show();
                break;
            case R.id.fly_code_info_layout:
                if (mEditPup != null) {
                    mEditPup.showAtLocation(mLinearLayout, Gravity.CENTER, 0, 0);
                    WindowUtil.showBackgroundAnimator(sUserInfoActivity, 0.5f);
                }
                break;
            case R.id.fly_sign_info_layout:
                intent.setClass(this, EditMyInfoActivity.class);
                intent.putExtra("title", "个性签名");
                intent.putExtra("my_signature", mFlySignText.getText());
                startActivityForResult(intent, EDIT_SIGNATURE_REQUEST_CODE);
                break;
            case R.id.mine_num_info_layout:
                Toast.makeText(this, "修改账号", Toast.LENGTH_SHORT).show();
                break;
            case R.id.share_card_text:
                Toast.makeText(this, "分享名片", Toast.LENGTH_SHORT).show();
                break;
            case R.id.login_out_text:
                JMessageClient.logout();
                intent.setAction("com.gameex.dw.justtalk.LOGIN_OUT");
                this.sendBroadcast(intent);
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
            default:
                break;
        }
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
                    mIcon = extras.getParcelable("data");
                    if (mIcon != null) {
                        File file = DataUtil.setPicToView(this, mIcon, "user_icon.jpg");
                        JMessageClient.updateUserAvatar(file, "user_icon", new BasicCallback() {
                            @Override
                            public void gotResult(int i, String s) {
                                LogUtil.d(TAG, "onActivityResult-CROP_PHOTO: " +
                                        "responseCode = " + i + " ;desc = " + s);
                                Toast.makeText(UserInfoActivity.this, "正在上传头像"
                                        , Toast.LENGTH_SHORT).show();
                                if (i == 0) {
                                    flag = true;
                                    mIconImg.setImageBitmap(mIcon);
                                    Toast.makeText(UserInfoActivity.this, "头像上传成功"
                                            , Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(UserInfoActivity.this, "头像上传失败"
                                            , Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
                break;
            case EDIT_NICK_REQUEST_CODE:
                if (data != null && resultCode == RESULT_OK) {
                    String nick = data.getStringExtra("my_nick");
                    mNickName.setText(nick);
                    mUserInfo.setNickname(nick);
                    flag = true;
                }
                break;
            case EDIT_SIGNATURE_REQUEST_CODE:
                if (data != null && resultCode == RESULT_OK) {
                    String signature = data.getStringExtra("my_signature");
                    mFlySignText.setText(signature);
                    mUserInfo.setSignature(signature);
                    flag = true;
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    /**
     * 获取相机权限
     */
    private void requestPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA}, 1);
        } else {
            Intent takePhotoIntent = new Intent("android.media.action.IMAGE_CAPTURE");
            mPhotoUri = DataUtil.getPhotoUri(this, null, "take_photo.jpg");
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
            startActivityForResult(takePhotoIntent, TAKE_PHOTO);
            mDialog.dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode
            , @NonNull String[] permissions
            , @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "无法拍照", Toast.LENGTH_SHORT).show();
                } else {
                    Intent takePhotoIntent = new Intent("android.media.action.IMAGE_CAPTURE");
                    mPhotoUri = DataUtil.getPhotoUri(this, null, "take_photo.jpg");
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                    startActivityForResult(takePhotoIntent, TAKE_PHOTO);
                    mDialog.dismiss();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 可指定组件visible和hint属性的PopupWindow
     *
     * @param hint 提示文字
     */
    public void editPupWin(String hint, String text, final TextView textView) {
        @SuppressLint("InflateParams") View view = this.getLayoutInflater().inflate(R.layout.user_info_edit_pup, null);
        final EditText edit;
        Button cancel, sure;
        edit = view.findViewById(R.id.user_info_edit);
        edit.setHint(hint);
        edit.setText(text);
        cancel = view.findViewById(R.id.cancel_btn);
        cancel.setOnClickListener(view1 -> mEditPup.dismiss());
        sure = view.findViewById(R.id.sure_btn);
        sure.setOnClickListener(view12 -> {
            if (mEditPup != null && mEditPup.isShowing()) {
                mEditPup.dismiss();
            }
            textView.setText(edit.getText().toString());
        });
        mEditPup = new PopupWindow(view, RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        mEditPup.setFocusable(true);
        mEditPup.setOutsideTouchable(false);
        mEditPup.setOnDismissListener(() -> WindowUtil.setWindowBackgroundAlpha(sUserInfoActivity, 1f));
        mEditPup.setAnimationStyle(R.style.pop_anim);
        mEditPup.update();
    }

    @Override
    public void onBackPressed() {
        if (flag) {
            JMessageClient.updateMyInfo(UserInfo.Field.all, mUserInfo, new BasicCallback() {
                @Override
                public void gotResult(int i, String s) {
                    LogUtil.d(TAG, "onBackPressed-gotResult: "
                            + "responseCode = " + i + " ; desc = " + s);
                    if (i == 0) {
                        Intent intent = new Intent(UPDATE_USER_INFO);
                        sendBroadcast(intent);
                        Toast.makeText(UserInfoActivity.this
                                , "用户信息更新成功", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(UserInfoActivity.this
                                , "用户信息更新失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            super.onBackPressed();
        }
    }
}
