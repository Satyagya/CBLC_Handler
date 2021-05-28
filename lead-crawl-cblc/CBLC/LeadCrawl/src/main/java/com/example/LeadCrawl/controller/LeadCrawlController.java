package com.example.LeadCrawl.controller;

import com.example.LeadCrawl.services.EmailGenerationService;
import com.example.LeadCrawl.services.implementation.LeadCrawlServiceImplementation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(value = "/lead-crawl")
@Slf4j
public class LeadCrawlController {

  @Autowired
  @Qualifier("com.example.LeadCrawl.services.implementation.LeadCrawlServiceImplementation")
  private LeadCrawlServiceImplementation leadCrawlServiceImplementation;

  @Autowired
  @Qualifier("com.example.LeadCrawl.services.implementation.EmailGenerationServiceImpl")
  private EmailGenerationService emailGenerationService;


  @PostMapping(value = "/checkEmailSending")
  public Set<String> startScheduledCrawl(@RequestBody JSONObject jsonObject) {
    List<String> emailList = (List<String>) jsonObject.get("emails");
    Set<String> emailsToCheck = new HashSet<>(emailList);
    return emailGenerationService.setSendingReductionMails(emailsToCheck);
  }

  @PostMapping(value = "/checkEmailGeneration")
  @Async
  public Boolean checkEmailGeneration(@RequestBody JSONObject jsonObject){
    String csvPath = (String) jsonObject.get("csvPath");
    emailGenerationService.generateEmailForCsv(csvPath);


    String fileName = csvPath.split("/")[(csvPath.split("/").length)-1];
    System.out.println(fileName);
    String query_url = "http://localhost:8004/CBLC/updateTableAfterEG/" + fileName;
    try
    {
      URL url = new URL(query_url);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setConnectTimeout(5000);
      conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      conn.setDoOutput(true);
      conn.setDoInput(true);
      conn.setRequestMethod("POST");
      // read the response
      InputStream in = new BufferedInputStream(conn.getInputStream());
      String result = IOUtils.toString(in, "UTF-8");
      System.out.println(result);
      in.close();
      conn.disconnect();
    }
    catch (Exception e) {
      System.out.println(e);
    }

    return true;
  }
}
