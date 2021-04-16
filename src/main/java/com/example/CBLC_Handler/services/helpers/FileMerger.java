package com.example.CBLC_Handler.services.helpers;

import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileMerger {
    @Value("${aws.bucket.name.PART_INPUT_FILE_PATH}")
    private static String PART_INPUT_FILE_PATH;

    @Value("${aws.bucket.name.LEADCRAWL_OUTPUT_PART_FILES}")
    private static String LEADCRAWL_OUTPUT_PART_FILES;

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

    public static void MergeFiles(String fileName, int stage) throws IOException {


        switch(stage)
        {
            case 0:

            case 1:
                String filePath=LEADCRAWL_OUTPUT_PART_FILES;

            case 2:

            case 3:
                String filePath_F=DOMAIN_FOUND_FILE_PATH;
                String filePath_Gen=DOMAIN_GEN_FOUND_FILE_PATH;

            case 4:

            case 5:
                String filePath_Em=EMAIL_GEN_FNF_FILE_PATH;

            case 6:
                String filePath_NFF_M=EMAIL_GEN_OUTPUT_PART_FILE_PATH;
        }

        List<File> csvFiles = new ArrayList<>();
        //just fill this list ^ from inputPath

        List<String> allCsvHeaders = new ArrayList<>();
        List<CsvVo> allCsvRecords = new ArrayList<>();
        for (File csv: csvFiles)
        {
            List<String> csvTempHeaders = CsvParser.getHeadersFromACsv(csv);
            List<CsvVo> csvTempRecords = CsvParser.getRecodrsFromACsv(csv, csvTempHeaders);
            // csvTempHeaders.forEach(h -> System.out.print(h + " "));
            // System.out.println();
            allCsvHeaders.addAll(csvTempHeaders);
            allCsvRecords.addAll(csvTempRecords);
        }

        Set<String> uniqueHeaders = new HashSet<>(allCsvHeaders);
        // uniqueHeaders.forEach(h -> System.out.print(h + " "));
        // System.out.println();

        CsvParser.writeToCsv(new File(outputPath), uniqueHeaders, allCsvRecords);
    }

}
