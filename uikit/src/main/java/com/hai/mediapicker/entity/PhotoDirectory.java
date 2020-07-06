package com.hai.mediapicker.entity;

import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by donglua on 15/6/28.
 */
public class PhotoDirectory {

    private int id;
    private String coverUri;
    private String name;
    private long dateAdded;
    private List<Photo> photos = new ArrayList<>();
    private boolean selected = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhotoDirectory)) return false;

        PhotoDirectory directory = (PhotoDirectory) o;
        return id == directory.getId();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCoverUri() {
        return coverUri;
    }

    public void setCoverUri(Photo coverPath) {
        if (Build.VERSION.SDK_INT >= 29) {
            Uri mediaUri = coverPath.getMimetype().startsWith("image")?MediaStore.Images.Media.EXTERNAL_CONTENT_URI:MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            int id = coverPath.getId();
            this.coverUri = mediaUri.buildUpon().appendPath(String.valueOf(id)).build().toString();
        } else {
            this.coverUri = "file:///" + coverPath;
        }
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        if (photos == null) return;
        for (int i = 0, j = 0, num = photos.size(); i < num; i++) {
            Photo p = photos.get(j);
            if (p == null) {
                photos.remove(j);
            } else {
                j++;
            }
        }
        this.photos = photos;
    }

    public List<String> getPhotoPaths() {
        List<String> paths = new ArrayList<>(photos.size());
        for (Photo photo : photos) {
            paths.add(photo.getPath());
        }
        return paths;
    }

    public void addPhoto(Photo photo) {
        photos.add(photo);
    }
}
