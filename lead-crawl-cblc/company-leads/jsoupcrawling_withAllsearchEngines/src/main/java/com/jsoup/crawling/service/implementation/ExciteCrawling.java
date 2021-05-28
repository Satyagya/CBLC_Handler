package com.jsoup.crawling.service.implementation;

import com.jsoup.crawling.service.helper.Notifier;
import com.jsoup.crawling.service.helper.ProxySetting;
import com.jsoup.crawling.service.helper.ReferrerAndUserAgentSetting;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.jsoup.crawling.contants.Constants.*;

@Service("com.jsoup.crawling.service.implementation.ExciteCrawling")
@Slf4j
public class ExciteCrawling {
    @Autowired
    @Qualifier("com.jsoup.crawling.service.helper.Notifier")
    private Notifier notifier;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.helper.ProxySetting")
    private ProxySetting proxySetting;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.helper.ReferrerAndUserAgentSetting")
    private ReferrerAndUserAgentSetting referrerAndUserAgentSetting;


    private boolean checkIfLeadPresent(Document document) {
        boolean isLeadPresent = true;
        if (null!=document) {
            try {
                Elements elements = document.getElementsByClass("web-bing__result");
                if (elements.size() == 0) {
                    isLeadPresent = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Exception while getting no lead present element. Reason: ", e);
            }
        }
        else {
            isLeadPresent=false;
        }
        return isLeadPresent;
    }

    private Document getSearchedDocument(String personaUrl, String referrer, String userAgent) {
        Document document = null;
        try {
            proxySetting.setProxy();
            log.info("Url to Search {}", personaUrl);
            log.info("for Referrer: {}, User Agent: {}", referrer, userAgent);
            Connection connection =
                    Jsoup.connect(personaUrl).referrer(referrer).userAgent(userAgent).proxy("p.webshare.io",80).timeout(60000);
            document = connection.get();
            log.info("document text: " + document.text());
//      for (int trial = 0; trial < 1; trial++) {
//        TimeUnit.SECONDS.sleep(7);
//        log.info("NODATA for {}, trying again...", personaUrl);
//        referrer = referrerAndUserAgentSetting.getRandomReferrer();
//        userAgent = referrerAndUserAgentSetting.getRandomUserAgent();
//        log.info("Url to Search {}, trial: {}", personaUrl, String.valueOf(trial));
//        log.info("for Referrer: {}, User Agent: {}", referrer, userAgent);
//        document = Jsoup.connect(personaUrl).referrer(referrer).userAgent(userAgent).get();
//        System.out.println("document text: " + document.text());
//      }
            if(checkIfLeadPresent(document)) {
                log.info("result found for {},", personaUrl);
            } else {
                log.info("NODATA for {}, trying again...", personaUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception occurred while executing bing query. Reason: " + e.toString());
        }
        return document;
    }
    public JSONObject getExciteDataFromDocument(String searchQuery) {
        String referrer = referrerAndUserAgentSetting.getRandomReferrer();
        String userAgent = referrerAndUserAgentSetting.getRandomUserAgent();
        Document document = getSearchedDocument(searchQuery, referrer, userAgent);
        JSONArray jsonArray = new JSONArray();
        JSONObject crawledResult = new JSONObject();
        if (null != document) {
            try {
                Elements elementList = document.getElementsByClass("web-bing__result");
                if (CollectionUtils.isNotEmpty(elementList)) {
                    for (Element element : elementList) {
                        JSONObject results = new JSONObject();
                        String searchTitle = "";
                        String profileUrl = "";
                        try {
                            searchTitle = element.selectFirst(ANCHOR_TAG).text();
                            profileUrl = element.selectFirst(ANCHOR_TAG).attr(HREF);
                        } catch (Exception e) {
                            log.error("Error occurred while getting data for query: {}, Reason: {}", searchQuery,
                                    e.toString());
                        }
                        if (!searchQuery.equalsIgnoreCase("") && !searchTitle.equalsIgnoreCase("")) {
                            results.put("title", searchTitle);
                            results.put("link", profileUrl);
                            jsonArray.add(results);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Error occurred while getting data for query: {}, Reason: {}", searchQuery,
                        e.toString());
                notifier.notifySlack(
                        "Error occurred while getting data for query: " + searchQuery + ", Reason: " + e
                                .toString());
            }
        }
        crawledResult.put("results", jsonArray);
        return crawledResult;
    }
}
