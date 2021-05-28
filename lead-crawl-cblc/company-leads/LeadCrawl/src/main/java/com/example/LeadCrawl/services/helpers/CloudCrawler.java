package com.example.LeadCrawl.services.helpers;


import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.*;

@Slf4j
@Service("com.example.LeadCrawl.services.helpers.CloudCrawler")
public class CloudCrawler {

    @Autowired
    @Qualifier("com.example.LeadCrawl.services.helpers.Notifier")
    private Notifier notifier;

    @Value("${load.balancer.api}")
    private String loadbalancerApi;


    public JSONObject getCrawledData(String searchQuery) {

        JSONObject jsonObject = null;
        JSONObject requestParameter = new JSONObject();
        requestParameter.put("searchQuery",searchQuery);
        try {
            URL url = new URL(loadbalancerApi);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(10000);
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("POST");

            OutputStream os = con.getOutputStream();
            os.write(requestParameter.toJSONString().getBytes("UTF-8"));
            os.close();

            InputStream in = new BufferedInputStream(con.getInputStream());
            String result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
            JSONParser jsonParser = new JSONParser();
            jsonObject = (JSONObject) jsonParser.parse(result);

            in.close();
            con.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            log.info("Exception occurred while hitting the Cloud Crawler API. Reason: {}", e.toString());
            notifier.notifySlack("Exception occurred while hitting the Cloud Crawler API. Reason: "+e.toString());
        }
        return jsonObject;
    }
}
