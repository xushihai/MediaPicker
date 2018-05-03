package com.hai.mediapicker.save;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
            copyToFile(new FileInputStream(preFile), destFile);
            preFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Copy data from a source stream to destFile.
     * Return true if succeed, return false if failed.
     */
    public static boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            if (destFile.exists()) {
                destFile.delete();
            }
            FileOutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.flush();
                try {
                    out.getFD().sync();
                } catch (IOException e) {
                }
                out.close();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
