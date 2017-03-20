package com.hai.mediapicker.view;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.hai.mediapicker.R;
import com.hai.mediapicker.util.MediaManager;


public class PopupWindowMenu extends PopupWindow {

    ListView listView;
    BaseAdapter baseAdapter;

    public PopupWindowMenu(Activity context, final AdapterView.OnItemClickListener onItemClickListener) {
        super(context);
        listView = new ListView(context);
        this.setContentView(listView);
        this.setWidth(LayoutParams.FILL_PARENT);
        this.setHeight(LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setAnimationStyle(R.style.PopupAnimation);
        ColorDrawable dw = new ColorDrawable(0xffffffff);
//        ColorDrawable dw = new ColorDrawable(0xb0000000);
        this.setBackgroundDrawable(dw);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaManager mediaManager = MediaManager.getInstance();
                if (position == mediaManager.getSelectIndex()) {
                    dismiss();
                    return;
                }

                mediaManager.getSelectDirectory().setSelected(false);
                mediaManager.getPhotoDirectorys().get(position).setSelected(true);
                mediaManager.setSelectIndex(position);
                baseAdapter.notifyDataSetChanged();
                if (onItemClickListener != null)
                    onItemClickListener.onItemClick(parent, view, position, id);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();//直接关闭的话视图不会立刻修改过来，看起来不舒服，等他修改过来后再关闭，体验好些
                    }
                }, 200);
            }
        });
    }

    public void setAdapter(BaseAdapter baseAdapter) {
        listView.setAdapter(baseAdapter);
        this.baseAdapter = baseAdapter;
    }

    public BaseAdapter getAdapter() {
        return baseAdapter;
    }

}
