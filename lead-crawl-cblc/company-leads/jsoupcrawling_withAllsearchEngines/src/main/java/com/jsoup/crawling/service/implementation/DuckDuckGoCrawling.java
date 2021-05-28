package com.jsoup.crawling.service.implementation;


import com.jsoup.crawling.service.helper.Notifier;
import com.jsoup.crawling.service.helper.ProxySetting;
import com.jsoup.crawling.service.helper.ReferrerAndUserAgentSetting;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.Proxy;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.jsoup.crawling.contants.Constants.*;

@Service("com.jsoup.crawling.service.implementation.DuckDuckGoCrawling")
@Slf4j
public class DuckDuckGoCrawling {


    @Autowired
    @Qualifier("com.jsoup.crawling.service.helper.Notifier")
    private Notifier notifier;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.helper.ProxySetting")
    private ProxySetting proxySetting;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.helper.ReferrerAndUserAgentSetting")
    private ReferrerAndUserAgentSetting referrerAndUserAgentSetting;


    private boolean checkIfLeadPresentInDuckDuckGo(Document document) {
        boolean isLeadPresent = true;
        if (null!=document) {

            try {
                Elements elements = document.getElementsByClass(DUCKDUCKGO_RESULTS_LIST_CLASS);
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

    private Document getSearchedDocument(String personaUrl, String referrer, String userAgent){
        Document document=null;
        try {
            proxySetting.setProxy();
//            Proxy proxy = new Proxy(Proxy.Type.HTTP, )
            log.info("Url to Search {}",personaUrl);
            log.info("for Referrer: {}, User Agent: {}", referrer, userAgent);
            document = Jsoup.connect(personaUrl).referrer(referrer).userAgent(userAgent).proxy("p.webshare.io",80).timeout(60000).get();
            log.info("document text: "+document.text());
//            for(int trial=0;trial<1;trial++){
//                TimeUnit.SECONDS.sleep(new Random().nextInt(8));
//                if(checkIfLeadPresentInDuckDuckGo(document))
//                    break;
//                else{
//                    log.info("NODATA for {}, trying again...",personaUrl);
//                    referrer = referrerAndUserAgentSetting.getRandomReferrer();
//                    userAgent = referrerAndUserAgentSetting.getRandomUserAgent();
//                    log.info("Url to Search {}, trial: {}",personaUrl,String.valueOf(trial));
//                    log.info("for Referrer: {}, User Agent: {}", referrer, userAgent);
//                    document = Jsoup.connect(personaUrl).referrer(referrer).userAgent(userAgent).get();
//                    System.out.println("document text: "+document.text());
//                }
//            }

            if(checkIfLeadPresentInDuckDuckGo(document))
                log.info("result found for {},",personaUrl);
            else
                log.info("NODATA for {}, trying again...", personaUrl);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception occurred while executing bing query. Reason: "+e.toString());

        }
        return document;
    }

    public JSONObject getDuckDuckGoDataFromDocument(String searchQuery){

        String referrer = referrerAndUserAgentSetting.getRandomReferrer();
        String userAgent = referrerAndUserAgentSetting.getRandomUserAgent();
        Document document = getSearchedDocument(searchQuery, referrer, userAgent);
        JSONArray jsonArray = new JSONArray();
        JSONObject crawledResult = new JSONObject();

        if(null!=document) {
            try {
                Elements elementList = document.getElementsByClass(DUCKDUCKGO_RESULTS_LIST_CLASS);
                if (CollectionUtils.isNotEmpty(elementList)) {
                    for (Element element : elementList) {
                        JSONObject results = new JSONObject();
                        String searchTitle = "";
                        String profileUrl = "";
//                        System.out.println(element);
                        try {
                            searchTitle = element.getElementsByTag(ANCHOR_TAG).text();
                            profileUrl = element.getElementsByTag(ANCHOR_TAG).attr(HREF);
                        }
                        catch (Exception e){
                            log.error("Error occurred while getting data for query: {}, Reason: {}", searchQuery, e.toString());
                        }
                        if(!searchQuery.equalsIgnoreCase("") && !searchTitle.equalsIgnoreCase("")) {
                            results.put("title", searchTitle);
                            results.put("link", profileUrl);
                            jsonArray.add(results);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Error occurred while getting data for query: {}, Reason: {}", searchQuery, e.toString());
                notifier.notifySlack("Error occurred while getting data for query: " + searchQuery
                        +", Reason: "+e.toString());
            }
        }
        crawledResult.put("results",jsonArray);

        return crawledResult;
    }

}
