package com.example.CBLC_Handler.controller;

import com.example.CBLC_Handler.services.CBLC_Handler_Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/CBLC")
public class Controller {

    @Autowired
    private CBLC_Handler_Service cblc_handler_service;


    //TO GET FILE FROM THE UI UPLOAD//
    @PostMapping(value = "/uploadnewfile")
    void upload_New_File()
    {
        //@RequestParam("inputFilePath") String inputFilePath , int stage
        String inputFilePath = "";
        int stage = 6; //default
        cblc_handler_service.start_Process(inputFilePath, stage);
    }
}

//### Stages:
//
//        - **Stage 0**
//        - File Uploaded and broken into parts
//        - **Stage 1**
//        - Lead Crawl completed
//        - **Stage 2**
//        - Domains generated for the whole file.
//        - **Stage 3**
//        - Emails generated for the whole file.
//        - **Stage 4**
//        - DomainGen Services Files Merged
//        - **Stage 5**
//        - Parts file Merged, Files Complete