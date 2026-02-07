package org.tcshare.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

/**
 * 频率高，时间短的音频快速播放， 比如滴滴滴的声音， 或游戏里的快速大量的打斗音播放。
 */
public class AudioPlayUtil {
    private static final int MAX_ALLOWED_MUSIC = 100;
    private final SoundPool soundPool;
    private static AudioPlayUtil instance;
    private int[] musicIDs = new int[0];

    private AudioPlayUtil() {
        AudioAttributes attr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME) // 设置音效使用场景
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(); // 设置音效的类型
        soundPool = new SoundPool.Builder().setAudioAttributes(attr) // 设置音效池的属性
                .setMaxStreams(MAX_ALLOWED_MUSIC) // 设置最多容纳音频流
                .build();
    }

    public static AudioPlayUtil getInstance() {
        synchronized (AudioPlayUtil.class) {
            if (instance == null) {
                synchronized (AudioPlayUtil.class) {
                    instance = new AudioPlayUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 载入音频
     * 资源ID列表
     *
     * @param context
     * @param resIDs
     */
    public void loadAudiosFromRes(Context context, int[] resIDs) {
        if(resIDs == null || resIDs.length > MAX_ALLOWED_MUSIC){
            throw new IllegalArgumentException("最大允许 " + MAX_ALLOWED_MUSIC + "条音频缓存");
        }
        musicIDs = new int[resIDs.length];
        for (int i = 0; i < resIDs.length; i++) {
            musicIDs[i] = soundPool.load(context, resIDs[i], 1);
        }
    }

    /**
     * 播放声音
     * 1 成功
     */
    public int playOnce(int pos) {
        if (pos > musicIDs.length) {
            return -1;
        }
        soundPool.play(musicIDs[pos], 1, 1, 0, 0, 1);
        return 1;
    }

    /**
     * 播放一次第一条，比如只有一条滴滴声，或打斗声音
     * @return
     */
    public int playOnceFist() {
        if (musicIDs.length < 1) {
            return -1;
        }
        soundPool.play(musicIDs[0], 1, 1, 0, 0, 1);
        return 1;
    }

    /**
     * 自定义播放
     * @param pos
     * @param loop
     * @param rate
     * @return
     */
    public int play(int pos, int loop, int rate) {
        if (pos > musicIDs.length) {
            return -1;
        }
        soundPool.play(musicIDs[pos], 1, 1, 0, loop, rate);
        return 1;
    }

}
