package com.gameex.dw.justtalk.soundController;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.util.BaseDialog;
import com.gameex.dw.justtalk.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import androidx.core.app.NotificationCompat;
import cn.jpush.im.android.api.JMessageClient;
import es.dmoral.toasty.Toasty;
import jaygoo.widget.wlv.WaveLineView;

import static com.gameex.dw.justtalk.activity.ChattingActivity.RECORD_COMPLETE;
import static com.tencent.wxop.stat.common.StatConstants.LOG_TAG;

/**
 * 录音服务
 */
public class RecordingService extends Service {
    private static final String TAG = "RecordingService";
    /**
     * 上下文参数
     */
    private Context mContext;

    /**
     * 录音文件名
     */
    private String mFileName;
    /**
     * 录音文件保存路径
     */
    private String mFilePath;

    private MediaRecorder mRecorder;
    /**
     * 开始录音的毫秒数
     */
    private long mStartingTimeMillis;
    /**
     * 时间（毫秒）
     */
    private long mElapsedMillis;
    /**
     * 时间（秒）
     */
    private int mElapsedSeconds = 0;
    /**
     * 计时器变化监听
     */
    private OnTimerChangedListener onTimerChangedListener = null;
    /**
     * 计时器
     */
    private Timer mTimer = null;
    /**
     * 计时线程
     */
    private TimerTask mIncrementTimerTask = null;
    /**
     * 时间格式
     */
    private static final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
    /**
     * 录音动画弹窗
     */
    private BaseDialog recordDialog;
    /**
     * 录音波浪线
     */
    private WaveLineView mWaveLine;
    /**
     * 时间
     */
    private TextView mRecordTime;

    private boolean isRecording = true;

    private Timer mWaveTimer;
    private TimerTask mWaveTask;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mElapsedSeconds > 29) {
                Toasty.warning(mContext, "时间太长").show();
                stopSelf();
            } else {
                mRecordTime.setText(mTimerFormat.format(mElapsedSeconds * 1000));
            }
        }
    };


    private static final int SAMPLE_RATE_IN_HZ = 8000;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);

    public interface OnTimerChangedListener {
        void onTimerChanged(int seconds);
    }

    public RecordingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = BaseActivity.sBaseActivity;
        startRecording();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mRecorder != null) {
            stopRecording();
        }
        super.onDestroy();
    }

    // 开始录音
    public void startRecording() {
        setFileNameAndPath();
//        new Thread(() -> {
//            short[] buffer = new short[BUFFER_SIZE];
//            while (isGetVoiceRun) {
////r是实际读取的数据长度，一般而言r会小于buffersize
//                int r = mRecorder.read(buffer, 0, BUFFER_SIZE);
//                long v = 0;
//// 将 buffer 内容取出，进行平方和运算
//                for (int i = 0; i < buffer.length; i++) {
//                    v += buffer[i] * buffer[i];
//                }
//                // 平方和除以数据总长度，得到音量大小。
//                double mean = v / (double) r;
//                double volume = 10 * Math.log10(mean);
//                LogUtil.d(TAG, "分贝值:" + volume);
//                // 大概一秒十次
//                synchronized (mLock) {
//                    try {
//                        mLock.wait(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); //录音文件保存的格式，这里保存为 mp4
        mRecorder.setOutputFile(mFilePath); // 设置录音文件的保存路径
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);
        // 设置录音文件的清晰度
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setAudioEncodingBitRate(192000);
        startTimer();
        showRecordDialog();
        //startForeground(1, createNotification());

        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.e(LOG_TAG, "prepare() failed");
        }
    }

    // 设置录音文件的名字和保存路径
    public void setFileNameAndPath() {
        int count = 0;
        File f;

        do {
            count++;
            mFileName = JMessageClient.getMyInfo().getUserName()
                    + "_" + System.currentTimeMillis() + "_" + count + ".mp3";
            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/FlyChat/recording/";
            File directory = new File(mFilePath);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            mFilePath += mFileName;
            f = new File(mFilePath);
        } while (f.exists() && !f.isDirectory());
    }

    // 停止录音
    public void stopRecording() {
        isRecording = false;
        recordDialog.dismiss();
        mRecorder.stop();
        mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);    //录音时长（毫秒）
        mRecorder.release();    //释放空间

//        getSharedPreferences("sp_name_audio", MODE_PRIVATE)
//                .edit()
//                .putString("audio_path", mFilePath)
//                .putLong("elpased", mElapsedMillis)
//                .apply();
        if ((int) (mElapsedMillis / 1000) > 2) {
            Intent intent = new Intent(RECORD_COMPLETE);
            intent.putExtra("audio_path", mFilePath);
            intent.putExtra("elpased", mElapsedMillis);
            mContext.sendBroadcast(intent);
        } else {
            Toasty.error(mContext, "时间太短").show();
            new File(mFilePath).delete();
        }
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }

        mRecorder = null;
    }

    /**
     * 计时
     */
    private void startTimer() {
        mTimer = new Timer();
        mIncrementTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (isRecording) {
                    mElapsedSeconds++;
                    mHandler.sendEmptyMessage(0);
//                if (onTimerChangedListener != null)
//                    onTimerChangedListener.onTimerChanged(mElapsedSeconds);
//                NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                mgr.notify(1, createNotification());
                }
            }
        };
        mTimer.scheduleAtFixedRate(mIncrementTimerTask, 1000, 1000);
    }

    /**
     * 录音动画弹窗
     */
    private void showRecordDialog() {
        recordDialog = new BaseDialog(mContext, R.style.CusVersionDialog
                , R.layout.dialog_record_anime);
        mWaveLine = recordDialog.findViewById(R.id.wave_line);//波形组件
        mWaveLine.startAnim();
        mRecordTime = recordDialog.findViewById(R.id.counter);  //显示录音时间
        recordDialog.setOnDismissListener(dialogInterface -> {
            mWaveLine.stopAnim();
            if (mWaveTask != null) {
                mWaveTask.cancel();
                mWaveTask = null;
            }
        });
        recordDialog.show();
        mWaveTimer = new Timer();
        mWaveTask = new TimerTask() {
            @Override
            public void run() {
                if (mRecorder == null) return;
                int maxAmplitude;
                try {
                    maxAmplitude = mRecorder.getMaxAmplitude();
                } catch (RuntimeException re) {
                    maxAmplitude = 0;
                    re.printStackTrace();
                }
                double ratio = (double) maxAmplitude / 100;
                double db = 0;// 分贝
                //默认的最大音量是100,可以修改，但其实默认的，在测试过程中就有不错的表现
                //你可以传自定义的数字进去，但需要在一定的范围内，比如0-200，就需要在xml文件中配置maxVolume
                //同时，也可以配置灵敏度sensibility
                if (ratio > 1)
                    db = 20 * Math.log10(ratio);
                //只要有一个线程，不断调用这个方法，就可以使波形变化
                //主要，这个方法必须在ui线程中调用
                mWaveLine.setVolume((int) (db * 1.2));  //每100毫秒获取一次分贝，并改变波形
            }
        };
        mWaveTimer.scheduleAtFixedRate(mWaveTask, 100, 100);
    }

    //TODO:
    private Notification createNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("录音中")
                        .setContentText(mTimerFormat.format(mElapsedSeconds * 1000))
                        .setOngoing(true);

        mBuilder.setContentIntent(PendingIntent.getActivities(getApplicationContext(), 0,
                new Intent[]{new Intent(getApplicationContext(), mContext.getClass())}, 0));

        return mBuilder.build();
    }
}
