package com.example.CBLC_Handler.services;

public interface CBLCHandlerService {

    void startProcess(String imputFilePath, int user_preference, String email);
    void uploadFileInS3(String filePath, String s3bucket);
}
