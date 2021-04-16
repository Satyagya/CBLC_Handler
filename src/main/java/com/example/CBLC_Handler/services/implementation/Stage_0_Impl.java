package com.example.CBLC_Handler.services.implementation;

import com.example.CBLC_Handler.entities.Parts_Of_Input_Files;
import com.example.CBLC_Handler.repositories.Input_FilesRepository;
import com.example.CBLC_Handler.repositories.Parts_Of_Input_FilesRepository;
import com.example.CBLC_Handler.services.Stage_0_Service;
import com.example.CBLC_Handler.services.helpers.S3_Functions;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.amazonaws.services.s3.AmazonS3;
import com.example.CBLC_Handler.entities.Input_Files;


import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.CBLC_Handler.constants.Constants.*;

@Slf4j
@Service
public class Stage_0_Impl implements Stage_0_Service {

    @Autowired
    S3_Functions s3_functions;

    @Value("${aws.bucket.name.PART_INPUT_FILE_PATH}")
    private String PART_INPUT_FILE_PATH;

    @Autowired
    private Input_FilesRepository input_filesRepository;

    @Autowired
    private Parts_Of_Input_FilesRepository parts_of_input_filesRepository;

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
            String partFilePath = pathToSavePartFile + "/p" + partNo + "_" + get_FileName(inputFilePath) ;
            FileWriter partFile = new FileWriter(partFilePath);
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
            s3_functions.upload_File_In_S3(partFilePath,PART_INPUT_FILE_PATH);

            store_Part_FileName_In_Parts_Of_Input_Files_Table(partFilePath);
        }
        catch(Exception e)
        {
            System.out.println("Error while making part of the file");
        }
    }

    void store_Part_FileName_In_Parts_Of_Input_Files_Table(String partFilePath)
    {
        try
        {
            String fileName = get_FileName(partFilePath);
            Parts_Of_Input_Files input_file_part = new Parts_Of_Input_Files();
            input_file_part.setInput_FileName_With_part(fileName);
            input_file_part.setLc_Status("SC");
            input_file_part.setDc_Status("NA");
            input_file_part.setEg_Found_Status("NA");
            input_file_part.setEg_NotFound_Status("NA");
            parts_of_input_filesRepository.save(input_file_part);


        }
        catch (Exception e)
        {
            System.out.println("cannot store part file in database Table");
        }
    }

    String get_FileName(String inputFilePath)
    {
        String[] partsOfPath = inputFilePath.split("/");
        return partsOfPath[partsOfPath.length-1];
    }

}
