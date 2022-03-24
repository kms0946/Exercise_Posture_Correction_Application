package com.mslog.health_record;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class loading extends AppCompatActivity {
    TextView percent;
    ImageView loading;
    AnimationDrawable animation;
    ProgressBar bar;
    String error;
    private Handler hdlr;
    private int i, max;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        Intent intent = getIntent() ;
        loading = findViewById(R.id.loading);
        animation = new AnimationDrawable();
        animation.addFrame(getResources().getDrawable(R.drawable.ic_loading1),500);
        animation.addFrame(getResources().getDrawable(R.drawable.ic_loading2),500);
        loading.setImageDrawable(animation);
        animation.start();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setResult(RESULT_OK);
                finish();
            }
        }, 44000); //딜레이 타임 조절



    }
}