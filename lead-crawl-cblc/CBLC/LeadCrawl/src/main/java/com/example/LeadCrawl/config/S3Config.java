package com.example.LeadCrawl.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class S3Config {

  private AmazonS3 s3client;

  @PostConstruct
  private void init() {
    s3client = AmazonS3ClientBuilder.standard().build();

  }

  public AmazonS3 getS3Client() {
    return s3client;
  }
}
