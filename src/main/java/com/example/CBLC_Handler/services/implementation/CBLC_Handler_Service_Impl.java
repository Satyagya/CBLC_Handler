package com.example.CBLC_Handler.services.implementation;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.example.CBLC_Handler.config.S3config;
import com.example.CBLC_Handler.entities.Input_Files;
import com.example.CBLC_Handler.repositories.Input_FilesRepository;
import com.example.CBLC_Handler.services.CBLC_Handler_Service;
//import com.example.CBLC_Handler.services.helpers.Notifiers;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import static com.example.CBLC_Handler.constants.Constants.*;

@Slf4j
@Service
public class CBLC_Handler_Service_Impl implements CBLC_Handler_Service {

    @Autowired
    private S3config s3config;

//    @Autowired
//    private Notifiers notifier;
//
//    @Value("${aws.bucket.input.file}")
//    private String awsBucketInputFile;

    @Autowired
    private Input_FilesRepository input_filesRepository;

    @Autowired
    private Stage_0_Impl stage0;


    @Override
    public void start_Process(String inputFilePath, int stage) {
        try
        {
            file_name_formater(inputFilePath);

            String awsBucketInputFile="";
            upload_File_In_S3(inputFilePath, awsBucketInputFile);
            store_FileName_In_Input_Files_Table(inputFilePath, stage);
            stage0.divide_FileInParts_Store_InS3And_PartsOfInputFiles(inputFilePath);
        }
        catch(Exception e)
        {
            System.out.println("File Uploading Failed...");
        }
    }

    void file_name_formater(String inputFilePath)
    {

    }


    void store_FileName_In_Input_Files_Table(String inputFilePath, int stage)
    {
        String fileName = get_FileName(inputFilePath);
        Integer noOfParts = get_NoOfParts(inputFilePath);
        Input_Files input_file = new Input_Files();
        input_file.setInput_FileName(fileName);
        input_file.setNo_Of_Parts(noOfParts);
        input_file.setStage(0);
        input_file.setStage_Required(stage);
        input_file.setUpload_Date(LocalDateTime.now().toString());
        input_filesRepository.save(input_file);
    }

    String get_FileName(String inputFilePath)
    {
        String[] partsOfPath = inputFilePath.split("/");
        //FORMAT FOR FILE NAME????
        return partsOfPath[partsOfPath.length-1];
    }

    Integer get_NoOfParts(String inputFilePath)
    {
        Integer rows = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            while (reader.readLine() != null) rows++;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ((rows/ROWS_IN_ONE_PART)+1);
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
