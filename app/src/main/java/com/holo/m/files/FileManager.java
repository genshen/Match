package com.holo.m.files;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by 根深 on 2015/12/20.
 */
public class FileManager {
    public static String getSDPath() {
        try {
            return Environment.getExternalStorageDirectory().getCanonicalPath();
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
        return null;
    }

    public static File CreateFile(String path) {
        if (!path.startsWith("/"))
            path = "/" + path;

        File file = new File(getSDPath() + path);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

}
