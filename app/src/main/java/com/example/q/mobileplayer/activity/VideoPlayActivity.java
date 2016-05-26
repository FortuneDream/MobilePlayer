package com.example.q.mobileplayer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.q.mobileplayer.R;
import com.example.q.mobileplayer.bean.VideoItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class VideoPlayActivity extends BaseActivity implements View.OnClickListener {
    private static final int PROGRESS = 0;
    private TextView videoNameTxt;
    private ImageView powerImg;
    private TextView systemTimeTxt;
    private ImageView loudImg;
    private SeekBar loudSeekBar;
    private ImageView switchImg;
    private TextView currentTimeTxt;
    private SeekBar progressSeekBar;
    private TextView durationTxt;
    private Button exitBtn;
    private Button preBnt;
    private Button playBtn;
    private Button nextBtn;
    private Button screenBtn;
    private MyReceiver myReceiver = new MyReceiver();
    private ArrayList<VideoItem> videoItems;
    private int postion;
    private int level;
    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    private boolean isDestroyed = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESS:
                    systemTimeTxt.setText(formatter.format(new Date()));//显示当前手机的时间
                    int currentPosition = videoView.getCurrentPosition();
                    currentTimeTxt.setText(formatter.format(currentPosition));//显示当前播放时间
                    progressSeekBar.setProgress(currentPosition);//SeekBar进度更新
                    if (!isDestroyed) {//线程不会因为Activity的Destroy而中断，所以要设置标记
                        mHandler.removeMessages(PROGRESS);
                        mHandler.sendEmptyMessageDelayed(PROGRESS, 1000);//重复发消息，定时更新,必须要延迟发送，不然会使手机变慢
                    }
                    break;

            }
        }
    };

    private VideoView videoView;//内部封装了MediaPlayer一个，且默认支持3gp，mp4，m3u8（碎片化，拖动和首次播放快,点播+直播）,且因为码率不同，可能格式符合，但是也不能播放
    //继承的SurfaceView在子线程渲染。
    //.so文件，C和C++生成动态链接库
    private Uri uri;

    @Override
    public void rightButtonClick() {

    }

    @Override
    public void leftButtonClick() {

    }

    @Override
    public View setContentView() {
        return View.inflate(VideoPlayActivity.this, R.layout.activity_video_play, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findId();
        initData();
        getDate();
        setDate();
        setTitleBarState(View.GONE);
        setListener();
        //videoView.setMediaController(new MediaController(this));//内置的控制器，暂停+快进
        //要等准备好了才播放，不要直接播放

    }

    private void setDate() {
        if (videoItems != null && videoItems.size() > 0) {
            videoView.setVideoPath(videoItems.get(postion).getPath());
            videoNameTxt.setText(videoItems.get(postion).getTitle());
        } else if (uri != null) {
            videoView.setVideoURI(uri);
            videoNameTxt.setText(uri.toString());
        }
    }

    //得到数据
    private void getDate() {
        postion = getIntent().getIntExtra("position", 0);
        videoItems = (ArrayList<VideoItem>) getIntent().getSerializableExtra("videolist");
        //得到播放地址，来自第三方软件-文件夹管理器和浏览器
        uri = getIntent().getData();
    }

    private void initData() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);//监听电量变化
        registerReceiver(myReceiver, filter);
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //得到电量的值0~100
            level = intent.getIntExtra("level", 0);
        }
    }

    private void setListener() {
        //准备完成监听
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int duration = mp.getDuration();
                currentTimeTxt.setText(formatter.format(duration));//得到视频长度
                progressSeekBar.setMax(duration);//设置进度条最大值
                mp.start();
                mHandler.sendEmptyMessage(PROGRESS);
            }
        });
        //播放完成监听
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //

            }
        });
        playBtn.setOnClickListener(this);
        progressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //状态变化
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    //不是程序设置的移动，而是来自用户的滑动
                    videoView.seekTo(progress);
                }
            }

            //手机拖动
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            //手指离开控件
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void findId() {
        videoView = (VideoView) findViewById(R.id.video_view);
        videoNameTxt = (TextView) findViewById(R.id.video_name_txt);
        powerImg = (ImageView) findViewById(R.id.power_img);
        systemTimeTxt = (TextView) findViewById(R.id.system_time_txt);
        loudImg = (ImageView) findViewById(R.id.loud_img);
        loudSeekBar = (SeekBar) findViewById(R.id.loud_seek_bar);
        switchImg = (ImageView) findViewById(R.id.switch_img);
        currentTimeTxt = (TextView) findViewById(R.id.current_time_txt);
        progressSeekBar = (SeekBar) findViewById(R.id.progress_seek_bar);
        durationTxt = (TextView) findViewById(R.id.duration_txt);
        exitBtn = (Button) findViewById(R.id.exit_btn);
        preBnt = (Button) findViewById(R.id.pre_bnt);
        playBtn = (Button) findViewById(R.id.play_btn);
        nextBtn = (Button) findViewById(R.id.next_btn);
        screenBtn = (Button) findViewById(R.id.screen_btn);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_btn:
                if (videoView.isPlaying()) {
                    videoView.pause();
                    playBtn.setText("播放");
                } else {
                    videoView.start();
                    playBtn.setText("暂停");
                }
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
        unregisterReceiver(myReceiver);
        myReceiver = null;
    }

}
