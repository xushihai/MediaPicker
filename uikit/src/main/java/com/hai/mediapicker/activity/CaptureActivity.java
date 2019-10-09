package com.hai.mediapicker.activity;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.cameraview.Camera1;
import com.google.android.cameraview.CameraView;
import com.hai.mediapicker.R;
import com.hai.mediapicker.entity.Photo;
import com.hai.mediapicker.util.GalleryFinal;
import com.hai.mediapicker.util.MemoryLeakUtil;
import com.hai.mediapicker.view.RingProgress;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.hai.mediapicker.util.GalleryFinal.TYPE_IMAGE;

/**
 * Created by Administrator on 2017/6/12.
 */

public class CaptureActivity extends AppCompatActivity implements View.OnClickListener {
    android.hardware.Camera camera;
    SurfaceView surfaceView;
    CameraView cameraView;
    int facing = Camera.CameraInfo.CAMERA_FACING_BACK, cameraId;
    String destnationPath;
    RelativeLayout rlStart, rlDecide;
    byte[] data;
    MediaRecorder mMediaRecorder;
    String videoPath;
    Point bestSize;
    MediaPlayer mediaplayer;
    View viewBig, viewSmall, viewSave;
    long start;
    int maxDuration = 10 * 1000;//最多只能录视频的时间长度
    int type;//拍摄类型
    RingProgress ringProgress;
    CountDownHandler countDownHandler;
    Handler handler = new Handler();
    Runnable captureVideo = new Runnable() {
        @Override
        public void run() {
            Log.e(CaptureActivity.class.getSimpleName(), "开始算为长按，开始拍视频");
            enlarge();
            prepareVideoRecorder();
            countDownHandler.start();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getIntent().getIntExtra("type", GalleryFinal.TYPE_ALL);
        destnationPath = getIntent().getStringExtra("destnationPath");
        maxDuration = getIntent().getIntExtra("maxDuration", 10 * 1000);
        initUi();
    }

    private void initUi() {
        WindowManager.LayoutParams winParams = getWindow().getAttributes();
        winParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        winParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        getWindow().setAttributes(winParams);

        setContentView(R.layout.activity_capture);
        cameraView = (CameraView) findViewById(R.id.camera_view);
        cameraView.setFocusableInTouchMode(true);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            cameraView.setSecure(true);
//        }
        surfaceView = (SurfaceView) findViewById(R.id.surface_capture);
        rlDecide = (RelativeLayout) findViewById(R.id.rl_decide);
        rlStart = (RelativeLayout) findViewById(R.id.rl_start);
        viewSmall = findViewById(R.id.view_small);
        viewSave = findViewById(R.id.iv_save);
        viewBig = findViewById(R.id.view_big);
        ringProgress = (RingProgress) findViewById(R.id.ring_progress);
        countDownHandler = new CountDownHandler(ringProgress, maxDuration, new Runnable() {
            @Override
            public void run() {
                MotionEvent simulateUpEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
                dispatchTouchEvent(simulateUpEvent);//最大时间限制到了，模拟用户松开手指
            }
        });
        findViewById(R.id.iv_cancel).setOnClickListener(this);
        findViewById(R.id.iv_ok).setOnClickListener(this);
        viewSave.setOnClickListener(this);
        rlStart.setOnClickListener(this);


//        cameraView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (camera != null)
//                    camera.autoFocus(null);
//            }
//        });

        cameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                focusOnTouch((int) event.getX(), (int) event.getY());
                return false;
            }
        });

        rlStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        start = System.currentTimeMillis();
                        if (type != TYPE_IMAGE)
                            handler.postDelayed(captureVideo, 300);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        if (type != TYPE_IMAGE)
                            handler.removeCallbacks(captureVideo);
                        if (mMediaRecorder == null) {
                            break;
                        }
                        long gap = System.currentTimeMillis() - start - 500;
                        int delay = gap > 1500 ? 0 : 1500;
                        countDownHandler.stop();
                        recover();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mMediaRecorder != null) {
                                    stopRecord();
                                }
                            }
                        }, delay);
                        break;
                }
                return (System.currentTimeMillis() - start) > 500;
            }
        });

        cameraView.addCallback(new CameraView.Callback() {

            @Override
            public void onPictureTaken(CameraView cameraView, byte[] data) {
                super.onPictureTaken(cameraView, data);
                CaptureActivity.this.data = data;
                //stopPreview();
                showDecide();

            }
        });
        camera = ((Camera1) cameraView.getImpl()).getCamera();

        facing = GalleryFinal.isSelfie() ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
        cameraView.setFacing(facing != CameraView.FACING_FRONT ? CameraView.FACING_BACK : CameraView.FACING_FRONT);
        checkPermission();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startPreview();
            return;
        }
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        ArrayList<String> needRequestPermission = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                needRequestPermission.add(permissions[i]);
            }
        }
        if (!needRequestPermission.isEmpty()) {
            requestPermissions(needRequestPermission.toArray(new String[needRequestPermission.size()]), 11);
        } else {
            startPreview();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        startPreview();
    }


    protected void focusOnRect(Rect rect) {
        if (camera != null) {
            try {
                Camera.Parameters parameters = camera.getParameters(); // 先获取当前相机的参数配置对象
                if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO))
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO); // 设置聚焦模式

                if (parameters.getMaxNumFocusAreas() > 0) {
                    List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
                    focusAreas.add(new Camera.Area(rect, 1000));
                    parameters.setFocusAreas(focusAreas);
                }
                camera.cancelAutoFocus(); // 先要取消掉进程中所有的聚焦功能
                camera.setParameters(parameters); // 一定要记得把相应参数设置给相机
                camera.autoFocus(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void focusOnTouch(int x, int y) {
        Rect rect = new Rect(x - 100, y - 100, x + 100, y + 100);
        int left = rect.left * 2000 / cameraView.getWidth() - 1000;
        int top = rect.top * 2000 / cameraView.getHeight() - 1000;
        int right = rect.right * 2000 / cameraView.getWidth() - 1000;
        int bottom = rect.bottom * 2000 / cameraView.getHeight() - 1000;
        // 如果超出了(-1000,1000)到(1000, 1000)的范围，则会导致相机崩溃
        left = left < -1000 ? -1000 : left;
        top = top < -1000 ? -1000 : top;
        right = right > 1000 ? 1000 : right;
        bottom = bottom > 1000 ? 1000 : bottom;
        focusOnRect(new Rect(left, top, right, bottom));
    }

    public void startPreview() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        cameraView.start();
        camera = ((Camera1) cameraView.getImpl()).getCamera();
        bestSize = new Point(camera.getParameters().getPictureSize().width,camera.getParameters().getPictureSize().height);
    }

    public void stopPreview() {
        if (camera == null)
            return;
        camera.stopPreview();
        camera.setPreviewCallback(null);
        camera.release();
        camera = null;
    }

    /**
     * 切换摄像头
     *
     * @param view
     */
    public void switchCamera(View view) {
        int facing = cameraView.getFacing();
        facing =facing == CameraView.FACING_FRONT ? CameraView.FACING_BACK : CameraView.FACING_FRONT;
        cameraView.setFacing(facing);
        camera = ((Camera1) cameraView.getImpl()).getCamera();
        bestSize = new Point(camera.getParameters().getPictureSize().width,camera.getParameters().getPictureSize().height);
    }


    private void enlarge() {
        ObjectAnimator smallScaleXAnimator = ObjectAnimator.ofFloat(viewSmall, "scaleX", 0.5f);
        ObjectAnimator smallScaleYAnimator = ObjectAnimator.ofFloat(viewSmall, "scaleY", 0.5f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(smallScaleXAnimator, smallScaleYAnimator);
        animatorSet.setDuration(400);
        animatorSet.start();

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewBig.getLayoutParams();
        layoutParams.height = getResources().getDimensionPixelSize(R.dimen.circle_large_size);
        layoutParams.width = layoutParams.height;
        viewBig.setLayoutParams(layoutParams);
    }

    private void recover() {
        ObjectAnimator smallScaleXAnimator = ObjectAnimator.ofFloat(viewSmall, "scaleX", 1);
        ObjectAnimator smallScaleYAnimator = ObjectAnimator.ofFloat(viewSmall, "scaleY", 1);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(smallScaleXAnimator, smallScaleYAnimator);
        animatorSet.setDuration(getResources().getInteger(R.integer.anim_duration));
        animatorSet.start();

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewBig.getLayoutParams();
        layoutParams.height = getResources().getDimensionPixelSize(R.dimen.circle_normal_size);
        layoutParams.width = layoutParams.height;
        viewBig.setLayoutParams(layoutParams);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        captureVideo = null;
        GalleryFinal.mOnCaptureListener = null;
        stopPreview();//之前退出去的时候没有释放camera，导致切换过摄像头并退出后再进来就会导致打开摄像头失败
        MemoryLeakUtil.fixInputMethodManagerLeak(this);
    }

    private void clearVideoFile() {
        if (videoPath == null)
            return;
        File file = new File(videoPath);
        if (file.exists())
            file.delete();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        justStopRecord();
        stopVideo();
        clearVideoFile();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.rl_start) {
            takePicture();
        } else if (v.getId() == R.id.iv_cancel) {
            data = null;
            showStart();
            stopVideo();
            startPreview();

            clearVideoFile();
        } else if (v.getId() == R.id.iv_ok) {
            if (data != null)
                savePictureAsync(data, camera);
            else
                saveVideo();
        } else if (v.getId() == R.id.iv_save) {
            saveMedia();
        }
    }


    private void saveMedia() {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                String file = videoPath;
                if (data != null) {
                    try {
                        Photo photo = savePicture(data, camera);
                        file = photo.getPath();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return GalleryFinal.getSaver().save(file);
            }

            @Override
            protected void onPostExecute(Boolean bool) {
                super.onPostExecute(bool);
                Toast.makeText(getApplicationContext(), getString(R.string.toast_save), Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    private void takePicture() {
        try {
            cameraView.takePicture();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveVideo() {
        int duration = mediaplayer != null ? mediaplayer.getDuration() : 0;
        stopVideo();
        Photo photo = new Photo();
        photo.setWidth(bestSize.x);
        photo.setHeight(bestSize.y);
        photo.setFullImage(false);
        photo.setMimetype("video/mp4");
        photo.setPath(videoPath);
        long length = new File(videoPath).exists() ? new File(videoPath).length() : 0;
        photo.setSize(length);
        photo.setDuration(duration);
        send(photo);
        finish();
    }

    private void send(Photo photo) {
        if (GalleryFinal.mOnCaptureListener != null)
            GalleryFinal.mOnCaptureListener.onSelected(photo);
        EventBus.getDefault().post(photo);
    }

    private void showStart() {
        rlDecide.setVisibility(View.GONE);
        rlStart.setVisibility(View.VISIBLE);
        findViewById(R.id.iv_switch).setVisibility(View.VISIBLE);
        viewSave.setVisibility(View.INVISIBLE);
    }

    public void showDecide() {
        rlDecide.setVisibility(View.VISIBLE);
        rlStart.setVisibility(View.GONE);
        findViewById(R.id.iv_switch).setVisibility(View.GONE);
        viewSave.setVisibility(View.VISIBLE);
    }

    public void savePictureAsync(final byte[] data, final Camera camera) {
        new AsyncTask<Void, Void, Photo>() {

            @Override
            protected Photo doInBackground(Void... params) {
                try {
                    return savePicture(data, camera);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Photo photo) {
                super.onPostExecute(photo);
                CaptureActivity.this.data = null;
                if (photo != null) {
                    send(photo);
                } else
                    Toast.makeText(CaptureActivity.this, "保存照片失败", Toast.LENGTH_SHORT).show();
                finish();
            }
        }.execute();
    }

    /**
     * 保存照片
     *
     * @param data
     * @param camera
     */
    private Photo savePicture(byte[] data, Camera camera) throws IOException {
        Camera.Size pictureSize = camera.getParameters().getPictureSize();
        Photo photo = new Photo();
        photo.setWidth(pictureSize.width);
        photo.setHeight(pictureSize.height);
        photo.setFullImage(false);
        photo.setMimetype("image/jpeg");

        FileOutputStream outStream = null;
        File dir = new File(destnationPath);
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(dir, createPictureName());
        if (!file.exists())
            file.createNewFile();
        outStream = new FileOutputStream(file, false);
        try {
            outStream.write(data);
            outStream.flush();
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        photo.setPath(file.getAbsolutePath());
        photo.setSize(file.length());
        return photo;
    }


    private String createPictureName() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMDD_hhmmss");
        return "IMG_" + simpleDateFormat.format(new Date()) + ".jpg";
    }


    private boolean prepareVideoRecorder() {
        findViewById(R.id.iv_switch).setVisibility(View.GONE);
        try {
            if (mMediaRecorder != null)
                mMediaRecorder.release();
            this.mMediaRecorder = new MediaRecorder();
            this.mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    Log.e("录视频", "错误码：" + what);
                }
            });

            Camera1 camera1 = (Camera1) cameraView.getImpl();
            Camera camera = camera1.getCamera();
            camera.unlock();

            this.mMediaRecorder.setCamera(camera);
            this.mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);


            this.mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            this.mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            CamcorderProfile localObject = null;

            int[] camcorderQuality = {CamcorderProfile.QUALITY_1080P, CamcorderProfile.QUALITY_720P, CamcorderProfile.QUALITY_480P, CamcorderProfile.QUALITY_LOW};
            for (int quality :
                    camcorderQuality) {
                if (CamcorderProfile.hasProfile(camera1.getCameraId(), quality)) {
                    localObject = CamcorderProfile.get(quality);
                    break;
                }
            }

            if (localObject == null) {
                return false;
            }

            File dir = new File(destnationPath);
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(dir, createVideoName());
            if (!file.exists())
                file.createNewFile();
            videoPath = file.getAbsolutePath();
            this.mMediaRecorder.setOutputFile(videoPath);

            this.mMediaRecorder.setVideoSize(1920, 1080);
            this.mMediaRecorder.setAudioEncodingBitRate(44100);
            if (((CamcorderProfile) localObject).videoBitRate > 2097152)
                this.mMediaRecorder.setVideoEncodingBitRate(2097152);
            else
                this.mMediaRecorder.setVideoEncodingBitRate(((CamcorderProfile) localObject).videoBitRate);
            // this.mMediaRecorder.setVideoFrameRate(((CamcorderProfile) localObject).videoFrameRate);
            this.mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            this.mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            this.mMediaRecorder.setPreviewDisplay(camera1.getSurface());

            mMediaRecorder.setOrientationHint(camera1.calcCameraRotation(getWindowManager().getDefaultDisplay().getRotation()));
            this.mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (Exception localException) {
            (localException).printStackTrace();
            return false;
        }
        return true;
    }


    private void stopRecord() {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.release(); // release the recorder object
            }
            mMediaRecorder = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopPreview();
        showDecide();
        playVideo(videoPath);
    }


    private void justStopRecord() {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.release(); // release the recorder object
            }
            mMediaRecorder = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopPreview();
    }

    private void playVideo(String path) {
        if (path == null)
            return;
        try {
            surfaceView.setVisibility(View.VISIBLE);
            mediaplayer = new MediaPlayer();
            mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaplayer.setDataSource(this, Uri.fromFile(new File(path)));
            mediaplayer.setDisplay(surfaceView.getHolder());
//            mediaplayer.setSurface(((Camera1)cameraView.getImpl()).getSurface());
            mediaplayer.setLooping(true);
            mediaplayer.prepare();
            mediaplayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopVideo() {
        surfaceView.setVisibility(View.INVISIBLE);
        if (mediaplayer == null) {
            return;
        }
        try {
            mediaplayer.stop();
            mediaplayer.release();
            mediaplayer = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createVideoName() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMDD_hhmmss");
        return "VID_" + simpleDateFormat.format(new Date()) + ".mp4";
    }

    public static class CountDownHandler extends Handler {
        RingProgress ringProgress;
        int maxDuration;
        long startTime;
        Runnable runnable;
        Timer timer;
        public static final int MSG_RESET = 1;
        public static final int MSG_PREOGRESS = 2;

        public CountDownHandler(RingProgress ringProgress, int maxDuration, Runnable runnable) {
            this.ringProgress = ringProgress;
            this.maxDuration = maxDuration;
            startTime = System.currentTimeMillis();
            this.ringProgress.setMax(maxDuration);
            this.runnable = runnable;


        }

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case MSG_RESET:
                    ringProgress.setProgress(0);
                    break;
                case MSG_PREOGRESS:
                    int progress = (int) (System.currentTimeMillis() - startTime);
                    if (progress > maxDuration) {
                        progress = maxDuration;
                        runnable.run();
                    }
                    ringProgress.setProgress(progress);
                    break;
            }
        }

        public void start() {
            startTime = System.currentTimeMillis();
            sendEmptyMessage(MSG_RESET);
            timer = new Timer();
            timer.schedule(new CountDownTask(this), 0, 40);
        }

        public void stop() {
            timer.cancel();
            startTime = System.currentTimeMillis();
            sendEmptyMessage(MSG_RESET);
        }

        public void updateProgress() {
            sendEmptyMessage(MSG_PREOGRESS);
        }
    }

    public static class CountDownTask extends TimerTask {
        CountDownHandler countDownHandler;

        public CountDownTask(CountDownHandler countDownHandler) {
            this.countDownHandler = countDownHandler;
        }

        @Override
        public void run() {
            countDownHandler.updateProgress();
        }
    }
}
