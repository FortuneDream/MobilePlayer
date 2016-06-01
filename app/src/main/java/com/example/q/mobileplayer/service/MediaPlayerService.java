package com.example.q.mobileplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.example.q.mobileplayer.IAudioPlayerService;
import com.example.q.mobileplayer.R;
import com.example.q.mobileplayer.audio.AudioPlayActivity;
import com.example.q.mobileplayer.bean.AudioItem;

import java.io.IOException;
import java.util.ArrayList;

public class MediaPlayerService extends Service {
    private static final String TAG = "MediaPlayerService";
    public static final String PREPARE_MESSAGE = "com.example.dell.q.prepare.message";//视频播放准备完成，发消息。
    private ArrayList<AudioItem> audioItems;
    private AudioItem currentAudioItem;//当前播放的音频信息
    private MediaPlayer MP;//播放器
    private int currentPosition;
    public static int REPEAT_MODE_NORMAL = 0;//默认模式，顺序循环,一定要设置为静态，否则无法从外部得到
    public static int REPEAT_MODE_CURRENT = 1;//单曲循环
    public static int MODE_ALL = 2;//播放全部
    private int playMode = REPEAT_MODE_NORMAL;

    public MediaPlayerService() {
        Log.e(TAG, "Service 构造器");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "Service onCreate");
        getAllAudio();
    }

    private void getAllAudio() {
        new Thread() {
            @Override
            public void run() {
                audioItems = new ArrayList<>();
                ContentResolver resolver = getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                };
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                while (cursor.moveToNext()) {
                    AudioItem audioItem = new AudioItem();
                    String path = cursor.getString(0);
                    String duration = cursor.getString(1);
                    long size = cursor.getLong(2);
                    String title = cursor.getString(3);
                    String artist = cursor.getString(4);
                    if (size > 1 * 1024 * 1024) {
                        audioItem.setTitle(title);
                        audioItem.setDuration(duration);
                        audioItem.setPath(path);
                        audioItem.setArtist(artist);
                        audioItem.setSize(size);
                        audioItems.add(audioItem);
                    }
                }
                cursor.close();
            }
        }.start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "Service onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "Service onBind");
        return new MyBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "Service onUnbind");
        return super.onUnbind(intent);
    }

    //打开音频
    private void openAudio(int position) throws IOException {
        currentAudioItem = audioItems.get(position);
        currentPosition = position;
        if (MP != null) {
            MP.reset();//释放上一次播放资源
            MP = null;
        }
        MP = new MediaPlayer();
        Log.e(TAG, "MP:" + String.valueOf(MP));
        setListener();
        MP.setDataSource(currentAudioItem.getPath());//设置播放路径
        MP.prepareAsync();//准备
    }

    private void setListener() {
        MP.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.e(TAG, "MediaPlayer准备完成");
                play();
                //准备好之后发广播
                notifyChange(PREPARE_MESSAGE);
            }
        });
        MP.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (getPlayMode() == MediaPlayerService.REPEAT_MODE_NORMAL) {
                    currentPosition--;
                    next();
                } else if (getPlayMode() == MediaPlayerService.MODE_ALL) {
                    if (currentPosition == audioItems.size() - 1) {
                        return;
                    }
                } else if (getPlayMode() == MediaPlayerService.REPEAT_MODE_CURRENT) {
                    next();
                }

            }
        });
        MP.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(MediaPlayerService.this, "播放出错了", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void notifyChange(String prepareMessage) {
        Intent intent = new Intent();
        intent.setAction(prepareMessage);
        sendBroadcast(intent);
    }

    //播放音频,状态栏弹出消息
    private void play() {
        if (MP != null) {
            MP.start();
        }
        //延期意图
        Intent intent = new Intent(this, AudioPlayActivity.class);
        intent.putExtra("from_notification", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new Notification.Builder(this)
                .setContentText("正在播放：" + getAudioName())
                .setSmallIcon(R.drawable.default_ico)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        //设置属性：点击后还在,而且执行某个任务
        notification.flags = Notification.FLAG_ONGOING_EVENT;
    }

    //暂停音频,并且消除掉状态栏的播放器效果
    private void pause() {
        if (MP != null) {
            MP.pause();
        }
        stopForeground(true);

    }

    //得到艺术家名字
    private String getArtist() {
        if (currentAudioItem != null) {
            return currentAudioItem.getArtist();
        }
        return "";
    }

    //得到歌曲名称
    private String getAudioName() {
        if (currentAudioItem != null) {
            return currentAudioItem.getTitle();
        }
        return "";
    }

    //得到总时长
    private int getDuration() {
        if (MP != null) {
            return MP.getDuration();
        }
        return 0;
    }

    //得到当前播放位置
    private int getCurrentPosition() {
        if (MP != null) {
            return MP.getCurrentPosition();
        }
        return 0;
    }

    //定位到音频的播放位置
    private void seekTo(int position) {
        if (MP != null) {
            MP.seekTo(position);
        }
    }

    /**
     * 设置播放模式，顺序，单曲，全部
     *
     * @param mode
     */
    private void setPlayMode(int mode) {
        playMode = mode;
    }

    //上一首
    private void pre() {
        if (MP != null) {
            currentPosition--;
            if (currentPosition == -1) {
                currentPosition = audioItems.size() - 1;
            }
            try {
                openAudio(currentPosition);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //下一首
    private void next() {
        if (MP != null) {
            currentPosition++;
            currentPosition = currentPosition % audioItems.size();
            try {
                openAudio(currentPosition);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int getPlayMode() {
        return playMode;
    }

    private class MyBinder extends IAudioPlayerService.Stub {
        MediaPlayerService service = MediaPlayerService.this;


        @Override
        public void notifyChange(String notify) throws RemoteException {
            service.notifyChange(notify);
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return service.isPlaying();
        }

        @Override
        public void openAudio(int position) throws RemoteException {
            try {
                service.openAudio(position);//调用服务里的方法
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void play() throws RemoteException {
            service.play();
        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public String getArtist() throws RemoteException {
            return service.getArtist();

        }

        @Override
        public String getAudioName() throws RemoteException {
            return service.getAudioName();

        }

        @Override
        public int getDuration() throws RemoteException {
            return service.getDuration();

        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return service.getCurrentPosition();

        }

        @Override
        public void seekTo(int position) throws RemoteException {
            service.seekTo(position);
        }

        @Override
        public void setPlayMode(int mode) throws RemoteException {
            service.setPlayMode(mode);
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return service.getPlayMode();
        }

        @Override
        public void pre() throws RemoteException {
            service.pre();
        }

        @Override
        public void next() throws RemoteException {
            service.next();
        }
    }

    private boolean isPlaying() {
        if (MP != null) {
            return MP.isPlaying();
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "Service onDestroy");
        Log.e(TAG, "MP" + String.valueOf(MP));
    }


}
