package com.example.CBLC_Handler.controller;

import com.example.CBLC_Handler.entities.Dto;
import com.example.CBLC_Handler.services.*;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(value = "/CBLC")
@Slf4j
public class Controller {

    @Autowired
    private CBLCHandlerService cblcHandlerService;

    @Autowired
    private LCSchedulerService lcSchedulerService;

    @Autowired
    private DCSchedulerService dcSchedulerService;

    @Autowired
    private DGSchedulerService dgSchedulerService;

    @Autowired
    private EGSchedulerService egSchedulerService;

    // GET FILE FROM UI //
    @PostMapping(value = "/uploadnewfile")
    void uplaodNewFile(@RequestBody JSONObject jsonObject)
    {
        log.info("FILENAME " + (String) jsonObject.get("file_name"));
        log.info("STAGE REQUIRED " + String.valueOf(Integer.parseInt((String)jsonObject.get("stage"))));
        log.info("EMAIL TO SEND OUTPUT FILE" + (String) jsonObject.get("email"));
        int userPreference=Integer.parseInt((String)jsonObject.get("stage"));//0->LC 1->EM 2->both
        String email = (String) jsonObject.get("email");
        String inputFilePath = "/Users/satyagtaparasharkumar/Documents/CBLC_Handler/lead-crawl-cblc/CBLC/front_end_gor_crawling/Files/" + (String) jsonObject.get("file_name");
        cblcHandlerService.startProcess(inputFilePath, userPreference, email);
    }


    @PostMapping(value = "/updateTableAfterLC/{fileName}")
    @Async
    void updateTableAfterLC(@PathVariable("fileName") String fileName) throws IOException {
        lcSchedulerService.updateTableAfterLC(fileName);
    }

    @PostMapping(value = "/updateTableAfterDC/{fileName}")
    @Async
    void updateTableAfterDC(@PathVariable("fileName") String fileName)
    {
        dcSchedulerService.updateTableAfterDC(fileName);
    }

    @PostMapping(value = "/updateTableAfterDG/{fileName}")
    @Async
    void updateTableAfterDG(@PathVariable("fileName") String fileName)
    {
        dgSchedulerService.updateTableAfterDG(fileName);
    }

    @PostMapping(value = "/updateTableAfterEG/{fileName}")
    @Async
    void updateTableAfterEG(@PathVariable("fileName") String fileName) throws IOException {
        egSchedulerService.updateTableAfterEG(fileName);
    }

    @PostMapping(value = "/storeInS3")
    void storeInS3(@RequestBody Dto dto)
    {
        cblcHandlerService.uploadFileInS3(dto.filePath, dto.s3Bucket);
    }

}
