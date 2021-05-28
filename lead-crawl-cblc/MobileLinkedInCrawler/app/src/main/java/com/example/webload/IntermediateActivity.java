package com.example.webload;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.util.TimeUtils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.webload.androidFileSystem.CountUpdate;
import com.example.webload.androidFileSystem.CreatePath;
import com.example.webload.androidFileSystem.StatusUpdate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.example.webload.NewActivity.isConnected;

public class IntermediateActivity extends AppCompatActivity {

    CreatePath createPath;
    StatusUpdate statusUpdate;
    CountUpdate countUpdate;
    private int STORAGE_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermediate);

        String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE , Manifest.permission.WRITE_EXTERNAL_STORAGE};
        createPath = new CreatePath();
        statusUpdate = new StatusUpdate();
        countUpdate = new CountUpdate();




            if (isRequestAvailable("/MyAlbums/Profile")) {
                if (!isRequestAvailable("/MyAlbums/Status")) {
                    statusUpdate.updateCrawlStatus("ProfileCrawl");
                    updateSleep(0+"_"+0,"/MyAlbums/sleep","sleep.txt");

                }
                setCrawlContent("profile.txt", "profile");
                MainActivity.status = "ProfileCrawl";

                if(!isRequestAvailable("/MyAlbums/sleep")){
                    updateSleep(0+"_"+0,"/MyAlbums/sleep","sleep.txt");
                    setCrawlContent("profile.txt", "profile");
                }
                String readSleep = readSleep("/MyAlbums/sleep","sleep.txt");
                String[] repeatCount = readSleep.split("_");

                if(Integer.parseInt(repeatCount[1])>=20){

                    try {
                        TimeUnit.MINUTES.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int count = 1 + Integer.parseInt(repeatCount[0])  ;
                    String sleepUpdate = count + "_"+0;
                    countUpdate.updateCount(count);
                    updateSleep(sleepUpdate ,"/MyAlbums/sleep","sleep.txt");
                    goToMainWithWait(2);


                }
                else{
                    goToMainWithWait(2);

                }





            } else {

                if(hasPermission(IntermediateActivity.this , permission)){
                    Intent intent = new Intent(IntermediateActivity.this, GetPayloadActivity.class);
                    startActivity(intent);

                }else
                {
                    ActivityCompat.requestPermissions(this ,permission, STORAGE_PERMISSION_CODE);
                }



            }

    }

    public String readUrlCountCompany(String FileName) throws IOException {

        File file;
        final File path =
                Environment.getExternalStoragePublicDirectory
                        (
                                //Environment.DIRECTORY_PICTURES
                                Environment.DIRECTORY_DCIM + "/MyAlbums/Company"
                        );

        file = new File(path, FileName);
        FileInputStream fin = new FileInputStream(file);
        StringBuilder total;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(fin))) {
            total = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                total.append(line).append('\n');
            }
        }


        return total.toString();
    }

    public void goToMainWithWait(int i) {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent1 = new Intent(IntermediateActivity.this, MainActivity.class);
                startActivity(intent1);

            }
        }, i * 1000);

    }


    public void setCrawlContent(String fileNameToRead, String type) {
        String s = null;
        ArrayList<String> list = new ArrayList<>();
        try {
            if (type.equals("profile")) {
                s = readUrlCountProfile(fileNameToRead);
                System.out.println(s);
            } else
                s = readUrlCountCompany(fileNameToRead);
        } catch (IOException e) {

            Log.d("IntermediateActivity", "Error in reading the file");
        }
        String ad[] = s.split("\n");
        MainActivity.arr = new String[ad.length];
        for (int i = 0; i < ad.length; i++) {
            MainActivity.arr[i] = ad[i];
        }
        MainActivity.length = ad.length;

        System.out.println(MainActivity.arr);
    }

    public String readUrlCountProfile(String FileName) throws IOException {

        File file;
        final File path = createPath.createFilePath("/MyAlbums/Profile");
        file = new File(path, FileName);
        FileInputStream fin = new FileInputStream(file);
        StringBuilder total;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(fin))) {
            total = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                total.append(line).append('\n');
            }
        }


        return total.toString();
    }

    public boolean isRequestAvailable(String txt) {
        File dir =
                Environment.getExternalStoragePublicDirectory
                        (
                                //Environment.DIRECTORY_PICTURES
                                Environment.DIRECTORY_DCIM + txt
                        );
        if (dir.isDirectory()) {
            return true;
        }
        return false;
    }



    public void updateSleep(String urlCount , String directory , String fileName ){
        File file;
        File path = createPath.createFilePath(directory);
        if (!path.exists()) {
            boolean status = path.mkdirs();
            Log.i("URLCount Path Created :", String.valueOf(status));
        }
        file = new File(path, fileName);

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
                myOutWriter.write(urlCount);
            }

            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            Log.e("File Not Found :", e.toString());
        } catch (IOException e) {
            Log.e("IOException :", e.toString());
        }

    }
    public String readSleep(String dir , String fileName) {

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

    private boolean hasPermission(Context context , String... permissions){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M && context != null && permissions!=null){
            for(String permission : permissions){
                if(ActivityCompat.checkSelfPermission(context,permission)!= PackageManager.PERMISSION_GRANTED){

                    return false;
                }
            }
        }
        return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission Granted" , Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this , "Permission Denied" , Toast.LENGTH_SHORT).show();
            }
        }

        // code to relaunch activity
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

}
