package com.example.q.mobileplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Q on 2016/5/23.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private Button leftBtn;
    private Button rightBtn;
    private TextView titleTxt;
    private LinearLayout contentLl;
    private View.OnClickListener mOnClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.left_btn:
                    leftButtonClick();
                    break;
                case R.id.right_btn:
                    rightButtonClick();
            }
        }
    };
    //如果直接实现View的OnClickListener，那么Base并不需要实现，因为他是抽象类，但是却让子类Activity实现，显然不合理
    public abstract void rightButtonClick();
    public abstract void leftButtonClick();
    public abstract View setContentView();//加载布局文件
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        findBaseId();
        setBaseListener();
        //注意Params必须是父亲的类的包
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        View content=setContentView();
        contentLl.addView(content,params);
    }

    private void setBaseListener() {
        leftBtn.setOnClickListener(mOnClickListener);
        rightBtn.setOnClickListener(mOnClickListener);
    }

    private void findBaseId() {
        leftBtn = (Button) findViewById(R.id.left_btn);
        rightBtn = (Button) findViewById(R.id.right_btn);
        titleTxt = (TextView) findViewById(R.id.title_txt);
        contentLl = (LinearLayout) findViewById(R.id.content_ll);
    }


    public void setLeftButtonState(int visibility){
        leftBtn.setVisibility(visibility);
    }

    public void setRightButtonState(int visibility){
        rightBtn.setVisibility(View.VISIBLE);
    }

    public void setTitle(String title){
        titleTxt.setText(title);
    }

}
