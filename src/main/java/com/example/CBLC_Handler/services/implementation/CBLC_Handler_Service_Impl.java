package com.example.CBLC_Handler.services.implementation;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.example.CBLC_Handler.config.S3config;
import com.example.CBLC_Handler.repository.FileRepository;
import com.example.CBLC_Handler.services.CBLC_Handler_Service;
import com.example.CBLC_Handler.services.helpers.Notifiers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

@Slf4j
public class CBLC_Handler_Service_Impl implements CBLC_Handler_Service {

    @Autowired
    private S3config s3config;

    @Autowired
    private Notifiers notifier;

    @Autowired
    FileRepository fileRepository;

    @Override
    public void start_Process(String filePath, int stage) {
        String orinialFilePath = "s3://";
        upload_File_In_S3(filePath, orinialFilePath);
        int total_row_count = totalRowCountFinder(filePath);
        float temporary_part_count = total_row_count/500;
        int numberOfParts = total_row_count/500;
        if(temporary_part_count!=numberOfParts) numberOfParts+=numberOfParts;

        //create Input_File object

//        fileRepository.save(Input_File_Obj);
//        fileBreaker();
    }

    int totalRowCountFinder(String filePath)
    {
        int totalRows=0;
        //code
        return totalRows;
    }

    void upload_File_In_S3(String filePath, String s3BucketPath)
    {
        try
        {
            File file = new File(filePath);
            log.info("Uploading the file {} into s3 bucket {}", file.getName(), s3BucketPath);
            AmazonS3 s3 = s3config.getS3Client();
            s3.putObject(s3BucketPath, file.getName(), file);

        }
        catch (Exception e) {
            String errorMessage = "Error occurred while uploading the leads data into s3 bucket due to "+e;
            //notifier.notifySlack(errorMessage);
            log.error("Error while uploading the file in s3 bucket {} due to {}", s3BucketPath, e);
        }
    }


}
