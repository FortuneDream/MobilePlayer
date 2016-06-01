package com.example.q.mobileplayer.video;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.q.mobileplayer.R;
import com.example.q.mobileplayer.audio.AudioListActivity;
import com.example.q.mobileplayer.bean.HomeItem;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity {
    private GridView gridView;
    private List<HomeItem> homeItemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLeftButtonState(View.GONE);
        setTitle("简易播放器");
        findId();
        initItemData();
        gridView.setAdapter(new MainAdapter());
    }

    private void initItemData() {
        HomeItem homeItem0 = new HomeItem("视频", R.drawable.home_video_ico);
        HomeItem homeItem1 = new HomeItem("音乐", R.drawable.home_music_ico);
        HomeItem homeItem2 = new HomeItem("网络", R.drawable.home_net_ico);
        HomeItem homeItem3 = new HomeItem("大全", R.drawable.home_tool_ico);
        HomeItem homeItem4 = new HomeItem("直播", R.drawable.home_living_ico);
        HomeItem homeItem5 = new HomeItem("推荐", R.drawable.home_recommend_ico);
        homeItemList.add(homeItem0);
        homeItemList.add(homeItem1);
        homeItemList.add(homeItem2);
        homeItemList.add(homeItem3);
        homeItemList.add(homeItem4);
        homeItemList.add(homeItem5);
    }

    private void findId() {
        gridView = (GridView) findViewById(R.id.grid_view);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                switch (position) {
                    case 0:
                        intent = new Intent(HomeActivity.this, VideoActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        intent =new Intent(HomeActivity.this, AudioListActivity.class);
                        startActivity(intent);
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;

                }
            }
        });
    }

    @Override
    public void rightButtonClick() {

    }

    @Override
    public void leftButtonClick() {

    }

    @Override
    public View setContentView() {
        return View.inflate(this, R.layout.activity_home, null);
    }

    private class MainAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return homeItemList.size();
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
                view = View.inflate(HomeActivity.this, R.layout.item_home, null);
                viewHolder.nameTxt = (TextView) view.findViewById(R.id.name_home_txt);
                viewHolder.imageView = (ImageView) view.findViewById(R.id.ico_home_img);
                view.setTag(viewHolder);
            }
            viewHolder.nameTxt.setText(homeItemList.get(position).getName());
            viewHolder.imageView.setImageResource(homeItemList.get(position).getResource());
            return view;
        }
    }

    static class ViewHolder {
        TextView nameTxt;
        ImageView imageView;
    }

}
