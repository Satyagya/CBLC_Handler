package com.example.CBLC_Handler.services.implementation;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.example.CBLC_Handler.config.S3config;
import com.example.CBLC_Handler.services.Stage_2_Service;
import com.example.CBLC_Handler.services.helpers.Notifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class Stage_2_Impl implements Stage_2_Service {

    @Autowired
    private S3config s3Config;

    @Autowired
    @Qualifier("com.example.CBLC_Handler.services.helpers.Notifier")
    private Notifier notifier;

    @Scheduled(fixedDelayString = "${poll.frequency.millis}")
    public void SendForFileForDomainChecker(String filename) throws IOException {
        
    }

}
