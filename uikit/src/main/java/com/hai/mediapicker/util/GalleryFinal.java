package com.hai.mediapicker.util;

import android.content.Context;
import android.content.Intent;

import com.hai.mediapicker.activity.MediaPickerActivity;
import com.hai.mediapicker.entity.Photo;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/3/16.
 */

public class GalleryFinal {
    static OnSelectMediaListener mOnSelectMediaListener;
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

    public interface OnSelectMediaListener {
        void onSelected(ArrayList<Photo> photoArrayList);
    }
}
