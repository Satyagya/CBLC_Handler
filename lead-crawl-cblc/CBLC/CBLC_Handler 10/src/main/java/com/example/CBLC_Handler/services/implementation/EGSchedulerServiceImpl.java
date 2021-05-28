package com.example.CBLC_Handler.services.implementation;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.CBLC_Handler.config.S3config;
import com.example.CBLC_Handler.entities.PartsOfInputFiles;
import com.example.CBLC_Handler.repositories.PartsOfInputFilesRepository;
import com.example.CBLC_Handler.services.EGSchedulerService;
import com.example.CBLC_Handler.services.helpers.Notifiers;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.CBLC_Handler.constants.Constants.*;

@Service
@Slf4j
public class EGSchedulerServiceImpl implements EGSchedulerService {

    @Autowired
    private Notifiers notifier;

    int processing=0;

    @Value("${aws.bucket.name.EmailGen_InputFolder_Path}")
    private String EmailGen_InputFolder_Path;

    @Value("${aws.bucket.name.EmailGen_OutputFolder_Path}")
    private String EmailGen_OutputFolder_Path;

    @Autowired
    private S3config s3config;

    @Autowired
    private PartsOfInputFilesRepository partsOfInputFilesRepository;

    private RedissonClient redissonClient;

    @PostConstruct
    private void init() {
        redissonClient = Redisson.create();
    }

    @Override
    @Scheduled(fixedDelayString = "${poll.frequency.millis}")
    public void hitECService() {
        String EGfileName = null;
        try {
            PartsOfInputFiles nextRowWithEGStatusSC = null;
            boolean DNFfile = false;
            RLock rLock = getRLock();
            if (rLock != null)
            {
                if (partsOfInputFilesRepository.SC_EG_FileCount() > 0 && partsOfInputFilesRepository.IP_EG_FileCount()==0 && partsOfInputFilesRepository.IP_EG_NF_FileCount()==0)
                {
                    nextRowWithEGStatusSC = partsOfInputFilesRepository.get_Next_Part_File_ForEG().get(0);
                    nextRowWithEGStatusSC.setEg_Found_Status(IN_PROGRESS);
                    partsOfInputFilesRepository.save(nextRowWithEGStatusSC);
                    log.info("FILE SCHEDULED FOR EMAIL GENERATION IS: " + nextRowWithEGStatusSC.getInput_FileName_With_part());
                    processing=0;
                }
                else if (partsOfInputFilesRepository.SC_EG_FileCount_ForFNF() > 0 && partsOfInputFilesRepository.IP_EG_NF_FileCount()==0 && partsOfInputFilesRepository.IP_EG_FileCount()==0)
                {
                    nextRowWithEGStatusSC = partsOfInputFilesRepository.get_Next_Part_File_ForEG_ForFNF().get(0);
                    nextRowWithEGStatusSC.setEg_NotFound_Status(IN_PROGRESS);
                    DNFfile = true;
                    partsOfInputFilesRepository.save(nextRowWithEGStatusSC);
                    log.info("FILE SCHEDULED FOR EMAIL GENERATION IS: " + addNFAtEnd(nextRowWithEGStatusSC.getInput_FileName_With_part()));
                    processing=0;
                }
            }
            rLock.unlock();

            //hit EMAIL GENERATION API HERE//
            if(nextRowWithEGStatusSC!=null)
            {
                if(DNFfile == true)
                {
                    EGfileName = addOutputAtEnd(addNFAtEnd(nextRowWithEGStatusSC.getInput_FileName_With_part()));
                }
                else
                {
                    EGfileName = addOutputAtEnd(nextRowWithEGStatusSC.getInput_FileName_With_part());
                }

                //PICK FILE FROM S3 BUCKET
                String pathForEGInputFile = EMAIL_GEN_LOCAL_PATH + EGfileName;
                String s3BucketPath = EmailGen_InputFolder_Path;
                takeFileFromS3(EGfileName, s3BucketPath, pathForEGInputFile);

                String query_url = EG_API;
                JSONObject json = new JSONObject();
                json.put("csvPath",pathForEGInputFile);
                try {
                    URL url = new URL(query_url);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestMethod("POST");
                    OutputStream os = conn.getOutputStream();
                    os.write(json.toJSONString().getBytes("UTF-8"));
                    os.close();
                    // read the response
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    String result = IOUtils.toString(in, "UTF-8");
                    log.info(result);
                    in.close();
                    conn.disconnect();
                } catch (Exception e) {
                    log.info(String.valueOf(e));
                }
            }
            else
            {
                processing++;
                log.info("NO FILES SCHEDULED FOR EMAIL GENERATION, RUNNING:" + processing);
                if(processing>1000000)
                {
                    log.info("DOMAIN GENERATION IS STUCK*******************************************************************");
                    processing=0;
                }
            }


        }
        catch (Exception e)
        {
            log.info("ERROR WHILE DOING EMAIL GENERATION FOR FILE: " + EGfileName);
            //notifier.notifySlack("ERROR WHILE DOING EMAIL GENERATION FOR FILE: " + EGfileName);
        }
    }




    String addNFAtEnd(String fileName)
    {
        return fileName.split("\\.")[0] + "_NF.csv";
    }

    String addOutputAtEnd(String fileName)
    {
        return fileName.split("\\.")[0] + "_output.csv";
    }

    String removeNFAtEnd(String fileName)
    {
        String[] fileNameParts = fileName.split("_NF");
        return fileNameParts[0]  + fileNameParts[1] ;
    }

    String removeOutputAtEnd(String fileName)
    {
        String[] fileNameParts = fileName.split("_output");
        return fileNameParts[0] + fileNameParts[1] ;
    }

    private RLock getRLock() {
        RLock rLock = null;
        try {
            rLock = redissonClient.getLock("GLOBAL_LOCK");
            rLock.lock();
        } catch (Exception e) {
            log.error("EXCEPTION WHILE GETTING LOCK{}", e);
        }
        return rLock;
    }

    @Override
    public void updateTableAfterEG(String fileName) throws IOException {

        String filePath = EMAIL_GEN_LOCAL_PATH + addFinalOutput(fileName);
        uploadFileInS3(filePath,EmailGen_OutputFolder_Path);

        fileName = removeOutputAtEnd(fileName);
        if(nameContains_NF(fileName))
        {
            PartsOfInputFiles next_row_WithEGStatus_SC = partsOfInputFilesRepository.get_Part_WithName(removeNFAtEnd(fileName)).get(0);
            next_row_WithEGStatus_SC.setEg_NotFound_Status(COMPLETED);
            partsOfInputFilesRepository.save(next_row_WithEGStatus_SC);
        }
        else
        {
            PartsOfInputFiles next_row_WithEGStatus_SC = partsOfInputFilesRepository.get_Part_WithName(fileName).get(0);
            next_row_WithEGStatus_SC.setEg_Found_Status(COMPLETED);
            partsOfInputFilesRepository.save(next_row_WithEGStatus_SC);
        }
    }

    boolean nameContains_NF(String fileName)
    {
        String[] fileNameParts = fileName.split("_");
        if(fileNameParts.length==2)
            return false;
        else
            return true;
    }

    String addFinalOutput(String fileName)
    {
        String[] fileNameParts = fileName.split("_output");
        return fileNameParts[0]  + "_final-output.csv";
    }

    void takeFileFromS3(String fileName, String s3BucketPath, String filePath)
    {
        try
        {
            File file = new File(filePath);
            AmazonS3 s3 = s3config.getS3Client();
            ObjectMetadata object = s3.getObject(new GetObjectRequest(s3BucketPath, fileName), file);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void uploadFileInS3(String filePath, String s3BucketPath)
    {
        try
        {
            File file = new File(filePath);
            log.info("Uploading the file {} into s3 bucket {}", file.getName(), s3BucketPath);
            AmazonS3 s3 = s3config.getS3Client();
            s3.putObject(s3BucketPath, file.getName(), file);

        }
        catch (Exception e) {
            String errorMessage = "ERROR OCCURRED WHILE UPLOADING THE LEADS DATA INTO S3 BUCKET DATA, ERROR: " +e;
            //notifier.notifySlack("ERROR OCCURRED WHILE UPLOADING THE LEADS DATA INTO S3 BUCKET DATA, ERROR" + errorMessage);
            log.error("ERROR WHILE UPLOADING INT THE S3 BUCKET {} DUE TO {}", s3BucketPath, e);
        }
    }


}
