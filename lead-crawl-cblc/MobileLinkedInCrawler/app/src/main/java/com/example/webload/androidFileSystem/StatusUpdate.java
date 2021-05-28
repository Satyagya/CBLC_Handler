package com.example.webload.androidFileSystem;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class StatusUpdate {

    private CreatePath createPath;

    public StatusUpdate() {
        createPath = new CreatePath();
    }

    public String readCrawlStatus() {
        File file;
        File path = createPath.createFilePath("/MyAlbums/Status");
        file = new File(path, "status.txt");
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
        return count.toString();
    }

    public void updateCrawlStatus(String crawlStatus) {
        File file;
        File path = createPath.createFilePath("/MyAlbums/Status");
        if (!path.exists()) {
            boolean pathCreate = path.mkdirs();
            Log.i("Status Path Created :" , String.valueOf(pathCreate));
        }
        file = new File(path, "status.txt");
        if (!file.exists()) {
            try {
                boolean fileCreated = file.createNewFile();
                Log.i("Status File Created :", String.valueOf(fileCreated));
            } catch (IOException e) {
                Log.e("Create status File :" , String.valueOf(e));
            }
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
            try (OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut)) {
                myOutWriter.write(crawlStatus);
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
