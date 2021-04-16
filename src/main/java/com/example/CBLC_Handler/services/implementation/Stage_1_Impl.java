package com.example.CBLC_Handler.services.implementation;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.example.CBLC_Handler.config.S3config;
import com.example.CBLC_Handler.services.Stage_1_Service;
import com.example.CBLC_Handler.services.helpers.FileMerger;
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
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class Stage_1_Impl implements Stage_1_Service {

    @Autowired
    private S3config s3Config;

    @Autowired
    @Qualifier("com.example.CBLC_Handler.services.helpers.Notifier")
    private Notifier notifier;

    @Scheduled(fixedDelayString = "${poll.frequency.millis}")
    public void SendFileForLeadCrawl(String filename) throws IOException {

    }

}

//    > create scheduler for leadcrawl:
//        1.takes one file one by one from s3 through database
//        2.hit api of leadcrawl for each file
//        3.manage multiple instances of leadcrawl
//        4.upload the output files with correct naming format to s3
//        5.have an option to merge files and send to user if required stage is met