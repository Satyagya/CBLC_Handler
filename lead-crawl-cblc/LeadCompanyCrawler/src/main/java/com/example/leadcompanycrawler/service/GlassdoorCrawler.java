package com.example.leadcompanycrawler.service;

import com.example.leadcompanycrawler.helper.ApiCall;
import com.example.leadcompanycrawler.helper.CompanyWriter;
import com.example.leadcompanycrawler.helper.S3Operations;
import com.example.leadcompanycrawler.model.CompanyNames;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.leadcompanycrawler.constants.Constants.EMPTY_SPACE;
import static com.example.leadcompanycrawler.constants.Constants.WWW;
import static com.example.leadcompanycrawler.constants.GlassdoorCompanyConstants.ANCHOR_TAG;
import static com.example.leadcompanycrawler.constants.GlassdoorCompanyConstants.BING_QUERY;
import static com.example.leadcompanycrawler.constants.GlassdoorCompanyConstants.COMPANY_DETAILS_DIV_CSSQUERY;
import static com.example.leadcompanycrawler.constants.GlassdoorCompanyConstants.COMPANY_NAME_CSSQUERY;
import static com.example.leadcompanycrawler.constants.GlassdoorCompanyConstants.FIRST_RESULT_CLASS;
import static com.example.leadcompanycrawler.constants.GlassdoorCompanyConstants.H2;
import static com.example.leadcompanycrawler.constants.GlassdoorCompanyConstants.HREF;
import static com.example.leadcompanycrawler.constants.GlassdoorCompanyConstants.USER_AGENT;
import static com.example.leadcompanycrawler.constants.GlassdoorConstants.BLANK;
import static com.example.leadcompanycrawler.constants.GlassdoorConstants.FOUNDED_ATTR;
import static com.example.leadcompanycrawler.constants.GlassdoorConstants.HEADQUARTERS_ATTR;
import static com.example.leadcompanycrawler.constants.GlassdoorConstants.HTTP;
import static com.example.leadcompanycrawler.constants.GlassdoorConstants.HTTPS;
import static com.example.leadcompanycrawler.constants.GlassdoorConstants.INDUSTRY_ATTR;
import static com.example.leadcompanycrawler.constants.GlassdoorConstants.LABEL;
import static com.example.leadcompanycrawler.constants.GlassdoorConstants.REVENUE_ATTR;
import static com.example.leadcompanycrawler.constants.GlassdoorConstants.SIZE_ATTR;
import static com.example.leadcompanycrawler.constants.GlassdoorConstants.SPAN;
import static com.example.leadcompanycrawler.constants.GlassdoorConstants.TYPE_ATTR;
import static com.example.leadcompanycrawler.constants.GlassdoorConstants.WEBSITE_ATTR;
import static com.example.leadcompanycrawler.enums.GlassdoorCompanyAttributes.COMPANY_TYPE;
import static com.example.leadcompanycrawler.enums.GlassdoorCompanyAttributes.DOMAIN;
import static com.example.leadcompanycrawler.enums.GlassdoorCompanyAttributes.EMPLOYEE_COUNT;
import static com.example.leadcompanycrawler.enums.GlassdoorCompanyAttributes.FOUNDED_YEAR;
import static com.example.leadcompanycrawler.enums.GlassdoorCompanyAttributes.GLASSDOOR_URL;
import static com.example.leadcompanycrawler.enums.GlassdoorCompanyAttributes.HQ_ADDRESS;
import static com.example.leadcompanycrawler.enums.GlassdoorCompanyAttributes.INDUSTRY;
import static com.example.leadcompanycrawler.enums.GlassdoorCompanyAttributes.NAME;
import static com.example.leadcompanycrawler.enums.GlassdoorCompanyAttributes.REVENUE;
import static com.example.leadcompanycrawler.enums.GlassdoorCompanyAttributes.WEBSITE_URL;

/**
 * Responsible for executing the glassdoor crawling
 */
@Slf4j
@Service("com.example.leadcompanycrawler.service.GlassdoorCrawler")
public class GlassdoorCrawler {

  @Qualifier("com.example.leadcompanycrawler.helper.S3OperationsImpl")
  @Autowired
  private S3Operations s3Operations;

  @Value("${aws.bucket.name}")
  private String bucketName;

  @Autowired
  @Qualifier("com.example.leadcompanycrawler.helper.ApiCall")
  private ApiCall apiCall;

  /**
   * gets the information for the provided glassdoor URL
   *
   * @param glassdoorCompanyUrl
   *
   * @return JSON of company details
   */
  public JSONObject getCompanyInformation(String glassdoorCompanyUrl) {
    JSONObject companyDetails = new JSONObject();
    try {
    CompanyWriter companyWriter = CompanyWriter.singletoneMethod();
    log.info("inside getCompanyInformation for url " + glassdoorCompanyUrl);
    Connection connection = Jsoup.connect(glassdoorCompanyUrl).userAgent(USER_AGENT);

      Document doc = null;
      int count = 0;

      while (doc == null && count < 10) {
        try {
          doc = connection.get();
        } catch (Exception e) {
          log.info("Error occurred while loading this company " + glassdoorCompanyUrl);
        }
        count++;
      }
      if (null != doc) {
        Elements elements = doc.select(COMPANY_DETAILS_DIV_CSSQUERY);
        Map<String, String> properties = new HashMap<>();

        for (Element element : elements) {
          properties
                  .put(element.getElementsByTag(LABEL).text(), element.getElementsByTag(SPAN).text());
        }
        try {
          String website = properties.get(WEBSITE_ATTR);
          Element companyElement = doc.selectFirst(COMPANY_NAME_CSSQUERY);
          companyDetails.put(NAME.name().toLowerCase(), companyElement.text());
          companyWriter.write("companyNames.txt", companyElement.text());
          companyDetails.put(WEBSITE_URL.name().toLowerCase(), website);
          companyDetails.put(HQ_ADDRESS.name().toLowerCase(), properties.get(HEADQUARTERS_ATTR));
          companyDetails.put(EMPLOYEE_COUNT.name().toLowerCase(), properties.get(SIZE_ATTR));
          companyDetails.put(FOUNDED_YEAR.name().toLowerCase(), properties.get(FOUNDED_ATTR));
          companyDetails.put(COMPANY_TYPE.name().toLowerCase(), properties.get(TYPE_ATTR));
          companyDetails.put(INDUSTRY.name().toLowerCase(), properties.get(INDUSTRY_ATTR));
          companyDetails.put(REVENUE.name().toLowerCase(), properties.get(REVENUE_ATTR));
          companyDetails.put(GLASSDOOR_URL.name().toLowerCase(), glassdoorCompanyUrl);
          String websiteForDomain = null;
          if (website.contains(HTTPS) || website.contains(HTTP)) {
            websiteForDomain = website;
          } else {
            websiteForDomain = String.format("%s%s", HTTP, website);
          }
          URL companyHost = new URL(websiteForDomain);
          if (companyHost.getHost().contains(WWW)) {
            companyDetails
                    .put(DOMAIN.name().toLowerCase(), companyHost.getHost().replace(WWW, BLANK));
          } else {
            companyDetails.put(DOMAIN.name().toLowerCase(), companyHost.getHost());
          }
        } catch (Exception e) {
          log.info("Exception occurred is :", e);
        }
      }
    } catch (Exception e){
      System.out.println(e);
    }
      return companyDetails;

  }

  /**
   * gets the glassdoor URL using bing query
   *
   * @param companyName
   *
   * @return String glassdoor URL
   */
  public String getGlassdoorUrl(String companyName) {
    try {
      String glassdoorUrl = null;
      String bingQuery = String.format("%s%s", BING_QUERY, companyName);
      Document document = null;
      try {
        document = Jsoup.connect(bingQuery).userAgent(USER_AGENT).get();
      } catch (IOException e) {
        log.info("Exception occurred while executing bing query");
      }
      if (null != document) {
        Elements elements = document.getElementsByClass(FIRST_RESULT_CLASS);
        List<String> urlList = new ArrayList<>();
        for (Element element : elements) {
          String url = element.selectFirst(H2).getElementsByTag(ANCHOR_TAG).attr(HREF);
          urlList.add(url);
        }
        if (!urlList.isEmpty()) {
          glassdoorUrl = urlList.get(0);
        }
      }
      return glassdoorUrl;
    }
    catch (Exception e){
      e.printStackTrace();
      return null;
    }
  }

  /**
   * initiates the Glassdoor crawl
   *
   * @param companyNames
   */
  @Async
  public void startCrawl(CompanyNames companyNames) {
    Long requestId = companyNames.getRequestId();
    List<String> list = companyNames.getCompanyNameList();
    Set<String> companiesSet = new HashSet<>();
    for (String companyName : list) {
      if (null != companyName) {
        companiesSet.add(companyName);
      }
    }
    int companyCount=0;
    int failureCount=0;
    log.info("total number of companies: " + companiesSet.size());
    for (String companyName : companiesSet) {

      int count = 0;
      String glassdoorUrl = null;
      while (glassdoorUrl == null && count < 20) {
        glassdoorUrl = getGlassdoorUrl(companyName);
        count++;
      }

      if (null != glassdoorUrl) {
        JSONObject data = getCompanyInformation(glassdoorUrl);
        log.info("got result for company: "+companyName);
        log.info(data.toJSONString());
        try (FileWriter file = new FileWriter(companyName.replace(EMPTY_SPACE, BLANK)+".json")) {

          file.write(data.toJSONString());
          file.flush();

        } catch (IOException e) {
          e.printStackTrace();
        }

        try{
          s3Operations
              .saveFile(data.toJSONString(), companyName.replace(EMPTY_SPACE, BLANK), bucketName);
        }
        catch (Exception e){
          log.info("Exception occurred is :",e);
        }
      }
      else {
        log.info("failed to get data for company: " + companyName);
        failureCount++;
      }

      companyCount  = companyCount+1;
      log.info("company count: "+ companyCount);
    }
    int status = 404;
    int retryCount = 0;
    while (status != 200 && retryCount < 5) {
      status = apiCall.hitCompleteApi(requestId);
      retryCount++;
    }
    log.info("Number of companies for which data crawling failed: "+failureCount);
  }
}
