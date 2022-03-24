package com.mslog.health_record;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

public class submain extends AppCompatActivity {
    private Button loginBtn, joinBtn,findPwd;
    public static Context mainActivityContext;
    static String postUrl = "http://192.168.0.184:5000/db";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submain);
        TedPermission.with(this)
                .setPermissionListener(permission)
                .setDeniedMessage("권한이 거부되었습니다. 설정 > 권한에서 허용해주세요.")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .check();
        loginBtn = (Button)findViewById(R.id.loginBtn);
        joinBtn = (Button)findViewById(R.id.joinBtn);

        ActivityCompat.requestPermissions(submain.this, new String[]{Manifest.permission.INTERNET}, 0);
        mainActivityContext = this;
        loginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(submain.this,login.class);
                startActivity(intent);
                finish();
            }

        });

        joinBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(submain.this,join.class);
                startActivity(intent);
                finish();
            }
        });

    }
    PermissionListener permission = new PermissionListener() {
        @Override
        public void onPermissionGranted() {

        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(submain.this, "권한 거부", Toast.LENGTH_SHORT).show();
        }
    };
}