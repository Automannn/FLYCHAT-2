package com.gameex.dw.justtalk.userInfo;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.managePack.BaseActivity;
import com.gameex.dw.justtalk.titleBar.OnViewClick;
import com.gameex.dw.justtalk.titleBar.TitleBarView;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.WindowUtil;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;

import static com.gameex.dw.justtalk.util.DataUtil.CHOOSE_PHOTO;
import static com.gameex.dw.justtalk.util.DataUtil.CROP_PHOTO;
import static com.gameex.dw.justtalk.util.DataUtil.TAKE_PHOTO;

/**
 * 用户详细信息activity
 */
public class UserInfoActivity extends BaseActivity implements View.OnClickListener {
    @SuppressLint("StaticFieldLeak")
    public static UserInfoActivity sUserInfoActivity;
    public static final String UPDATE_USER_INFO = "com.gameex.dw.flychat.UPDATE_USER";

    private static int[] ints;

    private LinearLayout mLinearLayout;
    private TitleBarView mBarView;
    private RelativeLayout mIconInfo, mQRCode, mFlyCode, mFlySign, mNumInfo;
    private CircularImageView mIconImg;
    private TextView mNickName, mChangeIconText, mFlyCodeText, mFlySignText, mNumText, mShareCard, mLoginOut;

    private AlertDialog mDialog;
    private Uri mPhotoUri;
    private Bitmap mIcon;

    private PopupWindow mEditPup;

    private UserInfo mUserInfo;

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
        mUserInfo = UserInfo.fromJson(getIntent().getStringExtra("mine_info"));
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
        mNickName = findViewById(R.id.mine_nick);
        mChangeIconText = findViewById(R.id.change_icon_text);
        mChangeIconText.setOnClickListener(this);
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
        mIconImg.setImageURI(mUserInfo.getExtra("icon_uri") == null
                ? DataUtil.resourceIdToUri(getPackageName(), R.drawable.icon_user)
                : Uri.parse(mUserInfo.getExtra("icon_uri")));
        mNickName.setText(TextUtils.isEmpty(mUserInfo.getNickname()) ? mUserInfo.getUserName()
                : mUserInfo.getNickname());
        mFlySignText.setText(TextUtils.isEmpty(mUserInfo.getSignature()) ? "未设置"
                : mUserInfo.getSignature());
        mNumText.setText(mUserInfo.getUserName());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mine_icon_info_layout:
                Toast.makeText(this, "修改用户名", Toast.LENGTH_SHORT).show();
                break;
            case R.id.change_icon_text:
                showTypeDialog();
                break;
            case R.id.mine_qr_code_layout:
                Toast.makeText(this, "展示二维码", Toast.LENGTH_SHORT).show();
                break;
            case R.id.fly_code_info_layout:
                if (mEditPup != null) {
                    mEditPup.showAtLocation(mLinearLayout, Gravity.CENTER, 0, 0);
                    WindowUtil.showBackgroundAnimator(sUserInfoActivity,0.5f);
                }
                break;
            case R.id.fly_sign_info_layout:
                Toast.makeText(this, "修改个性签名", Toast.LENGTH_SHORT).show();
                break;
            case R.id.mine_num_info_layout:
                Toast.makeText(this, "修改账号", Toast.LENGTH_SHORT).show();
                break;
            case R.id.share_card_text:
                Toast.makeText(this, "分享名片", Toast.LENGTH_SHORT).show();
                break;
            case R.id.login_out_text:
                JMessageClient.logout();
                Intent intentLoginOut = new Intent();
                intentLoginOut.setAction("com.gameex.dw.justtalk.LOGIN_OUT");
                this.sendBroadcast(intentLoginOut);
                break;
            case R.id.take_photo:
                requestPermission();
                break;
            case R.id.choose_from_lib_text:
                Intent choosePhotoIntent = new Intent(Intent.ACTION_PICK, null);
                choosePhotoIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(choosePhotoIntent, CHOOSE_PHOTO);
                mDialog.dismiss();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
                        DataUtil.setPicToView(this, mIcon, "user_icon.jpg");
                        mIconImg.setImageBitmap(mIcon);
                    }
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
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditPup.dismiss();
            }
        });
        sure = view.findViewById(R.id.sure_btn);
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEditPup != null && mEditPup.isShowing()) {
                    mEditPup.dismiss();
                }
                textView.setText(edit.getText().toString());
            }
        });
        mEditPup = new PopupWindow(view, RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        mEditPup.setFocusable(true);
        mEditPup.setOutsideTouchable(false);
        mEditPup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowUtil.setWindowBackgroundAlpha(sUserInfoActivity,1f);
            }
        });
        mEditPup.setAnimationStyle(R.style.pop_anim);
        mEditPup.update();
    }

    /**
     * 获得屏幕的宽高
     */
    private void getWH() {
        ints = new int[2];
        WindowManager windowManager = this.getWindowManager();
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        ints[0] = metrics.widthPixels;
        ints[1] = metrics.heightPixels;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(UPDATE_USER_INFO);
        intent.putExtra("icon_uri", DataUtil.getPhotoUri(this
                , new File(getExternalCacheDir(), "crop_icon.jpg")
                , "crop_user_icon.jpg").toString());
        intent.putExtra("username", mFlyCodeText.getText().toString());
        intent.putExtra("user_signed", "nothing");
        sendBroadcast(intent);
        finish();
    }
}
