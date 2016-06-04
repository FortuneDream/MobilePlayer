package com.example.q.mobileplayer.bean;

/**
 * Created by Q on 2016/6/4.
 */
//歌词三要素，歌词内容，歌词时间戳，歌词高亮显示的时间
public class Lyric {
    private String content;
    private long timePoint;

    public Lyric(String content, long timePoint, long sleepTime) {
        this.content = content;
        this.timePoint = timePoint;
        this.sleepTime = sleepTime;
    }

    private long sleepTime;
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimePoint() {
        return timePoint;
    }

    public void setTimePoint(long timePoint) {
        this.timePoint = timePoint;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }



}
