package com.example.CBLC_Handler.services.implementation;

import com.example.CBLC_Handler.config.S3config;
import com.example.CBLC_Handler.entities.PartsOfInputFiles;
import com.example.CBLC_Handler.repositories.PartsOfInputFilesRepository;
import com.example.CBLC_Handler.services.FinalPartMerger;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.CBLC_Handler.constants.Constants.*;
import static com.example.CBLC_Handler.constants.Constants.FNF_MERGER_INPUT;


@Service
@Slf4j
public class FinalPartMergerImpl implements FinalPartMerger {

    @Autowired
    private S3config s3config;

    @Autowired
    private PartsOfInputFilesRepository partsOfInputFilesRepository;

    @Value("${aws.bucket.name.EmailGen_OutputFolder_Path}")
    private String EmailGen_OutputFolder_Path;

    @Value("${aws.bucket.name.MergerFNF_OutputFolder_Path}")
    private String MergerFNF_OutputFolder_Path;

    @Value("${aws.bucket.name.FinalOutputFolder_Path}")
    private String FinalOutputFolder_Path;


    @Override
    @Scheduled(fixedDelayString = "${poll.frequency.millis}")
    public void FinalMerger() throws IOException {

        String fileName = null;
        String tempFileName=null;
        int iter=1;
        int countTotalpartsWithCompleted=0;
        int countTotalparts=0;
        List<String> Files = new ArrayList<String>();
        try
        {
            if(partsOfInputFilesRepository.count_Final_merger()>0)
            {
                PartsOfInputFiles row = partsOfInputFilesRepository.get_Next_Final_Part().get(0);
                fileName=row.getInput_FileName_With_part();

                String[] fileNameParts = fileName.split("_");

                while(true)
                {
                    tempFileName = "p" + iter + "_"+fileNameParts[1];
                    int present = partsOfInputFilesRepository.file_present(tempFileName);
                    int completed = partsOfInputFilesRepository.file_completed_EG(tempFileName);
                    if(completed==1) countTotalpartsWithCompleted++;

                    if(present==1)
                    {
                        countTotalparts++;
                        Files.add(tempFileName);
                    }
                    else
                    {
                        break;
                    }
                    iter++;
                }
                log.info("No_of_parts= " + countTotalparts);
                log.info("No_of_completed_parts= " + countTotalpartsWithCompleted);
                //countTotalpartsWithCompleted++;
                if(countTotalparts==countTotalpartsWithCompleted && (countTotalparts!=0))
                {
                    log.info("Merging final_part_files: No_of_parts= " + countTotalparts);
                    log.info("Merging final_part_files: No_of_completed_parts= " + countTotalpartsWithCompleted);
                    mergeFiles(Files);
                }
            }
            else
            {
                log.info("NO FINAL_PART FILES TO MERGE");
            }

        }
        catch (Exception e)
        {
            log.info(String.valueOf(e));
            //notifier.notifySlack("ERROR WHILE DOING FILE MERGER FOR FILE: " + fileName );
        }

    }

    void mergeFiles(List<String> Files) throws IOException
    {
        String outputpartFilePath = EG_MERGER_OUTPUT + Files.get(0).split("_")[1] + "_COMPLETED" + ".csv" ;
        FileWriter partFile = new FileWriter(outputpartFilePath);
        CSVWriter writer = new CSVWriter(partFile);
        FileReader fileReader;
        CSVReader csvReader;
        String[] nextRecord;

        for(String file: Files )
        {
            fileReader = new FileReader(FNF_MERGER_OUTPUT + file);
            csvReader = new CSVReader(fileReader);
            while ((nextRecord = csvReader.readNext()) != null)
            {
                writer.writeNext(nextRecord);
            }
        }
        writer.flush();
        writer.close();
        log.info("FINAL EMAIL_GEN_COMPLETED PARTS MERGED: OUTPUT FILE CREATED AT: " + outputpartFilePath);
        //uploadFileInS3(outputpartFilePath,FinalOutputFolder_Path);
        //sendEmail(outputpartFilePath);
    }

}

