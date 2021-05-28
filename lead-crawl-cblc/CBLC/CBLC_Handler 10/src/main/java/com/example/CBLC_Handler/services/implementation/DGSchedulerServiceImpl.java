package com.example.CBLC_Handler.services.implementation;

import com.example.CBLC_Handler.entities.DomainFiles;
import com.example.CBLC_Handler.entities.PartsOfInputFiles;
import com.example.CBLC_Handler.repositories.DomainFliesRepository;
import com.example.CBLC_Handler.repositories.PartsOfInputFilesRepository;
import com.example.CBLC_Handler.services.DGSchedulerService;
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
public class DGSchedulerServiceImpl implements DGSchedulerService {

    @Autowired
    private Notifiers notifier;

    int processing=0;

    @Autowired
    private DomainFliesRepository domainFliesRepository;

    @Autowired
    private PartsOfInputFilesRepository partsOfInputFilesRepository;

    @Override
    @Scheduled(fixedDelayString = "${poll.frequency.millis}")
    public void hitDGService()
    {
        DomainFiles nextRowWithDGStatusSC = null;


        try
        {
            if(domainFliesRepository.SC_DG_FileCount()>0 && domainFliesRepository.IP_DG_FileCount()==0)
            {
                nextRowWithDGStatusSC = domainFliesRepository.get_Next_Part_File_ForDG().get(0);
                nextRowWithDGStatusSC.setDg_Status(IN_PROGRESS);
                domainFliesRepository.save(nextRowWithDGStatusSC);
                log.info("FILE SCHEDULED FOR DOMAIN GENERATION IS: "+ nextRowWithDGStatusSC.getDomainNF_FileName_With_Part());
                processing=0;
            }
            else if(domainFliesRepository.FA_DG_FileCount()>0 && domainFliesRepository.IP_DG_FileCount()==0)
            {
                nextRowWithDGStatusSC = domainFliesRepository.get_Next_Part_File_ForDG_FA().get(0);
                nextRowWithDGStatusSC.setDg_Status(InPROGRESS_FAILED);
                domainFliesRepository.save(nextRowWithDGStatusSC);
                log.info("FILE RE-SCHEDULED FOR DOMAIN GENERATION IN INSTANCE 1 IS: "+ nextRowWithDGStatusSC.getDomainNF_FileName_With_Part());
                processing=0;
            }

            if(nextRowWithDGStatusSC!=null)
            {
                PartsOfInputFiles fileRow = partsOfInputFilesRepository.get_Part_WithName(removeNFAtEnd(nextRowWithDGStatusSC.getDomainNF_FileName_With_Part())).get(0);
                String fileName = nextRowWithDGStatusSC.getDomainNF_FileName_With_Part().split("\\.")[0] + "_output.csv";

                //..hit DOMAIN CHECKER API..//
                String query_url = DG_API;
                JSONObject json = new JSONObject();
                json.put("file_name",fileName);
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
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            else
            {
                processing++;
                log.info("DOMAIN GENERATION HAS NOT RECEIVED A FILE, RUNNING: " + processing);
                if(processing>1000000)
                {
                    log.info("DOMAIN GENERATION IS STUCK*******************************************************************");
                    processing=0;
                }
            }

        }
        catch(Exception e)
        {
            String fileName = null;
            if(nextRowWithDGStatusSC!=null)
            {
                log.info("ERROR DOING DOMAIN_GENERATION FOR FILE: " + e.toString());
                notifier.notifySlack("ERROR WHILE DOING DOMAIN GENERATION FOR FILE: " + nextRowWithDGStatusSC.getDomainNF_FileName_With_Part());
                fileName = nextRowWithDGStatusSC.getDomainNF_FileName_With_Part();

                DomainFiles inputFilePart = new DomainFiles();
                inputFilePart.setDomainNF_FileName_With_Part(fileName);
                if (inputFilePart.getDg_Status() == IN_PROGRESS) {
                    log.info("RETRYING FOR FILE: " + e.toString());
                    inputFilePart.setDg_Status(FAILED);
                } else {
                    log.info("DOMAIN GENERATION FOR FILE :" + e.toString() + " FAILED");
                    notifier.notifySlack("ERROR WHILE DOING DOMAIN GENERATION FOR FILE: " + nextRowWithDGStatusSC.getDomainNF_FileName_With_Part());
                    inputFilePart.setDg_Status("NA");
                }
            }
        }
    }

    String removeNFAtEnd(String fileName)
    {
        String[] fileNameParts = fileName.split("_NF");
        return fileNameParts[0]  + fileNameParts[1] ;
    }

    String addNFAtEnd(String fileName)
    {
        return fileName.split("\\.")[0] + "_NF.csv";
    }

    String removeOutputAtEnd(String fileName)
    {
        String[] fileNameParts = fileName.split("_output");
        return fileNameParts[0] + fileNameParts[1] ;
    }

    @Override
    public void updateTableAfterDG(String fileName)
    {
        DomainFiles nextRowWithDGStatusSC = domainFliesRepository.get_Part_WithName(removeOutputAtEnd(fileName)).get(0);
        nextRowWithDGStatusSC.setDg_Status(COMPLETED);
        PartsOfInputFiles row_ToSC_NF =  partsOfInputFilesRepository.get_Part_WithName(removeNFAtEnd(removeOutputAtEnd(fileName))).get(0);
        row_ToSC_NF.setEg_NotFound_Status(SCHEDULED);
        partsOfInputFilesRepository.save(row_ToSC_NF);
        domainFliesRepository.save(nextRowWithDGStatusSC);
    }

}
