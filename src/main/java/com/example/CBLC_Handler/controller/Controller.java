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
    void uplaod_New_File()
    {
        //@RequestParam("inputFilePath") String inputFilePath , int stage
        String inputFilePath = "";
        int stage = 6; //default
        cblc_handler_service.start_Process(inputFilePath, stage);
    }
}
