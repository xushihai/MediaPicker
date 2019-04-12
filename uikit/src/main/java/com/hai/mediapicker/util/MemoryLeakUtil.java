package com.hai.mediapicker.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2017/6/12.
 */

public class MemoryLeakUtil {



    /**
     * 修复输入法管理器引起的内存泄漏
     * <p>
     * 有两种情况：
     * 1：启动这个activity之前还有activity，这个时候关闭当前activity，InputMethodManager的这三个变量都是之前的activity的，不能将他们置空，否则之前的activity键盘就弹不出来了。
     * 2：启动这个activity之前没有activity，这个时候关闭当前activity，InputMethodManager的这三个变量都是当前activity的，需要将他们都置空，否则就会导致当前activity泄漏
     *
     * @param destContext
     */
    public static void fixInputMethodManagerLeak(Context destContext) {
        if (destContext == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) destContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        String[] arr = new String[]{"mCurRootView", "mServedView", "mNextServedView", "mLastSrvView"};
        Field f = null;
        Object obj_get = null;
        Activity activity = (Activity) destContext;
        for (int i = 0; i < arr.length; i++) {
            String param = arr[i];
            try {
                f = imm.getClass().getDeclaredField(param);
                if (f.isAccessible() == false) {
                    f.setAccessible(true);
                } // author: sodino mail:sodino@qq.com
                obj_get = f.get(imm);

                if (obj_get != null && obj_get instanceof View) {
                    View view = ((View) obj_get);
                    if (view.getId() == -1) {
                        Log.e("InputMethodManager", arr[i] + "   view.getId()==-1" + view.getContext().getClass().getCanonicalName() + " " + destContext.getClass().getCanonicalName());
                        if (destContext == view.getContext()) {
                            f.set(imm, null); // 置空，破坏掉path to gc节点
                            Log.e("InputMethodManager", arr[i] + "置空");
                        }
                        continue;
                    }
                    View currentView = activity.findViewById(view.getId());
                    if (currentView != null && currentView == view) {
                        f.set(imm, null); // 置空，破坏掉path to gc节点
                        Log.e("InputMethodManager", arr[i] + "置空");
                    } else if (currentView != null) {
                        Log.e("InputMethodManager", arr[i] + "保留");
                    }

                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
