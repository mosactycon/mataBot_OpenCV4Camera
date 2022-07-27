package com.example.mataBot;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG="MainActivity";

    private Mat mRgba;
    private Mat mGray;
    private CameraBridgeViewBase mOpenCvCameraView;
    private ImageView flip_camera;
    private int mCameraId=0;
    private ImageView take_picture_button;
    private int take_image=0;
    private ImageView image_gallery_icon;
    private ImageView change_resolution_button;
    private ListView set_resolution;
    private int show_resolution_list=0;
    Camera mCamera;
    private MediaRecorder recorder;
    private ImageView video_camera_button;
    private int video_or_photo=0;
    private int take_video_or_not=0;
    private int mHeight=0;
    private int mWidth=0;


    private BaseLoaderCallback mLoaderCallback =new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface
                        .SUCCESS:{
                    Log.i(TAG,"OpenCv Is loaded");
                    mOpenCvCameraView.enableView();
                }
                default:
                {
                    super.onManagerConnected(status);

                }
                break;
            }
        }
    };

    public CameraActivity(){
        Log.i(TAG,"Instantiated new "+this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int MY_PERMISSIONS_REQUEST_CAMERA=0;
        // if camera permission is not given it will ask for it on device
        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        setContentView(R.layout.activity_camera);

        mOpenCvCameraView=(CameraBridgeViewBase) findViewById(R.id.frame_Surface);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.enableFpsMeter();

        flip_camera=findViewById(R.id.flip_camera);

        flip_camera.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    flip_camera.setColorFilter(Color.DKGRAY);
                    return true;
                }
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    flip_camera.setColorFilter(Color.WHITE);
                    swapCamera();
                    return true;
                }
                return false;
            }
        });



        image_gallery_icon=findViewById(R.id.image_gallery_icon);

        image_gallery_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CameraActivity.this,GalleryActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));

            }
        });

        image_gallery_icon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    image_gallery_icon.setColorFilter(Color.DKGRAY);
                    return true;
                }
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    image_gallery_icon.setColorFilter(Color.WHITE);
                    startActivity(new Intent(CameraActivity.this,GalleryActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    return true;
                }
                return false;
            }
        });

        mCamera= Camera.open();
        Camera.Parameters params=mCamera.getParameters();
        List<Camera.Size> sizes=params.getSupportedPreviewSizes();
        ArrayList<String> resolution_array_list=new ArrayList<>();
        for(int i=0;i<sizes.size();i++){
            int frameWidth=(int) sizes.get(i).width;
            int frameHeight=(int) sizes.get(i).height;
            String frameWidth_S=Integer.toString(frameWidth);
            String frameHeight_S=Integer.toString(frameHeight);

            resolution_array_list.add(frameWidth_S+"x"+frameHeight_S);
            Log.w("CameraActivity", frameWidth_S+"x"+frameHeight_S);

        }
        change_resolution_button=findViewById(R.id.change_resolution_button);
        set_resolution=findViewById(R.id.set_resolution);

        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(this,R.layout.resolution_item,R.id.textView,resolution_array_list);
        String null_array[]={};
        ArrayAdapter<String> null_array_adapter=new ArrayAdapter<>(this,R.layout.resolution_item,R.id.textView,null_array);
        set_resolution.setAdapter(null_array_adapter);


        change_resolution_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    change_resolution_button.setColorFilter(Color.DKGRAY);
                    return true;
                }
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    change_resolution_button.setColorFilter(Color.WHITE);
                    if(show_resolution_list==0){
                        set_resolution.setAdapter(arrayAdapter);
                        show_resolution_list=1;
                    }
                    else {
                        set_resolution.setAdapter(null_array_adapter);
                        show_resolution_list=0;
                    }
                    return true;
                }
                return false;
            }
        });

        set_resolution.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                int frameWidth=(int) sizes.get(position).width;
                int frameHeight=(int) sizes.get(position).height;

                mOpenCvCameraView.disableView();
                mOpenCvCameraView.setMaxFrameSize(frameWidth,frameHeight);
                mOpenCvCameraView.enableView();
                set_resolution.setAdapter(null_array_adapter);
                show_resolution_list=0;

            }
        });

        recorder=new MediaRecorder();
        video_camera_button=findViewById(R.id.video_camera_button);
        video_camera_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    video_camera_button.setColorFilter(Color.DKGRAY);
                    return true;
                }
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    video_camera_button.setColorFilter(Color.WHITE);

                    if(video_or_photo==0){
                        take_picture_button.setImageResource(R.drawable.circle_button);
                        take_picture_button.setColorFilter(Color.WHITE);
                        video_or_photo=1;
                    }
                    else{
                        take_picture_button.setImageResource(R.drawable.take_picture_icon);
                        video_or_photo=0;
                    }

                    return true;
                }
                return false;
            }
        });

        take_picture_button=findViewById(R.id.take_picture_button);
        take_picture_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    if(video_or_photo==0){
                        if(take_image==0){
                            take_picture_button.setColorFilter(Color.DKGRAY);
                        }
                    }
                    return true;
                }
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    if(video_or_photo==1){

                        if(take_video_or_not==0){
                            try{
                                File folder=new File(Environment.getExternalStorageDirectory().getPath()+"/mataBot");
                                boolean success=true;
                                if(!folder.exists()){
                                    success=folder.mkdirs();
                                }
                                take_picture_button.setImageResource(R.drawable.circle_button);
                                take_picture_button.setColorFilter(Color.RED);
                                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
                                CamcorderProfile camcorderProfile=CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                                recorder.setProfile(camcorderProfile);
                                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                                String current_date_and_time=sdf.format(new Date());
                                String filename=Environment.getExternalStorageDirectory().getPath()+"/mataBot/"+current_date_and_time+".mp4";
                                recorder.setOutputFile(filename);
                                recorder.setVideoSize(mHeight,mWidth);
                                recorder.prepare();
                                mOpenCvCameraView.setRecorder(recorder);
                                recorder.start();
                            }
                            catch (IOException e){
                                e.printStackTrace();
                            }
                            take_video_or_not=1;
                        }
                        else {
                            take_picture_button.setImageResource(R.drawable.circle_button);
                            take_picture_button.setColorFilter(Color.WHITE);
                            mOpenCvCameraView.setRecorder(null);
                            recorder.stop();
                            try{
                                Thread.sleep(1000);
                            }
                            catch (InterruptedException e){
                                throw new RuntimeException(e);
                            }
                            take_video_or_not=0;
                        }
                    }
                    else{
                        take_picture_button.setColorFilter(Color.WHITE);
                        if(take_image==0){
                            take_image=1;
                        }
                        else{
                            take_image=0;
                        }
                    }

                    return true;
                }
                return false;
            }
        });

    }

    private void swapCamera() {
        mCameraId=mCameraId^1;
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setCameraIndex(mCameraId);
        mOpenCvCameraView.enableView();
        mOpenCvCameraView.enableFpsMeter();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()){
            //if load success
            Log.d(TAG,"Opencv initialization is done");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            //if not loaded
            Log.d(TAG,"Opencv is not loaded. try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,mLoaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }

    }

    public void onCameraViewStarted(int width ,int height){
        mRgba=new Mat(height,width, CvType.CV_8UC4);
        mGray =new Mat(height,width,CvType.CV_8UC1);
    }
    public void onCameraViewStopped(){
        mRgba.release();
    }
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){

        mRgba=inputFrame.rgba();
        mGray=inputFrame.gray();

        if(mCameraId==1){
            Core.flip(mRgba,mRgba, 1);
            Core.flip(mGray,mGray, -1);

        }
        take_image=take_picture_button_rgb(take_image,mRgba);

        mHeight=mRgba.height();
        mWidth=mRgba.width();

        return mRgba;

    }

    private int take_picture_button_rgb(int take_image, Mat mRgba) {
        if(take_image==1){
            Mat save_mat=new Mat();
            Core.flip(mRgba.t(),save_mat,1);
            Imgproc.cvtColor(save_mat,save_mat,Imgproc.COLOR_RGBA2BGRA);
            File folder=new File(Environment.getExternalStorageDirectory().getPath()+"/mataBot");
            boolean success=true;
            if(!folder.exists()){
                success=folder.mkdirs();
            }
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String currentDateAndTime= sdf.format(new Date());
            String fileName=Environment.getExternalStorageDirectory().getPath()+"/mataBot/"+currentDateAndTime+".jpg";
            Imgcodecs.imwrite(fileName,save_mat);
            take_image=0;
        }
        return take_image;
    }

}