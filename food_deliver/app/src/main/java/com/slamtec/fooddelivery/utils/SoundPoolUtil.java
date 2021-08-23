package com.slamtec.fooddelivery.utils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * File   : SoundPoolUtil
 * Author : Qikun.Xiong
 * Date   : 2021/8/14 3:57 PM
 */
public class SoundPoolUtil {
    private volatile static SoundPoolUtil client;
    private SoundPool mSoundPool;
    private AudioManager mAudioManager;
    /*允许同时播放的音频数（为1时会立即结束上一个音频播放当前的音频）*/
    private static final int MAX_STREAMS = 1;
    // Stream type.
    private static final int streamType = AudioManager.STREAM_MUSIC;
    private int mSoundId;
    private int mResId;
    private Context mainContext;

    public static SoundPoolUtil getInstance(Context context) {
        if (client == null) {
            synchronized (SoundPoolUtil.class) {
                if (client == null) {
                    client = new SoundPoolUtil(context);
                }
            }
        }
        return client;
    }

    private SoundPoolUtil(Context context) {
        this.mainContext = context;
        mAudioManager = (AudioManager) this.mainContext.getSystemService(Context.AUDIO_SERVICE);
        ((Activity) this.mainContext).setVolumeControlStream(streamType);
        this.mSoundPool = new SoundPool(MAX_STREAMS, streamType, 0);
    }

    /**
     * 播放音频
     *
     * @param resId 本地音频资源
     */
    public void playSoundWithRedId(int resId) {
        this.mResId = resId;
        this.mSoundId = this.mSoundPool.load(this.mainContext, this.mResId, 1);
        this.mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                playSound();
            }
        });

    }

    /**
     * 播放音频，但是当前有音频这正播放中时不响应该次音频播放
     *
     * @param resId 本地音频资源
     */
    public synchronized void playSoundUnfinished(int resId) {
        if (isFmActive()) {
            return;
        }
        this.mResId = resId;
        this.mSoundId = this.mSoundPool.load(this.mainContext, this.mResId, 1);
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                playSound();
            }
        });
    }

    /**
     * 播放音频文件
     */
    private void playSound() {
        mSoundPool.play(this.mSoundId, 0.3f, 0.3f, 0, 0, 1f);
    }

    /**
     * 判断当前设备是否正在播放音频
     */
    private boolean isFmActive() {
        if (mAudioManager == null) {
            return false;
        }
        return mAudioManager.isMusicActive();
    }

}
