package com.example.geckoview;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.geckoview.androidFileSystem.CreatePath;
import com.example.geckoview.androidFileSystem.FileStructure;
import com.example.geckoview.androidFileSystem.JsonUpdate;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class IntermediateActivity extends AppCompatActivity {

    public static final String SLEEP_MESSAGE = "Device on sleep for 90 minutes \n Timer Started at : ";
    FileStructure fileStructure;
    CreatePath createPath;
    EditText setTimer;
    private int STORAGE_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermediate);

        String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        createPath = new CreatePath();
        setTimer = findViewById(R.id.editText1);


        File dir = createPath.createFilePath("/MyAlbums/FlightMode/");
        deleteRecursive(dir);

        if (!hasPermission(IntermediateActivity.this, permission)) {
            ActivityCompat.requestPermissions(this, permission, STORAGE_PERMISSION_CODE);

        }

        File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/MyAlbums/");
        if (!path.exists()) {
            boolean mkdirs = path.mkdirs();
            Log.i("Create Folder Status : ", String.valueOf(mkdirs));
        }
        File file = new File(path, "detail.txt");
        if (!file.exists()) {
            try {
                boolean newFile = file.createNewFile();
                Log.i("Json Created : ", String.valueOf(newFile));
            } catch (IOException e) {
                Log.e("Json Creation Error :", Arrays.toString(e.getStackTrace()));
            }

            fileStructure = new FileStructure();
            boolean updateStatus = JsonUpdate.setJson(fileStructure);
            Log.i("Json Updated : ", String.valueOf(updateStatus));

            goToGetPayload(2);
        } else {
            fileStructure = JsonUpdate.getJson();
            if (fileStructure.getRequestId() == 0) {

                goToGetPayload(2);
            } else {
                String[] repeatCount = fileStructure.getSleepCount().split("_");

                int totalRepeatCount = Integer.parseInt(repeatCount[2]);
                int currentUrlRepeatCount = Integer.parseInt(repeatCount[1]);
                if (totalRepeatCount >= 20) {

                    Date date = Calendar.getInstance().getTime();
                    setTimer.setText(String.format("%s%s", SLEEP_MESSAGE, date));

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                TimeUnit.MINUTES.sleep(90);
                            } catch (InterruptedException e) {
                                Log.e("Error in Sleep :", Arrays.toString(e.getStackTrace()));

                            }
                            int count = 1 + Integer.parseInt(repeatCount[0]);
                            String sleepUpdate = count + "_0_0";
                            fileStructure.setSleepCount(sleepUpdate);
                            fileStructure.setUrlCount(count);
                            JsonUpdate.setJson(fileStructure);
                        }
                    }, 2000);


                    goToMainWithWait(3);

                } else if (currentUrlRepeatCount >= 5) {
                    int count = 1 + Integer.parseInt(repeatCount[0]);
                    String sleepUpdate = count + "_0_" + totalRepeatCount;
                    fileStructure.setSleepCount(sleepUpdate);
                    fileStructure.setUrlCount(count);
                    JsonUpdate.setJson(fileStructure);
                    goToMainWithWait(3);
                } else {
                    goToMainWithWait(3);

                }


            }
        }
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

    public void goToGetPayload(int i) {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent1 = new Intent(IntermediateActivity.this, GetPayloadActivity.class);
                startActivity(intent1);

            }
        }, i * 1000);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }

        // code to relaunch activity
        Intent intent = getIntent();
        finish();
        startActivity(intent);
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

    private boolean hasPermission(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {

                    return false;
                }
            }
        }
        return true;

    }
}

