package com.gameex.dw.justtalk.activity;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTouch;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.donkingliang.labels.LabelsView;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.tools.GifSizeFilter;
import com.gameex.dw.justtalk.tools.Glide4Engine;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.soundController.RecordingService;
import com.gameex.dw.justtalk.util.BarUtil;
import com.gameex.dw.justtalk.util.CallBackUtil;
import com.gameex.dw.justtalk.util.FileUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.OkHttpUtil;
import com.rey.material.app.Dialog;
import com.rey.material.widget.Button;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditSpaceInfoActivity extends BaseActivity {
    private static final String TAG = "EditSpaceInfoActivity";
    /**
     * ZhiHu`s image picker`s request code
     */
    private static final int REQUEST_CODE_CHOOSE = 23;
    /**
     * 上传图片接口
     */
    private static final String UPLOAD_IMG_PATH = "upload/uploadImg";
    /**
     * 上传大文件接口
     */
    private static final String BREAK_POINT_UPLOAD_PATH = "upload/breakpointUpload";
    /**
     * 录音文件存储路径
     */
    private static final String VOICE_EXTERNAL_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/FlyChat/recording/my_voice/";
    /**
     * 录音文件名
     */
    private static final String VOICE_EXTERNAL_NAME = "my_voice" + System.currentTimeMillis() + ".mp3";
    /**
     * 编辑基本信息code
     */
    private static final int REQUEST_EDIT_BASIC_CODE = 311;
    /**
     * 运动标签
     */
    private ArrayList<String> mSportLabels = new ArrayList<>();
    /**
     * 美食标签
     */
    private ArrayList<String> mFoodLabels = new ArrayList<>();
    /**
     * 音乐标签
     */
    private ArrayList<String> mMusicLabels = new ArrayList<>();
    /**
     * 电影标签
     */
    private ArrayList<String> mMoviesLabels = new ArrayList<>();

    /**
     * 用户名或昵称、性别和年龄、星座、职业或距离和最近在线时间
     */
    @BindViews({R.id.username, R.id.gender_age, R.id.constellation, R.id.career})
    TextView[] mTextViews;
    /**
     * 签名编辑框
     */
    @BindView(R.id.signature)
    EditText mSignature;

    /**
     * 签名编辑框的软键盘回车事件
     *
     * @param key keyEvent
     * @return true
     */
    @OnEditorAction(R.id.signature)
    boolean onEditorAction(KeyEvent key) {
        Toasty.normal(this, "点击回车，保存修改").show();
        return true;
    }

    /**
     * 语音录制按钮
     */
    @BindView(R.id.record)
    Button mRecord;

    /**
     * 图片集
     */
    @BindViews({R.id.first_picture, R.id.second_picture, R.id.third_picture
            , R.id.fourth_picture, R.id.fifth_picture, R.id.sixth_picture})
    ImageView[] mImageViews;

    /**
     * 录音按钮动作监听
     *
     * @param view  view
     * @param event motionEvent
     * @return boolean
     */
    @OnTouch(R.id.record)
    boolean onTouch(View view, MotionEvent event) {
        File file = new File(VOICE_EXTERNAL_PATH + VOICE_EXTERNAL_NAME);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                requestPermission();
                posY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                curY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                stopService(intentVoice);
                if (posY > curY) {
                    uploadBigFile(file);
                } else {
//                    if (file.exists()) file.delete();
                    Toasty.normal(EditSpaceInfoActivity.this, "取消").show();
                }
                break;
        }
        return true;
    }

    /**
     * 运动、美食、音乐、电影
     */
    @BindViews({R.id.sport, R.id.food, R.id.music, R.id.movies})
    LabelsView[] mLabelsViews;

    @OnClick({R.id.first_picture, R.id.second_picture, R.id.third_picture
            , R.id.fourth_picture, R.id.fifth_picture, R.id.sixth_picture
            , R.id.basic_info
            , R.id.sport_label, R.id.food_label, R.id.music_label, R.id.movie_label})
    void doClick(View view) {
        switch (view.getId()) {
            case R.id.basic_info:
                Intent intent = new Intent(this, EditBasicSpaceActivity.class);
                startActivityForResult(intent, REQUEST_EDIT_BASIC_CODE);
                break;
            case R.id.sport_label:
                showLabelsDialog("运动", mSportLabels, mLabelsViews[0]);
                break;
            case R.id.food_label:
                showLabelsDialog("美食", mFoodLabels, mLabelsViews[1]);
                break;
            case R.id.music_label:
                showLabelsDialog("音乐", mMusicLabels, mLabelsViews[2]);
                break;
            case R.id.movie_label:
                showLabelsDialog("电影", mMoviesLabels, mLabelsViews[3]);
                break;
            default:
                Matisse.from(this)
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
                mImgView = (ImageView) view;
                break;
        }
    }

    /**
     * 记录点击的imageView
     */
    private ImageView mImgView;
    private Intent intentVoice;
    private boolean isRecord = false;
    private float posY, curY;
    private UserInfo mUserInfo;
    private List<String> mSelectLabels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_space_info);
        initLabels();
        ButterKnife.bind(this);
        initData();
        BarUtil.setStatusBarColor(this, getResources().getColor(R.color.colorPrimary));
        mUserInfo = JMessageClient.getMyInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mUserInfo == null) JMessageClient.getMyInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mUserInfo == null) JMessageClient.getMyInfo();
    }

    /**
     * 初始化数据
     */
    @SuppressLint("SetTextI18n")
    private void initData() {
        intentVoice = new Intent(EditSpaceInfoActivity.this, RecordingService.class);
        intentVoice.putExtra("file_path", VOICE_EXTERNAL_PATH);
        intentVoice.putExtra("file_name", VOICE_EXTERNAL_NAME);
        UserInfo userInfo = JMessageClient.getMyInfo();
        if (userInfo == null) return;
        Map<String, String> extras = userInfo.getExtras();
        //用户名或昵称
        mTextViews[0].setText(userInfo.getNickname() == null ? userInfo.getUserName() : userInfo.getNickname());
        String gender;
        switch (userInfo.getGender()) {
            case male:
                gender = "男 ";
                break;
            case female:
                gender = "女 ";
                break;
            default:
                gender = "未知 ";
                break;
        }
        mTextViews[1].setText(gender + extras.get("age"));  //性别和年龄
        mTextViews[2].setText(extras.get("constellation")); //星座
        mTextViews[3].setText(extras.get("career"));    //职业或距离和最近在线时间
        mSignature.setText(userInfo.getSignature()); //个性签名
        //运动标签
        List<String> sports = JSON.parseArray(extras.get("sports"), String.class);
        mLabelsViews[0].setLabels(sports);
        //美食标签
        List<String> foods = JSON.parseArray(extras.get("foods"), String.class);
        mLabelsViews[1].setLabels(foods);
        //音乐标签
        List<String> musics = JSON.parseArray(extras.get("musics"), String.class);
        mLabelsViews[2].setLabels(musics);
        //电影标签
        List<String> movies = JSON.parseArray(extras.get("movies"), String.class);
        mLabelsViews[0].setLabels(movies);
    }

    /**
     * 初始化标签弹窗的标签
     */
    private void initLabels() {
        mSportLabels.add("单车");
        mSportLabels.add("乒乓球");
        mSportLabels.add("羽毛球");
        mSportLabels.add("网球");
        mSportLabels.add("台球");
        mSportLabels.add("射箭");
        mSportLabels.add("射击");
        mSportLabels.add("暴走");
        mSportLabels.add("游泳");
        mSportLabels.add("跑步");
        mSportLabels.add("瑜伽");
        mSportLabels.add("篮球");
        mSportLabels.add("足球");
        mSportLabels.add("滑板");
        mSportLabels.add("滑雪");
        mSportLabels.add("高尔夫");
        mSportLabels.add("舞蹈");
        mSportLabels.add("街舞");
        mSportLabels.add("健身房");
        mSportLabels.add("击剑");
        mSportLabels.add("拳击");
        mSportLabels.add("跆拳道");
        mSportLabels.add("爬山");
        mSportLabels.add("骑马");
        mSportLabels.add("郊游");
        mSportLabels.add("睡觉");

        mFoodLabels.add("烤串");
        mFoodLabels.add("生煎蛋");
        mFoodLabels.add("寿司");
        mFoodLabels.add("卤肉饭");
        mFoodLabels.add("生鱼片");
        mFoodLabels.add("日式拉面");
        mFoodLabels.add("牛排");
        mFoodLabels.add("意大利面");
        mFoodLabels.add("披萨");
        mFoodLabels.add("火锅");
        mFoodLabels.add("麻辣烫");
        mFoodLabels.add("麻辣香锅");
        mFoodLabels.add("汉堡");
        mFoodLabels.add("蛋包饭");
        mFoodLabels.add("薯条");
        mFoodLabels.add("美式炸鸡");
        mFoodLabels.add("素食");
        mFoodLabels.add("提拉米苏");
        mFoodLabels.add("巧克力");
        mFoodLabels.add("冰淇凌");
        mFoodLabels.add("奶酪");
        mFoodLabels.add("慕斯蛋糕");
        mFoodLabels.add("泰国菜");
        mFoodLabels.add("墨西哥Tacos");


        mMusicLabels.add("日韩");
        mMusicLabels.add("欧美");
        mMusicLabels.add("流行");
        mMusicLabels.add("嘻哈");
        mMusicLabels.add("轻音乐");
        mMusicLabels.add("周杰伦");
        mMusicLabels.add("陈奕迅");
        mMusicLabels.add("五月天");
        mMusicLabels.add("汪峰");
        mMusicLabels.add("Justin Bieber");
        mMusicLabels.add("摇滚");
        mMusicLabels.add("电子");
        mMusicLabels.add("R&B");
        mMusicLabels.add("爵士");
        mMusicLabels.add("布鲁斯");
        mMusicLabels.add("金属");
        mMusicLabels.add("古典");
        mMusicLabels.add("乡村");
        mMusicLabels.add("校园民谣");
        mMusicLabels.add("60年代经典");
        mMusicLabels.add("80年代经典");
        mMusicLabels.add("王菲");
        mMusicLabels.add("王力宏");
        mMusicLabels.add("萧敬腾");
        mMusicLabels.add("苏打绿");
        mMusicLabels.add("G.E.M.邓紫棋");
        mMusicLabels.add("刘若英");
        mMusicLabels.add("孙燕姿");
        mMusicLabels.add("范玮琪");
        mMusicLabels.add("萧亚轩");
        mMusicLabels.add("张惠妹");
        mMusicLabels.add("莫文蔚");
        mMusicLabels.add("曲婉婷");
        mMusicLabels.add("莫西子诗");
        mMusicLabels.add("杨宗纬");
        mMusicLabels.add("林宥嘉");
        mMusicLabels.add("宋冬野");
        mMusicLabels.add("张国荣");
        mMusicLabels.add("张学友");
        mMusicLabels.add("刘德华");
        mMusicLabels.add("李宗盛");
        mMusicLabels.add("罗大佑");
        mMusicLabels.add("谭咏麟");
        mMusicLabels.add("许巍");
        mMusicLabels.add("筷子兄弟");
        mMusicLabels.add("逃跑计划");
        mMusicLabels.add("maroon5");
        mMusicLabels.add("Justin Timberlake");
        mMusicLabels.add("Taylor Swift");
        mMusicLabels.add("Rihanna");
        mMusicLabels.add("Adele");
        mMusicLabels.add("Michael Jackson");
        mMusicLabels.add("Madonna");
        mMusicLabels.add("Linkin Park");
        mMusicLabels.add("Lady Gaga");
        mMusicLabels.add("Kety Perry");
        mMusicLabels.add("Bruno Mars");
        mMusicLabels.add("Westlife");
        mMusicLabels.add("Coldplay");
        mMusicLabels.add("One Direction");

        mMoviesLabels.add("肖申克的救赎");
        mMoviesLabels.add("这个杀手不太冷");
        mMoviesLabels.add("憨豆先生");
        mMoviesLabels.add("阿甘正传");
        mMoviesLabels.add("黑客帝国");
        mMoviesLabels.add("当幸福来敲门");
        mMoviesLabels.add("怦然心动");
        mMoviesLabels.add("老无所依");
        mMoviesLabels.add("教父");
        mMoviesLabels.add("海上钢琴师");
        mMoviesLabels.add("低俗小说");
        mMoviesLabels.add("猫鼠游戏");
        mMoviesLabels.add("守望者");
        mMoviesLabels.add("蝙蝠侠");
        mMoviesLabels.add("死侍 我爱我家");
        mMoviesLabels.add("金刚狼");
        mMoviesLabels.add("飞屋环游记");
        mMoviesLabels.add("爱宠大机密");
        mMoviesLabels.add("玩具总动员");
        mMoviesLabels.add("你的名字");
        mMoviesLabels.add("山坡上的虞美人");
        mMoviesLabels.add("返老还童");
        mMoviesLabels.add("时间旅行者的妻子");
        mMoviesLabels.add("黑衣人");
    }

    /**
     * 申请录音权限
     */
    @SuppressLint("CheckResult")
    private void requestPermission() {
        new RxPermissions(this)
                .request(Manifest.permission.RECORD_AUDIO)
                .subscribe(this::accept);
    }

    /**
     * 选择标签弹窗
     *
     * @param title  弹窗标题
     * @param labels 弹窗标签
     */
    private void showLabelsDialog(String title, ArrayList<String> labels, LabelsView labelView) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_choose_labels);
        LabelsView labelsView = dialog.findViewById(R.id.labels);
        labelsView.setLabels(labels);
        dialog.title(title).positiveAction("确定")
                .negativeAction("取消")
                .positiveActionClickListener(view -> {
                    mSelectLabels = labelsView.getSelectLabelDatas();
                    Toasty.normal(EditSpaceInfoActivity.this, mSelectLabels.toString()).show();
                    labelView.setLabels(mSelectLabels);
                })
                .negativeActionClickListener(view -> dialog.dismiss())
                .cancelable(true)
                .show();
    }

    /**
     * 上传图片
     *
     * @param file 图片文件
     * @param uri  路径
     */
    private void upLoadImg(File file, Uri uri) {
        HashMap<String, String> paramsMap = new HashMap<>();
        OkHttpUtil.okHttpUploadFile(UPLOAD_IMG_PATH, file, "file", OkHttpUtil.FILE_TYPE_IMAGE, paramsMap, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                e.printStackTrace();
                Toasty.error(EditSpaceInfoActivity.this, "网络连接异常").show();
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    boolean success = object.getBoolean("success");
                    if (success) {
                        Glide.with(EditSpaceInfoActivity.this)
                                .load(uri)
                                .into(mImgView);
                    }
                    Toasty.normal(EditSpaceInfoActivity.this, object.getString("data")).show();
                    LogUtil.d(TAG, "upLoadImg-onResponse: " + "response = " + response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 上传大文件（音频文件）
     *
     * @param file file
     */
    private void uploadBigFile(File file) {
        HashMap<String, String> paramsMap = new HashMap<>();
        OkHttpUtil.okHttpUploadFile(BREAK_POINT_UPLOAD_PATH, file, "file", OkHttpUtil.FILE_TYPE_AUDIO, paramsMap, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                e.printStackTrace();
                Toasty.error(EditSpaceInfoActivity.this, "网络连接失败").show();
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    boolean success = object.getBoolean("success");
                    if (success) {
                        Toasty.success(EditSpaceInfoActivity.this, "上传成功").show();
                    }
                    Toasty.normal(EditSpaceInfoActivity.this, object.getString("data")).show();
                    LogUtil.d(TAG, "uploadBigFile-onResponse: " + "response = " + response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            assert data != null;
            List<Uri> path = Matisse.obtainResult(data);
            upLoadImg(FileUtil.getFileByUri(EditSpaceInfoActivity.this
                    , path.get(0)), path.get(0));
//            new Handler().post(() -> FileUtil.uploadFile(FileUtil.getFileByUri(EditSpaceInfoActivity.this
//                    , path.get(0)), REALM_NAME + UPLOAD_IMG_PATH));
        } else if (requestCode == REQUEST_EDIT_BASIC_CODE && resultCode == RESULT_OK) {
            //TODO: 更新基本信息
        }
    }

    private void accept(Boolean granted) {
        if (granted) {
            if (isRecord) startService(intentVoice);
            if (!isRecord) isRecord = true;
        } else {
            Toasty.normal(EditSpaceInfoActivity.this, "授权失败").show();
        }
    }
}
