package com.example.q.mobileplayer;

import android.content.Intent;
import android.os.IBinder;

interface IAudioPlayerService {
    //发广播
    void notifyChange(String notify);
    //是否播放中
    boolean isPlaying();
    //打开音频
    void openAudio(int position);
    //播放音频
    void play();
    //暂停音频
    void pause();
    //得到艺术家名字
    String getArtist();
    //得到歌曲名称
    String getAudioName();
    //得到总时长
    int getDuration();
    //得到当前播放位置
    int getCurrentPosition();
    //定位到音频的播放位置
    void seekTo(int position);
    /**
     * 设置播放模式，顺序，单曲，全部
     * @param mode
     */
    void setPlayMode(int mode);

    int getPlayMode();
    //上一首
    void pre();
    //下一首
    void next();


}
