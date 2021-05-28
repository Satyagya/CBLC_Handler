package com.example.CBLC_Handler.services.implementation;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.CBLC_Handler.config.S3config;
import com.example.CBLC_Handler.entities.PartsOfInputFiles;
import com.example.CBLC_Handler.repositories.PartsOfInputFilesRepository;
import com.example.CBLC_Handler.services.FNFMERSchedulerService;


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
public class FNFMERSchedulerServiceImpl implements FNFMERSchedulerService {

    private RedissonClient redissonClient;

    @Autowired
    private Notifiers notifier;

    @PostConstruct
    private void init() {
        redissonClient = Redisson.create();
    }

    @Autowired
    private S3config s3config;

    @Autowired
    private PartsOfInputFilesRepository partsOfInputFilesRepository;

    @Value("${aws.bucket.name.EmailGen_OutputFolder_Path}")
    private String EmailGen_OutputFolder_Path;

    @Value("${aws.bucket.name.MergerFNF_OutputFolder_Path}")
    private String MergerFNF_OutputFolder_Path;

    @Value("${aws.bucket.name.FinalOutputFolder_Path}")
    private String FinalOutputFolder_Path;

    @Override
    @Scheduled(fixedDelayString = "${poll.frequency.millis}")
    public void FNFfileMergerService()
    {
        String fileName = null;
        try
        {
            if(partsOfInputFilesRepository.count_FNF_merger()>0)
            {
                PartsOfInputFiles row = partsOfInputFilesRepository.get_Next_FNF_Part().get(0);

                String s3BucketPath = EmailGen_OutputFolder_Path;
                fileName = row.getInput_FileName_With_part().split("\\.")[0] + "_final-output.csv" ;
                System.out.println(row.getInput_FileName_With_part().split("\\.")[0] + "_final-output.csv" );
                String fileName_NF = row.getInput_FileName_With_part().split("\\.")[0] + "_NF_final-output.csv" ;
                System.out.println(row.getInput_FileName_With_part().split("\\.")[0] + "_NF_final-output.csv");
                takeFileFromS3(fileName, s3BucketPath, FNF_MERGER_INPUT + fileName);
                takeFileFromS3(fileName_NF, s3BucketPath, FNF_MERGER_INPUT + fileName_NF);
                mergeFNFFiles(fileName, fileName_NF);

            }
            else
            {
                log.info("NO FNF FILES TO MERGE");
            }

        }
        catch (Exception e)
        {
            log.info(String.valueOf(e));
            //notifier.notifySlack("ERROR WHILE DOING FILE MERGER FOR FILE: " + fileName );
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



    void mergeFNFFiles(String file1, String file2) throws IOException
    {

        String outputpartFilePath = FNF_MERGER_OUTPUT + file1.split("_")[0] + "_" + file1.split("_")[1] + ".csv" ;
        FileWriter partFile = new FileWriter(outputpartFilePath);
        CSVWriter writer = new CSVWriter(partFile);

        FileReader fileReader1 = new FileReader(FNF_MERGER_INPUT + file1);
        CSVReader csvReader1 = new CSVReader(fileReader1);
        String[] nextRecord1;
        List<String[]> rowsList1 = new ArrayList<>();
        while ((nextRecord1 = csvReader1.readNext()) != null) {
            rowsList1.add(nextRecord1);
        }

        writer.writeAll(rowsList1);

        FileReader fileReader2 = new FileReader(FNF_MERGER_INPUT + file2);
        CSVReader csvReader2 = new CSVReader(fileReader2);
        String[] nextRecord2;
        List<String[]> rowsList2 = new ArrayList<>();
        while ((nextRecord2 = csvReader2.readNext()) != null) {
            rowsList2.add(nextRecord2);
        }

        int count =0;
        for (String[] nextRecord : rowsList2)
        {
            if(count!=0)
            {
                writer.writeNext(nextRecord);
            }
            count=1;
        }

        writer.flush();
        writer.close();
        log.info("FINAL OUTPUT FILE PART MADE " + file1.split("_")[0] + file1.split("_")[1] + ".csv");

        uploadFileInS3(outputpartFilePath,MergerFNF_OutputFolder_Path);
        setStatus(outputpartFilePath);

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
            //notifier.notifySlack("ERROR OCCURRED WHILE UPLOADING THE LEADS DATA INTO S3 BUCKET DATA, ERROR"+errorMessage);
            log.error("ERROR WHILE UPLOADING INT THE S3 BUCKET {} DUE TO {}", s3BucketPath, e);
        }
    }


    void setStatus(String outputpart_FilePath) throws IOException {

        String fileName = outputpart_FilePath.split("/")[outputpart_FilePath.split("/").length - 1];
        JSONObject json = new JSONObject();
        String emailID = partsOfInputFilesRepository.get_Part_WithName(fileName).get(0).getEmailID();
        json.put("fileName", fileName);
        json.put("email", emailID);
        try {
            URL url = new URL(EmailSend_API);
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

        PartsOfInputFiles row = partsOfInputFilesRepository.get_Part_WithName(fileName).get(0);
        row.setStatus(COMPLETED);
        partsOfInputFilesRepository.save(row);
    }
}

