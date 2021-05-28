package com.example.webload.androidFileSystem;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class LocalPath {

    /**
     * No need to create object as include single function
     */
    private LocalPath() {
    }

    public static File getFile(String sdPathName, String fileName) {
        File file;
        File path = Environment.getExternalStorageDirectory();
        File path1 = new File(path.getAbsolutePath() + sdPathName);
        if (!path1.exists()) {
            boolean mkdirs = path1.mkdirs();

        }
        file = new File(path1, fileName);
        try {
            boolean newFile = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }


}
