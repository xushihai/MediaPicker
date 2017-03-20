package com.hai.mediapicker.viewholder;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hai.mediapicker.R;
import com.hai.mediapicker.view.SquareImageView;

/**
 * Created by Administrator on 2017/3/14.
 */

public class GalleryHolder extends RecyclerView.ViewHolder {
    public SquareImageView thumbIv;
    public AppCompatCheckBox appCompatCheckBox;
    public TextView tvVideoDuration;
    public ImageView ivVideoFlag;

    public GalleryHolder(View itemView) {
        super(itemView);
        itemView.setClickable(true);
        thumbIv = (SquareImageView) itemView.findViewById(R.id.iv_thumb);
        thumbIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        appCompatCheckBox = (AppCompatCheckBox) itemView.findViewById(R.id.cb_media);
        tvVideoDuration = (TextView) itemView.findViewById(R.id.tv_video_duration);
        ivVideoFlag = (ImageView) itemView.findViewById(R.id.iv_video_flag);
        thumbIv.setShade(new ColorDrawable(Color.parseColor("#92000000")));
    }
}