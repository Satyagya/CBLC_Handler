package com.example.CBLC_Handler.services.implementation;

import com.amazonaws.services.s3.AmazonS3;
import com.example.CBLC_Handler.config.S3config;
import com.example.CBLC_Handler.entities.InputFiles;
import com.example.CBLC_Handler.entities.PartsOfInputFiles;
import com.example.CBLC_Handler.repositories.InputFilesRepository;
import com.example.CBLC_Handler.repositories.PartsOfInputFilesRepository;
import com.example.CBLC_Handler.services.CBLCHandlerService;
import com.example.CBLC_Handler.services.helpers.Notifiers;
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
import java.util.UUID;

import static com.example.CBLC_Handler.constants.Constants.*;


@Service
@Slf4j
public class CBLCHandlerServiceImpl implements CBLCHandlerService {

    @Autowired
    private S3config s3config;

    @Value("${aws.bucket.name.FULL_INPUT_FILE_PATH}")
    private String FULL_INPUT_FILE_PATH;

    @Value("${aws.bucket.name.PART_LCOUTPUT_FILE_PATH}")
    private String PART_LCOUTPUT_FILE_PATH;

    @Value("${aws.bucket.name.PART_INPUT_FILE_PATH}")
    private String PART_INPUT_FILE_PATH;


    @Autowired
    private Notifiers notifier;

    @Autowired
    private InputFilesRepository inputFilesRepository;

    @Autowired
    private PartsOfInputFilesRepository partsOfInputFilesRepository;



    // START PROCESS FOR FILE //
    @Override
    public void startProcess(String inputFilePath, int userPreference, String emailID)
    {
        try
        {
            storeFileNameInInputFilesTable(inputFilePath,userPreference,emailID);
            uploadFileInS3(inputFilePath, FULL_INPUT_FILE_PATH);
            divideFileInPartsStoreInS3AndPartsOfInputFiles(inputFilePath, userPreference, emailID);

        }
        catch(Exception e)
        {
            log.info("AN ERROR OCCURRED WHILE UPLOADING THE MAIN FILE");
            log.info("ERROR: " + e.toString());
        }

    }


    void storeFileNameInInputFilesTable(String inputFilePath, int userPreference, String emailID)
    {
        String fileName = getFileName(inputFilePath);
        Integer noOfParts = getNoOfParts(inputFilePath);
        InputFiles inputFiles = new InputFiles();
        inputFiles.setInput_FileName(fileName);
        inputFiles.setNo_Of_Parts(noOfParts);
        inputFiles.setStage(0);
        inputFiles.setStage_Required(6);
        inputFiles.setUpload_Date(LocalDateTime.now().toString());
        inputFiles.setUser_preference(userPreference);
        inputFiles.setEmailID(emailID);
        inputFiles.setEmailSentStatus(NOT_SENT);
        inputFilesRepository.save(inputFiles);
    }

    String getFileName(String inputFilePath)
    {
        String[] partsOfPath = inputFilePath.split("/");
        return partsOfPath[partsOfPath.length-1];
    }

    Integer getNoOfParts(String inputFilePath)
    {
        Integer rows = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            while (reader.readLine() != null) rows++;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if((rows%ROWS_IN_ONE_PART)==0)
            return (rows/ROWS_IN_ONE_PART);
        return ((rows/ROWS_IN_ONE_PART)+1);
    }

    void divideFileInPartsStoreInS3AndPartsOfInputFiles(String inputFilePath, int userPreference, String emailID)
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
                    storePartInS3AndPartsOfInputFiles(rowsList, inputFilePath, partNo++,userPreference, emailID);
                    rowsList.clear();
                }
            }
            if(rowsList.isEmpty()==false)
            {
                storePartInS3AndPartsOfInputFiles(rowsList,inputFilePath,partNo, userPreference, emailID);
            }

        } catch (IOException e) {
            log.error("ERROR WHILE MAKING PARTS OF FILE: "+e.toString());
            notifier.notifySlack("Error occurred while DIVIDING FILES: "+e.toString());
        }


    }



    void storePartInS3AndPartsOfInputFiles(List<String[]> rowsList, String inputFilePath, int partNo, int userPreference, String emailID)
    {
        try
        {
            String partFilePath = pathToSavePartFile + "/p" + partNo + "_" + getFileName(inputFilePath) ;
            FileWriter partFile = new FileWriter(partFilePath);
            CSVWriter writer = new CSVWriter(partFile);

            if(userPreference==1)
            {
                String[] header = {"id", "Full name", "Profile url", "First name", "Last name", "Avatar", "Title", "Company", "Position", "Function", "Size", "Country", "Linkedin URL1"};
                if(partNo!=1) {writer.writeNext(header);}
            }
            else
            {
                String[] header = {"id", "Full name", "Profile url", "First name", "Last name", "Avatar", "Title", "Company", "Position", "Function", "Size", "Country"};
                if(partNo!=1) {writer.writeNext(header);}
            }

            writer.writeAll(rowsList);
            writer.flush();
            writer.close();
            log.info("FILE PART MADE " + partNo);
            if(userPreference == 1)
            {
                uploadFileInS3(partFilePath, PART_LCOUTPUT_FILE_PATH);
            }
            else
            {
                uploadFileInS3(partFilePath,PART_INPUT_FILE_PATH);
            }

            storePartFileNameInPartsOfInputFilesTable(partFilePath, userPreference, emailID);

        }
        catch(Exception e)
        {
            log.error("ERROR WHILE UPLOADING TO S3 FOR FILE: "+e.toString());
        }

    }


    void storePartFileNameInPartsOfInputFilesTable(String partFilePath, int userPreference, String emailID)
    {
        try
        {
            String fileName = getFileName(partFilePath);
            PartsOfInputFiles inputFilePart = new PartsOfInputFiles();
            inputFilePart.setInput_FileName_With_part(fileName);
            if(userPreference==0)
            {
                inputFilePart.setLc_Status(SCHEDULED);
                inputFilePart.setDc_Status(NOT_REQUIRED);
            }
            else if(userPreference==2)
            {
                inputFilePart.setLc_Status(SCHEDULED);
                inputFilePart.setDc_Status(NOT_SCHEDULED);
            }
            else
            {
                inputFilePart.setLc_Status(NOT_REQUIRED);
                inputFilePart.setDc_Status(SCHEDULED);
            }
            inputFilePart.setEg_Found_Status(NOT_SCHEDULED);
            inputFilePart.setEg_NotFound_Status(NOT_SCHEDULED);
            inputFilePart.setEmailID(emailID);
            inputFilePart.setStatus(NOT_COMPLETED);
            inputFilePart.setUser_preference(userPreference);
            partsOfInputFilesRepository.save(inputFilePart);


        }
        catch (Exception e)
        {
            log.info("CANNOT STORE FILE IN DATABASE: " + e.toString());
        }
    }


    @Override
    public void uploadFileInS3(String filePath, String s3BucketPath)
    {
        try
        {
            File file = new File(filePath);
            log.info("UPLOADING THE FILE {} INTO S3 BUCKET {}", file.getName(), s3BucketPath);
            AmazonS3 s3 = s3config.getS3Client();
            s3.putObject(s3BucketPath, file.getName(), file);

        }
        catch (Exception e) {
            String errorMessage = "ERROR OCCURRED WHILE UPLOADING THE LEADS DATA INTO S3 BUCKET DATA, ERROR: " +e;
            notifier.notifySlack("ERROR OCCURRED WHILE UPLOADING THE LEADS DATA INTO S3 BUCKET DATA, ERROR"+errorMessage);
            log.error("ERROR WHILE UPLOADING INT THE S3 BUCKET {} DUE TO {}", s3BucketPath, e);
        }
    }

}
