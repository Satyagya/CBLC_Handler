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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;


@Service("com.crawling.loadbalancer.service.helper.WebshareIntegration")
@Slf4j
public class WebshareIntegration {

    @Value("${webshare.api}")
    private String webshareApi;

    public JSONObject getWebshareIpRotationData() {
        JSONObject jsonObject = null;
        try {
            URL url = new URL(webshareApi);
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
            e.printStackTrace();
            log.info("Exception occurred while hitting the Webshare API, Reason: {}",e.toString());
        }
        return jsonObject;
    }

    public static void main(String[] args) {
        WebshareIntegration webshareIntegration = new WebshareIntegration();
        JSONObject jsonObject = webshareIntegration.getWebshareIpRotationData();
        String time =(String) jsonObject.get("next_rotate_at");
        time = time.substring(0,23)+"Z";
        OffsetDateTime now = OffsetDateTime.now( ZoneOffset.UTC );
        System.out.println("offset: "+now);
        Instant rotationTime = Instant.parse(time);
        rotationTime = rotationTime.plus(7, ChronoUnit.HOURS);
        System.out.println("rotation time: "+rotationTime.toString());

    }

}

