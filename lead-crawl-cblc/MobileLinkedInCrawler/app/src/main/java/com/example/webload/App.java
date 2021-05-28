package com.example.webload;

import android.app.Application;
import android.util.Log;


import com.example.webload.androidFileSystem.CreatePath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class App extends Application {
    static private CreatePath createPath=new CreatePath();

    static Retrofit retrofit;

    static Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS).build();
            String ipAddress= readSleep("/config/","ip.txt");
            retrofit = new Retrofit.Builder().baseUrl("http://"+ipAddress).addConverterFactory(GsonConverterFactory.create()).client(client).build();
        }
        return retrofit;
    }


    public static String readSleep(String dir , String fileName) {

        File file;
        File path = createPath.createFilePath(dir);
        file = new File(path, fileName);
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
}