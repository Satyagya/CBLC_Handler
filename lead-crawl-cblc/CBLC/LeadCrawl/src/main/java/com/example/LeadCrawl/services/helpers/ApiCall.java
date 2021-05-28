package com.example.LeadCrawl.services.helpers;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("com.example.LeadCrawl.services.helpers.ApiCall")
public class ApiCall {

    @Autowired
    @Qualifier("com.example.LeadCrawl.services.helpers.Notifier")
    private Notifier notifier;

    @Value("${email.generator.api}")
    String emailGenerationAPI;
    /**
     * responsible for hitting complete API
     *
     * @param emailPatternsJson
     */

    public JSONObject hitCompleteApi(JSONObject emailPatternsJson) {
        int status = 404;
        JSONObject jsonObject = null;
        try {
            URL url = new URL(emailGenerationAPI);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000);
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("POST");

            OutputStream os = con.getOutputStream();
            os.write(emailPatternsJson.toJSONString().getBytes("UTF-8"));
            os.close();

            InputStream in = new BufferedInputStream(con.getInputStream());
            String result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
            JSONParser jsonParser = new JSONParser();
            jsonObject = (JSONObject) jsonParser.parse(result);

            in.close();
            con.disconnect();
        } catch (Exception e) {
            log.info("Exception occurred while hitting the Email Generator API");
            notifier.notifySlack("Exception occurred while hitting the Email Generator API. Reason: "+e.toString());
        }
        return jsonObject;
    }
}
