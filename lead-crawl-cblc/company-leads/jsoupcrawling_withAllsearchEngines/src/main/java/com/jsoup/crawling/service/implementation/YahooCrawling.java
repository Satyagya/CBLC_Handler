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

import java.util.ArrayList;
import java.util.List;
import static com.jsoup.crawling.contants.Constants.*;


@Service("com.jsoup.crawling.service.implementation.YahooCrawling")
@Slf4j
public class YahooCrawling {

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
                Elements elements = document.getElementsByClass("dd algo algo-sr relsrch richAlgo");
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
    public JSONObject getYahooDataFromDocument(String searchQuery) {
        String referrer = referrerAndUserAgentSetting.getRandomReferrer();
        String userAgent = referrerAndUserAgentSetting.getRandomUserAgent();
        Document document = getSearchedDocument(searchQuery, referrer, userAgent);
        JSONArray jsonArray = new JSONArray();
        JSONObject crawledResult = new JSONObject();
        if (null != document) {
            try {
                Elements elementList = document.getElementsByClass("dd algo algo-sr relsrch richAlgo");
                if (CollectionUtils.isNotEmpty(elementList)) {
                    for (Element element : elementList) {
                        JSONObject results = new JSONObject();
                        String searchTitle = "";
                        String profileUrl = "";
                        try {
                            searchTitle = element.selectFirst(H3).getElementsByTag(ANCHOR_TAG).text();
                            profileUrl = element.selectFirst(H3).getElementsByTag(ANCHOR_TAG).attr(HREF);
                            profileUrl = profileUrl.split("/RU=")[1];
                            profileUrl = profileUrl.split("/RK=")[0];
//                            profileUrl = profileUrl.replace("%3A%2F%2F", "://");
//                            profileUrl = profileUrl.replace("%2F", "/");
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

    public JSONObject getSearchPageLinks(String searchQuery, int limit){
        String referrer = referrerAndUserAgentSetting.getRandomReferrer();
        String userAgent = referrerAndUserAgentSetting.getRandomUserAgent();
        Document document = getSearchedDocument(searchQuery, referrer, userAgent);
        List<String> searchPageLinks = new ArrayList<>();
        JSONArray listOfExtractedLinks = new JSONArray();
        try{
            searchPageLinks.add(searchQuery);
            Elements pageList = document.getElementsByClass("pages");
            for (Element element : pageList) {
                Elements elementsList = element.getElementsByAttribute("href");
                for (Element element1 : elementsList) {
                    String link = element1.attr("href");
                    if (link.contains("https://in.search.yahoo.com/search;")) {
                        searchPageLinks.add(link);
                    }
                }
            }

        }
        catch (Exception e){
            log.error("Error occurred while extracting search pages link in {} for {}", YAHOO, searchQuery);
        }
        if (limit>searchPageLinks.size())
            limit = searchPageLinks.size();
        for (int i=0;i<limit;i++){
            String pageLink = searchPageLinks.get(i);
            log.info("for searchpage: {}",pageLink);
            JSONObject extractedLinksFromPage = getYahooDataFromDocument(pageLink);
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

