package com.gameex.dw.justtalk.soundController;

import android.media.MediaPlayer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoiceSpeaker {
    private static final String TAG = "VoiceSpeaker";

    private static VoiceSpeaker sInstance;

    private ExecutorService service;

    private VoiceSpeaker() {
        service = Executors.newCachedThreadPool();
    }

    /**
     * @return VoiceSpeaker
     */
    public static synchronized VoiceSpeaker getInstance() {

        if (sInstance == null) {
            sInstance = new VoiceSpeaker();
        }
        return sInstance;
    }

    /**
     * @param path 音频路径
     */
    public void speakSingle(final String path) {
        if (service != null) {
            service.execute(() -> startSingle(path));
        }
    }

    /**
     * 播放单音频
     *
     * @param path 音频路径
     */
    private void startSingle(final String path) {
        synchronized (this) {
            MediaPlayer player = new MediaPlayer();
            if (path == null) return;
            try {
                player.setDataSource(path);
                player.prepareAsync();
                player.setOnPreparedListener(MediaPlayer::start);
                player.setOnCompletionListener(MediaPlayer::release);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param list 音频集合
     */
    public void speak(final List<String> list) {
        if (service != null) {
            service.execute(() -> start(list));
        }
    }

    /**
     * 遍历播放集合音频
     *
     * @param list 音频集合
     */
    private void start(final List<String> list) {
        synchronized (this) {
            MediaPlayer player = new MediaPlayer();
            if (list == null || list.size() <= 0) return;
            final int[] counter = {0};
            try {
                String path = list.get(counter[0]);
                player.setDataSource(path);
                player.prepareAsync();
                player.setOnPreparedListener(MediaPlayer::start);
                player.setOnCompletionListener(mp -> {
                    mp.reset();
                    counter[0]++;
                    if (counter[0] < list.size()) {
                        String p = list.get(counter[0]);
                        try {
                            mp.setDataSource(p);
                            mp.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        mp.release();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
