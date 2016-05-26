package com.example.q.mobileplayer.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.q.mobileplayer.R;
import com.example.q.mobileplayer.bean.VideoItem;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VideoActivity extends BaseActivity {

    private ListView videoListView;
    private TextView notFindTxt;
    private SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
    private List<VideoItem> videoItemList = new ArrayList<>();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (videoItemList != null && videoItemList.size() > 0) {
                videoListView.setAdapter(new VideoListAdapter());
                notFindTxt.setVisibility(View.GONE);
            } else {
                notFindTxt.setVisibility(View.VISIBLE);
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
        return View.inflate(this, R.layout.activity_video, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("本地视频");
        setRightButtonState(View.GONE);
        findId();
        getAllVideo();
        setListener();
    }

    private void setListener() {
        videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //传入单个视频
//                String path = videoItemList.get(position).getPath();
//                Intent intent = new Intent();
//                intent.setData(Uri.parse(path));//String生成一个uri
//                intent.setClass(VideoActivity.this, VideoPlayActivity.class);
//                startActivity(intent);
                Intent intent = new Intent(VideoActivity.this, VideoPlayActivity.class);
                Bundle extras = new Bundle();
                //传入视频列表
                extras.putSerializable("videolist", (Serializable) videoItemList);//传递对象需要序列化
                intent.putExtras(extras);
                //传入视频列表的位置
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
    }

    //加载视频
    //直接将视频拖到sdcard下，没有调用媒体扫描器，所以不能得到视频，需要发广播通知媒体扫描器
    private void getAllVideo() {
        new Thread() {
            @Override
            public void run() {
                ContentResolver resolver = getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {
                        MediaStore.Video.Media.TITLE,
                        MediaStore.Video.Media.DURATION,//时长
                        MediaStore.Video.Media.SIZE,//可用size来去掉短视频，不用时长来判断
                        MediaStore.Video.Media.DATA,
                };
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                while (cursor.moveToNext()) {
                    VideoItem videoItem = new VideoItem();
                    String title = cursor.getString(0);
                    String duration = cursor.getString(1);
                    long size = cursor.getLong(2);
                    String path = cursor.getString(3);
                    //屏蔽视频小于3mb的小文件
                    if (size > 3 * 1024 * 1024) {
                        videoItem.setTitle(title);
                        videoItem.setDuration(duration);
                        videoItem.setPath(path);
                        videoItem.setSize(size);
                        videoItemList.add(videoItem);
                    }
                }
                handler.sendEmptyMessage(0);
                cursor.close();
            }
        }.start();
    }

    private void findId() {
        videoListView = (ListView) findViewById(R.id.video_list_view);
        notFindTxt = (TextView) findViewById(R.id.not_find_txt);
    }

    private class VideoListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return videoItemList.size();
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
                view = View.inflate(VideoActivity.this, R.layout.item_video, null);
                viewHolder.nameTxt = (TextView) view.findViewById(R.id.video_name_txt);
                viewHolder.imageView = (ImageView) view.findViewById(R.id.video_item_img);
                viewHolder.durationTxt = (TextView) view.findViewById(R.id.video_duration_txt);
                viewHolder.sizeTxt = (TextView) view.findViewById(R.id.video_size_txt);
                view.setTag(viewHolder);
            }
            viewHolder.nameTxt.setText(videoItemList.get(position).getTitle());
            viewHolder.durationTxt.setText(formatter.format(new Date(Long.parseLong(videoItemList.get(position).getDuration()))));
            viewHolder.sizeTxt.setText(Formatter.formatFileSize(VideoActivity.this, videoItemList.get(position).getSize()));
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
