package com.example.leadcompanycrawler.controller;

import com.example.leadcompanycrawler.model.CompanyNames;
import com.example.leadcompanycrawler.service.GlassdoorCrawler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@Slf4j
public class GlassdoorController {

  @Autowired
  @Qualifier("com.example.leadcompanycrawler.service.GlassdoorCrawler")
  private GlassdoorCrawler glassdoorCrawler;

  @PostMapping(value = "/startCompanyCrawl")
  public ResponseEntity<String> startCompanyCrawl(@RequestBody CompanyNames companyNames) {
    System.out.println(companyNames.toString());
    glassdoorCrawler.startCrawl(companyNames);
    return new ResponseEntity<>("Success", HttpStatus.OK);
  }
}
