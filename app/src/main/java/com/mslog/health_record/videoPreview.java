package com.mslog.health_record;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class videoPreview extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "MainActivity";
    public static Context mContext;
    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonS3 s3;
    TransferUtility transferUtility;
    Button sendBtn, retryBtn;
    ImageButton playBtn;
    VideoView Videoview;
    File f;
    File f1;
    private String userChoosenTask;
    Uri VideoUri;
    String videopath, score, add_advice1, add_advice2, add_advice3, taway_advice, top_advice1, top_advice2, top_advice3, top_advice4, top_advice5, down_advice,
            imp_advice1, imp_advice2,imp_advice3, thu_advice1, thu_advice2, thu_advice3, thu_advice4, thu_advice5,down_advice2, worst, adressscore, takebackscore, topascore, iascore,
            dscore, truascore, fscore, finish_advice1, finish_advice2, finish_advice3;
    String error = "0";
    private Uri mImageUri;
    private int REQUEST_CAMERA;
    String imagename;
    private int SELECT_FILE;
    public static Context context_main;
    private static final String ACCESS_KEY = "AKIAV7WUXMYC2J5GO6ND";
    private static final String SECRET_KEY = "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";
    private AmazonS3 amazonS3;
    int num = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        sendBtn = (Button) findViewById(R.id.send);
        retryBtn = (Button) findViewById(R.id.retry);
        Videoview = (VideoView) findViewById(R.id.video_view);
        playBtn = (ImageButton) findViewById(R.id.playBtn);
        sendBtn.setOnClickListener(this);
        retryBtn.setOnClickListener(this);

        setClipToOutline(findViewById(R.id.video_view_container),true);
        AWSCredentials awsCredentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "ap-northeast-2:8c1ea4a3-6b45-4d03-a486-d7459dda3815", // 자격 증명 풀 ID
                Regions.AP_NORTHEAST_2 // 리전
        );
        s3 = new AmazonS3Client(credentialsProvider);
        transferUtility = new TransferUtility(s3, getApplicationContext());
        s3.setRegion(Region.getRegion(Regions.AP_NORTHEAST_2));
        s3.setEndpoint("s3.ap-northeast-2.amazonaws.com");

        transferUtility = new TransferUtility(s3, getApplicationContext());
        SELECT_FILE = getIntent().getIntExtra("PICTURE_CHOICE",5);
        REQUEST_CAMERA = getIntent().getIntExtra("REQUEST_CAMERA",5);
        if(SELECT_FILE == 3)
            galleryIntent();
        if(REQUEST_CAMERA == 2)
            cameraIntent();

    }


    public void setClipToOutline(View view, boolean clipToOutline) {
        view.setClipToOutline(clipToOutline);
        view.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
    }

    @Override
    public void onClick(View view) {
        String userid = ((login)login.context_main).userid;
        switch (view.getId()) {
            case R.id.send:
                double bytes = f.length();
                double kilobytes = (bytes/1024);
                double megabytes = (kilobytes/1024);
                if(megabytes>100){
                    Toast.makeText(this.getApplicationContext(),"파일 용량이 너무 큽니다.", Toast.LENGTH_SHORT).show();
                    break;
                }
                else {
                    JSONObject video = new JSONObject();
                    try {
                        video.put("subject", "video");
                        video.put("userid", userid);
                        video.put("URL", "https://golfapplication.s3.ap-northeast-2.amazonaws.com/" + userid + "/" + f.getName());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), video.toString());

                    postRequest(submain.postUrl, body);
                    TransferObserver observer = transferUtility.upload(
                            "healthapplication" + "/" + userid,
                            f.getName(),
                            f
                    );
                    imagename = f.getName().replace("mp4", "jpg");

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            for(int i = 0; i < 7; i++) {
                                String Strnum = Integer.toString(num);
                                f1 = new File("/sdcard/" + userid +"/image/" + imagename + Strnum + ".jpg");

                                TransferObserver observer1 = transferUtility.download(
                                        "healthapplication/" + userid + "/image",     /* The bucket to download from */
                                        f.getName() + '-' + Strnum + ".jpg",    /* The key for the object to download */
                                        f1       /* The file to download the object to */
                                );
                                num++;
                            }
                        }
                    }, 40000); //딜레이 타임 조절
                    Intent intent = new Intent(videoPreview.this,loading.class);
                    startActivityForResult(intent,123);
                    break;
                }

            case R.id.retry:
                selectImage();
                break;
        }
    }
    public void postRequest(String postUrl, RequestBody postBody) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure (Call call, IOException e){
                // Cancel the post on failure.
                call.cancel();
                Log.d("qweqwe", e.getMessage());

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }

            @Override
            public void onResponse (Call call,final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            error = jsonObject.getString("error");
                            adressscore = jsonObject.getString("adressscore");
                            takebackscore = jsonObject.getString("takebackscore");
                            topascore = jsonObject.getString("topascore");
                            dscore = jsonObject.getString("dscore");
                            iascore = jsonObject.getString("iascore");
                            truascore = jsonObject.getString("truascore");
                            fscore = jsonObject.getString("fscore");
                            score = jsonObject.getString("score");
                            add_advice1 = jsonObject.getString("add_advice1");
                            add_advice2 = jsonObject.getString("add_advice2");
                            add_advice3 = jsonObject.getString("add_advice3");
                            taway_advice = jsonObject.getString("taway_advice");
                            top_advice1 = jsonObject.getString("top_advice1");
                            top_advice2 = jsonObject.getString("top_advice2");
                            top_advice3 = jsonObject.getString("top_advice3");
                            top_advice4 = jsonObject.getString("top_advice4");
                            top_advice5 = jsonObject.getString("top_advice5");
                            down_advice = jsonObject.getString("down_advice");
                            down_advice2 = jsonObject.getString("down_advice2");
                            imp_advice1 = jsonObject.getString("imp_advice1");
                            imp_advice2 = jsonObject.getString("imp_advice2");
                            imp_advice3 = jsonObject.getString("imp_advice3");
                            thu_advice1 = jsonObject.getString("thu_advice1");
                            thu_advice2 = jsonObject.getString("thu_advice2");
                            thu_advice3 = jsonObject.getString("thu_advice3");
                            thu_advice4 = jsonObject.getString("thu_advice4");
                            thu_advice5 = jsonObject.getString("thu_advice5");
                            finish_advice1 = jsonObject.getString("finish_advice1");
                            finish_advice2 = jsonObject.getString("finish_advice2");
                            finish_advice3 = jsonObject.getString("finish_advice3");
                            worst = jsonObject.getString("worst");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

    }
    private void selectImage() {

        Log.d(TAG, "select Image");
        final CharSequence[] items = {"촬영하기", "사진 가져오기",
                "취소"};

        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setTitle("사진가져오기");
        builder1.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(getApplicationContext());

                if (items[item].equals("촬영하기")) {
                    userChoosenTask = "촬영하기";
                    if (result)
                        cameraIntent();


                } else if (items[item].equals("사진 가져오기")) {
                    userChoosenTask = "사진 가져오기";
                    if (result)
                        galleryIntent();

                } else if (items[item].equals("취소")) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog2 = builder1.create();
        builder1.show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals("촬영하기"))
                        cameraIntent();
                    else if (userChoosenTask.equals("사진 가져오기"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

    public void galleryIntent() {
        Log.d(TAG, "Gallery Intent");
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    public void cameraIntent() {
        startActivityForResult(new Intent(videoPreview.this, health_record.class), REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("zxczxc",error);
        if(error.equals("-1"))
        {
            Intent intent = new Intent(videoPreview.this,Error.class);
            startActivity(intent);
            finish();
        }
        else if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                Log.d(TAG, "onActivityResult, SELECT_FILE");
                onSelectFromGalleryResult(data, SELECT_FILE);
            }
            else if(requestCode == REQUEST_CAMERA)
            {

                Camera(data);

            }
            else if(requestCode == 123)
            {
                finish();
            }
        }
    }
    private void Camera(Intent data) {

        String filepath = data.getStringExtra("filepath");
        String filepath1 = "sdcard/DCIM/Camera/캐뤼.mp4";
        final MediaController mediaController =
                new MediaController(this);
        Uri cVideoUri = Uri.parse(filepath);
        f = new File(filepath);
        Videoview.setVideoURI(cVideoUri);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Videoview.setMediaController(mediaController);

                Videoview.start();

            }
        });
    }

    private void onSelectFromGalleryResult(Intent data, int imagetype) {
        final MediaController mediaController =
                new MediaController(this);
        Log.d(TAG, "onSelectFromGalleryResult");
        VideoUri = data.getData();
        if (Build.VERSION.SDK_INT < 11) {
            videopath = RealPathUtil.getRealPathFromURI_BelowAPI11(this, VideoUri);
            Log.d(TAG, Build.VERSION.SDK_INT + "");
        } else if (Build.VERSION.SDK_INT < 19) {
            Log.d(TAG, Build.VERSION.SDK_INT + "");
            videopath = RealPathUtil.getRealPathFromURI_API11to18(this, VideoUri);
        } else {
            Log.d(TAG, Build.VERSION.SDK_INT + "");
            videopath = RealPathUtil.getRealPathFromURI_API19(this, VideoUri);
        }
        f = new File(videopath);
        Videoview.setVideoURI(VideoUri);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Videoview.setMediaController(mediaController);

                Videoview.start();

            }
        });
    }
}