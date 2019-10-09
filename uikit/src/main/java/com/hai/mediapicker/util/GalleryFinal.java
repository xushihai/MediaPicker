package com.hai.mediapicker.util;

import android.content.Context;
import android.content.Intent;

import com.hai.mediapicker.activity.AlbumActivity;
import com.hai.mediapicker.activity.CaptureActivity;
import com.hai.mediapicker.activity.CaptureActivity2;
import com.hai.mediapicker.activity.MediaPickerActivity;
import com.hai.mediapicker.entity.Photo;
import com.hai.mediapicker.save.BaseSaver;
import com.hai.mediapicker.save.ISaver;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/3/16.
 */

public class GalleryFinal {
    public static OnSelectMediaListener mOnSelectMediaListener;
    public static OnCaptureListener mOnCaptureListener;
    public static final int TYPE_IMAGE = 1;//图片类型
    public static final int TYPE_VIDEO = 2;//视频类型
    public static final int TYPE_ALL = 3;//所有类型
    private static ISaver iSaver = new BaseSaver();

    private static boolean isSelfie = false;//默认是否进来就是自拍模式

    public static void initSaver(ISaver iSaver) {
        GalleryFinal.iSaver = iSaver;
    }

    public static ISaver getSaver() {
        return iSaver;
    }

    public static final int IMAGE_ENGINE_IMAGE_LOADER = 1;
    public static final int IMAGE_ENGINE_GLIDE = 2;
    private static int imageEngine = IMAGE_ENGINE_IMAGE_LOADER;

    public static void setImageEngine(int imageEngine) {
        if (imageEngine == IMAGE_ENGINE_IMAGE_LOADER) {
            GalleryFinal.imageEngine = imageEngine;
            return;
        }
        GalleryFinal.imageEngine = IMAGE_ENGINE_GLIDE;
    }

    public static int getImageEngine() {
        return imageEngine;
    }

    public static void selectMedias(Context context, int type, int maxSum, OnSelectMediaListener onSelectMediaListener) {
        mOnSelectMediaListener = onSelectMediaListener;
        Intent intent = new Intent(context, MediaPickerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("maxSum", maxSum);
        if (type != TYPE_ALL)
            intent.putExtra("type", type);
        context.startActivity(intent);
    }

    public static void selectMedias(Context context, int maxSum, OnSelectMediaListener onSelectMediaListener) {
        selectMedias(context, TYPE_ALL, maxSum, onSelectMediaListener);
    }

    public static void selectMedias(Context context, int type, int maxSum) {
        mOnSelectMediaListener = null;
        Intent intent = new Intent(context, MediaPickerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("maxSum", maxSum);
        if (type != TYPE_ALL)
            intent.putExtra("type", type);
        context.startActivity(intent);
    }

    /**
     * 自己提供数据供查看
     * @param context
     * @param maxSum
     * @param photoArrayList
     */
    public static void selectMedias(Context context, int maxSum, ArrayList<Photo> photoArrayList) {
        mOnSelectMediaListener = null;
        Intent intent = new Intent(context, AlbumActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("maxSum", maxSum);
        intent.putExtra(AlbumActivity.EXTREA_PHOTOS, photoArrayList);
        context.startActivity(intent);
    }

    public static void showMedias(Context context,  ArrayList<Photo> photoArrayList) {
        mOnSelectMediaListener = null;
        Intent intent = new Intent(context, AlbumActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AlbumActivity.EXTREA_PHOTOS, photoArrayList);
        intent.putExtra(AlbumActivity.EXTREA_SELECT_MODE, false);
        context.startActivity(intent);
    }


    public static void selectMedias(Context context, int maxSum) {
        selectMedias(context, TYPE_ALL, maxSum);
    }


    public static boolean isSelfie() {
        return isSelfie;
    }

    public static void setDefaultSelfie(boolean isSelfie) {
        GalleryFinal.isSelfie = isSelfie;
    }

    /**
     * @param context
     * @param destnationPath 目录路径，非最终的媒体文件的路径
     */
    public static void captureMedia(Context context, String destnationPath) {
        captureMedia(context, TYPE_ALL, destnationPath, -1);
    }

    public static void captureMedia(Context context, int type, String destnationPath) {
        captureMedia(context, type, destnationPath, -1);
    }

    public static void captureMedia(Context context, int type, String destnationPath, OnCaptureListener onCaptureListener) {
        captureMedia(context, type, destnationPath, -1, onCaptureListener);
    }

    /**
     * @param context
     * @param type
     * @param destnationPath
     * @param maxDuration
     */
    public static void captureMedia(Context context, int type, String destnationPath, int maxDuration) {
        captureMedia(context, type, destnationPath, maxDuration, null);
    }

    /**
     * @param context
     * @param type:TYPE_IMAGE,TYPE_ALL 只能选择图片或者图片和视频都可以的
     * @param destnationPath
     * @param maxDuration
     * @param onCaptureListener
     */
    public static void captureMedia(Context context, int type, String destnationPath, int maxDuration, OnCaptureListener onCaptureListener) {
        mOnCaptureListener = onCaptureListener;
        Intent intent = new Intent(context, CaptureActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (type != TYPE_IMAGE)
            type = TYPE_ALL;
        intent.putExtra("type", type);
        intent.putExtra("destnationPath", destnationPath);
        if (maxDuration > 0)
            intent.putExtra("maxDuration", maxDuration);
        context.startActivity(intent);
    }

    public interface OnSelectMediaListener {
        void onSelected(ArrayList<Photo> photoArrayList);
    }

    public interface OnCaptureListener {
        void onSelected(Photo photo);
    }
}
