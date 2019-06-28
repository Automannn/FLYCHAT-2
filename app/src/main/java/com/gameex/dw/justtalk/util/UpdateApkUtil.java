package com.gameex.dw.justtalk.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.TextView;

import com.allenliu.versionchecklib.callback.APKDownloadListener;
import com.allenliu.versionchecklib.v2.AllenVersionChecker;
import com.allenliu.versionchecklib.v2.builder.DownloadBuilder;
import com.allenliu.versionchecklib.v2.builder.NotificationBuilder;
import com.allenliu.versionchecklib.v2.builder.UIData;
import com.allenliu.versionchecklib.v2.callback.CustomDownloadingDialogListener;
import com.allenliu.versionchecklib.v2.callback.RequestVersionListener;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.manage.ActivityCollector;

import java.io.File;

import androidx.annotation.Nullable;
import es.dmoral.toasty.Toasty;

/**
 * app更新工具类
 */
public class UpdateApkUtil {
    private static final String TAG = "UpdateApkUtil";

    private Context mContext;
    private DownloadBuilder mBuilder;

    public UpdateApkUtil(Context context) {
        mContext = context;
    }

    /**
     * 版本检测请求,成功后判断版本号,若有新版本，则初始化弹窗和通知栏并展示
     */
    public void sendRequest() {
        mBuilder = AllenVersionChecker
                .getInstance()
                .requestVersion()
                .setRequestUrl("https://www.baidu.com")
                .request(new RequestVersionListener() {
                    @Nullable
                    @Override
                    public UIData onRequestVersionSuccess(String result) {
                        LogUtil.d(TAG, "sendRequest-onRequestVersionSuccess: "
                                + "result = " + result + " ;versionName = " + getVersionName()
                                + " ;versionCode = " + getVersionCode());
                        return createUIData("检测到新版本v1.0.1"
                                , "http://test-1251233192.coscd.myqcloud.com/1_1.apk"
                                , "1、修复已知Bug\n2、优化用户体验\n3、增加在线更新功能\n4、增加录音动画");
                    }

                    @Override
                    public void onRequestVersionFailure(String message) {
                        LogUtil.d(TAG, "sendRequest-onRequestVersionFailure: "
                                + "message = " + message);
                    }
                });
        setForceUpdate();
        createCusNotify(false, R.drawable.logo, "飞聊更新"
                , "飞聊v1.0.1", "正在更新...");
        setApkPath("/storage/emulated/0/FlyChat/apk/");
        setFileName("飞聊v1.0.1");
        setDownloadListener();
        setOnCancel();
        createCusVersionDialog();
        //mBuilder.setNewestVersionCode(1);
        createCusDownloadingDialog();
        mBuilder.executeMission(mContext);
    }

    /**
     * 构造UI需要显示的数据
     *
     * @param title   标题
     * @param url     下载链接
     * @param content 提示文本
     * @return UIData
     */
    private UIData createUIData(String title, String url, String content) {
        UIData uiData = UIData.create();
        uiData.setTitle(title);
        uiData.setDownloadUrl(url);
        uiData.setContent(content);
        return uiData;
    }

    /**
     * 取消下载
     */
    public void cancelUpdate() {
        AllenVersionChecker.getInstance().cancelAllMission(mContext);
    }

    /**
     * 设置静默下载
     *
     * @param isSilent 静默否
     */
    public void setSilent(boolean isSilent) {
        mBuilder.setSilentDownload(isSilent);   //默认false
    }

    /**
     * 设置强制更新
     */
    public void setForceUpdate() {
        mBuilder.setForceUpdateListener(ActivityCollector::finishAll);
    }

    /**
     * 设置下载忽略本地缓存
     *
     * @param isForce 忽略否
     */
    public void setForceRe(boolean isForce) {
        mBuilder.setForceRedownload(isForce);   //默认false
    }

    /**
     * 设置是否显示下载对话框
     *
     * @param isShowDownloading 显示否
     */
    public void setShowDownLoading(boolean isShowDownloading) {
        mBuilder.setShowDownloadingDialog(isShowDownloading);   //默认true
    }

    /**
     * 设置是否显示下载失败对话框
     *
     * @param isShowFail 显示否
     */
    public void setShowFailDialog(boolean isShowFail) {
        mBuilder.setShowDownloadFailDialog(isShowFail); //默认true
    }

    /**
     * 设置是否显示通知栏
     *
     * @param isNotify 显示否
     */
    public void setShowNotify(boolean isNotify) {
        mBuilder.setShowNotification(isNotify); //默认true
    }

    /**
     * 自定义通知栏
     *
     * @param isRing      是否响铃/震动
     * @param icon        显示图标
     * @param ticker      ticker
     * @param title       标题
     * @param contentText 提示文本
     */
    public void createCusNotify(boolean isRing, int icon, String ticker
            , String title, String contentText) {
        mBuilder.setNotificationBuilder(
                NotificationBuilder.create()
                        .setRingtone(isRing)
                        .setIcon(icon)
                        .setTicker(ticker)
                        .setContentTitle(title)
                        .setContentText(contentText));
    }

    /**
     * 设置安装包下载路径
     *
     * @param apkPath 路径
     */
    public void setApkPath(String apkPath) {
        mBuilder.setDownloadAPKPath(apkPath);//默认/storage/emulated/0/AllenVersionPath/
    }

    /**
     * 设置下载文件名
     *
     * @param apkName 文件名
     */
    public void setFileName(String apkName) {
        mBuilder.setApkName(apkName);   //默认getPackageName
    }

    /**
     * 设置下载监听
     */
    public void setDownloadListener() {
        mBuilder.setApkDownloadListener(new APKDownloadListener() {
            @Override
            public void onDownloading(int progress) {
                LogUtil.d(TAG, "setDownloadListener-onDownloading: "
                        + "progress = " + progress);
            }

            @Override
            public void onDownloadSuccess(File file) {
                Toasty.success(mContext, "下载成功", Toasty.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadFail() {
                Toasty.error(mContext, "下载失败", Toasty.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 设置取消监听
     */
    public void setOnCancel() {
        mBuilder.setOnCancelListener(() -> Toasty.warning(mContext, "已取消下载更新", Toasty.LENGTH_SHORT).show());
    }

    /**
     * 自定义更新弹窗
     */
    private void createCusVersionDialog() {
        mBuilder.setCustomVersionDialogListener((context, versionBundle) -> {
            BaseDialog baseDialog = new BaseDialog(context, R.style.CusVersionDialog
                    , R.layout.dialog_custom_version);
            baseDialog.setCancelable(false);
//            baseDialog.setCanceledOnTouchOutside(false);
            //versionBundle 就是UIData，之前开发者传入的，在这里可以拿出UI数据并展示
            TextView title = baseDialog.findViewById(R.id.title);
            title.setText(versionBundle.getTitle());
            TextView msg = baseDialog.findViewById(R.id.msg);
            msg.setText(versionBundle.getContent());
//            Button commit=baseDialog.findViewById(R.id.versionchecklib_version_dialog_commit);
//            commit.setOnClickListener(view -> {
//
//            });
            return baseDialog;
        });
    }

    /**
     * 自定义下载进度弹窗界面
     */
    private void createCusDownloadingDialog() {
        mBuilder.setCustomDownloadingDialogListener(new CustomDownloadingDialogListener() {
            private void onCancel(DialogInterface dialogInterface) {
                AllenVersionChecker.getInstance().cancelAllMission(mContext);
            }

            @Override
            public Dialog getCustomDownloadingDialog(Context context, int progress, UIData versionBundle) {
                BaseDialog baseDialog = new BaseDialog(context, R.style.CusVersionDialog
                        , R.layout.dialog_custom_download);
                baseDialog.setOnDismissListener(dialogInterface -> AllenVersionChecker.getInstance().cancelAllMission(mContext));
                return baseDialog;
            }

            //下载中会不断回调updateUI方法
            @Override
            public void updateUI(Dialog dialog, int progress, UIData versionBundle) {
                TextView tvProgress = dialog.findViewById(R.id.tv_progress);
                NumberProgressBar progressBar = dialog.findViewById(R.id.pb);
                progressBar.setProgress(progress);
                tvProgress.setText(mContext.getString(R.string.versionchecklib_progress, progress));
            }
        });
    }

    /**
     * 获取当前应用版本号
     *
     * @return string
     */
    private String getVersionCode() {
        // 获取packagemanager的实例
        PackageManager packageManager = mContext.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packInfo == null ? null : String.valueOf(packInfo.versionCode);
    }

    /**
     * 获取当前应用版本名
     *
     * @return string
     */
    private String getVersionName() {
        // 获取packagemanager的实例
        PackageManager packageManager = mContext.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packInfo == null ? null : packInfo.versionName;
    }
}
