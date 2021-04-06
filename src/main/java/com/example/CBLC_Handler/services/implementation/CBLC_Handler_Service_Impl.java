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

    @Autowired
    private Input_FilesRepository input_filesRepository;

//    @Value("${aws.bucket.input.file}")
//    private String awsBucketInputFile;

    @Override
    public void start_Process(String inputFilePath) {

        try
        {
            //upload_File_In_S3(inputFilePath, awsBucketInputFile);

            store_FileName_In_Input_Files_Table(inputFilePath);

            divide_FileInParts_Store_InS3And_PartsOfInputFiles(inputFilePath);

        }
        catch(Exception e)
        {
            System.out.println("sorry");
        }
        
    }


    void store_FileName_In_Input_Files_Table(String inputFilePath)
    {
        String fileName = get_fileName(inputFilePath);
        Integer noOfParts = get_noOfParts(inputFilePath);
        Input_Files input_file = new Input_Files();
        input_file.setInput_FileName(fileName);
        input_file.setNo_Of_Parts(noOfParts);
        input_file.setStage(0);
        input_file.setStage_Required(6);
        input_file.setUpload_Date(LocalDateTime.now().toString());
        input_filesRepository.save(input_file);
    }

    String get_fileName(String inputFilePath)
    {
        String[] partsOfPath = inputFilePath.split("/");
        return partsOfPath[partsOfPath.length-1];
    }

    Integer get_noOfParts(String inputFilePath)
    {
        Integer rows = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            while (reader.readLine() != null) rows++;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ((rows/ROWS_IN_ONE_PART)+1);
    }

    void divide_FileInParts_Store_InS3And_PartsOfInputFiles(String inputFilePath)
    {
        List<String[]> rowsList = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(inputFilePath);
            CSVReader csvReader = new CSVReader(fileReader);
            String[] nextRecord;
            int count = 0;
            int partNo = 1;
            while ((nextRecord = csvReader.readNext()) != null) {
                rowsList.add(nextRecord);
                count++;
                if( (count % ROWS_IN_ONE_PART) == 0 )
                {
                    store_Part_In_S3_And_PartsOfInputFiles(rowsList, inputFilePath, partNo++);
                    rowsList.clear();
                }
            }
            if(rowsList.isEmpty()==false)
            {
                store_Part_In_S3_And_PartsOfInputFiles(rowsList,inputFilePath,partNo);
            }

        } catch (IOException e) {
            log.error("Error occurred while making parts of the file: "+e.toString());
            //notifier.notifySlack("Error occurred while checking duplicates: "+e.toString());
        }


    }



    void store_Part_In_S3_And_PartsOfInputFiles(List<String[]> rowsList, String inputFilePath, int partNo)
    {
        try
        {
            FileWriter partFile = new FileWriter(pathToSavePartFile + "/p" + partNo + "_" + get_fileName(inputFilePath) );
            CSVWriter writer = new CSVWriter(partFile);
            String[] header = {"NAME", "WEBSITE", "PROFILE_URL", "FIRST_NAME", "LAST_NAME", "MATCH_DESIGNATION", "EMAIL_ID"};
            writer.writeNext(header);

            int countOfRows =0;
            for (String[] nextRecord : rowsList)
            {
                //[] row = {nextRecord[0], nextRecord[1], nextRecord[2], nextRecord[3], nextRecord[4], nextRecord[5]};
                writer.writeNext(nextRecord);
                countOfRows++;
            }
            System.out.println("rows inserted in the part file {}"+ countOfRows);
            upload_File_In_S3()

        }
        catch(Exception e)
        {
            System.out.println("Error while making part of the file");
        }


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
