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
import cn.jpush.im.api.BasicCallback;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
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
     * 录制自述音频action
     */
    public static final String RECORD_MY_VOICE_ACTION =
            "com.gameex.dw.justtalk.activity." + TAG + ".RECORD_MY_VOICE_ACTION";
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

    @BindView(R.id.scroll)
    ScrollView mScroll;

    /**
     * 用户名或昵称、性别和年龄、星座、职业或距离和最近在线时间
     */
    @BindViews({R.id.username, R.id.gender_age, R.id.constellation, R.id.career
            , R.id.sport_notice, R.id.food_notice, R.id.music_notice, R.id.movies_notice})
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
    boolean onEditorAction(TextView text, KeyEvent key) {
        mUserInfo.setSignature(text.getText().toString());
        return true;
    }

    /**
     * 语音录制按钮
     */
    @BindView(R.id.record)
    TextView mRecord;

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
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startService(intentVoice);
                posX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                curX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                stopService(intentVoice);
                if (posX < curX) {
                    Toasty.normal(this, "保存").show();
                } else {
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
                showLabelsDialog("运动", mSportLabels, 0);
                break;
            case R.id.food_label:
                showLabelsDialog("美食", mFoodLabels, 1);
                break;
            case R.id.music_label:
                showLabelsDialog("音乐", mMusicLabels, 2);
                break;
            case R.id.movie_label:
                showLabelsDialog("电影", mMoviesLabels, 3);
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
    /**
     * 录音服务intent
     */
    private Intent intentVoice;
    /**
     * 记录按下录音按钮时的x坐标、手指滑动时的x坐标
     */
    private float posX, curX;
    /**
     * 极光用户信息体
     */
    private UserInfo mUserInfo;
    /**
     * 存储已选择的标签
     */
    private List<String> mSelectLabels = new ArrayList<>();
    /**
     * 存储上传图片成功后返回的链接地址
     */
    private List<String> mImgUrl = new ArrayList<>();

    private EditSpaceReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_space_info);
        initLabels();
        ButterKnife.bind(this); //黄油刀，GitHub热门框架，快速绑定资源id，减少代码重复，提高效率
        initData();
        BarUtil.setStatusBarColor(this, getResources().getColor(R.color.colorPrimary));
        mUserInfo = JMessageClient.getMyInfo();
        mReceiver = new EditSpaceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(RECORD_MY_VOICE_ACTION);
        registerReceiver(mReceiver, filter);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    /**
     * 初始化数据
     */
    @SuppressLint("SetTextI18n")
    private void initData() {
        intentVoice = new Intent(EditSpaceInfoActivity.this, RecordingService.class);
        intentVoice.putExtra("isSelfVoice", true);
        UserInfo userInfo = JMessageClient.getMyInfo();
        if (userInfo == null) return;
        Map<String, String> extras = userInfo.getExtras();
        initBasicInfo(userInfo, extras);
        mSignature.setText(userInfo.getSignature()); //个性签名
        //运动标签
        List<String> sports = JSON.parseArray(extras.get("sports"), String.class);
        mLabelsViews[0].setLabels(sports);
        mLabelsViews[0].setTag("sports");
        if (sports != null && sports.size() > 0) mTextViews[4].setVisibility(View.GONE);
        //美食标签
        List<String> foods = JSON.parseArray(extras.get("foods"), String.class);
        mLabelsViews[1].setLabels(foods);
        mLabelsViews[1].setTag("foods");
        if (sports != null && sports.size() > 0) mTextViews[5].setVisibility(View.GONE);
        //音乐标签
        List<String> musics = JSON.parseArray(extras.get("musics"), String.class);
        mLabelsViews[2].setLabels(musics);
        mLabelsViews[2].setTag("musics");
        if (sports != null && sports.size() > 0) mTextViews[6].setVisibility(View.GONE);
        //电影标签
        List<String> movies = JSON.parseArray(extras.get("movies"), String.class);
        mLabelsViews[3].setLabels(movies);
        mLabelsViews[3].setTag("movies");
        if (sports != null && sports.size() > 0) mTextViews[7].setVisibility(View.GONE);
    }

    @SuppressLint("SetTextI18n")
    private void initBasicInfo(UserInfo userInfo, Map<String, String> extras) {
        //用户名或昵称
        mTextViews[0].setText(TextUtils.isEmpty(userInfo.getNickname()) ? userInfo.getUserName() : userInfo.getNickname());
        String gender;
        switch (userInfo.getGender()) {
            case male:
                gender = "男 ";
                break;
            case female:
                gender = "女 ";
                break;
            default:
                gender = "保密 ";
                break;
        }
        mTextViews[1].setText(gender + extras.get("age"));  //性别和年龄
        mTextViews[2].setText(extras.get("constellation")); //星座
        mTextViews[3].setText(extras.get("career"));    //职业或距离和最近在线时间
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
     * 选择标签弹窗
     *
     * @param title  弹窗标题
     * @param labels 弹窗标签
     */
    private void showLabelsDialog(String title, ArrayList<String> labels, int index) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_choose_labels);
        LabelsView labelsView = dialog.findViewById(R.id.labels);
        labelsView.setLabels(labels);
        dialog.title(title).positiveAction("确定")
                .negativeAction("取消")
                .positiveActionClickListener(view -> {
                    mSelectLabels = labelsView.getSelectLabelDatas();
                    mLabelsViews[index].setLabels(mSelectLabels);
                    if (mSelectLabels.size() > 0) mTextViews[index + 4].setVisibility(View.GONE);
                    else mTextViews[index + 4].setVisibility(View.VISIBLE);
                    mUserInfo.setUserExtras((String) mLabelsViews[index].getTag(), JSON.toJSONString(mSelectLabels));
                    dialog.dismiss();
                })
                .negativeActionClickListener(view -> dialog.dismiss())
                .cancelable(true)
                .show();
    }

    /**
     * 上传图片
     *
     * @param file   图片文件
     * @param bitmap 裁剪后的图片
     */
    private void upLoadImg(File file, Bitmap bitmap) {
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
                    String data = object.getString("data");
                    if (success) {
                        Glide.with(EditSpaceInfoActivity.this)
                                .load(bitmap)
                                .into(mImgView);
                        mImgUrl.add(data);
                    } else
                        Toasty.normal(EditSpaceInfoActivity.this, data).show();
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
                    String data = object.getString("data");
                    if (success) {
                        Toasty.success(EditSpaceInfoActivity.this, "上传成功").show();
                        mUserInfo.setUserExtras("voice", data);
                    }
                    Toasty.normal(EditSpaceInfoActivity.this, data).show();
                    LogUtil.d(TAG, "uploadBigFile-onResponse: " + "response = " + response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 调用系统图片裁剪
     *
     * @param uri uri
     */
    private void photoClip(Uri uri) {
        // 调用系统中自带的图片剪裁
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 240);
        intent.putExtra("outputY", 250);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode
            , @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            assert data != null;
            List<Uri> path = Matisse.obtainResult(data);
            photoClip(path.get(0));
        } else if (requestCode == REQUEST_EDIT_BASIC_CODE && resultCode == RESULT_OK) {
            UserInfo userInfo = JMessageClient.getMyInfo();
            initBasicInfo(userInfo, userInfo.getExtras());
        } else if (requestCode == 3 && resultCode == RESULT_OK) {
            assert data != null;
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                //在这里获得了剪裁后的Bitmap对象，可以用于上传
                Bitmap image = bundle.getParcelable("data");
                assert image != null;
                upLoadImg(FileUtil.saveBitmapFile(image), image);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mImgUrl.size() > 0) {
            mUserInfo.setUserExtras("images", JSON.toJSONString(mImgUrl));
        }
        JMessageClient.updateMyInfo(UserInfo.Field.all, mUserInfo, new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                if (i == 0) {
                    Toasty.normal(EditSpaceInfoActivity.this, "更新成功").show();
                    finish();
                } else {
                    Toasty.normal(EditSpaceInfoActivity.this, "desc = " + s).show();
                }
            }
        });
    }

    /**
     * 广播接收器，接收录音文件
     */
    class EditSpaceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            if (action.equals(RECORD_MY_VOICE_ACTION)) {
                String voicePath = intent.getStringExtra("audio_path");
                if (!TextUtils.isEmpty(voicePath)) uploadBigFile(new File(voicePath));
            }
        }
    }
}
