package com.example.webload;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.webload.androidFileSystem.CreatePath;
import com.example.webload.androidFileSystem.FileStructure;
import com.example.webload.androidFileSystem.JsonUpdate;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    FileStructure fileStructure;
    int count;
    int requestId;
    private CreatePath createPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileStructure = JsonUpdate.getJson();
        createPath = new CreatePath();

        File dir = createPath.createFilePath("/MyAlbums/FlightMode/");
        deleteRecursive(dir);

        count = fileStructure.getUrlCount();
        requestId = fileStructure.getRequestId();

        if (count < fileStructure.getProfile().size()) {

            Intent intent = new Intent(MainActivity.this, NewActivity.class);
            intent.putExtra("url", fileStructure.getProfile().get(count));
            intent.putExtra("urlCount", count);
            startActivity(intent);
        } else {

            String SDPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String dataPath = SDPath + "/DCIM/MyAlbums/Html/" + DeviceId.deviceId(getApplicationContext()) + "_" + requestId;
            String zipPath = SDPath + "/DCIM/MyAlbums/";


            File zipFile = new File(zipPath, DeviceId.deviceId(getApplicationContext()) + requestId + ".zip");

            if (!zipFile.exists()) {
                if (FileHelper.zip(dataPath, zipPath, DeviceId.deviceId(getApplicationContext()) + "_" + requestId + ".zip", false)) {
                    Toast.makeText(MainActivity.this, "Zip successfully.", Toast.LENGTH_LONG).show();


                }
            }

            createPath.createFilePath("/upload/");

            String requestId1 = DeviceId.deviceId(getApplicationContext()) + "_" + requestId;
            File oldFolder1 = new File(Environment.getExternalStorageDirectory(), "/DCIM/MyAlbums/Html/" + requestId1);
            File newFolder1 = new File(Environment.getExternalStorageDirectory(), "DCIM/upload/" + requestId1);
            boolean success1 = oldFolder1.renameTo(newFolder1);
            if (!success1)
                Log.e("Upload :", "failure Change Path Process");

            File oldFolder = new File(Environment.getExternalStorageDirectory(), "/DCIM/MyAlbums");
            File newFolder = new File(Environment.getExternalStorageDirectory(), "DCIM/" + requestId);
            boolean success = oldFolder.renameTo(newFolder);

            createUploadFile("upload.txt");

            Toast.makeText(this, String.valueOf(success), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, IntermediateActivity.class);
            startActivity(intent);


        }

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

    public void createUploadFile(String fileName) {

        File file;
        File path = createPath.createFilePath("/");
        file = new File(path, fileName);

        if (!file.exists()) {
            try {
                boolean newFile = file.createNewFile();
                Log.i("Upload File Created :", String.valueOf(newFile));
            } catch (IOException e) {
                Log.i("File Not Created : ", e.toString());
            }
        }
    }


}

