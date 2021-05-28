package com.example.geckoview;

import com.example.geckoview.pojos.GetPayLoadResponse;
import com.example.geckoview.pojos.UploadPayloadResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface APIInterface {


    @GET("/getPayload")
    Call<GetPayLoadResponse> getPayoad(@Query("deviceId") String deviceId);

    @Multipart
    @POST("/uploadZipData")
    Call<UploadPayloadResponse> uploadData(@Query("deviceId") String deviceID, @Query("requestId") String requestId, @Query("status") String status, @Part MultipartBody.Part file);
}