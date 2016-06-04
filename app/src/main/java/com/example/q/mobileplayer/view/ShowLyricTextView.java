package com.example.q.mobileplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.example.q.mobileplayer.bean.Lyric;

import java.util.ArrayList;

/**
 * Created by Q on 2016/6/2.
 */
//显示歌词
public class ShowLyricTextView extends TextView {
    private Paint currentPaint;
    private Paint notCurrentPaint;
    private ArrayList<Lyric> lyricArrayList;
    private int index;//当前句歌词的位置
    private int width;//当前控件的宽
    private int height;//高
    private float textHeight = 40;//每行的高度
    private int currentPosition;
    private long timePoint;//时间戳
    private long sleepTime;//高亮显示时间

    //根据播放进度计算该显示哪句歌词
    public void setShowNextLyric(int currentPosition) {
        this.currentPosition = currentPosition;
        if (lyricArrayList == null) {
            return;
        }
        //找出那句该高亮显示
        for (int i = 1; i < lyricArrayList.size(); i++) {
            if (currentPosition < lyricArrayList.get(i).getTimePoint()) {
                int tempIndex = i - 1;//包含了第0句
                //立刻找出符合高亮显示的那句歌词:得到歌词的位置，得到歌词的时间戳，歌词的内容
                if (currentPosition >= lyricArrayList.get(tempIndex).getTimePoint()) {
                    index = tempIndex;
                    timePoint = lyricArrayList.get(tempIndex).getTimePoint();
                    sleepTime = lyricArrayList.get(tempIndex).getSleepTime();
                }
            }
        }
        //导致onDraw方法执行
        invalidate();

    }

    public ShowLyricTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {

        //其他歌词的画笔
        notCurrentPaint = new Paint();
        notCurrentPaint.setColor(Color.WHITE);//设置颜色
        notCurrentPaint.setAntiAlias(true);//设置抗锯齿
        notCurrentPaint.setTextAlign(Paint.Align.CENTER);//文字居中
        notCurrentPaint.setTextSize(30);

        //当前歌词的画笔
        currentPaint = new Paint();
        currentPaint.setColor(Color.GREEN);//设置高亮颜色
        currentPaint.setAntiAlias(true);//设置抗锯齿
        currentPaint.setTextAlign(Paint.Align.CENTER);//文字居中
        currentPaint.setTextSize(30);

        //添加假设歌词
        lyricArrayList = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            Lyric lyric = new Lyric(i + "aaaa" + i, 2000 * i, 2000);
            //歌词添加到歌词列表中
            lyricArrayList.add(lyric);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        //在Y轴上移动平移的坐标
        float plus;
        if (sleepTime == 0) {
            plus = 0;
        } else {
            //平移的距离=刚开始的+移动这行的百分之多少的距离
            //移动这行的百分之多少的距离=移动的速度*行的高度
            float datel = ((currentPosition - timePoint) / sleepTime) * textHeight;
            plus = textHeight + datel;
            //dx，X的坐标
            //dy，Y的坐标
            canvas.translate(0, -plus);
        }
        if (lyricArrayList != null && lyricArrayList.size() > 0) {
            //得到当前句的内容,画当前句歌词
            String currentContent = lyricArrayList.get(index).getContent();
            canvas.drawText(currentContent, width / 2, height / 2, currentPaint);
            //画当前句前部分歌词
            float tempY = height / 2;
            for (int i = index - 1; i > 0; i--) {
                String nextContent = lyricArrayList.get(i).getContent();
                tempY -= textHeight;
                canvas.drawText(nextContent, width / 2, tempY, notCurrentPaint);
                if (tempY < 0) {//
                    break;
                }
            }
            //画当前句后部分歌词
            for (int i = index + 1; i < lyricArrayList.size(); i++) {
                String preContent = lyricArrayList.get(i).getContent();
                tempY += textHeight;
                canvas.drawText(preContent, width / 2, tempY, notCurrentPaint);
                if (tempY > height) {
                    break;
                }
            }
        } else {
            //画布，getWith(),得到屏幕的一般的宽
            canvas.drawText("没有歌词", width / 2, height / 2, currentPaint);
        }

    }

    //得到当前控件的宽和高
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }
}
