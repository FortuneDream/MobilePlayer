package com.example.q.mobileplayer.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;

import com.example.q.mobileplayer.R;

public class SplashActivity extends AppCompatActivity {
    private boolean isEnter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isEnter) {
                    enterHome();
                }
                finish();
            }
        }, 2000);
    }

    private void enterHome() {
        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnter) {
            enterHome();
            isEnter = true;
        }
        return super.onTouchEvent(event);
    }
}
