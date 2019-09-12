package com.hai.mediapicker.activity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hai.mediapicker.R;
import com.hai.mediapicker.adapter.GalleryAdapter;
import com.hai.mediapicker.adapter.PopupDirectoryListAdapter;
import com.hai.mediapicker.decoration.SpaceItemDecoration;
import com.hai.mediapicker.entity.Photo;
import com.hai.mediapicker.entity.PhotoDirectory;
import com.hai.mediapicker.util.GalleryFinal;
import com.hai.mediapicker.util.MediaManager;
import com.hai.mediapicker.util.MediaStoreHelper;
import com.hai.mediapicker.util.MemoryLeakUtil;
import com.hai.mediapicker.view.PopupWindowMenu;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MediaPickerActivity extends AppCompatActivity implements MediaManager.OnCheckchangeListener {
    RecyclerView imageRecyclerView;
    GalleryAdapter galleryAdapter;
    Button btnSend;
    TextView tvPreview, tvDirectory;
    static protected boolean selectMode = true;
    public static final String EXTREA_SELECT_MODE = "select_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_picker);
        initUi();
        readIntentParams();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void sendMedia(ArrayList<Photo> photoList) {
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        GalleryFinal.mOnSelectMediaListener = null;
        MediaManager.getInstance().removeOnCheckchangeListener(this);
        EventBus.getDefault().unregister(this);
        MediaManager.getInstance().clear();
        MemoryLeakUtil.fixInputMethodManagerLeak(this);
    }

    protected void initUi() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert toolbar != null;
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvDirectory = (TextView) findViewById(R.id.tv_dictory);
        tvDirectory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
            }
        });
        Drawable drawable = getResources().getDrawable(R.drawable.btn_dropdown);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        tvDirectory.setCompoundDrawables(null, null, drawable, null);
        btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaManager.getInstance().send();
            }
        });
        tvPreview = (TextView) findViewById(R.id.tv_preview);
        imageRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        imageRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        imageRecyclerView.addItemDecoration(new SpaceItemDecoration(this, 1));
        imageRecyclerView.setHasFixedSize(true);
        imageRecyclerView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        tvPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), PreviewActivity.class);
                intent.putExtra(EXTREA_SELECT_MODE, selectMode);
                startActivity(intent);
            }
        });

        CaptureActivity2.checkPermission(this);
        loadMedia();
    }

    private void loadMedia() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        MediaManager.getInstance().init();
        MediaStoreHelper.getPhotoDirs(this, getIntent().getIntExtra("type", GalleryFinal.TYPE_ALL), new MediaStoreHelper.PhotosResultCallback() {
            @Override
            public void onResultCallback(List<PhotoDirectory> dirs) {
                if (dirs.size() > 0)
                    dirs.get(0).setSelected(true);
                MediaManager.getInstance().setPhotoDirectorys(dirs);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        directoryChanged();
                        tvDirectory.setText(MediaManager.getInstance().getPhotoDirectorys().get(0).getName());
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        loadMedia();
    }

    protected void readIntentParams() {
        Intent intent = getIntent();
        int maxMedia = intent.getIntExtra("maxSum", 9);
        MediaManager.getInstance().setMaxMediaSum(maxMedia);
    }

    private static boolean hasNavigationBar(Activity context) {
//        IWindowManager mWindowManagerService = WindowManagerGlobal.getWindowManagerService();
//            mWindowManagerService.hasNavigationBar()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = context.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y != size.y;
        } else {
            boolean menu = ViewConfiguration.get(context).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            if (menu || back) {
                return false;
            } else {
                return true;
            }
        }
    }

    public static int getNavigationBarHeight(Activity activity) {
        int height = 0;
        try {
            boolean hasNavigationBar = hasNavigationBar(activity);
            if (hasNavigationBar) {
                Resources resources = activity.getResources();
                int resourceId = resources.getIdentifier("navigation_bar_height",
                        "dimen", "android");
                //获取NavigationBar的高度
                height = resources.getDimensionPixelSize(resourceId);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return height;
    }

    PopupWindowMenu menuWindow;

    private void show() {
        if (menuWindow == null) {
            menuWindow = new PopupWindowMenu(MediaPickerActivity.this, new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    directoryChanged();
                    tvDirectory.setText(MediaManager.getInstance().getPhotoDirectorys().get(position).getName());
                }
            });
            menuWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    imageRecyclerView.setAlpha(1.0f);
                }
            });
            PopupDirectoryListAdapter popupDirectoryListAdapter = new PopupDirectoryListAdapter(MediaManager.getInstance().getPhotoDirectorys());
            menuWindow.setAdapter(popupDirectoryListAdapter);
            menuWindow.setHeight(calculatePopupWindowHeight(menuWindow.getAdapter().getCount()));
        }
        ObjectAnimator.ofFloat(imageRecyclerView, "alpha", 1.0f, 0.2f).setDuration(getResources().getInteger(R.integer.anim_duration)).start();
        int barHeight = getResources().getDimensionPixelOffset(R.dimen.toolbar_height);
        int windowHeight = getNavigationBarHeight(this) + barHeight;
        menuWindow.showAtLocation(findViewById(R.id.bottom), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, windowHeight);
    }

    private void directoryChanged() {
        if (MediaManager.getInstance().getPhotoDirectorys().isEmpty()) {
            ViewStub emptyStub = ((ViewStub) findViewById(R.id.stub_empty));
            emptyStub.inflate();
            return;
        }
        if (galleryAdapter == null) {
            galleryAdapter = new GalleryAdapter(MediaPickerActivity.this, MediaManager.getInstance().getSelectDirectory());
            galleryAdapter.setImageRecyclerView(imageRecyclerView);
            galleryAdapter.setSelectMode(selectMode);
            imageRecyclerView.setAdapter(galleryAdapter);
            MediaManager.getInstance().addOnCheckchangeListener(MediaPickerActivity.this);
            galleryAdapter.setOnItemClickListener(new GalleryItemClickImpl());
        } else {
            galleryAdapter.setImages(MediaManager.getInstance().getSelectDirectory());
            imageRecyclerView.getLayoutManager().scrollToPosition(0);
        }
    }

    /**
     * 不使用匿名内部类，避免出现内存泄漏
     */
    private static class GalleryItemClickImpl implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(view.getContext(), PreviewActivity.class);
            intent.putExtra("index", position);
            intent.putExtra("dir", MediaManager.getInstance().getSelectIndex());
            intent.putExtra(EXTREA_SELECT_MODE, selectMode);
            view.getContext().startActivity(intent);
        }
    }

    int calculatePopupWindowHeight(int count) {
        int maxHeight = imageRecyclerView.getMeasuredHeight() - getResources().getDimensionPixelSize(R.dimen.directory_window_margin_top);
        int windowHeight = count * getResources().getDimensionPixelOffset(R.dimen.directory_item_height);
        windowHeight = windowHeight < maxHeight ? windowHeight : maxHeight;
        return windowHeight;
    }


    @Override
    public void onCheckedChanged(Map<Integer, Photo> checkStaus, int changedId, boolean uiUpdated) {
        final int checkSize = checkStaus.size();
        btnSend.setEnabled(checkSize != 0);
        btnSend.setText(checkSize == 0 ? getString(R.string.btn_send) : String.format(getString(R.string.send_multi), checkSize, MediaManager.getInstance().getMaxMediaSum()));

        tvPreview.setEnabled(checkSize != 0);
        tvPreview.setText(checkSize == 0 ? getString(R.string.preview) : getString(R.string.preview_multi, checkSize));
        if (uiUpdated)
            galleryAdapter.updateCheckbox(changedId);
    }

}
