package com.hai.mediapicker.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hai.mediapicker.R;
import com.hai.mediapicker.entity.PhotoDirectory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by donglua on 15/6/28.
 */
public class PopupDirectoryListAdapter extends BaseAdapter {

    private List<PhotoDirectory> directories = new ArrayList<>();

    public PopupDirectoryListAdapter(List<PhotoDirectory> directories) {
        this.directories = directories;
    }


    @Override
    public int getCount() {
        return directories.size();
    }


    @Override
    public PhotoDirectory getItem(int position) {
        return directories.get(position);
    }


    @Override
    public long getItemId(int position) {
        return directories.get(position).hashCode();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater mLayoutInflater = LayoutInflater.from(parent.getContext());
            convertView = mLayoutInflater.inflate(R.layout.pop_directory_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.bindData(directories.get(position));

        return convertView;
    }

    private class ViewHolder {

        public ImageView ivCover;
        public TextView tvName;
        public TextView tvCount;
        public ImageView ivSelect;

        public ViewHolder(View rootView) {
            ivCover = (ImageView) rootView.findViewById(R.id.iv_dir_cover);
            tvName = (TextView) rootView.findViewById(R.id.tv_dir_name);
            tvCount = (TextView) rootView.findViewById(R.id.tv_dir_count);
            ivSelect = (ImageView) rootView.findViewById(R.id.iv_dir);
        }

        public void bindData(PhotoDirectory directory) {
            Glide.with(ivCover.getContext()).load("file:///" + directory.getCoverPath()).into(ivCover);
            tvName.setText(directory.getName());
            tvCount.setText(tvCount.getContext().getString(R.string.__picker_image_count, directory.getPhotos().size()));
            ivSelect.setVisibility(directory.isSelected() ? View.VISIBLE : View.GONE);
            Drawable drawable = tintDrawable(ivCover.getContext().getResources().getDrawable(R.mipmap.ic_check_dir), ColorStateList.valueOf(Color.parseColor("#FF45C01A")));
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            ivSelect.setImageDrawable(drawable);
        }

        /**
         * 给图片上色
         *
         * @param drawable
         * @param colors
         * @return
         */
        public Drawable tintDrawable(Drawable drawable, ColorStateList colors) {
            final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTintList(wrappedDrawable, colors);
            return wrappedDrawable;
        }
    }

}
