package com.example.CBLC_Handler.services.helpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import static com.example.CBLC_Handler.constants.Constants.pathToSavePartFile;

@Slf4j
public class FileNameFormatter {

    @Value("${aws.bucket.name.PART_INPUT_FILE_PATH}")
    private static String PART_INPUT_FILE_PATH;

    @Value("${aws.bucket.name.LEADCRAWL_INPUT_PART_FILE_PATH}")
    private static String LEADCRAWL_INPUT_PART_FILE_PATH;

    @Value("${aws.bucket.name.DOMAIN_FOUND_FILE_PATH}")
    private static String DOMAIN_FOUND_FILE_PATH;
    @Value("${aws.bucket.name.DOMAIN_NOT_FOUND_FILE_PATH}")
    private static String DOMAIN_NOT_FOUND_FILE_PATH;

    @Value("${aws.bucket.name.DOMAIN_GEN_FOUND_FILE_PATH}")
    private static String DOMAIN_GEN_FOUND_FILE_PATH;

    @Value("${aws.bucket.name.EMAIL_GEN_FNF_FILE_PATH}")
    private static String EMAIL_GEN_FNF_FILE_PATH;

    @Value("${aws.bucket.name.EMAIL_GEN_OUTPUT_PART_FILE_PATH}")
    private static String EMAIL_GEN_OUTPUT_PART_FILE_PATH;

    @Value("${aws.bucket.name.EMAIL_GEN_FINAL_OUTPUT}")
    private static String EMAIL_GEN_FINAL_OUTPUT;




    public static void name_formatter(String filename, int stage)
    {
//        String fileName = get_FileName(inputFilePath);
//        pathToSavePartFile + "/p" + partNo + "_" + get_fileName(inputFilePath)

        switch (stage)
        {
            case 0:
//                filePath=PART_INPUT_FILE_PATH;

            case 1:
//                filePath=LEADCRAWL_INPUT_PART_FILE_PATH;

            case 2:
                String filePath_F=DOMAIN_FOUND_FILE_PATH;
                String filePath_NF=DOMAIN_NOT_FOUND_FILE_PATH;


            case 3:
                String filePath_Gen=DOMAIN_GEN_FOUND_FILE_PATH;

            case 4:
                String filePath_Em=EMAIL_GEN_FNF_FILE_PATH;

            case 5:
                String filePath_NFF_M=EMAIL_GEN_OUTPUT_PART_FILE_PATH;

            case 6:
                String filePath_final=EMAIL_GEN_FINAL_OUTPUT;

        }

    }
}
