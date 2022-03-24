package com.mslog.health_record;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class health_record extends AppCompatActivity implements SurfaceHolder.Callback {
    CountDownTimer swingTimer, readyTimer;
    TextView time_text, ready_text;
    ProgressBar progressBar;
    TextToSpeech tts;
    public static Context context_main;
    int i = 0;
    final int READYTIME = 6000;
    final int TOTALTIME = 10000;
    final int COUNT_DOWN_INTERVAL = 1000;
    private Camera camera;
    private MediaRecorder mediaRecorder;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    String filepath;
    boolean finish = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_record);
        final MediaController mediaController =
                new MediaController(this);
        TedPermission.with(this)
                .setPermissionListener(permission)
                .setRationaleMessage("녹화를 위하여 권한을 허용해주세요.")
                .setDeniedMessage("권한이 거부되었습니다. 설정 > 권한에서 허용해주세요.")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .check();
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=android.speech.tts.TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        final ToggleButton tbtn = (ToggleButton) this.findViewById(R.id.recordBtn);
        tbtn.setOnClickListener(v -> {
            if (tbtn.isChecked()) { //화면 녹화 시작

                readyTimer();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        tbtn.setBackgroundDrawable(
                                getResources().getDrawable(R.drawable.ic_stop)
                        );
                        Toast.makeText(health_record.this, "녹화를 시작합니다.", Toast.LENGTH_SHORT).show();
                        String totalSpeak = "헬스 자세를 보여주세요.";
                        tts.setPitch(1.0f);
                        tts.setSpeechRate(1.0f);
                        tts.speak(totalSpeak, TextToSpeech.QUEUE_FLUSH, null);

                        try {
                            long now = System.currentTimeMillis();
                            Date mDate = new Date(now);
                            SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMdd_hhmmss");
                            String getTime = simpleDate.format(mDate);
                            filepath = "sdcard/DCIM/" + getTime + ".mp4";
                            mediaRecorder = new MediaRecorder();
                            camera.unlock();
                            mediaRecorder.setCamera(camera);
                            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
                            mediaRecorder.setOrientationHint(90);
                            mediaRecorder.setOutputFile(filepath);
                            mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
                            mediaRecorder.prepare();
                            mediaRecorder.start();

                        } catch (Exception e) {
                            e.printStackTrace();
                            mediaRecorder.release();
                        }
                    }
                },READYTIME);

            } else {//화면 녹화 중지
                finish = false;
                mediaRecorder.stop();
                Intent intent = new Intent();
                intent.putExtra("filepath", filepath);
                setResult(RESULT_OK,intent);
                finish();

            }
        });
    }

    PermissionListener permission = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            media();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(health_record.this, "권한 거부", Toast.LENGTH_SHORT).show();
        }
    };
    public  void media() {
        camera = android.hardware.Camera.open();
        camera.setDisplayOrientation(90);
        Camera.Parameters params = camera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(params);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(health_record.this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public  void readyTimer() {
        ready_text = (TextView) findViewById(R.id.readyBtn);
        ready_text.setText("5초");
        readyTimer = new CountDownTimer(READYTIME, COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                ready_text.setText(millisUntilFinished / 1000 + "초");
                if (millisUntilFinished / 1000 == 0){
                    ready_text.setText("시작!");
                }
            }

            @Override
            public void onFinish() {
                ready_text.setVisibility(View.INVISIBLE);
                swingTimer();
            }
        }.start();
    }
    public void swingTimer() {
        time_text = (TextView) findViewById(R.id.countDown);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setProgress(i);
        swingTimer = new CountDownTimer(TOTALTIME, COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                i++;
                time_text.setText(millisUntilFinished / 1000 + "초");
                progressBar.setProgress((int) i * 100 / (TOTALTIME / COUNT_DOWN_INTERVAL));
            }

            @Override
            public void onFinish() { //시간이 다 되면 자동 녹화종료 -> 화면 넘어감
                if(finish) {
                    String totalSpeak = "헬스 자세를 종료합니다.";
                    tts.setPitch(1.0f);
                    tts.setSpeechRate(1.0f);
                    tts.speak(totalSpeak, TextToSpeech.QUEUE_FLUSH, null);

                    Handler mHandler = new Handler();
                    mHandler.postDelayed(new Runnable()  {
                        public void run() {
                            mediaRecorder.stop();
                            tts.shutdown();
                            Intent intent = new Intent();
                            intent.putExtra("filepath", filepath);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }, 1200);
                }
            }
        }.start();
    }
    private void refreshCamera(android.hardware.Camera camera) {
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        try {
            camera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setCamera(camera);
    }
    private void setCamera(android.hardware.Camera cam) {

        camera = cam;

    }
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        refreshCamera(camera);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }
}