package com.example.webload;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.webload.androidFileSystem.CountUpdate;
import com.example.webload.androidFileSystem.CreatePath;
import com.example.webload.androidFileSystem.RequestIdUpdate;
import com.example.webload.androidFileSystem.StatusUpdate;

import java.io.File;
import java.io.IOException;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    static int count;
    static String status;
    static String arr[];


    static int length;

    private CountUpdate countUpdate;
    private CreatePath createPath;
    private StatusUpdate statusUpdate;


    EditText url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createPath = new CreatePath();
        countUpdate = new CountUpdate();
        statusUpdate = new StatusUpdate();

        status = statusUpdate.readCrawlStatus();
        File dir = createPath.createFilePath("/MyAlbums/FlightMode/");
        deleteRecursive(dir);
        File file;
        final File path = createPath.createFilePath("/MyAlbums/urlCount");
        if (!path.exists()) {
            path.mkdirs();
        }
        file = new File(path, "urlCount.txt");
        if (!file.exists())
            countUpdate.updateCount(0);

        count = countUpdate.readUrlCount();
        if (count < length) {

            Intent intent = new Intent(MainActivity.this, NewActivity.class);
            intent.putExtra("url", arr[count]);
            intent.putExtra("status", status);
            count++;
            countUpdate.updateCount(count);
            startActivity(intent);
        }
        else{

            RequestIdUpdate requestIdUpdate = new RequestIdUpdate();
            String SDPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String dataPath = SDPath + "/DCIM/MyAlbums/Html/"+DeviceId.deviceId(getApplicationContext())+"_"+requestIdUpdate.getRequestId();
            String zipPath = SDPath + "/DCIM/MyAlbums/" ;


            File zipFile = new File(zipPath, DeviceId.deviceId(getApplicationContext()) + requestIdUpdate.getRequestId() + ".zip");

            if (!zipFile.exists()) {
                if (FileHelper.zip(dataPath, zipPath, DeviceId.deviceId(getApplicationContext())+"_"+requestIdUpdate.getRequestId()+".zip", false)){
                    Toast.makeText(MainActivity.this,"Zip successfully.", Toast.LENGTH_LONG).show();
                    createFlightModeFolder("upload.txt");

                }
            }

            File uploadpath = createPath.createFilePath("/upload/");

            String requestId1 = DeviceId.deviceId(getApplicationContext())+"_"+requestIdUpdate.getRequestId();
            File oldFolder1 = new File(Environment.getExternalStorageDirectory(),"/DCIM/MyAlbums/Html/"+requestId1);
            File newFolder1 = new File(Environment.getExternalStorageDirectory(),"DCIM/upload/"+requestId1);
            boolean success1 = oldFolder1.renameTo(newFolder1);



            String requestId = String.valueOf(requestIdUpdate.getRequestId());
            File oldFolder = new File(Environment.getExternalStorageDirectory(),"/DCIM/MyAlbums");
            File newFolder = new File(Environment.getExternalStorageDirectory(),"DCIM/"+requestId);
            boolean success = oldFolder.renameTo(newFolder);

            Toast.makeText(this,String.valueOf(success),Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this,IntermediateActivity.class);
            startActivity(intent);



        }

//        if (count == length && status.equals("ProfileCrawl")) {
//            Intent intent = new Intent(MainActivity.this, IntermediateActivity.class);
//            countUpdate.updateCount(0);
//            statusUpdate.updateCrawlStatus("CompanyCrawl");
//            startActivity(intent);
//        } else if (count == length && status.equals("CompanyCrawl")) {
//            Toast.makeText(MainActivity.this, "Congrates , We are finished", Toast.LENGTH_LONG).show();
//            statusUpdate.updateCrawlStatus("Complete");
//            Intent intent = new Intent(MainActivity.this, UploadDataActivity.class);
//            startActivity(intent);
//        }


    }

    public void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles())) {
                deleteRecursive(child);
            }
        }

        boolean delete = fileOrDirectory.delete();
        Log.i("Deleted flight folder ", String.valueOf(delete));
    }
    public void createFlightModeFolder(String fileName) {

        File file;
        File path = createPath.createFilePath("/");
        file = new File(path, fileName);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.i("File Not Created : ", e.toString());
            }
        }
    }




}

