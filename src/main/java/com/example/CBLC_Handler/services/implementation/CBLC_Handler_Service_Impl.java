package com.example.CBLC_Handler.services.implementation;

import com.amazonaws.services.s3.AmazonS3;
import com.example.CBLC_Handler.config.S3config;
import com.example.CBLC_Handler.entities.Input_Files;
import com.example.CBLC_Handler.repositories.Input_FilesRepository;
import com.example.CBLC_Handler.services.CBLC_Handler_Service;
//import com.example.CBLC_Handler.services.helpers.Notifiers;
import com.example.CBLC_Handler.services.helpers.FileMerger;
import com.example.CBLC_Handler.services.helpers.FileNameFormatter;
import com.example.CBLC_Handler.services.helpers.S3_Functions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;

import static com.example.CBLC_Handler.constants.Constants.*;

@Slf4j
@Service
public class CBLC_Handler_Service_Impl implements CBLC_Handler_Service {

    @Autowired
    private S3config s3config;

    @Value("${aws.bucket.name.FULL_INPUT_FILE_PATH}")
    private String FULL_INPUT_FILE_PATH;

//    @Autowired
//    private Notifiers notifier;
//
//    @Value("${aws.bucket.input.file}")
//    private String awsBucketInputFile;

    @Autowired
    private Input_FilesRepository input_filesRepository;

    @Autowired
    private Input_Files input_files;

    @Autowired
    private S3_Functions s3_functions;

    @Autowired
    private FileNameFormatter file_name_formatter;

    @Autowired
    private FileMerger fileMerger;

    @Autowired
    private Stage_0_Impl stage0;

    @Autowired
    private Stage_1_Impl stage1;

    @Autowired
    private Stage_2_Impl stage2;


    @Override
    public void start_Process(String inputFilePath, int stage_start, int stage_end) {
        try
        {
            s3_functions.upload_File_In_S3(inputFilePath, FULL_INPUT_FILE_PATH);
            store_FileName_In_Input_Files_Table(inputFilePath, stage_start, stage_end);
            String filename = get_FileName(inputFilePath);

            int curr_stage = stage_start;
            while(curr_stage<=stage_end)
            {
                switch(curr_stage)
                {
                    case 0:
                        stage0.divide_FileInParts_Store_InS3And_PartsOfInputFiles(inputFilePath);
                        break;

                    case 1:
                        stage1.SendFileForLeadCrawl(filename);
                        break;

                    case 2:

                    case 3:

                    case 4:

                    case 5:

                    case 6:
                }
                file_name_formatter.name_formatter(filename,curr_stage);
                curr_stage++;
                input_files.setStage_curr(curr_stage);
            }
            if(curr_stage==1||curr_stage==3)
            {
                fileMerger.MergeFiles(filename,curr_stage);
                //send email
            }
        }
        catch(Exception e)
        {
            System.out.println("File Uploading Failed...");
        }
    }

    void store_FileName_In_Input_Files_Table(String inputFilePath, int stage_start, int stage_end)
    {
        String fileName = get_FileName(inputFilePath);
        Integer noOfParts = get_NoOfParts(inputFilePath);
        Input_Files input_file = new Input_Files();
        input_file.setInput_FileName(fileName);
        input_file.setNo_Of_Parts(noOfParts);
        input_file.setStage_start(stage_start);
        input_file.setStage_end(stage_end);
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
