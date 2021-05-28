package com.example.LeadCrawl.services.helpers;

import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("com.example.LeadCrawl.services.helpers.CountryMatch")
public class CountryMatch {

    @Value("${country.code.file.path}")
    private String csvPath;

    public List<Map<String,String>> getCountryCode(){
        List<Map<String,String>> countryCodeMapList = new ArrayList<>();

        try{
            FileReader fileReader = new FileReader(csvPath);
            CSVReader csvReader = new CSVReader(fileReader);
            String[] nextRecord;
            int count = 0;
            while ((nextRecord = csvReader.readNext()) != null) {
                if (count == 0) {
                    count++;
                    continue;
                }
                Map<String,String> countryCode = new HashedMap();
                countryCode.put(nextRecord[1].toLowerCase(), nextRecord[0]);
                countryCodeMapList.add(countryCode);
            }
        }
        catch (Exception e){
            log.info("Error occurred while getting company codes from CSV. Reason: {}",e.toString());
        }

        return countryCodeMapList;
    }

}
