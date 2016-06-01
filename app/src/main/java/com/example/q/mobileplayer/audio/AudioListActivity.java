package com.example.q.mobileplayer.audio;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.q.mobileplayer.R;
import com.example.q.mobileplayer.bean.AudioItem;
import com.example.q.mobileplayer.video.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AudioListActivity extends BaseActivity {
    private ListView audioListView;
    private ArrayList<AudioItem> audioItems;
    private TextView noAudioTxt;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (audioItems != null && audioItems.size() > 0) {
                noAudioTxt.setVisibility(View.GONE);
                audioListView.setAdapter(new AudioListAdapter());
            } else {
                noAudioTxt.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    public void rightButtonClick() {

    }

    @Override
    public void leftButtonClick() {

    }

    @Override
    public View setContentView() {
        return View.inflate(AudioListActivity.this, R.layout.activity_audio_list, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRightButtonState(View.INVISIBLE);
        setLeftButtonState(View.INVISIBLE);
        setTitle("音乐列表");
        findId();
        setListener();
        initData();

    }

    private void initData() {
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
                mHandler.sendEmptyMessage(0);
                cursor.close();
            }
        }.start();

    }

    private void setListener() {
        audioListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AudioItem audioItem = audioItems.get(position);
                Intent intent = new Intent();
                intent.putExtra("position", position);
                intent.setClass(AudioListActivity.this, AudioPlayActivity.class);
                startActivity(intent);
            }
        });
    }

    private void findId() {
        audioListView = (ListView) findViewById(R.id.audio_list_view);
        noAudioTxt = (TextView) findViewById(R.id.no_audio_txt);

    }

    private class AudioListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return audioItems.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            View view;
            if (convertView != null) {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            } else {
                viewHolder = new ViewHolder();
                view = View.inflate(AudioListActivity.this, R.layout.item_audio_list, null);
                viewHolder.nameTxt = (TextView) view.findViewById(R.id.audio_name_txt);
                viewHolder.imageView = (ImageView) view.findViewById(R.id.audio_item_img);
                viewHolder.durationTxt = (TextView) view.findViewById(R.id.audio_duration_txt);
                viewHolder.sizeTxt = (TextView) view.findViewById(R.id.audio_size_txt);
                view.setTag(viewHolder);
            }
            viewHolder.nameTxt.setText(audioItems.get(position).getTitle());
            viewHolder.durationTxt.setText(simpleDateFormat.format(new Date(Long.parseLong(audioItems.get(position).getDuration()))));
            viewHolder.sizeTxt.setText(Formatter.formatFileSize(AudioListActivity.this, audioItems.get(position).getSize()));
            return view;
        }
    }

    static class ViewHolder {
        ImageView imageView;
        TextView nameTxt;
        TextView durationTxt;
        TextView sizeTxt;
    }

}
