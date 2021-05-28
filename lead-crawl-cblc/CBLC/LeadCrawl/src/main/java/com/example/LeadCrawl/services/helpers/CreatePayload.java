package com.example.LeadCrawl.services.helpers;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.example.LeadCrawl.config.S3Config;
import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.time.LocalDateTime;

@Component
@Slf4j
public class CreatePayload {
  @Autowired
  private S3Config s3Config;

  @Value("${aws.bucket.name.payload}")
  private String bucketName;

  @Value("${payload.path}")
  private String outputDirectory;

  @Autowired
  @Qualifier("com.example.LeadCrawl.services.helpers.Notifier")
  Notifier notifier;

  public void createPayload(String filepath) {
    try {
      CSVReader csvReader = new CSVReader(new FileReader(filepath));
      Set<String> uniqueLinkedInUrls = new HashSet<>();
      String url = "";
      String[] nextRecord;
      int check=0;
      while ((nextRecord = csvReader.readNext()) != null) {
        if (check==0){
          check++;
          continue;
        }
        String linkedinurl = nextRecord[2];
        uniqueLinkedInUrls.add(linkedinurl);
        check++;
      }
      List<String> urlList = new ArrayList<>();
      double totalUrlSize = uniqueLinkedInUrls.size();
      int count = 0;
      double partCount = 0;
      double parts = Math.ceil(totalUrlSize / 1000);

      for (String i : uniqueLinkedInUrls) {
        urlList.add(i);
        if (count % 1000 == 0 && count != 0) {
          createJsonPayloadAndUpload(urlList);
          urlList.clear();
          partCount++;
        }
        count++;
      }
      if (count == totalUrlSize && partCount != parts) {
        createJsonPayloadAndUpload(urlList);
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Error occurred while creating payload. Reason: ", e.toString());
      notifier.notifySlack("Error occurred while creating payload. Reason: "+ e.toString());
    }
  }

  private void createJsonPayloadAndUpload(List<String> urlList) {
    AmazonS3 s3Client = s3Config.getS3Client();
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("REQUEST_ID", 1029);
    jsonObject.put("PROFILE_URL", urlList);

    String payloadFilePath = outputDirectory;
    LocalDateTime localDateTime = LocalDateTime.now();
    String time = localDateTime.toString();
    String payloadCompletePath = payloadFilePath + time + ".json";
    try {
      PrintWriter payloadJson = new PrintWriter(new FileWriter(payloadCompletePath));
      payloadJson.write(jsonObject.toJSONString());
      payloadJson.flush();
      payloadJson.close();
      File file = new File(payloadCompletePath);
      PutObjectResult putObjectResult = s3Client.putObject(bucketName,file.getName(),file);
      System.out.println(putObjectResult);
      // add upload code to S3
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
