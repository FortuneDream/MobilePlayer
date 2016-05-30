package com.example.q.mobileplayer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.q.mobileplayer.R;
import com.example.q.mobileplayer.bean.VideoItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class VideoPlayActivity extends BaseActivity implements View.OnClickListener {
    private static final int PROGRESS = 0;
    private static final int HIDE_CONTROLLER = 1;
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
    private Button preBtn;
    private Button playBtn;
    private Button nextBtn;
    private Button screenBtn;
    private LinearLayout controllerLl;
    private LinearLayout loadingLl;
    private LinearLayout bufferingLl;
    private boolean isShowControl = false;//是否显示控制面板
    private AudioManager am;
    private int currentVolume;
    private int maxVolume;
    private boolean isMute = false;//是否为静音
    private MyReceiver myReceiver = new MyReceiver();
    private ArrayList<VideoItem> videoItems;
    private int position;
    private int level;
    private GestureDetector gestureDetector;
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
                    //设置缓存进度,0%-100%
                    int percentage = videoView.getBufferPercentage();
                    int total = percentage * progressSeekBar.getMax();//当前最大可以播放的大小
                    int secondaryProgress=total/100;
                    progressSeekBar.setSecondaryProgress(secondaryProgress);
                    if (!isDestroyed) {//线程不会因为Activity的Destroy而中断，所以要设置标记
                        mHandler.removeMessages(PROGRESS);
                        mHandler.sendEmptyMessageDelayed(PROGRESS, 1000);//重复发消息，定时更新,必须要延迟发送，不然会使手机变慢
                    }
                    break;
                case HIDE_CONTROLLER:
                    hideControlPlayer();

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
            videoView.setVideoPath(videoItems.get(position).getPath());
            videoNameTxt.setText(videoItems.get(position).getTitle());
        } else if (uri != null) {
            videoView.setVideoURI(uri);
            videoNameTxt.setText(uri.toString());
            //设置上一步和下一步按钮不可点击
            preBtn.setEnabled(false);
            nextBtn.setEnabled(false);
        }
        loudSeekBar.setMax(maxVolume);//seekBar与音量相互关联
        loudSeekBar.setProgress(currentVolume);
    }

    //得到数据
    private void getDate() {
        position = getIntent().getIntExtra("position", 0);
        videoItems = (ArrayList<VideoItem>) getIntent().getSerializableExtra("videolist");
        //得到播放地址，来自第三方软件-文件夹管理器和浏览器
        uri = getIntent().getData();
    }

    private void initData() {
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);//当前音量大小
        maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//音量在最大值
        //设置当播放的时候，不锁屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideControlPlayer();
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

    private float startY;//手指在屏幕滑动的起始Y轴坐标
    private float audioTouchRang;//屏幕滑动的范围
    private int mVol;//滑动前的音量值

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);//事件传给手势识别器
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //1.记录初始值

                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;//对事件处理了
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
                loadingLl.setVisibility(View.GONE);
            }
        });
        //播放完成监听
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNextVideo();
            }
        });
        playBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        preBtn.setOnClickListener(this);
        progressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //状态变化
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    //不是程序设置的移动，而是来自用户的滑动
                    videoView.seekTo(progress);
                }
            }

            //手机拖动,不能隐藏controller
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                hideControlPlayer();
                mHandler.removeMessages(HIDE_CONTROLLER);
            }

            //手指离开控件
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.sendEmptyMessageDelayed(HIDE_CONTROLLER, 6000);
            }
        });
        gestureDetector = new GestureDetector(VideoPlayActivity.this, new GestureDetector.SimpleOnGestureListener() {
            //长按屏幕,暂停或者播放
            @Override
            public void onLongPress(MotionEvent e) {
                setPlayOrPause();
            }

            //双击屏幕
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return super.onDoubleTap(e);
            }

            //单击屏幕,隐藏或者显示控制栏
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                super.onSingleTapConfirmed(e);
                if (isShowControl) {
                    hideControlPlayer();
                } else {
                    showControlPlayer();
                }

                return true;
            }
        });
        loudSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updateVolume(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                showControlPlayer();
                mHandler.removeMessages(HIDE_CONTROLLER);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                hideControlPlayer();
            }
        });
        loudImg.setOnClickListener(this);
        //设置监听播放出错
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(getApplicationContext(), "出错了", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        //监听播放卡，并且提示用户-这个方法从Android2.3（含）之后
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what){
                    //卡
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        bufferingLl.setVisibility(View.VISIBLE);
                        break;
                    //卡结束
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        bufferingLl.setVisibility(View.GONE);
                        break;
                }
                return true;
            }
        });

    }


    /**
     * 调节音量的方法
     * 音量值的范围0-15
     *
     * @param volume 要调节的音量值
     */
    private void updateVolume(int volume) {
        if (isMute) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 1);
            loudSeekBar.setProgress(0);
        } else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 1);
            loudSeekBar.setProgress(volume);
        }
        currentVolume = volume;//如果是点击了静音，再点回来的时候就返回原来的状态

    }

    //播放上一首
    private void playPreVideo() {
        if (videoItems != null && videoItems.size() > 0) {
            position--;
            if (position >= 0) {
                //获取下一个视频的信息
                videoView.setVideoPath(videoItems.get(position).getPath());
                videoNameTxt.setText(videoItems.get(position).getTitle());
                setPlayOrPauseStatus();
            } else {
                position = 0;//复原
                Toast.makeText(getApplicationContext(), "最后一个视频了", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //播放下一首
    private void playNextVideo() {
        if (videoItems != null && videoItems.size() > 0) {
            position++;
            if (position < videoItems.size()) {
                //获取下一个视频的信息
                videoView.setVideoPath(videoItems.get(position).getPath());
                videoNameTxt.setText(videoItems.get(position).getTitle());
                setPlayOrPauseStatus();
            } else {
                Toast.makeText(getApplicationContext(), "最后一个视频了", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (uri != null) {
            Toast.makeText(getApplicationContext(), "最后一个视频了", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setPlayOrPauseStatus() {
        if (position == 0) {//第一个和最后一个设置不可点击
            preBtn.setEnabled(false);
        }
        if (position == videoItems.size() - 1) {
            nextBtn.setEnabled(false);
        }
        nextBtn.setEnabled(true);
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
        preBtn = (Button) findViewById(R.id.pre_btn);
        playBtn = (Button) findViewById(R.id.play_btn);
        nextBtn = (Button) findViewById(R.id.next_btn);
        screenBtn = (Button) findViewById(R.id.screen_btn);
        controllerLl = (LinearLayout) findViewById(R.id.controller_ll);
        loadingLl = (LinearLayout) findViewById(R.id.loading_ll);
        bufferingLl = (LinearLayout) findViewById(R.id.buffering_ll);
    }

    @Override
    public void onClick(View v) {
        mHandler.removeMessages(HIDE_CONTROLLER);
        hideControlPlayer();
        switch (v.getId()) {
            case R.id.play_btn:
                setPlayOrPause();
                break;
            case R.id.next_btn:
                playNextVideo();
                break;
            case R.id.pre_btn:
                playPreVideo();
                break;
            case R.id.loud_img:
                isMute = !isMute;//取反
                updateVolume(currentVolume);
                break;
        }
    }

    private void setPlayOrPause() {
        if (videoView.isPlaying()) {
            videoView.pause();
            playBtn.setText("播放");
        } else {
            videoView.start();
            playBtn.setText("暂停");
        }
    }

    private void hideControlPlayer() {
        controllerLl.setVisibility(View.GONE);
        isShowControl = false;
    }

    private void showControlPlayer() {
        controllerLl.setVisibility(View.VISIBLE);
        isShowControl = true;
        mHandler.sendEmptyMessageDelayed(HIDE_CONTROLLER, 6000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
        unregisterReceiver(myReceiver);
        myReceiver = null;
    }

}
