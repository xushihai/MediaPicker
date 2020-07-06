package com.hai.picker;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

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
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        Button button = new Button(this);
        button.setText("拍照");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GalleryFinal.setImageEngine(GalleryFinal.IMAGE_ENGINE_IMAGE_LOADER);
                GalleryFinal.setDefaultSelfie(false);
                GalleryFinal.initSaver(new EncryptSaver(MainActivity.this));
                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                if(Build.VERSION.SDK_INT>=29)
                    path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
                Log.e("拍摄",path);
                GalleryFinal.captureMedia(MainActivity.this, GalleryFinal.TYPE_ALL, path, new GalleryFinal.OnCaptureListener() {
                    @Override
                    public void onSelected(Photo photo) {
                        Log.e("拍摄", "拍摄完成：" + photo);
                    }
                });
            }
        });
        linearLayout.addView(button);

        button = new Button(this);
        button.setText("查看图片");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        linearLayout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GalleryFinal.setImageEngine(GalleryFinal.IMAGE_ENGINE_GLIDE);
                GalleryFinal.selectMedias(MainActivity.this, GalleryFinal.TYPE_ALL, 10, new GalleryFinal.OnSelectMediaListener() {
                    @Override
                    public void onSelected(ArrayList<Photo> photoArrayList) {

                    }
                });
            }
        });
        setContentView(linearLayout);
        EventBus.getDefault().register(this);


//        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                null, null, null, null);
//        ArrayList<Photo> photoArrayList = new ArrayList<>();
//        while (cursor.moveToNext()) {
//            Photo photo = new Photo();
//            photo.setAdddate(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED)));
//            photo.setHeight(cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT)));
//            photo.setWidth(cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH)));
//            photo.setId(cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID)));
//            photo.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)));
//            photo.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)));
//            photo.setMimetype(cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE)));
//            photoArrayList.add(photo);
//        }
//        cursor.close();
//        GalleryFinal.showMedias(this,photoArrayList);
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
