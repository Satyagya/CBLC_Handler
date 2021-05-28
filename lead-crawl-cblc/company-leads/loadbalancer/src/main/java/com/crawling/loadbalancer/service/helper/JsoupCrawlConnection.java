package com.crawling.loadbalancer.service.helper;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service("com.crawling.loadbalancer.service.helper.JsoupCrawlConnection")
@Slf4j
public class JsoupCrawlConnection {

    @Value("${jsoup.crawling.api}")
    private String jsoupCrawlingApi;

    public JSONObject getCrawledData(String searchQuery, String searchEngine) {

        JSONObject jsonObject = null;
        JSONObject requestParameter = new JSONObject();
        requestParameter.put("searchQuery",searchQuery);
        requestParameter.put("searchEngine",searchEngine);
        try {
            URL url = new URL(jsoupCrawlingApi);
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
            log.info("Exception occurred while hitting the Jsoup Crawler API");
//            notifier.notifySlack("Exception occurred while hitting the Cloud Crawler API. Reason: "+e.toString());
        }
        return jsonObject;
    }


}
