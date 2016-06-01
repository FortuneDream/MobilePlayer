package com.example.q.mobileplayer.audio;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.q.mobileplayer.IAudioPlayerService;
import com.example.q.mobileplayer.R;
import com.example.q.mobileplayer.service.MediaPlayerService;
import com.example.q.mobileplayer.video.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AudioPlayActivity extends BaseActivity implements View.OnClickListener {
    private static final int PROGRESS = 0;//进度条
    private IAudioPlayerService IAPS;
    private int position;
    private Intent intent;
    private ImageView audioPlayIco;
    private TextView artistNameTxt;
    private TextView audioNameTxt;
    private TextView timeTxt;
    private Button modeBtn;
    private Button preBtn;
    private Button playBtn;
    private Button nextBtn;
    private Button lyricsBtn;
    private SeekBar progressSbr;
    private BroadcastReceiver receiver;
    private Boolean isDestroy;//Activity是否已经销毁
    private Boolean isFromNotification;//是否通过状态栏进入
    private SimpleDateFormat format = new SimpleDateFormat("mm:ss");
    private int currentPlayMode=0;
    private ServiceConnection SC = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IAPS = IAudioPlayerService.Stub.asInterface(service);//得到服务
            if (IAPS != null) {
                try {
                    if (!isFromNotification){
                        IAPS.openAudio(position);
                    }else {
                        //发一个消息，告诉Activity准备好了,setViewStatus,这样就避免了，从notification进来后无法更新界面的问题（因为没有再次handler.sendMessage);
                        IAPS.notifyChange(MediaPlayerService.PREPARE_MESSAGE);
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESS:
                    if (!isDestroy) {
                        mHandler.sendEmptyMessageDelayed(PROGRESS, 1000);
                    }
                    int currentPosition;
                    try {
                        currentPosition = IAPS.getCurrentPosition();
                        progressSbr.setProgress(currentPosition);//刷新进度条
                        timeTxt.setText(format.format(new Date(IAPS.getCurrentPosition())) + "/" + format.format(new Date(IAPS.getDuration())));//刷新时间
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
            }
        }
    };

    @Override
    public void rightButtonClick() {

    }

    @Override
    public void leftButtonClick() {
        finish();
    }

    @Override
    public View setContentView() {
        return View.inflate(AudioPlayActivity.this, R.layout.activity_audio_play, null);
    }

    /**
     * 1.注册广播接收者，监听来自服务的广播
     * 2.得到position
     * 3.开启并绑定服务
     * 4.在SC中得到远程服务MP，传入position，调用远程服务方法openAudio
     * 5.openAudio:在远程服务中，开始播放后，发送广播，Activity中接收广播，更新时间,歌曲名，艺术家名，开启进度条更新
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDate();
        initView();
        findId();
        getDate();
        setListener();
        intent = new Intent(AudioPlayActivity.this, MediaPlayerService.class);
        startService(intent);
        bindService(intent, SC, Context.BIND_AUTO_CREATE);
    }

    //初始化数据
    private void initDate() {
        isDestroy = false;
        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaPlayerService.PREPARE_MESSAGE);//监听自定义广播
        receiver = new MyBroadcastReceiver();
        registerReceiver(receiver, filter);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            setViewStatus();//设置View的状态,通过广播启动页面更新
        }


    }

    //MP准备好之后发广播，这是界面初时状态，handler发消息，进度条和时间开始移动
    private void setViewStatus() {
        try {
            artistNameTxt.setText(IAPS.getArtist());
            audioNameTxt.setText(IAPS.getAudioName());
            timeTxt.setText(format.format(new Date(IAPS.getCurrentPosition())) + "/" + format.format(new Date(IAPS.getDuration())));
            progressSbr.setMax(IAPS.getDuration());//这是progress的进度条的最大值。
            currentPlayMode=IAPS.getPlayMode();
            changeMode(currentPlayMode);
            mHandler.sendEmptyMessage(PROGRESS);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void initView() {
        setTitle("播放界面");
        setRightButtonState(View.INVISIBLE);
    }

    private void setListener() {
        modeBtn.setOnClickListener(this);
        preBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        lyricsBtn.setOnClickListener(this);
        progressSbr.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    try {
                        IAPS.seekTo(progress);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void findId() {
        audioPlayIco = (ImageView) findViewById(R.id.audio_play_ico);
        artistNameTxt = (TextView) findViewById(R.id.artist_name_txt);
        audioNameTxt = (TextView) findViewById(R.id.audio_name_txt);
        timeTxt = (TextView) findViewById(R.id.time_txt);
        modeBtn = (Button) findViewById(R.id.mode_btn);
        preBtn = (Button) findViewById(R.id.pre_btn);
        playBtn = (Button) findViewById(R.id.play_btn);
        nextBtn = (Button) findViewById(R.id.next_btn);
        lyricsBtn = (Button) findViewById(R.id.lyrics_btn);
        progressSbr = (SeekBar) findViewById(R.id.progress_sbr);

    }


    public void getDate() {
        isFromNotification=getIntent().getBooleanExtra("from_notification",false);
        if (!isFromNotification){
            position = getIntent().getIntExtra("position", 0);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //设置播放模式
            case R.id.mode_btn:
                try {
                    currentPlayMode=(IAPS.getPlayMode()+1)%3;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                changeMode(currentPlayMode);
                break;
            case R.id.pre_btn:
                try {
                    IAPS.pre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.play_btn:
                try {
                    if (IAPS.isPlaying()) {
                        IAPS.pause();
                        playBtn.setText("Play");
                    } else {
                        IAPS.play();
                        playBtn.setText("Pause");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.next_btn:
                try {
                    IAPS.next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.lyrics_btn:
                break;
        }
    }

    private void changeMode(int currentPlayMode) {
        try {
            IAPS.setPlayMode(currentPlayMode);
            if (currentPlayMode==MediaPlayerService.REPEAT_MODE_NORMAL){
                currentPlayMode=MediaPlayerService.REPEAT_MODE_NORMAL;
                modeBtn.setText("全部循环");
            }else if (currentPlayMode==MediaPlayerService.MODE_ALL){
                currentPlayMode=MediaPlayerService.MODE_ALL;
                modeBtn.setText("顺序播放");
            }else if (currentPlayMode==MediaPlayerService.REPEAT_MODE_CURRENT){
                currentPlayMode=MediaPlayerService.REPEAT_MODE_CURRENT;
                modeBtn.setText("单曲循环");
            }
            IAPS.setPlayMode(currentPlayMode);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //设置按钮状态
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //stopService(intent);//非必须，这里不能用，因为每次openAudio都会开启一个服务，然后取得一个MediaPlayer，如果stopService，再次打开的时候，又会创建一个服务，又会取得一个MediaPlayer。同时持有多个MP
        //如果不stopService，那么再次openAudio时，不会调用onCreate方法，就持有的还是原来那个MP
        unbindService(SC);
        unregisterReceiver(receiver);
        receiver = null;
        isDestroy = true;
    }
}
