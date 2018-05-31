package com.hai.picker;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.hai.mediapicker.entity.Photo;
import com.hai.mediapicker.save.ISaver;
import com.hai.mediapicker.util.GalleryFinal;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;

public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        GalleryFinal.selectMedias(this, 10);
        EventBus.getDefault().register(this);

        GalleryFinal.selectMedias(this, GalleryFinal.TYPE_ALL, 10, new GalleryFinal.OnSelectMediaListener() {
            @Override
            public void onSelected(ArrayList<Photo> photoArrayList) {

            }
        });

//        GalleryFinal.initSaver(new EncryptSaver(this));
//        GalleryFinal.captureMedia(this, Environment.getExternalStorageDirectory().getAbsolutePath(), new GalleryFinal.OnCaptureListener() {
//            @Override
//            public void onSelected(Photo photo) {
//                Log.e("拍摄", "拍摄完成：" + photo);
//            }
//        });
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
        //MemoryLeakUtil.fixInputMethodManagerLeak(this);
        GalleryFinal.mOnSelectMediaListener = null;
        System.gc();
    }


    private static class EncryptSaver implements ISaver {
        File dir;

        public EncryptSaver(Context context) {
            dir = new File(context.getFilesDir(), "camera");
            dir = context.getExternalFilesDir("camera");
            if (!dir.exists())
                dir.mkdir();
            Log.e("文件", dir.getAbsolutePath());
        }

        @Override
        public boolean save(String previousFile) {
            try {
                File previousFiles = new File(previousFile);
                File file = new File(dir, previousFiles.getName());
                Cipher cipher = Cipher.getInstance("DES");
                KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
                keyGenerator.init(64, new SecureRandom("dnp123fggfhht".getBytes()));
                cipher.init(Cipher.ENCRYPT_MODE, keyGenerator.generateKey());
                CipherOutputStream cipherOutputStream = new CipherOutputStream(new FileOutputStream(file), cipher);
                int len = -1;
                byte[] buffer = new byte[4986];
                FileInputStream fileInputStream = new FileInputStream(previousFiles);
                while ((len = fileInputStream.read(buffer)) != -1) {
                    cipherOutputStream.write(buffer, 0, len);
                }
                fileInputStream.close();
                cipherOutputStream.flush();
                cipherOutputStream.close();
                previousFiles.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

    }

}
