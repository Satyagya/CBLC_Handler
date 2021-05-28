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

import static com.jsoup.crawling.contants.Constants.*;

@Service("com.jsoup.crawling.service.implementation.LycosCrawling")
@Slf4j
public class LycosCrawling {

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
        if (null != document) {
            try {
                Elements elements = document.getElementsByClass("results search-results");
                if (CollectionUtils.isNotEmpty(elements)) {
                    if (elements.get(0).getElementsByClass("result-item").size() == 0) {
                        isLeadPresent = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Exception while getting no lead present element. Reason: ", e);
            }
        } else {
            isLeadPresent = false;
        }
        return isLeadPresent;
    }

    private Map getHeaderMap(String searchQuery){
        Map<String, String> map = new HashMap<>();
        try{
            Connection.Response response = Jsoup.connect("https://www.bing.com").method(Connection.Method.POST).timeout(60000).execute();
        }
        catch (Exception e){

        }
        map.put("authority", "www.bing.com");
        map.put("path", searchQuery.replace("https://www.bing.com", ""));
        map.put("method", "GET");
        map.put("scheme", "https");
        map.put("accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,"
                        + "application/signed-exchange;v=b3;q=0.9");
        map.put("accept-encoding", "gzip, deflate, br");
        map.put("accept-language", "en-GB,en-US;q=0.9,en;q=0.8");
        map.put("sec-fetch-dest", "document");
        map.put("sec-fetch-mode", "navigate");
        map.put("sec-fetch-site", "same-origin");
        map.put("sec-fetch-user", "?1");
        map.put("upgrade-insecure-requests", "1");


        return map;
    }
    private Document getSearchedDocument(String personaUrl, String referrer, String userAgent) {
        Document document = null;
        try {
            proxySetting.setProxy();
            Random ran = new Random();
            int x = ran.nextInt(10) + 10;
            String search = "search"+String.valueOf(x);
            personaUrl = personaUrl.replace("search16",search);
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
//        referrer = "https://www.facebook.com";
//        userAgent = "Mozilla/5.0 CK={} (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko";
//        log.info("Url to Search {}, trial: {}", personaUrl, String.valueOf(trial));
//        log.info("for Referrer: {}, User Agent: {}", referrer, userAgent);
//        document = Jsoup.connect(personaUrl).referrer(referrer).userAgent(userAgent).get();
//        System.out.println("document text: " + document.text());
//      }
            if(checkIfLeadPresent(document)) {
                log.info("result found for {}", personaUrl);
            } else {
                log.info("NODATA for {}, trying again...", personaUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception occurred while executing bing query. Reason: " + e.toString());
        }
        return document;
    }
    public JSONObject getLycosDataFromDocument(String searchQuery) {
        String referrer = referrerAndUserAgentSetting.getRandomReferrer();
        String userAgent = referrerAndUserAgentSetting.getRandomUserAgent();

        Document document = getSearchedDocument(searchQuery, referrer, userAgent);
        JSONArray jsonArray = new JSONArray();
        JSONObject crawledResult = new JSONObject();
        if (null != document) {
            try {
                Elements elements = document.getElementsByClass("results search-results");
                if (CollectionUtils.isNotEmpty(elements)) {
                    Elements elementList = elements.get(0).getElementsByClass("result-item");
                    if (CollectionUtils.isNotEmpty(elementList)) {
                        for (Element element : elementList) {
                            JSONObject results = new JSONObject();
                            String searchTitle = "";
                            String profileUrl = "";
                            try {
                                searchTitle = element.selectFirst(H2).getElementsByTag(ANCHOR_TAG).text();
                                profileUrl = element.selectFirst(H2).getElementsByTag(ANCHOR_TAG).attr(HREF).split("&as=")[1];
                                profileUrl = profileUrl.replace("%3A%2F%2F", "://");
                                profileUrl = profileUrl.replace("%2F", "/");

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
            log.error("Error occurred while extracting search pages link in {} for {}", LYCOS, searchQuery);
        }
        if (limit>searchPageLinks.size())
            limit = searchPageLinks.size();
        for (int i=0;i<limit;i++){
            String pageLink = searchPageLinks.get(i);
            log.info("for searchpage: {}",pageLink);
            JSONObject extractedLinksFromPage = getLycosDataFromDocument(pageLink);
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

