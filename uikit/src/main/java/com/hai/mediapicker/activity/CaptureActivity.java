package com.hai.mediapicker.activity;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/6/12.
 */

public class CaptureActivity extends AppCompatActivity implements View.OnClickListener {
    android.hardware.Camera camera;
    SurfaceView surfaceView;
    int facing = Camera.CameraInfo.CAMERA_FACING_BACK, cameraId;
    String destnationPath;
    RelativeLayout rlStart, rlDecide;
    byte[] data;
    MediaRecorder mMediaRecorder;
    String videoPath;
    Point bestSize;
    MediaPlayer mediaplayer;
    View viewBig, viewSmall;
    long start;
    int maxDuration = 10 * 1000;//最多只能录视频的时间长度
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
        initUi();
        destnationPath = getIntent().getStringExtra("destnationPath");
        maxDuration = getIntent().getIntExtra("maxDuration", 10 * 1000);
    }

    private void initUi() {
        WindowManager.LayoutParams winParams = getWindow().getAttributes();
        winParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        winParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        getWindow().setAttributes(winParams);

        setContentView(R.layout.activity_capture);
        surfaceView = (SurfaceView) findViewById(R.id.surface_capture);
        surfaceView.setFocusableInTouchMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            surfaceView.setSecure(true);
        }
        rlDecide = (RelativeLayout) findViewById(R.id.rl_decide);
        rlStart = (RelativeLayout) findViewById(R.id.rl_start);
        viewSmall = findViewById(R.id.view_small);
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
        rlStart.setOnClickListener(this);


        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera != null)
                    camera.autoFocus(null);
            }
        });

        rlStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e("拍摄", event.getAction() + "");
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        start = System.currentTimeMillis();
                        handler.postDelayed(captureVideo, 300);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
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

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, final int width, final int height) {
                if (holder.getSurface() == null)
                    return;

            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                holder.setKeepScreenOn(true);
                holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                startPreview(-1);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                stopPreview();
            }
        });

        checkPermission();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
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
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        startPreview(-1);
    }

    public void startPreview(int _cameraId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (_cameraId == -1) {
            int numberOfCameras = Camera.getNumberOfCameras();
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    facing = cameraInfo.facing;
                    cameraId = i;
                    break;
                }
            }
        } else
            cameraId = _cameraId;
        try {
            camera = android.hardware.Camera.open(cameraId);
            camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(surfaceView.getHolder());
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            android.hardware.Camera.Parameters parameters = camera.getParameters();
            bestSize = getBestCameraResolution(parameters, new Point(displayMetrics.widthPixels, displayMetrics.heightPixels));

            Point bestPictureSize = getBestCameraResolution(parameters.getSupportedPictureSizes(), new Point(displayMetrics.widthPixels, displayMetrics.heightPixels));

            parameters.setPreviewSize(bestSize.x, bestSize.y);
            parameters.setPictureSize(bestPictureSize.x, bestPictureSize.y);
            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO))
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            camera.setParameters(parameters);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
            if(camera!=null){
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        }
        if (camera == null) {
            new AlertDialog.Builder(CaptureActivity.this)
                    .setTitle("提示")
                    .setCancelable(false)
                    .setMessage("无法获取摄像头数据，请在手机应用权限管理中打开摄像头权限。")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .create().show();
            return;
        }
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
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing != facing) {
                stopPreview();
                startPreview(i);
                facing = cameraInfo.facing;
                cameraId = i;
                break;
            }
        }
    }

    private Point getBestCameraResolution(Camera.Parameters parameters, Point screenResolution) {
        float tmp = 0f;
        float mindiff = 100f;
        float x_d_y = (float) screenResolution.x / (float) screenResolution.y;
        android.hardware.Camera.Size best = null;
        List<android.hardware.Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();

        for (android.hardware.Camera.Size s : supportedPreviewSizes) {
            tmp = Math.abs(((float) s.height / (float) s.width) - x_d_y);
            if (tmp < mindiff) {
                mindiff = tmp;
                best = s;
            }
        }
        return new Point(best.width, best.height);
    }

    private Point getBestCameraResolution(List<android.hardware.Camera.Size> supportedSizes, Point screenResolution) {
        float tmp = 0f;
        float mindiff = 100f;
        float x_d_y = (float) screenResolution.x / (float) screenResolution.y;
        android.hardware.Camera.Size best = null;
        for (android.hardware.Camera.Size s : supportedSizes) {
            if (s.width == 1280 && s.height == 720)
                return new Point(best.width, best.height);
            tmp = Math.abs(((float) s.height / (float) s.width) - x_d_y);
            if (tmp < mindiff) {
                mindiff = tmp;
                best = s;
            }
        }
        return new Point(best.width, best.height);
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
            startPreview(cameraId);

            clearVideoFile();
        } else if (v.getId() == R.id.iv_ok) {
            if (data != null)
                savePictureAsync(data, camera);
            else
                saveVideo();
        }
    }

    private void takePicture() {
        if (camera == null)
            return;
        try {
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    CaptureActivity.this.data = data;
                    stopPreview();
                    showDecide();
                }
            });
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
        photo.setSize(new File(videoPath).length());
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
    }

    public void showDecide() {
        rlDecide.setVisibility(View.VISIBLE);
        rlStart.setVisibility(View.GONE);
        findViewById(R.id.iv_switch).setVisibility(View.GONE);
    }

    public void savePictureAsync(final byte[] data, final Camera camera) {
        new AsyncTask<Void, Void, Photo>() {

            @Override
            protected Photo doInBackground(Void... params) {
                try {
                    return savePicture(data, camera);
                } catch (IOException e) {
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
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Matrix matrix = new Matrix();
        matrix.postRotate(getRotateDegree(facing));
        Bitmap rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        bitmap.recycle();
        Photo photo = new Photo();
        photo.setWidth(rotateBitmap.getWidth());
        photo.setHeight(rotateBitmap.getHeight());
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
        rotateBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
        try {
            outStream.flush();
            outStream.close();
            rotateBitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }
        photo.setPath(file.getAbsolutePath());
        photo.setSize(file.length());
        return photo;
    }

    /**
     * 保存图片的时候，后置摄像头需要旋转90度，前置摄像头需要旋转270度
     *
     * @param cameraId
     * @return
     */
    private int getRotateDegree(int cameraId) {
        return cameraId == Camera.CameraInfo.CAMERA_FACING_BACK ? 90 : 270;
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
            camera.unlock();
            this.mMediaRecorder.setCamera(camera);
            this.mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            this.mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            this.mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            CamcorderProfile localObject = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
            File dir = new File(destnationPath);
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(dir, createVideoName());
            if (!file.exists())
                file.createNewFile();
            videoPath = file.getAbsolutePath();
            this.mMediaRecorder.setOutputFile(videoPath);

            this.mMediaRecorder.setVideoSize(bestSize.x, bestSize.y);
            this.mMediaRecorder.setAudioEncodingBitRate(44100);
            if (((CamcorderProfile) localObject).videoBitRate > 2097152)
                this.mMediaRecorder.setVideoEncodingBitRate(2097152);
            else
                this.mMediaRecorder.setVideoEncodingBitRate(((CamcorderProfile) localObject).videoBitRate);
            // this.mMediaRecorder.setVideoFrameRate(((CamcorderProfile) localObject).videoFrameRate);
            this.mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            this.mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            this.mMediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());

            mMediaRecorder.setOrientationHint(facing == Camera.CameraInfo.CAMERA_FACING_BACK ? 90 : 270);
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
        try {
            mediaplayer = new MediaPlayer();
            mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaplayer.setDataSource(this, Uri.fromFile(new File(path)));
            mediaplayer.setDisplay(surfaceView.getHolder());
            mediaplayer.setLooping(true);
            mediaplayer.prepare();
            mediaplayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopVideo() {
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
