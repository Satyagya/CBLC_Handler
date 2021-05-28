package com.example.LeadCrawl.controller;

import com.example.LeadCrawl.services.EmailGenerationService;
import com.example.LeadCrawl.services.implementation.LeadCrawlServiceImplementation;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
  public String checkEmailGeneration(@RequestParam(value = "csvPath") String csvPath){
    return emailGenerationService.generateEmailForCsv(csvPath);
  }
}
