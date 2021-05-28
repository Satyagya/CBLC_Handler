package com.example.leadcompanycrawler.helper;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.example.leadcompanycrawler.config.S3Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.leadcompanycrawler.constants.GlassdoorCompanyConstants.JSON_EXTENSION;

@Slf4j
@Service("com.example.leadcompanycrawler.helper.S3OperationsImpl")
public class S3Operations {
  @Autowired
  private S3Config s3Config;

  /**
   * saves the comapany details JSON in s3
   *
   * @param json
   * @param keyName
   * @param bucket
   *
   * @return
   */
  public Boolean saveFile(String json, String keyName, String bucket) {
    AmazonS3 s3Client;
    log.info("Saving request in s3 - filename {}", keyName);
    s3Client = s3Config.getS3Client();
    keyName += JSON_EXTENSION;
    PutObjectResult putObjectResult = s3Client.putObject(bucket, keyName, json);
    System.out.println(putObjectResult.toString());
    return true;
  }
}