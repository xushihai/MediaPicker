package com.hai.picker;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.hai.mediapicker.entity.Photo;
import com.hai.mediapicker.util.GalleryFinal;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        GalleryFinal.selectMedias(this, 10);
        EventBus.getDefault().register(this);

//        GalleryFinal.selectMedias(this, GalleryFinal.TYPE_IMAGE, 10, new GalleryFinal.OnSelectMediaListener() {
//            @Override
//            public void onSelected(ArrayList<Photo> photoArrayList) {
//
//            }
//        });

        GalleryFinal.captureMedia(this, Environment.getExternalStorageDirectory().getAbsolutePath());
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void sendMedia(Photo photo) {
        Log.e("多媒体", photo.toString());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void sendMedia(ArrayList<Photo> photoList) {
        Log.e("多媒体", photoList.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        System.gc();
    }


}
