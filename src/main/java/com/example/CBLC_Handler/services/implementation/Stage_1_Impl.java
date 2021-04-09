package com.example.CBLC_Handler.services.implementation;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.example.CBLC_Handler.config.S3config;
import com.example.CBLC_Handler.services.Stage_1_Service;
import com.example.CBLC_Handler.services.helpers.Notifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.redisson.api.RLock;

import java.io.File;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Slf4j
@Service
public class Stage_1_Impl implements Stage_1_Service {

    @Autowired
    private S3config s3Config;

    @Autowired
    @Qualifier("com.example.CBLC_Handler.services.helpers.Notifier")
    private Notifier notifier;

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Value("${input.path}")
    private String inputDirectory;

    @Value("${output.path}")
    private String outputDirectory;

    @Value("${aws.bucket.name.input}")
    private String bucketNameInput;

    @Value("${aws.bucket.name.process}")
    private String bucketNameProcess;




    private RedissonClient redissonClient;

    @Scheduled(fixedDelayString = "${poll.frequency.millis}")
    public void getLatestFileFromS3() {
        String inputPath = null;
        String outputPath = null;

//        HashMap<String, String> desgMap = mariaDbConnector.connectDb(MARIADB_DESG_COLUMN);
//        HashMap<String, String> mailMap = mariaDbConnector.connectDb(MARIADB_MAIL_COLUMN);

        AmazonS3 s3Client;
        String latestFile = null;
        String FolderRegex = "(LEAD_CRAWL_CSV/INPUT_PART_FILES/)";
        s3Client = s3Config.getS3Client();
        List<String> objectKey = new ArrayList<>();
        RLock rLock = getRLock();
        if (rLock != null) {
            try {
                ListObjectsRequest listObjectsRequest =
                        new ListObjectsRequest().withBucketName(bucketName).withPrefix("LEAD_CRAWL_CSV/INPUT_PART_FILES/");
                ObjectListing objectListing;
                do {
                    objectListing = s3Client.listObjects(listObjectsRequest);
                    for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                        log.info("Files in Input bucket "+objectSummary.getKey());
                        objectKey.add(objectSummary.getKey());
                    }
                    listObjectsRequest.setMarker(objectListing.getNextMarker());
                } while (objectListing.isTruncated());
                if (CollectionUtils.isNotEmpty(objectKey) && objectKey.size() > 1) {
                    for (String anObjectKey : objectKey) {
                        if (StringUtils.isNotEmpty(anObjectKey.replaceFirst(FolderRegex, ""))) {
                            latestFile = anObjectKey.replaceFirst(FolderRegex, "");
                            break;
                        }
                    }
                }

                if (StringUtils.isNotEmpty(latestFile)) {
                    log.info("Downloading the input file {} from s3 bucket for lead crawling", latestFile);
                    String outPutFile = getOutPutFileName(latestFile);
                    inputPath = inputDirectory + latestFile;
                    outputPath = outputDirectory + outPutFile;

                    File localFile = new File(inputPath);
                    try {
                        ObjectMetadata object =
                                s3Client.getObject(new GetObjectRequest(bucketNameInput, latestFile), localFile);
                        System.out.println(object);
                        CopyObjectResult copyObjectResult =
                                s3Client.copyObject(bucketNameInput, latestFile, bucketNameProcess, latestFile);
                        s3Client.deleteObject(bucketNameInput, latestFile);
                    } catch (Exception e) {
                        log.error("Exception occurred while downloading the input file from s3 bucket", e);
                    }
                } else {
                    log.info("No file available to crawl at S3 Bucket");
                }
            } catch (Exception e) {
                String errorMessage = "Error occured while taking the input file from S3 bucket "+e;
                notifier.notifySlack(errorMessage);
                log.error(errorMessage + " Failed due to: {}", e);
            } finally {
                rLock.unlock();
            }
            if (StringUtils.isNotEmpty(latestFile)) {
                String prod = getProdName(latestFile);
                //String request_id = latestFile.split("_")[2];
                LocalTime time = LocalTime.now();
                String message =
                        latestFile + " File downloaded from S3 bucket for Lead Crawling at " + time.toString();
                notifier.notifySlack(message);

                //original line:
//              startCrawlingForSearchProfiles(inputPath, outputPath, latestFile, request_id, prod, desgMap,mailMap);
                startCrawling(inputPath, outputPath, latestFile);
            } else {
                log.info("No file Available to crawl");
            }
        } else {
            log.info("Unable to access s3 due to lock not being acquired");
        }
    }

    private RLock getRLock() {
        RLock rLock = null;
        try {
            rLock = redissonClient.getLock("GLOBAL_LOCK");
            rLock.lock();
        } catch (Exception e) {
            log.error("Exception while gaining Lock {}", e);
        }
        return rLock;
    }

    private String getOutPutFileName(String inputFile) {
        String prefix = inputFile.split("\\.")[0];
//        String outputPrefix = prefix + "_output";
        String outputPrefix = prefix;
        return inputFile.replace(prefix, outputPrefix);
    }
    private String getProdName(String fileName) {
        String prodName;
        prodName = fileName.split("_")[1];
        return prodName;
    }

    private void startCrawling(String inputPath,String outputPath,String latestFile)
    {

    }

}



//    > create scheduler for leadcrawl:
//        1.takes one file one by one from s3 through database
//        2.hit api of leadcrawl for each file
//        3.manage multiple instances of leadcrawl
//        4.upload the output files with correct naming format to s3
//        5.have an option to merge files and send to user if required stage is met