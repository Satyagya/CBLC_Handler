package com.example.CBLC_Handler.services.implementation;

import com.amazonaws.services.s3.AmazonS3;
import com.example.CBLC_Handler.config.S3config;
import com.example.CBLC_Handler.entities.Input_Files;
import com.example.CBLC_Handler.repositories.Input_FilesRepository;
import com.example.CBLC_Handler.services.CBLC_Handler_Service;
//import com.example.CBLC_Handler.services.helpers.Notifiers;
import com.example.CBLC_Handler.services.helpers.S3_Functions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;

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
    private S3_Functions s3_functions;

    @Autowired
    private Stage_0_Impl stage0;

    @Autowired
    private Stage_1_Impl stage1;


    @Override
    public void start_Process(String inputFilePath, int stage) {
        try
        {
            //file_name_formatter(inputFilePath);
            String awsBucketInputFile="";
            s3_functions.upload_File_In_S3(inputFilePath, awsBucketInputFile);
            store_FileName_In_Input_Files_Table(inputFilePath, stage);
            stage0.divide_FileInParts_Store_InS3And_PartsOfInputFiles(inputFilePath);
        }
        catch(Exception e)
        {
            System.out.println("File Uploading Failed...");
        }
    }

    void store_FileName_In_Input_Files_Table(String inputFilePath, int stage)
    {
        String fileName = get_FileName(inputFilePath);
        Integer noOfParts = get_NoOfParts(inputFilePath);
        Input_Files input_file = new Input_Files();
        input_file.setInput_FileName(fileName);
        input_file.setNo_Of_Parts(noOfParts);
        input_file.setStage(stage);
        input_file.setStage_Required(stage);
        input_file.setUpload_Date(LocalDateTime.now().toString());
        input_filesRepository.save(input_file);
    }

    String get_FileName(String inputFilePath)
    {
        String[] partsOfPath = inputFilePath.split("/");
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




}
