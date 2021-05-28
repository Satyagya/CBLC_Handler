package com.example.CBLC_Handler.services.implementation;

import com.amazonaws.services.s3.AmazonS3;
import com.example.CBLC_Handler.config.S3config;
import com.example.CBLC_Handler.entities.PartsOfInputFiles;
import com.example.CBLC_Handler.repositories.DomainFliesRepository;
import com.example.CBLC_Handler.repositories.PartsOfInputFilesRepository;
import com.example.CBLC_Handler.services.LCSchedulerService;


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
public class LCSchedulerServiceImpl implements LCSchedulerService {


    private RedissonClient redissonClient;
    int processing=0;

    @Autowired
    private Notifiers notifier;

    @Autowired
    private S3config s3config;

    @PostConstruct
    private void init() {
        redissonClient = Redisson.create();
    }


    @Autowired
    private PartsOfInputFilesRepository partsOfInputFilesRepository;

    @Autowired
    private DomainFliesRepository domainFliesRepository;

    @Value("${aws.bucket.name.PART_LCOUTPUT_FILE_PATH}")
    private String PART_LCOUTPUT_FILE_PATH;

    @Override
    @Scheduled(fixedDelayString = "${poll.frequency.millis}")
    public void hitLCService()
    {
        PartsOfInputFiles nextRowWithLCStatusSC = null;
        try
        {
            RLock rLock = getRLock();
            if (rLock != null)
            {
                if(partsOfInputFilesRepository.SC_LC_FileCount()>0 && partsOfInputFilesRepository.IP_LC_FileCount()==0)
                {

                    nextRowWithLCStatusSC = partsOfInputFilesRepository.get_Next_Part_File_ForLC().get(0);
                    nextRowWithLCStatusSC.setLc_Status(IN_PROGRESS);
                    partsOfInputFilesRepository.save(nextRowWithLCStatusSC);
                    log.info("FILE SCHEDULED FOR LEAD CRAWL IN INSTANCE 1 IS: "+ nextRowWithLCStatusSC.getInput_FileName_With_part());
                    processing=0;
                }
                else if(partsOfInputFilesRepository.FA_LC_FileCount()>0 && partsOfInputFilesRepository.IP_LC_FileCount()==0)
                {
                    nextRowWithLCStatusSC = partsOfInputFilesRepository.get_Next_Part_File_ForLC_FA().get(0);
                    nextRowWithLCStatusSC.setLc_Status(InPROGRESS_FAILED);
                    partsOfInputFilesRepository.save(nextRowWithLCStatusSC);
                    log.info("FILE RE-SCHEDULED FOR LEAD CRAWL IN INSTANCE 1 IS: "+ nextRowWithLCStatusSC.getInput_FileName_With_part());
                    processing=0;
                }
            }

            rLock.unlock();


                //..hiting lead crawl API..//
            if(nextRowWithLCStatusSC!=null)
            {
                String query_url = LC_API;
                JSONObject json = new JSONObject();
                json.put("file_name",nextRowWithLCStatusSC.getInput_FileName_With_part());
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
                log.info("LEAD CRAWL HAS NOT RECEIVED A FILE, RUNNING: " + processing);
                if(processing>1000000)
                {
                    log.info("LEAD CRAWL IS STUCK*******************************************************************");
                    processing=0;
                }
            }
        }
        catch(Exception e)
        {
            log.info("ERROR DOING LEAD CRAWL FOR FILE: " + e.toString());
            notifier.notifySlack("ERROR WHILE DOING LEAD CRAWL FOR FILE: " + nextRowWithLCStatusSC.getInput_FileName_With_part());
            String fileName = nextRowWithLCStatusSC.getInput_FileName_With_part();
            PartsOfInputFiles input_file_part = new PartsOfInputFiles();
            input_file_part.setInput_FileName_With_part(fileName);
            if(input_file_part.getLc_Status() == IN_PROGRESS )
            {
                log.info("RETRYING FOR FILE: " + e.toString());
                input_file_part.setLc_Status(FAILED);
            }
            else
            {
                log.info("LEADCRAWL FOR FILE :" + e.toString() + " FAILED");
                notifier.notifySlack("ERROR WHILE DOING LEAD CRAWL FOR FILE: " + nextRowWithLCStatusSC.getInput_FileName_With_part());
                input_file_part.setLc_Status("NA");
            }
        }

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
    public void updateTableAfterLC(String fileName) throws IOException {

        String tempFileName=null;
        int iter=1;
        int countTotalparts = 0;
        int countTotalpartsWithCompleted = 0;
        List<String> Files = new ArrayList<String>();

        PartsOfInputFiles nextRowWithLCStatusSC = partsOfInputFilesRepository.get_Part_WithName(fileName).get(0);
        nextRowWithLCStatusSC.setLc_Status(COMPLETED);
        if(nextRowWithLCStatusSC.getUser_preference()==2)
        {
            nextRowWithLCStatusSC.setDc_Status(SCHEDULED);
        }
        if(nextRowWithLCStatusSC.getUser_preference()==0)
        {
            String[] fileNameParts = fileName.split("_");

            while(true)
            {
                tempFileName = "p" + iter + "_"+fileNameParts[1];
                int present = partsOfInputFilesRepository.file_present(tempFileName);
                int completed = partsOfInputFilesRepository.file_completed_LC(tempFileName);
                if(completed==1) countTotalpartsWithCompleted++;

                if(present==1)
                {
                    countTotalparts++;
                    Files.add(tempFileName);
                }
                else
                {
                    break;
                }
                iter++;
            }
            countTotalpartsWithCompleted++;
            if(countTotalparts==countTotalpartsWithCompleted && (countTotalparts!=0))
            {
                log.info("Merging lead_crawl_files: No_of_parts= " + countTotalparts);
                log.info("Merging lead_crawl_files: No_of_completed_parts= " + countTotalpartsWithCompleted);
                mergeFiles(Files);
            }
        }

        partsOfInputFilesRepository.save(nextRowWithLCStatusSC);

    }

    void mergeFiles(List<String> Files) throws IOException
    {
        String outputpartFilePath = pathToSavePartFile + Files.get(0).split("_")[1] + "_LC_COMPLETED" + ".csv" ;
        FileWriter partFile = new FileWriter(outputpartFilePath);
        CSVWriter writer = new CSVWriter(partFile);
        FileReader fileReader;
        CSVReader csvReader;
        String[] nextRecord;

        for(String file: Files )
        {
            fileReader = new FileReader(pathToSavePartFile + file);
            csvReader = new CSVReader(fileReader);
            while ((nextRecord = csvReader.readNext()) != null)
            {
                writer.writeNext(nextRecord);
            }
        }
        writer.flush();
        writer.close();
        log.info("FINAL LEADCRAWL MERGED OUTPUT FILE CREATED, " + outputpartFilePath);
        uploadFileInS3(outputpartFilePath,PART_LCOUTPUT_FILE_PATH);
        sendEmail(outputpartFilePath);
    }


    void sendEmail(String outputpartFilePath)
    {
        JSONObject json = new JSONObject();
        json.put("csvPath",outputpartFilePath);
        try {
            URL url = new URL(LC_API);
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
            notifier.notifySlack("ERROR OCCURRED WHILE UPLOADING THE LEADS DATA INTO S3 BUCKET DATA, ERROR"+errorMessage);
            log.error("ERROR WHILE UPLOADING INT THE S3 BUCKET {} DUE TO {}", s3BucketPath, e);
        }
    }

}
