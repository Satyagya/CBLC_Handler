package com.example.geckoview.androidFileSystem;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public class CreatePath {

    public File createFilePath(String fileName) {
        File path =
                Environment.getExternalStoragePublicDirectory
                        (
                                //Environment.DIRECTORY_PICTURES
                                Environment.DIRECTORY_DCIM + fileName
                        );

        if (!path.exists()) {
            boolean status = path.mkdirs();
            Log.i("Path Created :", String.valueOf(status));
        }
        return path;
    }
}
