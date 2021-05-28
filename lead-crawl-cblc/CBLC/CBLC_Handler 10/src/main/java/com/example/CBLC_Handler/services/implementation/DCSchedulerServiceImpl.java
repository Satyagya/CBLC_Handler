package com.example.CBLC_Handler.services.implementation;

import com.example.CBLC_Handler.entities.DomainFiles;
import com.example.CBLC_Handler.entities.PartsOfInputFiles;
import com.example.CBLC_Handler.repositories.DomainFliesRepository;
import com.example.CBLC_Handler.repositories.PartsOfInputFilesRepository;
import com.example.CBLC_Handler.services.DCSchedulerService;
import com.example.CBLC_Handler.services.helpers.Notifiers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.CBLC_Handler.constants.Constants.*;

@Service
@Slf4j
public class DCSchedulerServiceImpl implements DCSchedulerService {

    @Autowired
    private Notifiers notifier;

    int processing = 0;

    @Autowired
    private PartsOfInputFilesRepository partsOfInputFilesRepository;

    @Autowired
    private DomainFliesRepository domainFliesRepository;



    @Override
    @Scheduled(fixedDelayString = "${poll.frequency.millis}")
    public void hitDCService()
    {
        PartsOfInputFiles nextRowWithDCStatusSC = null;


        try
        {
            if(partsOfInputFilesRepository.SC_DC_FileCount()>0 && partsOfInputFilesRepository.IP_DC_FileCount()==0)
            {
                nextRowWithDCStatusSC = partsOfInputFilesRepository.get_Next_Part_File_ForDC().get(0);
                nextRowWithDCStatusSC.setDc_Status(IN_PROGRESS);
                partsOfInputFilesRepository.save(nextRowWithDCStatusSC);
                log.info("FILE SCHEDULED FOR DOMAIN CHECKER IS: " + nextRowWithDCStatusSC.getInput_FileName_With_part());
                processing=0;
            }
            else if(partsOfInputFilesRepository.FA_DC_FileCount()>0 && partsOfInputFilesRepository.IP_DC_FileCount()==0)
            {
                nextRowWithDCStatusSC = partsOfInputFilesRepository.get_Next_Part_File_ForDC_FA().get(0);
                nextRowWithDCStatusSC.setDc_Status(InPROGRESS_FAILED);
                partsOfInputFilesRepository.save(nextRowWithDCStatusSC);
                log.info("FILE RE-SCHEDULED FOR DOMAIN CHECKER IN INSTANCE 1 IS: "+ nextRowWithDCStatusSC.getInput_FileName_With_part());
                processing=0;
            }


            //..hit DOMAIN CHECKER API..//
            if(nextRowWithDCStatusSC!=null)
            {
                String query_url = DC_API;
                JSONObject json = new JSONObject();
                json.put("file_name", nextRowWithDCStatusSC.getInput_FileName_With_part());
                try {
                    URL url = new URL(query_url);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestMethod("POST");
                    OutputStream os = conn.getOutputStream();
                    os.write(json.toJSONString().getBytes("UTF-8"));
                    os.close();
                    // read the response
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    String result = IOUtils.toString(in, "UTF-8");
                    log.info(result);
                    in.close();
                    conn.disconnect();
                }
                catch (Exception e) {
                    log.info(String.valueOf(e));
                }
            }
            else
            {
                processing++;
                log.info("DOMAIN CHECKER HAS NOT RECEIVED A FILE, RUNNING: " + processing);
                if(processing>1000000)
                {
                    log.info("DOMAIN CHECKER IS STUCK*******************************************************************");
                    processing=0;
                }
            }
        }
        catch(Exception e)
        {
            if(nextRowWithDCStatusSC!=null) {
                log.info("ERROR DOING DOMAIN_CHECKER FOR FILE: " + e.toString());
                notifier.notifySlack("ERROR WHILE DOING DOMAIN CHECKER FOR FILE: " + nextRowWithDCStatusSC.getInput_FileName_With_part());
                String fileName = nextRowWithDCStatusSC.getInput_FileName_With_part();
                PartsOfInputFiles inputFilePart = new PartsOfInputFiles();
                inputFilePart.setInput_FileName_With_part(fileName);
                if (inputFilePart.getDc_Status() == IN_PROGRESS) {
                    log.info("RETRYING FOR FILE: " + e.toString());
                    inputFilePart.setDc_Status(FAILED);
                } else {
                    log.info("DOMAIN CHECKER FOR FILE :" + e.toString() + " FAILED");
                    notifier.notifySlack("ERROR WHILE DOING DOMIAN CHECKER FOR FILE: " + nextRowWithDCStatusSC.getInput_FileName_With_part());
                    inputFilePart.setDc_Status("NA");
                }
            }
        }
    }


    @Override
    public void updateTableAfterDC(String fileName) {
        PartsOfInputFiles next_row_WithDCStatus_SC = partsOfInputFilesRepository.get_Part_WithName(fileName).get(0);
        next_row_WithDCStatus_SC.setDc_Status(COMPLETED);
        next_row_WithDCStatus_SC.setEg_Found_Status(SCHEDULED);
        DomainFiles domain_file = new DomainFiles();
        domain_file.setDomainNF_FileName_With_Part(addNFAtEnd(next_row_WithDCStatus_SC.getInput_FileName_With_part()));
        domain_file.setDg_Status(SCHEDULED);
        domainFliesRepository.save(domain_file);
        partsOfInputFilesRepository.save(next_row_WithDCStatus_SC);

    }

    String addNFAtEnd(String fileName)
    {
        return fileName.split("\\.")[0] + "_NF.csv";
    }


}
