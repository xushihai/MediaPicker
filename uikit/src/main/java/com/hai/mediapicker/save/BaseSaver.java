package com.hai.mediapicker.save;

import android.os.Environment;
import android.os.FileUtils;

import java.io.File;

/**
 * Created by Administrator on 2018/5/2.
 */

public class BaseSaver implements ISaver {

    String destDir;

    public BaseSaver() {
        destDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "DCIM" + File.separator + "Camera";
    }

    public String getDestDir() {
        return destDir;
    }

    public void setDestDir(String destFile) {
        this.destDir = destFile;
    }

    @Override
    public boolean save(String previousFile) {
        try {
            File preFile = new File(previousFile);
            File dir = new File(destDir);
            if (!dir.exists())
                dir.mkdirs();
            File destFile = new File(dir, preFile.getName());
            if (destFile.exists())
                destFile.delete();
            FileUtils.copyFile(preFile, destFile);
            preFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
