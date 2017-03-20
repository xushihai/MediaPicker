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

    public static void selectMedias(Context context, int maxSum, OnSelectMediaListener onSelectMediaListener) {
        mOnSelectMediaListener = onSelectMediaListener;
        Intent intent = new Intent(context, MediaPickerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("maxSum", maxSum);
        context.startActivity(intent);
    }

    public static void selectMedias(Context context, int maxSum) {
        mOnSelectMediaListener = null;
        Intent intent = new Intent(context, MediaPickerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("maxSum", maxSum);
        context.startActivity(intent);
    }

    public  interface OnSelectMediaListener {
        void onSelected(ArrayList<Photo> photoArrayList);
    }
}
