package com.example.webload.androidFileSystem;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class CountUpdate {

    private CreatePath createPath;

    public CountUpdate() {
        createPath = new CreatePath();
    }

    public int readUrlCount() {

        File file;
        File path = createPath.createFilePath("/MyAlbums/urlCount");
        file = new File(path, "urlCount.txt");
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Log.e("File Not Found", String.valueOf(e));
        }
        StringBuilder count = null;
        try (InputStreamReader myInputReader = new InputStreamReader(fin)) {
            int i;
            count = new StringBuilder();
            while ((i = myInputReader.read()) != -1) {
                count.append((char) i);
            }
        } catch (IOException e) {
            Log.e("IO Exception :", e.toString());
        }
        try {
            fin.close();
        } catch (IOException e) {
            Log.e("IOException :", e.toString());
        }

        return Integer.parseInt(count.toString());
    }

    public void updateCount(int urlCount) {
        File file;
        File path = createPath.createFilePath("/MyAlbums/urlCount");
        if (!path.exists()) {
            boolean status = path.mkdirs();
            Log.i("URLCount Path Created :", String.valueOf(status));
        }
        file = new File(path, "urlCount.txt");

        if (!file.exists()) {
            try {
                boolean status = file.createNewFile();
                Log.i("urlCount File Created :", String.valueOf(status));
            } catch (IOException e) {
                Log.e("Create URL File :", String.valueOf(e));
            }
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
            try (OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut)) {
                myOutWriter.write(String.valueOf(urlCount));
            }

            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            Log.e("File Not Found :", e.toString());
        } catch (IOException e) {
            Log.e("IOException :", e.toString());
        }

    }
}
