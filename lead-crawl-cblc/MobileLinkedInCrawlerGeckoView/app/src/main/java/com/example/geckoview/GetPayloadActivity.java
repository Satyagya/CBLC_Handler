package com.example.geckoview;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.geckoview.androidFileSystem.CreatePath;
import com.example.geckoview.androidFileSystem.FileStructure;
import com.example.geckoview.androidFileSystem.JsonUpdate;
import com.example.geckoview.pojos.GetPayLoadResponse;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetPayloadActivity extends AppCompatActivity {

    CreatePath createPath;
    FileStructure fileStructure;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_payload);
        startRepeating();

        fileStructure = JsonUpdate.getJson();
        createPath = new CreatePath();

    }

    public void startRepeating() {
        apiCall.run();

    }

    public void stopRepeating() {

        mHandler.removeCallbacks(apiCall);
    }

    private Runnable apiCall = new Runnable() {
        @Override
        public void run() {

            //
            App.getRetrofit().create(APIInterface.class).getPayoad(DeviceId.deviceId(getApplicationContext())).enqueue(new Callback<GetPayLoadResponse>() {
                @Override
                public void onResponse(Call<GetPayLoadResponse> call, Response<GetPayLoadResponse> response) {


                    GetPayLoadResponse getPayLoadResponse = response.body();

                    if (response.code() == 200) {

                        if (getPayLoadResponse.getREQUEST_ID() == 0) {

                            Log.e("Invalid RequestId :", String.valueOf(getPayLoadResponse.getREQUEST_ID()));
                        } else {
                            fileStructure.getProfile().addAll(getPayLoadResponse.getPROFILE_URL());
                            fileStructure.setRequestId(getPayLoadResponse.getREQUEST_ID());
                            JsonUpdate.setJson(fileStructure);
                            stopRepeating();
                            Intent intent = new Intent(GetPayloadActivity.this, IntermediateActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(GetPayloadActivity.this, String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                    }


                }

                @Override
                public void onFailure(Call<GetPayLoadResponse> call, Throwable t) {

                    Log.i("PayLoad Not Received", Arrays.toString(t.getStackTrace()));
                }
            });
            mHandler.postDelayed(apiCall, 30 * 10 * 1000);
        }
    };
}
