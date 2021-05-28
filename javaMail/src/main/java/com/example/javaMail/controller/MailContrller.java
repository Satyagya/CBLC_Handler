package com.example.javaMail.controller;

import com.example.javaMail.EmailService.DeleteFileService;
import com.example.javaMail.EmailService.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController

public class MailContrller {

    @Autowired
    EmailService emailService;

    @Autowired
    DeleteFileService deleteFileService;

    @GetMapping("/sendMail")
    void sendMail() {

       emailService.sendSimpleMessage();

    }

    @GetMapping("/deleteFiles")
    boolean deleteFiles() {
        return deleteFileService.deleteFiles();
    }


}
