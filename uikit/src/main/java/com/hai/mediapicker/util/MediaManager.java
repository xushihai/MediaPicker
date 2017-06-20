package com.hai.mediapicker.util;

import com.hai.mediapicker.entity.Photo;
import com.hai.mediapicker.entity.PhotoDirectory;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/14.
 */

public class MediaManager {
    private static final MediaManager ourInstance = new MediaManager();

    public static MediaManager getInstance() {
        return ourInstance;
    }

    private MediaManager() {
    }

    Map<Integer, Photo> checkStatus;
    Map<Integer, Boolean> originalImage;
    List<OnCheckchangeListener> onCheckchangeListeners;
    List<PhotoDirectory> photoDirectorys;
    private int maxMediaSum;
    private int selectIndex;

    public void init() {
        checkStatus = new HashMap<>();
        onCheckchangeListeners = new ArrayList<>();
        photoDirectorys = new ArrayList<>();
        originalImage = new HashMap<>();
        maxMediaSum = Integer.MAX_VALUE;
        selectIndex = 0;
    }

    public void clear() {
        checkStatus.clear();
        originalImage.clear();
        photoDirectorys.clear();
    }

    public int getMaxMediaSum() {
        return maxMediaSum;
    }

    public void setMaxMediaSum(int maxMediaSum) {
        this.maxMediaSum = maxMediaSum;
    }

    public void addOnCheckchangeListener(OnCheckchangeListener onCheckchangeListener) {
        if (onCheckchangeListener != null) {
            onCheckchangeListeners.add(onCheckchangeListener);
        }
    }

    public void removeOnCheckchangeListener(OnCheckchangeListener onCheckchangeListener) {
        if (onCheckchangeListener != null)
            onCheckchangeListeners.remove(onCheckchangeListener);
    }

    /**
     * 超过最大允许的数量就不能再添加了
     *
     * @param id
     * @param photo
     * @param uiUpdated
     * @return
     */
    public boolean addMedia(int id, Photo photo, boolean uiUpdated) {
        if (checkStatus.size() >= maxMediaSum) {
            return false;
        }
        if (!checkStatus.containsKey(id)) {
            checkStatus.put(id, photo);
            notifyDataChange(id, uiUpdated);
        }
        return true;
    }

    public boolean addMedia(int id, Photo photo) {
        return addMedia(id, photo, false);
    }

    public void removeMedia(int id, boolean uiUpdated) {
        if (checkStatus.containsKey(id)) {
            checkStatus.remove(id);
            notifyDataChange(id, uiUpdated);
        }
    }

    public void removeMedia(int id) {
        if (checkStatus.containsKey(id)) {
            checkStatus.remove(id);
            notifyDataChange(id, false);
        }
    }

    public int getSelectIndex() {
        return selectIndex;
    }

    public void setSelectIndex(int selectIndex) {
        this.selectIndex = selectIndex;
    }

    public boolean exsit(int id) {
        return checkStatus.containsKey(id);
    }

    public Map<Integer, Photo> getCheckStatus() {
        return checkStatus;
    }

    public List<PhotoDirectory> getPhotoDirectorys() {
        return photoDirectorys;
    }

    public void setPhotoDirectorys(List<PhotoDirectory> photoDirectorys) {
        this.photoDirectorys = photoDirectorys;
    }

    public PhotoDirectory getSelectDirectory() {
        return photoDirectorys.get(selectIndex);
    }

    public void setOriginal(int id, boolean original) {
        originalImage.put(id, original);
    }

    public void removeOriginal(int id) {
        if (originalImage.containsKey(id))
            originalImage.remove(id);
    }

    public boolean isOriginal(int id) {
        return originalImage.containsKey(id);
    }

    private void notifyDataChange(int id, boolean uiUpdated) {
        for (OnCheckchangeListener listner :
                onCheckchangeListeners) {
            listner.onCheckedChanged(checkStatus, id, uiUpdated);
        }
    }

    public interface OnCheckchangeListener {
        void onCheckedChanged(Map<Integer, Photo> checkStaus, int changedId, boolean uiUpdated);
    }

    public void send() {
        ArrayList<Photo> photoArrayList = new ArrayList<>(checkStatus.size());
        for (Integer integer : checkStatus.keySet()) {
            Photo photo = checkStatus.get(integer);
            photo.setFullImage(originalImage.containsKey(photo.getId()));
            photoArrayList.add(photo);
        }

        if (GalleryFinal.mOnSelectMediaListener != null)
            GalleryFinal.mOnSelectMediaListener.onSelected(photoArrayList);
        EventBus.getDefault().post(photoArrayList);
    }

}
