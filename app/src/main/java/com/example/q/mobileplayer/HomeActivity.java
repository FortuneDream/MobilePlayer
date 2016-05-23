package com.example.q.mobileplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

public class HomeActivity extends BaseActivity{
    private GridView gridView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLeftButtonState(View.GONE);
        setTitle("简易播放器");
        findId();
        gridView.setAdapter(new MainAdapter());
    }

    private void findId() {
        gridView = (GridView) findViewById(R.id.grid_view);
    }

    @Override
    public void rightButtonClick() {

    }

    @Override
    public void leftButtonClick() {

    }

    @Override
    public View setContentView() {
        return View.inflate(this,R.layout.activity_home,null);
    }

    private class MainAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return 0;
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

            return null;
        }
    }

}
