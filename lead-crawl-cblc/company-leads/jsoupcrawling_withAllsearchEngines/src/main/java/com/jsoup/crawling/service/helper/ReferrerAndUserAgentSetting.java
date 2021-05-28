package com.jsoup.crawling.service.helper;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service("com.jsoup.crawling.service.helper.ReferrerAndUserAgentSetting")
@Slf4j
public class ReferrerAndUserAgentSetting {

    @Value("${user.agent.path}")
    private String userAgentFilePath;

    @Value("${referrer.file.path}")
    private String referrerFilePath;


    private Random random = new Random();
    private List<String> referrerList;
    private List<String> userAgentList;
    private String oldReferrer;
    private String oldUserAgent;

    @PostConstruct
    private void init(){
        referrerList = getReferrers();
        userAgentList = getUserAgents();
        oldReferrer = "";
        oldUserAgent="";
    }

    private List<String> getReferrers(){
        List<String> referrersList = new ArrayList<>();
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(referrerFilePath));
            String line="";
            while((line = bufferedReader.readLine())!=null){
                referrersList.add(line);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            log.error("Error reading referrers from Referrer file. Reason: {}", e.toString());
        }
        return referrersList;
    }

    private List<String> getUserAgents(){
        List<String> userAgentsList = new ArrayList<>();
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(userAgentFilePath));
            String line="";
            while((line = bufferedReader.readLine())!=null){
                userAgentsList.add(line);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            log.error("Error reading User Agents from User Agents file. Reason: {}", e.toString());
        }
        return userAgentsList;
    }

    public String getRandomReferrer(){
        String newReferrer = referrerList.get(random.nextInt(referrerList.size()));
        if (!oldReferrer.equalsIgnoreCase(newReferrer)){
            oldReferrer = newReferrer;
        }
        else{
            newReferrer = referrerList.get(random.nextInt(referrerList.size()));
            oldReferrer = newReferrer;
        }
        return newReferrer;
    }

    public String getRandomUserAgent(){
        String newUserAgent = userAgentList.get(random.nextInt(userAgentList.size()));

        if (!oldUserAgent.equalsIgnoreCase(newUserAgent)){
            oldUserAgent = newUserAgent;
        }
        else{
            newUserAgent = userAgentList.get(random.nextInt(userAgentList.size()));
            oldUserAgent = newUserAgent;
        }
        return newUserAgent;
    }


}
