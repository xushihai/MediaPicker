package com.hai.mediapicker.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hai.mediapicker.R;
import com.hai.mediapicker.entity.Photo;
import com.hai.mediapicker.entity.PhotoDirectory;
import com.hai.mediapicker.util.MediaManager;
import com.hai.mediapicker.viewholder.GalleryHolder;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Administrator on 2017/3/14.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryHolder> {
    private PhotoDirectory images;
    private LayoutInflater layoutInflater;
    private AdapterView.OnItemClickListener onItemClickListener;
    private RecyclerView imageRecyclerView;

    public GalleryAdapter(Activity activity, PhotoDirectory images) {
        this.images = images;
        layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setImageRecyclerView(RecyclerView imageRecyclerView) {
        this.imageRecyclerView = imageRecyclerView;
    }

    public void setImages(PhotoDirectory images) {
        this.images = images;
        notifyDataSetChanged();
    }

    public PhotoDirectory getImages() {
        return images;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        String mimeType = images.getPhotos().get(position).getMimetype();
        return mimeType.contains("video") ? 2 : 1;
    }

    @Override
    public GalleryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutRes = viewType == 2 ? R.layout.gallery_video_item : R.layout.gallery_image_item;
        View view = layoutInflater.inflate(layoutRes, parent, false);
        return new GalleryHolder(view);
    }

    @Override
    public void onBindViewHolder(final GalleryHolder holder, final int position) {

        final Photo photo = images.getPhotos().get(position);
        String url = "file:///" + photo.getPath();
        boolean check = MediaManager.getInstance().exsit(photo.getId());
        holder.thumbIv.justSetShowShade(check);
        //ImageLoader.getInstance().displayImage(url, holder.thumbIv);
        Glide.with(holder.thumbIv.getContext()).load(url)
                .placeholder(android.R.color.black)
                .priority(Priority.IMMEDIATE)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(holder.thumbIv);
        holder.appCompatCheckBox.setChecked(check);
        if (photo.getMimetype().contains("video")) {
            holder.tvVideoDuration.setText(converDuration(photo.getDuration()));
        }

        holder.appCompatCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = holder.appCompatCheckBox.isChecked();
                if (isChecked) {
                    boolean result = MediaManager.getInstance().addMedia(photo.getId(), photo, true);//表示Checkbox已经更新过了，不需要再次进行更新，主要用于区分再其他地方更新了选择状态，但是这边的UI，没更新的情况。
                    if (result) {
                        holder.thumbIv.setShowShade(true);
                    } else {
                        holder.appCompatCheckBox.setChecked(false);
                        Toast.makeText(v.getContext(), String.format(v.getContext().getResources().getString(R.string.select_max_sum),MediaManager.getInstance().getMaxMediaSum()), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    MediaManager.getInstance().removeMedia(photo.getId(), true);
                    holder.thumbIv.setShowShade(false);
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null)
                    onItemClickListener.onItemClick(null, v, position, 0);
            }
        });
    }

    /**
     * 更新多媒体的选择 只更新在屏幕上显示的，更节约内存和功耗。
     *
     * @param id
     */
    public void updateCheckbox(int id) {
        GridLayoutManager gridLayoutManager = (GridLayoutManager) imageRecyclerView.getLayoutManager();
        int firstVisibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition();
        int lastVisibleItemPosition = gridLayoutManager.findLastVisibleItemPosition();
        List<Photo> photoList = images.getPhotos();
        for (int i = firstVisibleItemPosition; i <= lastVisibleItemPosition; i++) {
            if (id == photoList.get(i).getId()) {
                GalleryHolder holder = (GalleryHolder) imageRecyclerView.findViewHolderForAdapterPosition(i);
                boolean check = MediaManager.getInstance().exsit(id);
                holder.appCompatCheckBox.setChecked(check);
                holder.thumbIv.setShowShade(check);
                break;
            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(GalleryHolder holder) {
        holder.appCompatCheckBox.setOnCheckedChangeListener(null);
        super.onViewDetachedFromWindow(holder);
    }

    String converDuration(long duration) {
        StringBuilder durationString = new StringBuilder();
        int second = (int) (duration / 1000);
        int min = second / 60;
        int hour = min / 60;
        if (hour > 0)
            durationString.append(hour + ":");
        durationString.append(min + ":");
        durationString.append(new DecimalFormat("00").format(second));
        return durationString.toString();
    }

    @Override
    public int getItemCount() {
        return images.getPhotos().size();
    }

}
