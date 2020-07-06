package com.hai.mediapicker.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.hai.mediapicker.R;
import com.hai.mediapicker.entity.Photo;
import com.hai.mediapicker.util.GalleryFinal;
import com.hai.mediapicker.util.MediaManager;
import com.hai.mediapicker.util.MemoryLeakUtil;
import com.hai.mediapicker.view.TouchImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class PreviewActivity extends AppCompatActivity implements MediaManager.OnCheckchangeListener {
    Button btnSend;
    AppCompatCheckBox cbSelect;
    RadioButton rbOriginal;
    ViewPager viewPager;
    ImageAdapter imageAdapter;
    View buttomBar, divider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        initUi();
        EventBus.getDefault().register(this);
    }

    private void initUi() {
        if (Build.VERSION.SDK_INT >= 16)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        fitsSystemWindows(findViewById(R.id.toolbar_layout));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        buttomBar = findViewById(R.id.bottom);
        divider = findViewById(R.id.bar_divider);
        btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaManager.getInstance().send();
            }
        });

        boolean selectMode = getIntent().getBooleanExtra(MediaPickerActivity.EXTREA_SELECT_MODE, true);
        if (!selectMode)
            switchOverlay("");


        rbOriginal = (RadioButton) findViewById(R.id.rb_original);
        cbSelect = (AppCompatCheckBox) findViewById(R.id.cb_pre);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setPageTransformer(true, new DepthPageTransformer());
        int index = getIntent().getIntExtra("index", 0);
        int dirIndex = getIntent().getIntExtra("dir", -1);
        List<Photo> selectPhotoList;
        if (dirIndex == -1) {
            selectPhotoList = new ArrayList<>();
            for (Integer integer : MediaManager.getInstance().getCheckStatus().keySet()) {
                Photo photo = MediaManager.getInstance().getCheckStatus().get(integer);
                selectPhotoList.add(photo);
            }
        } else {
            selectPhotoList = MediaManager.getInstance().getPhotoDirectorys().get(dirIndex).getPhotos();
        }
        getSupportActionBar().setTitle((index + 1) + "/" + selectPhotoList.size());
        imageAdapter = new ImageAdapter(this, selectPhotoList);
        viewPager.setAdapter(imageAdapter);
        viewPager.setCurrentItem(index);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateUi(position);
            }
        });

        cbSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Photo photo = imageAdapter.getItem(viewPager.getCurrentItem());
                boolean isChecked = cbSelect.isChecked();
                if (isChecked) {
                    boolean result = MediaManager.getInstance().addMedia(photo.getId(), photo, true);//表示Checkbox已经更新过了，不需要再次进行更新，主要用于区分再其他地方更新了选择状态，但是这边的UI，没更新的情况。
                    if (!result) {
                        cbSelect.setChecked(false);
                        Toast.makeText(getBaseContext(), String.format(getString(R.string.select_max_sum), MediaManager.getInstance().getMaxMediaSum()), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    MediaManager.getInstance().removeMedia(photo.getId(), true);
                }
            }
        });
        rbOriginal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Photo photo = imageAdapter.getItem(viewPager.getCurrentItem());
                boolean check = !MediaManager.getInstance().isOriginal(photo.getId());
                rbOriginal.setChecked(check);
                MediaManager.getInstance().setOriginal(photo.getId(), check);
            }
        });
        MediaManager.getInstance().addOnCheckchangeListener(this);
        onCheckedChanged(MediaManager.getInstance().getCheckStatus(), 0, true);
        updateUi(viewPager.getCurrentItem());
    }

    /**
     * 状态栏透明后要想一部分View从屏幕顶部开始，一部分View从原来的状态栏开始使用fitsSystemWindows属性实现不了，只好手动实现
     *
     * @param view
     */
    private void fitsSystemWindows(View view) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        int status_bar_height;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            status_bar_height = getResources().getDimensionPixelSize(resourceId);
        } else {
            int statusHeightDp = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 24 : 25;
            status_bar_height = (int) Math.ceil(statusHeightDp * view.getContext().getResources().getDisplayMetrics().density);
        }

        layoutParams.setMargins(layoutParams.leftMargin, status_bar_height + layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin);
        view.setLayoutParams(layoutParams);
    }

    private void updateUi(int position) {
        getSupportActionBar().setTitle((position + 1) + "/" + imageAdapter.getCount());
        Photo photo = imageAdapter.getItem(position);
        cbSelect.setChecked(MediaManager.getInstance().exsit(photo.getId()));
        if (photo.getMimetype().contains("image")) {
            rbOriginal.setVisibility(View.VISIBLE);
            rbOriginal.setChecked(MediaManager.getInstance().isOriginal(photo.getId()));
            rbOriginal.setText(getString(R.string.original_image, Formatter.formatFileSize(getBaseContext(), photo.getSize())));
        } else {
            rbOriginal.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void sendMedia(ArrayList<Photo> photoList) {
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void switchOverlay(String url) {
        if (getSupportActionBar().isShowing()) {
            WindowManager.LayoutParams winParams = getWindow().getAttributes();
            winParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            winParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            getWindow().setAttributes(winParams);
            buttomBar.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
            btnSend.setVisibility(View.GONE);
            getSupportActionBar().hide();
        } else {
            WindowManager.LayoutParams winParams = getWindow().getAttributes();
            winParams.flags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            winParams.flags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            getWindow().setAttributes(winParams);
            getSupportActionBar().show();
            buttomBar.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
            btnSend.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageAdapter.clearCache();
        MediaManager.getInstance().removeOnCheckchangeListener(this);
        EventBus.getDefault().unregister(this);
        MemoryLeakUtil.fixInputMethodManagerLeak(this);
    }

    @Override
    public void onCheckedChanged(Map<Integer, Photo> checkStaus, int changedId, boolean uiUpdated) {
        btnSend.setEnabled(!checkStaus.isEmpty());
        btnSend.setText(checkStaus.isEmpty() ? getString(R.string.btn_send) : String.format(getString(R.string.send_multi), checkStaus.size(), MediaManager.getInstance().getMaxMediaSum()));
    }

    public static class PagerHolder extends RecyclerView.ViewHolder {
        public TouchImageView touchImageView;
        public ImageView ivVideoPlay;
        public int viewType;
        String url = "";
        Photo photo;
        DisplayImageOptions displayImageOptions;

        public PagerHolder(final View itemView) {
            super(itemView);
            touchImageView = (TouchImageView) itemView.findViewById(R.id.iv_touch);
            ivVideoPlay = (ImageView) itemView.findViewById(R.id.iv_play);
            touchImageView.setMaxZoom(6);
            touchImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(url);
                }
            });

            if (ivVideoPlay != null) {
                ivVideoPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = v.getContext();
                        VideoPlayer.playVideo(context, photo);

//                        Intent intent = new Intent(Intent.ACTION_VIEW);
//                        intent.setDataAndType(Uri.parse(url), "video/*");
//                        try {
//                           context.startActivity(intent);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            Toast.makeText(context, context.getString(R.string.play_video_fail), Toast.LENGTH_SHORT).show();
//                        }
                    }
                });
            }

            displayImageOptions = new DisplayImageOptions.Builder()
                    .displayer(new BitmapDisplayer() {

                        @Override
                        public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
                            float viewRatio = itemView.getWidth() * 1.0f / itemView.getHeight();
                            float imageRatio = bitmap.getWidth() * 1.0f / bitmap.getHeight();
                            if (imageRatio >= viewRatio) {
                                imageAware.setImageBitmap(bitmap);
                            } else {
                                int destHeight = (int) (bitmap.getHeight() * (itemView.getWidth() * 1.0f / bitmap.getWidth()));
                                Log.e("xx", itemView.getWidth() + "   " + destHeight);
                                //  destHeight = displayHeight;
                                if (itemView.getWidth() == 0 || destHeight == 0) {
                                    imageAware.setImageBitmap(bitmap);
                                    return;
                                }
                                Bitmap transformed = Bitmap.createScaledBitmap(bitmap, itemView.getWidth(), destHeight, false);
                                imageAware.setImageBitmap(transformed);
                            }
                        }
                    })
                    .build();
        }

        public void bindData(Photo photo) {
            url = photo.getMediaUri();
            this.photo = photo;
            switch (GalleryFinal.getImageEngine()) {
                case GalleryFinal.IMAGE_ENGINE_IMAGE_LOADER:
                    ImageLoader.getInstance().displayImage(url, touchImageView, displayImageOptions, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            super.onLoadingComplete(imageUri, view, loadedImage);
                            try {
                                touchImageView.setZoom(1f);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                default:
                    SimpleTarget<GlideDrawable> simpleTarget = new SimpleTarget<GlideDrawable>() {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation glideAnimation) {
                            touchImageView.setImageDrawable(resource);
                            try {
                                touchImageView.setZoom(1f);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    Glide.with(touchImageView.getContext()).load(url)
                            .placeholder(android.R.color.black)
                            .priority(Priority.IMMEDIATE)
                            .transform(new FitOrCenterBitmapTransformation(touchImageView.getContext(), photo.getWidth(), photo.getHeight()))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(simpleTarget);
                    break;
            }
        }
    }

    public static class FitOrCenterBitmapTransformation extends BitmapTransformation {
        int imageWidth, imageHeight;
        float imageRatio;

        public FitOrCenterBitmapTransformation(Context context, int imageWidth, int imageHeight) {
            super(context);
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
            imageRatio = imageWidth * 1.0f / imageHeight;
        }

        /**
         * 我需要的图片缩放的效果是：图片的宽能填满控件，高可以不填满控件，fitCenter方式在图片的宽高比大于控件的宽高比的时候没问题，小于的时候宽就不能填满控件的宽了，这个时候可以用centercrop来
         *
         * @param pool
         * @param toTransform
         * @param outWidth
         * @param outHeight
         * @return
         */
        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            float viewRatio = outWidth * 1.0f / outHeight;

            /**
             * ||imageWidth==0有的图片通过查询数据库的方式可能得不到长宽，这个时候默认就用fitcenter方式裁剪图片了
             */
            if (imageRatio >= viewRatio || imageWidth == 0) {
                return TransformationUtils.fitCenter(toTransform, pool, outWidth, outHeight);
            } else {
                final Bitmap toReuse = pool.get(outWidth, outHeight, toTransform.getConfig() != null
                        ? toTransform.getConfig() : Bitmap.Config.ARGB_8888);
                Bitmap transformed = TransformationUtils.centerCrop(toReuse, toTransform, outWidth, outHeight);
                if (toReuse != null && toReuse != transformed && !pool.put(toReuse)) {
                    toReuse.recycle();
                }
                return transformed;
            }
        }

        @Override
        public String getId() {
            return "FitCenterOrCenterCrop.com.bumptech.glide.load.resource.bitmap";
        }
    }

    public class ImageAdapter extends PagerAdapter {
        List<Photo> images;
        LayoutInflater layoutInflater;

        public ImageAdapter(Context context, List<Photo> images) {
            this.images = images;
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        public PagerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case 1:
                    PagerHolder pagerHolder = new PagerHolder(layoutInflater.inflate(R.layout.preview_video_item, parent, false));
                    pagerHolder.viewType = viewType;
                    return pagerHolder;
                case 2:
                    pagerHolder = new PagerHolder(layoutInflater.inflate(R.layout.preview_image_item, parent, false));
                    pagerHolder.viewType = viewType;
                    return pagerHolder;
            }
            return null;
        }


        public void onBindViewHolder(PagerHolder holder, int position) {
            holder.bindData(images.get(position));
        }

        public int getItemViewType(int position) {
            return getItem(position).getMimetype().contains("video") ? 1 : 2;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            PagerHolder pagerHolder = (PagerHolder) ((View) object).getTag(R.id.tag_holder);
            if (pagerHolder != null) caches.get(getItemViewType(position)).offer(pagerHolder);
        }

        public Photo getItem(int position) {
            return images.get(position);
        }

        @Override
        public int getCount() {
            return images.size();
        }

        SparseArray<Queue<PagerHolder>> caches = new SparseArray<>();

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            int viewType = getItemViewType(position);
            Queue<PagerHolder> pagerHolderList = caches.get(viewType);
            if (pagerHolderList == null) {
                pagerHolderList = new ArrayDeque<>();
                caches.put(viewType, pagerHolderList);
            }
            PagerHolder pagerHolder = pagerHolderList.poll();
            if (pagerHolder == null) {
                pagerHolder = onCreateViewHolder(container, viewType);
            }
            container.addView(pagerHolder.itemView);
            onBindViewHolder(pagerHolder, position);
            pagerHolder.itemView.setTag(R.id.tag_holder, pagerHolder);
            return pagerHolder.itemView;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public void clearCache() {
            caches.clear();
        }
    }

    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}
