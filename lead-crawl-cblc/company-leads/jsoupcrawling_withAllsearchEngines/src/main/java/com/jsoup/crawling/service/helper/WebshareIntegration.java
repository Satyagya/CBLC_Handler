package com.jsoup.crawling.service.helper;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service("com.jsoup.crawling.service.helper.WebshareIntegration")
@Slf4j
public class WebshareIntegration {

    public JSONObject hitCompleteApi() {
        JSONObject jsonObject = null;
        try {
            URL url = new URL("https://proxy.webshare.io/api/subscription/");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000);
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Authorization", "Token 05db6e6af563d30219b5f00e5947b66fead5695c");
            con.setDoOutput(true);
            con.setDoInput(true);

            con.setRequestMethod("GET");

            InputStream in = new BufferedInputStream(con.getInputStream());
            String result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
            JSONParser jsonParser = new JSONParser();
            jsonObject = (JSONObject) jsonParser.parse(result);

            in.close();
            con.disconnect();
        } catch (Exception e) {
            log.info("Exception occurred while hitting the Email Generator API");
        }
        return jsonObject;
    }

    public static void main(String[] args) {
        WebshareIntegration webshareIntegration = new WebshareIntegration();
        System.out.println(webshareIntegration.hitCompleteApi().toJSONString());
    }

}
