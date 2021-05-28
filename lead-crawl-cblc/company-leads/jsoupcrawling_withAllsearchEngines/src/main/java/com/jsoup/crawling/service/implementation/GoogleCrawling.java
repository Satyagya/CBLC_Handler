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

import java.util.*;
import java.util.concurrent.TimeUnit;
import static com.jsoup.crawling.contants.Constants.*;
@Service("com.jsoup.crawling.service.implementation.GoogleCrawling")
@Slf4j
public class GoogleCrawling {

    @Autowired
    @Qualifier("com.jsoup.crawling.service.helper.Notifier")
    private Notifier notifier;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.helper.ProxySetting")
    private ProxySetting proxySetting;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.helper.ReferrerAndUserAgentSetting")
    private ReferrerAndUserAgentSetting referrerAndUserAgentSetting;

    private boolean checkIfLeadPresentInGoogle(Document document) {
        boolean isLeadPresent = true;
        if (null!=document) {
            try {
                Elements elements = document.getElementsByClass(GOOGLE_RESULTS_LIST_CLASS);
                Elements elements1 = document.getElementsByClass("ZINbbc xpd O9g5cc uUPGi");
                Elements elements2 = document.getElementsByClass("yuRUbf");
                Elements elements3 = document.getElementsByClass("fuLhoc ZWRArf");
                if ((elements.size() == 0) && (elements1.size() == 0) && (elements2.size() == 0) && (elements3.size() == 0)) {
                    isLeadPresent = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Exception while getting no lead present element. Reason: {}", e.toString());
            }
        }
        else {
            isLeadPresent=false;
        }
        return isLeadPresent;
    }

    private Document getSearchedDocument(String personaUrl, String referrer, String userAgent){
        Document document=null;
        try {
            proxySetting.setProxy();
            log.info("Url to Search {}",personaUrl);
            log.info("for Referrer: {}, User Agent: {}", referrer, userAgent);
            Connection.Response response = Jsoup.connect("https://www.bing.com").referrer(referrer).userAgent(userAgent).method(Connection.Method.POST).execute();
            Map<String,String> cookies = new HashMap<>();
            cookies = response.cookies();
            document = Jsoup.connect(personaUrl).referrer(referrer).userAgent(userAgent).cookies(cookies).timeout(60000).get();
            log.info("document text: "+document.text());

            if(checkIfLeadPresentInGoogle(document))
                log.info("result found for {},",personaUrl);
            else {
                log.info("NODATA for {}, trying again...", personaUrl);
                notifier.notifySlack(String.format("NODATA for {}, for searchEngine: {}",personaUrl ,BING));
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception occurred while executing bing query. Reason: "+e.toString());
        }
        return document;
    }

    public JSONObject getGoogleDataFromDocument(String searchQuery) {
        String referrer = referrerAndUserAgentSetting.getRandomReferrer();
        String userAgent = referrerAndUserAgentSetting.getRandomUserAgent();
        Document document = getSearchedDocument(searchQuery, referrer, userAgent);
        JSONArray jsonArray = new JSONArray();
        JSONObject crawledResult = new JSONObject();
        if (null != document) {
            try {
                Elements elementList = document.getElementsByClass(GOOGLE_RESULTS_LIST_CLASS);
                if (CollectionUtils.isNotEmpty(elementList)) {
                    for (Element element : elementList) {
                        JSONObject results = new JSONObject();
                        String searchTitle = "";
                        String profileUrl = "";
                        try {
                            searchTitle = element.selectFirst(H3).text();
                            profileUrl = element.selectFirst(ANCHOR_TAG).attr(HREF);
                            if (profileUrl.contains("&sa="))
                                profileUrl = profileUrl.replace("/url?q=","").split("&sa=")[0];

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
                } else {
                    Elements elements = document.getElementsByClass("ZINbbc xpd O9g5cc uUPGi");
                    if (CollectionUtils.isNotEmpty(elements)) {
                        for (Element element : elements) {
                            JSONObject results = new JSONObject();
                            String searchTitle = "";
                            String profileUrl = "";
                            try {
                                Elements titleElements = element.selectFirst("a").getElementsByTag("div");
                                if (CollectionUtils.isNotEmpty(titleElements)) {
                                    searchTitle = titleElements.get(0).text();
                                }
                                profileUrl = element.selectFirst(ANCHOR_TAG).attr(HREF);
                                if (profileUrl.contains("&sa="))
                                    profileUrl = profileUrl.replace("/url?q=","").split("&sa=")[0];
                            } catch (Exception e) {
                                log.error("Error occurred while getting data for query: {}, Reason: {}",
                                        searchQuery, e.toString());
                            }
                            if (!searchQuery.equalsIgnoreCase("") && !searchTitle.equalsIgnoreCase("")) {
                                results.put("title", searchTitle);
                                results.put("link", profileUrl);
                                jsonArray.add(results);
                            }
                        }
                    } else {
                        Elements elementsList = document.getElementsByClass("yuRUbf");
                        if (CollectionUtils.isNotEmpty(elementsList)) {
                            for (Element element : elementsList) {
                                JSONObject results = new JSONObject();
                                String searchTitle = "";
                                String profileUrl = "";
                                try {
                                    searchTitle = element.selectFirst(H3).text();
                                    profileUrl = element.selectFirst(ANCHOR_TAG).attr(HREF);
                                } catch (Exception e) {
                                    log.error("Error occurred while getting data for query: {}, Reason: {}",
                                            searchQuery, e.toString());
                                }
                                if (!searchQuery.equalsIgnoreCase("") && !searchTitle.equalsIgnoreCase("")) {
                                    results.put("title", searchTitle);
                                    results.put("link", profileUrl);
                                    jsonArray.add(results);
                                }
                            }
                        } else {
                            Elements elementsList2 = document.getElementsByClass("fuLhoc ZWRArf");
                            if (CollectionUtils.isNotEmpty(elementsList2)) {
                                for (Element element : elementsList2) {
                                    JSONObject results = new JSONObject();
                                    String searchTitle = "";
                                    String profileUrl = "";
                                    try {
                                        searchTitle = element.text();
                                        profileUrl = element.attr(HREF);
                                        if (profileUrl.contains("&sa="))
                                            profileUrl = profileUrl.replace("/url?q=","").split("&sa=")[0];

                                    } catch (Exception e) {
                                        log.error("Error occurred while getting data for query: {}, Reason: {}",
                                                searchQuery, e.toString());
                                    }
                                    if (!searchQuery.equalsIgnoreCase("") && !searchTitle.equalsIgnoreCase("")) {
                                        results.put("title", searchTitle);
                                        results.put("link", profileUrl);
                                        jsonArray.add(results);
                                    }
                                }
                            }
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

    public JSONObject getSearchPageLinks(String searchQuery, int limit){
        String referrer = referrerAndUserAgentSetting.getRandomReferrer();
        String userAgent = referrerAndUserAgentSetting.getRandomUserAgent();
        Document document = getSearchedDocument(searchQuery, referrer, userAgent);
        List<String> searchPageLinks = new ArrayList<>();
        JSONArray listOfExtractedLinks = new JSONArray();
        try{
            searchPageLinks.add(searchQuery);
            Elements elementsList = document.getElementsByClass("fl");
            for (Element element : elementsList) {
                String link = element.attr("href");
                if(link.contains("/search?q=site:")) {
                    link = "https://www.google.com" + link;
                    searchPageLinks.add(link);
                }
            }

        }
        catch (Exception e){
            log.error("Error occurred while extracting search pages link in {} for {}", GOOGLE, searchQuery);
        }
        if (limit>searchPageLinks.size())
            limit = searchPageLinks.size();
        for (int i=0;i<limit;i++){
            String pageLink = searchPageLinks.get(i);
            log.info("for searchpage: {}",pageLink);
            JSONObject extractedLinksFromPage = getGoogleDataFromDocument(pageLink);
            JSONArray pageLinksJsonArray = (JSONArray) extractedLinksFromPage.get("results");
            pageLinksJsonArray.stream().forEach(jsonArray->{
                listOfExtractedLinks.add(jsonArray);
            });
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("results",listOfExtractedLinks);

        return jsonObject;
    }

}