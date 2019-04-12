package com.hai.picker;

import android.app.Application;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.squareup.leakcanary.LeakCanary;

import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * Created by Administrator on 2017/3/17.
 */

public class App extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);



        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
//                .showImageOnLoading(new ColorDrawable(Color.parseColor("#EEEEEE")))
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .build();

        ImageLoaderConfiguration imageLoaderConfiguration = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(displayImageOptions)
                .build();
        ImageLoader.getInstance().init(imageLoaderConfiguration);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e("进程","低内存了，开始释放内存");
    }
}
