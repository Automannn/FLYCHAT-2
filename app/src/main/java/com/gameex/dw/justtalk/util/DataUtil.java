package com.gameex.dw.justtalk.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.TypedValue;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.gameex.dw.justtalk.R;
import com.github.promeg.pinyinhelper.Pinyin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 数据处理工具类
 */
public class DataUtil {
    private static final String ANDROID_RESOURCE = "android.resource://";
    private static final String FOREWARD_SLASH = "/";
    /**
     * 验证手机号码是否合法的正则表达式
     * 13[0-9],14[5/7],15[0-3]/[5-9],17[6-8],18[0-9]
     * "13"代表前两位数字
     * "[0-9]"代表第二位可以为0-9中的一个
     * "[^4]"代表除了4
     * "\\d{8}"代表后面可以是0-9的数字，有8位
     */
    public static final String TELEPHONE_REGEX
            = "((13[0-9])|(15[^4])|(18[0-9])|(17[0-8])|(147,145))\\d{8}$";
    /**
     * (?!^\d+$)不能全是数字
     * (?!^[a-zA-Z]+$)不能全是字母
     * .{8,}长度不能小于8
     */
    private static final String PASSWORD_REGEX = "(?!^\\d+$)(?!^[a-zA-Z]+$).{8,}";
    /**
     * [a-zA-Z]字母
     */
    private static final String WORD_REGEX = "[a-zA-z]+";
    /**
     * 汉字正则表达式
     */
    private static final String CHINESE_REGEX = "[\\u4E00-\\u9FA5]+";
    /**
     * requestCode:拍照
     */
    public static final int TAKE_PHOTO = 1;
    /**
     * requestCode:从相册选择
     */
    public static final int CHOOSE_PHOTO = 2;
    /**
     * requestCode:裁剪
     */
    public static final int CROP_PHOTO = 3;

    /**
     * 将资源id转换成Uri
     *
     * @param packageName app包名
     * @param resourceId  头像资源id
     * @return 返回uri
     */
    public static Uri resourceIdToUri(String packageName, int resourceId) {
        return Uri.parse(ANDROID_RESOURCE + "" + packageName + "" + FOREWARD_SLASH + "" + resourceId);
    }

    /**
     * dp转px
     *
     * @param context 上下文参数
     * @param dp      想要转换的dp值
     * @return 返回像素px
     */
    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, context.getResources().getDisplayMetrics());
    }

    /**
     * 正则表达式判断是否是手机号
     *
     * @param mobile 想要判断的字符串
     * @return 返回boolean
     */
    public static boolean isMobileNumber(String mobile) {
        return !TextUtils.isEmpty(mobile) && mobile.matches(TELEPHONE_REGEX);
    }

    /**
     * 正则表达式判断密码是否符合规范
     *
     * @param pwd 想要判断的字符串
     * @return 返回boolean
     */
    public static boolean isPWDCorrect(String pwd) {
        return !TextUtils.isEmpty(pwd) && pwd.matches(PASSWORD_REGEX);
    }

    /**
     * 获取系统的日期
     *
     * @return 返回日期字符串
     */
    public static String getCurrentDateStr() {
        Calendar calendar = Calendar.getInstance();   //获取系统的日期
        int month = calendar.get(Calendar.MONTH) + 1;   //月
        int day = calendar.get(Calendar.DAY_OF_MONTH);    //日
        return month + "月" + day + "日";
    }

    /**
     * 获取系统的时间
     *
     * @return 返回时间字符串
     */
    public static String getCurrentTimeStr() {
        Calendar calendar = Calendar.getInstance();   //获取系统的时间
        int hour = calendar.get(Calendar.HOUR_OF_DAY);   //小时
        int minute = calendar.get(Calendar.MINUTE);    //分钟
        return hour + ":" + minute;
    }

    /**
     * 将毫秒数转化为-月-日格式的字符串
     *
     * @param milliSecond 毫秒数
     * @return 字符串date
     */
    public static String msFormYYYY(long milliSecond) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("YYYY");
        return format.format(milliSecond);
    }

    /**
     * 将毫秒数转化为-月-日格式的字符串
     *
     * @param milliSecond 毫秒数
     * @return 字符串date
     */
    public static String msFormMMDD(long milliSecond) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("MM-dd");
        return format.format(milliSecond).replaceAll("-", "月") + "日";
    }

    /**
     * 将毫秒数转化位时:分格式的字符串
     *
     * @param milliSecond 毫秒数
     * @return 字符串time
     */
    public static String msFormHHmmTime(long milliSecond) {
        @SuppressLint("SimpleDateFormat") DateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(milliSecond);
    }

    /**
     * 将毫秒数转化位时:分格式的字符串
     *
     * @param milliSecond 毫秒数
     * @return 字符串time
     */
    public static String msFormmmssTime(long milliSecond) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("mm:ss");
        return format.format(milliSecond);
    }

    /**
     * 判断消息的日期是否比上一条消息晚一天
     *
     * @param date     上一条消息的日期的字符串
     * @param thanDate 待比较的日期字符串
     * @return 返回boolean
     */
    public static boolean isMoreThanOneDay(String date, String thanDate) {
//        if (Integer.parseInt(date.substring()))
        int day = Integer.parseInt(date.substring(3, 5));
        int thanDay = Integer.parseInt(thanDate.substring(3, 5));
        return thanDay - day >= 1;
    }

    /**
     * 字符串转字母容器
     */
    private static StringBuffer sBuffer = new StringBuffer();

    /**
     * 获取汉字字符串的第一个字母，并大写
     *
     * @param str 汉字字符串
     * @return 返回此str的第一个首字母
     */
    public static String getPinYinFirstLetter(String str) {
        char c = str.charAt(0);
        //如果c为汉字，则返回大写拼音；如果c不是汉字，则返回String.valueOf(c)
        String pinYin = Pinyin.toPinyin(c);
        if (pinYin.matches(WORD_REGEX)) { //判断是否为字母
            return pinYin.substring(0, 1).toUpperCase();  //是则转换大写后返回
        } else {
            return "#";    //否则返回“#”
        }
    }

    /**
     * 创建File对象，用于存贮拍照后的图片
     */
    public static Uri getPhotoUri(Context context, File file, String fileName) {
        Uri photoUri;
        if (file == null) {
            File outputPhoto = new File(context.getExternalCacheDir()
                    , fileName);
            try {
                if (outputPhoto.exists()) {
                    outputPhoto.delete();
                }
                outputPhoto.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (Build.VERSION.SDK_INT >= 24) {
                photoUri = FileProvider.getUriForFile(context, context.getResources()
                        .getString(R.string.author_name), outputPhoto);
            } else {
                photoUri = Uri.fromFile(outputPhoto);
            }
        } else {
            if (Build.VERSION.SDK_INT >= 24) {
                photoUri = FileProvider.getUriForFile(context,
                        context.getResources().getString(R.string.author_name), file);
            } else {
                photoUri = Uri.fromFile(file);
            }
        }
        return photoUri;
    }

    /**
     * 调用系统的裁剪功能
     *
     * @param uri 需裁剪的uri
     */
    public static void cropPhoto(Activity activity, Fragment fragment, Uri uri) {
//        Uri outputUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory()
//                .getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg"));
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setDataAndType(uri, "image/*");
        //是否可裁剪
        intent.putExtra("crop", "true");
//        intent.putExtra("scale", "true");
        // aspectX aspectY 是宽高比列
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);
        intent.putExtra("return-data", true);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
//        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        if (fragment != null) {
            fragment.startActivityForResult(intent, CROP_PHOTO);
        } else {
            activity.startActivityForResult(intent, CROP_PHOTO);
        }
    }

    /**
     * 将图片保存在SD卡中
     *
     * @param bitmap 缓存头像
     */
    public static File setPicToView(Context context, Bitmap bitmap, String fileName) {
        FileOutputStream b = null;
        File file = new File(context.getExternalCacheDir(), fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            b = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭流
                assert b != null;
                b.flush();
                b.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 将byte数组转为bitmap对象
     *
     * @param bytes   byte数组
     * @param options BitmapFactory.Options
     * @return bitmap
     */
    public static Bitmap getPicFromBytes(byte[] bytes, BitmapFactory.Options options) {
        if (bytes != null)
            if (options != null)
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            else
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return null;
    }

    /**
     * 将bitmap转化为file
     *
     * @param bitmap 位图
     * @return file
     */
    public File bitmapToFile(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        File file = new File(Environment.getExternalStorageDirectory() + "/temp.jpg");
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            int x = 0;
            byte[] b = new byte[1024 * 100];
            while ((x = is.read(b)) != -1) {
                fos.write(b, 0, x);
            }
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 是否为浮点数？integer、double或float类型。
     *
     * @param str 传入的字符串。
     * @return 是浮点数返回true, 否则返回false。
     */
    public static boolean isLegleYuan(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
        return pattern.matcher(str).matches();
    }

    /**
     * 获取十六进制的颜色代码.例如  "#5A6677"
     * 分别取R、G、B的随机值，然后加起来即可
     *
     * @return int
     */
    public static int getRandColor(String key) {
//        String R, G, B;
//        Random random = new Random();
//        R = Integer.toHexString(random.nextInt(256)).toUpperCase();
//        G = Integer.toHexString(random.nextInt(256)).toUpperCase();
//        B = Integer.toHexString(random.nextInt(256)).toUpperCase();
//
//        R = R.length() == 1 ? "0" + R : R;
//        G = G.length() == 1 ? "0" + G : G;
//        B = B.length() == 1 ? "0" + B : B;

        ColorGenerator generator = ColorGenerator.MATERIAL;
        return generator.getColor(key);
    }
}