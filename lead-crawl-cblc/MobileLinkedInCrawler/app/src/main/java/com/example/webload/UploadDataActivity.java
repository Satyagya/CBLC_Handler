package com.example.webload;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.webload.androidFileSystem.CreatePath;
import com.example.webload.androidFileSystem.RequestIdUpdate;
import com.example.webload.pojos.UploadPayloadResponse;

import java.io.File;
import java.util.Objects;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadDataActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();
    RequestIdUpdate requestIdUpdate;
    CreatePath createPath;

    private String SDPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String dataPath = SDPath + "/DCIM/MyAlbums/Html" ;
    private String zipPath = SDPath + "/DCIM/MyAlbums/" ;
    final static String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_data);
//        apiCall.run();
        requestIdUpdate = new RequestIdUpdate();
        createPath = new CreatePath();



        File zipFile = new File(zipPath, "Html_" + requestIdUpdate.getRequestId() + ".zip");

        if (!zipFile.exists()) {
            if (FileHelper.zip(dataPath, zipPath, "Html_"+requestIdUpdate.getRequestId()+".zip", false)){
                Toast.makeText(UploadDataActivity.this,"Zip successfully.", Toast.LENGTH_LONG).show();
            }
        }



        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), zipFile);
        MultipartBody.Part multipartBody = MultipartBody.Part.createFormData("file", zipFile.getName(), requestFile);
        App.getRetrofit().create(APIInterface.class).uploadData(DeviceId.deviceId(getApplicationContext()), String.valueOf(requestIdUpdate.getRequestId()), "complete", multipartBody).enqueue(new Callback<UploadPayloadResponse>() {
            @Override
            public void onResponse(Call<UploadPayloadResponse> call, Response<UploadPayloadResponse> response) {

                if (response.code() == 200) {

                    UploadPayloadResponse uploadPayloadResponse = response.body();
                    if (uploadPayloadResponse.getMessage().equals("success")) {
                        File dir = createPath.createFilePath("/MyAlbums/");
                        deleteRecursive(dir);
                        Toast.makeText(UploadDataActivity.this, "success", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(UploadDataActivity.this, GetPayloadActivity.class);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(UploadDataActivity.this, String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onFailure(Call<UploadPayloadResponse> call, Throwable t) {

                Toast.makeText(UploadDataActivity.this, "Failure", Toast.LENGTH_SHORT).show();
                Log.e("Failure", "Not Loaded");
            }
        });
    }


    public void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles())) {
                deleteRecursive(child);
            }
        }

        boolean status = fileOrDirectory.delete();
        Log.i("Directory Deleted ", String.valueOf(status));
    }
}
