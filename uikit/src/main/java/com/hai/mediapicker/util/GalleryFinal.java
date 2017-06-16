package com.hai.mediapicker.util;

import android.content.Context;
import android.content.Intent;

import com.hai.mediapicker.activity.CaptureActivity;
import com.hai.mediapicker.activity.MediaPickerActivity;
import com.hai.mediapicker.entity.Photo;

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

    public static void selectMedias(Context context, int maxSum) {
        selectMedias(context, TYPE_ALL, maxSum);
    }


    /**
     * @param context
     * @param destnationPath 目录路径，非最终的媒体文件的路径
     */
    public static void captureMedia(Context context, String destnationPath) {
        captureMedia(context, destnationPath, -1);
    }

    public static void captureMedia(Context context, String destnationPath,OnCaptureListener onCaptureListener) {
        captureMedia(context, destnationPath, -1,onCaptureListener);
    }

    /**
     * @param context
     * @param destnationPath 目录路径，非最终的媒体文件的路径
     * @param maxDuration  单位：毫秒
     */
    public static void captureMedia(Context context, String destnationPath, int maxDuration) {
        captureMedia(context,destnationPath,maxDuration,null);
    }

    /**
     * @param context
     * @param destnationPath 目录路径，非最终的媒体文件的路径
     * @param maxDuration  单位：毫秒
     */
    public static void captureMedia(Context context, String destnationPath, int maxDuration,OnCaptureListener onCaptureListener) {
        mOnCaptureListener = onCaptureListener;
        Intent intent = new Intent(context, CaptureActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
