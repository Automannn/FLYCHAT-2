package com.gameex.dw.justtalk.createGroup;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.publicInterface.DoneCreateGroupCallBack;
import com.gameex.dw.justtalk.util.DataUtil;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.model.UserInfo;

import static android.app.Activity.RESULT_OK;
import static com.gameex.dw.justtalk.createGroup.ChooseContactFragment.ARG_PARAM;
import static com.gameex.dw.justtalk.createGroup.CreateGroupActivity.sActivity;
import static com.gameex.dw.justtalk.util.DataUtil.CHOOSE_PHOTO;
import static com.gameex.dw.justtalk.util.DataUtil.CROP_PHOTO;
import static com.gameex.dw.justtalk.util.DataUtil.TAKE_PHOTO;

public class DoneCreateFragment extends Fragment implements View.OnClickListener {

    private CircularImageView mIcon;
    private EditText mName;
    private TextView mNum;
    private RecyclerView mView;

    private List<UserInfo> mUserInfos = new ArrayList<>();
    private AlertDialog mDialog;
    private Uri mPhotoUri;

    private List<Uri> mUris = new ArrayList<>();

    private DoneCreateGroupCallBack mCallBack;

    public static DoneCreateFragment newInstance(String userInfosStr) {
        DoneCreateFragment fragment = new DoneCreateFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PARAM, userInfosStr);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        mActivity = (Activity) context;
        assert getArguments() != null;
        mUserInfos = (List<UserInfo>) UserInfo.fromJsonToCollection(
                getArguments().getString(ARG_PARAM));
        mCallBack = (DoneCreateGroupCallBack) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater
            , @Nullable ViewGroup container
            , @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.set_basic_info_create_group, container, false);
        mIcon = view.findViewById(R.id.group_icon_create);
        mName = view.findViewById(R.id.group_name_create);
        mNum = view.findViewById(R.id.group_num_create);
        mView = view.findViewById(R.id.group_member_rec);
        initData();
        return view;
    }

    /**
     * 获取相机权限
     */
    private void requestPermission() {

        if (ContextCompat.checkSelfPermission(sActivity,
                Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(sActivity, new String[]{
                    Manifest.permission.CAMERA}, 1);
        } else {
            Intent takePhotoIntent = new Intent("android.media.action.IMAGE_CAPTURE");
            mPhotoUri = DataUtil.getPhotoUri(sActivity, null, "take_photo.jpg");
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
                    Toast.makeText(sActivity, "无法拍照", Toast.LENGTH_SHORT).show();
                } else {
                    Intent takePhotoIntent = new Intent("android.media.action.IMAGE_CAPTURE");
                    mPhotoUri = DataUtil.getPhotoUri(sActivity, null, "take_photo.jpg");
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
     * 初始化数据
     */
    private void initData() {
        mIcon.setOnClickListener(this);
        mName.setText("添加群名称");
        mName.setOnFocusChangeListener((view, b) -> {
            if (b) {
                mName.setText("");
            } else {
                mCallBack.sendGroupName(mName.getText().toString());
            }
        });
        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mCallBack.sendGroupName(mName.getText().toString());
            }
        });
        mNum.setText(mUserInfos == null ? "0个群成员" : mUserInfos.size() + "个群成员");
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(300);
        animator.setChangeDuration(300);
        animator.setMoveDuration(300);
        animator.setRemoveDuration(300);
        mView.setItemAnimator(animator);
        mView.setLayoutManager(new LinearLayoutManager(sActivity));
        DoneCreateAdapter adapter = new DoneCreateAdapter(sActivity, mUserInfos);
        mView.setAdapter(adapter);

        setUris();
    }

    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    DataUtil.cropPhoto(null, this, mPhotoUri);
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    assert data != null;
                    DataUtil.cropPhoto(null, this, data.getData());
                }
                break;
            case CROP_PHOTO:
                if (data != null) {
                    Bundle extras = data.getExtras();
                    assert extras != null;
                    Bitmap icon = extras.getParcelable("data");
                    if (icon != null) {
                        DataUtil.setPicToView(sActivity, icon, "crop_group_icon.jpg");
                        mIcon.setImageBitmap(icon);
                        mCallBack.sendGroupIcon(Uri.parse(MediaStore.Images.Media
                                .insertImage(Objects.requireNonNull(getActivity()).getContentResolver()
                                        , icon, null, null)));
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.group_icon_create:
                showTypeDialog();
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

    /**
     * 改变图片方式Dialog
     */
    private void showTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(sActivity);
        mDialog = builder.create();
        View view = View.inflate(sActivity, R.layout.dialog_set_photo, null);
        TextView takePhoto = view.findViewById(R.id.take_photo);
        takePhoto.setOnClickListener(this);
        TextView choosePhoto = view.findViewById(R.id.choose_from_lib_text);
        choosePhoto.setOnClickListener(this);
        mDialog.setView(view);
        mDialog.show();
    }

    /**
     * 提取已选联系人的头像信息
     */
    @SuppressLint("NewApi")
    private void setUris() {
        if (mUserInfos == null) {
            return;
        }
        for (UserInfo userInfo : mUserInfos) {
            String uri = userInfo.getExtra("icon_uri");
            mUris.add(TextUtils.isEmpty(uri) ? DataUtil.resourceIdToUri(
                    Objects.requireNonNull(getActivity()).getPackageName(), R.drawable.icon_user) : Uri.parse(uri));
        }
        mCallBack.sendUris(mUris);
    }
}
