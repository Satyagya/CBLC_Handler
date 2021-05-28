package com.example.webload;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.webload.androidFileSystem.CreatePath;
import com.example.webload.androidFileSystem.RequestIdUpdate;
import com.example.webload.pojos.GetPayLoadResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetPayloadActivity extends AppCompatActivity {

    RequestIdUpdate requestIdUpdate;
    CreatePath createPath ;

    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_payload);
        startRepeating();

        requestIdUpdate = new RequestIdUpdate();
        createPath = new CreatePath();

    }

public void startRepeating(){
    apiCall.run();

}
public void stopRepeating(){

        mHandler.removeCallbacks(apiCall);
}
private Runnable apiCall = new Runnable() {
    @Override
    public void run() {

        App.getRetrofit().create(APIInterface.class).getPayoad(DeviceId.deviceId(getApplicationContext())).enqueue(new Callback<GetPayLoadResponse>() {
            @Override
            public void onResponse(Call<GetPayLoadResponse> call, Response<GetPayLoadResponse> response) {


                GetPayLoadResponse getPayLoadResponse = response.body();

                if(response.code()==200){

                    if(getPayLoadResponse.getREQUEST_ID()==0){

                        System.out.println(getPayLoadResponse.getPROFILE_URL()+" "+getPayLoadResponse.getREQUEST_ID());
                    }
                    else{
                        List<String> urls = getPayLoadResponse.getPROFILE_URL();
                        requestIdUpdate.setRequestId(getPayLoadResponse.getREQUEST_ID());
                        Log.i("PayLoad With RequestId:",String.valueOf(getPayLoadResponse.getREQUEST_ID()));
                        for(int i=0; i<urls.size();i++){
                            updateProfiles(urls.get(i)+"\n");
                        }
                        stopRepeating();

                        Intent intent = new Intent(GetPayloadActivity.this , IntermediateActivity.class);
                        startActivity(intent);
                    }
                }
                else{
                    Toast.makeText(GetPayloadActivity.this , String.valueOf(response.code()),Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onFailure(Call<GetPayLoadResponse> call, Throwable t) {

                Log.i("PayLoad Not Received" , t.getMessage());
            }
        });
        mHandler.postDelayed(apiCall , 30*10*1000);
    }
};

    public void updateProfiles(String txt) {
        File file;
        final File path = createPath.createFilePath("/MyAlbums/Profile");
        if (!path.exists()) {
            boolean status = path.mkdirs();
            Log.i("Profile Folder Created", String.valueOf(status));
        }
        file = new File(path,"profile.txt");

        if (!file.exists()) {
            try {
                boolean status = file.createNewFile();
                Log.i("Profile File Created :", String.valueOf(status));
            } catch (IOException e) {
                Log.e("Create URL File :", String.valueOf(e));
            }
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file , true);
            try (OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut)) {
                myOutWriter.append(txt);
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
