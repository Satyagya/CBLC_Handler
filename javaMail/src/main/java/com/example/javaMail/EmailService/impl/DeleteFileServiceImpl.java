package com.example.javaMail.EmailService.impl;

import com.example.javaMail.EmailService.DeleteFileService;

import com.example.javaMail.constants.DeleteFileConfig;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
public class DeleteFileServiceImpl implements DeleteFileService {


    @Override
    public boolean deleteFiles() {
        try {
            FileUtils.cleanDirectory(DeleteFileConfig.DIRECTORY);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
