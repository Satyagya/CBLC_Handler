package com.example.CBLC_Handler.services.helpers;

import com.amazonaws.services.s3.AmazonS3;
import com.example.CBLC_Handler.config.S3config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;


@Slf4j
public class S3_Functions {
    @Autowired
    private S3config s3config;

    public S3_Functions(S3config s3config) {
        this.s3config = s3config;
    }

    public void upload_File_In_S3(String filePath, String s3BucketPath)
    {
        try
        {
            File file = new File(filePath);
            log.info("Uploading the file {} into s3 bucket {}", file.getName(), s3BucketPath);
            AmazonS3 s3 = s3config.getS3Client();
            s3.putObject(s3BucketPath, file.getName(), file);

        }
        catch (Exception e) {
            String errorMessage = "Error occurred while uploading the data into s3 bucket due to "+e;
            //notifier.notifySlack(errorMessage);
            log.error("Error while uploading the file in s3 bucket {} due to {}", s3BucketPath, e);
        }
    }
}
