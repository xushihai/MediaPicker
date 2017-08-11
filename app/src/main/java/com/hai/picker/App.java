package com.hai.picker;

import android.app.Application;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

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
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e("进程","低内存了，开始释放内存");
    }
}
