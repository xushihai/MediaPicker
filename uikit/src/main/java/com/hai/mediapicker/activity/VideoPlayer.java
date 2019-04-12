package com.hai.mediapicker.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.hai.mediapicker.R;
import com.hai.mediapicker.entity.Photo;
import com.hai.mediapicker.util.MemoryLeakUtil;

import java.io.IOException;

/**
 * Created by Administrator on 2018/5/7.
 */

public class VideoPlayer extends AppCompatActivity implements SurfaceHolder.Callback {

    public static void playVideo(Context context, Photo video) {
        Intent intent = new Intent(context, VideoPlayer.class);
        intent.putExtra("video", video);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    MediaPlayer mediaPlayer;
    Photo video;
    ImageView ivPlay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        WindowManager.LayoutParams winParams = getWindow().getAttributes();
        winParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        winParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        getWindow().setAttributes(winParams);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);


        ivPlay = (ImageView) findViewById(R.id.iv_play);
        ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                mediaPlayer.start();
            }
        });
        video = (Photo) getIntent().getSerializableExtra("video");
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(video.getPath());
            SurfaceHolder surfaceHolder = ((SurfaceView) findViewById(R.id.surface_video)).getHolder();
            surfaceHolder.addCallback(this);
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Toast.makeText(VideoPlayer.this, getString(R.string.total_play_error), Toast.LENGTH_LONG).show();
                    return false;
                }
            });
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (mp.getVideoWidth() > mp.getVideoHeight()) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                    mp.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    ivPlay.setVisibility(View.VISIBLE);
                }
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mediaPlayer.setDisplay(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            MemoryLeakUtil.fixInputMethodManagerLeak(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
