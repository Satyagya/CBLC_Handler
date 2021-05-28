package com.example.leadcompanycrawler.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@Service("com.example.leadcompanycrawler.helper.ApiCall")
public class ApiCall {

  /**
   * responsible for hitting complete API
   *
   * @param requestId
   */
  public int hitCompleteApi(Long requestId) {
    int status = 404;
    try {
      URL url = new URL("http://localhost:5000/companyCrawlComplete?requestId="+requestId);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("POST");
      status = con.getResponseCode();
      BufferedReader streamReader = null;
      if (status > 299) {
        streamReader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
      } else {
        streamReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
      }
      String inputLine;
      while ((inputLine = streamReader.readLine()) != null) {
        System.out.println(inputLine);
      }
      streamReader.close();
      con.disconnect();
    } catch (Exception e) {
      log.info("Exception occurred while hitting the API");
    }
    return status;
  }
}