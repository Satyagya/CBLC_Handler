package com.jsoup.crawling.service.implementation;


import com.jsoup.crawling.service.JsoupCrawlingService;
import com.jsoup.crawling.service.helper.Notifier;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.jsoup.crawling.contants.Constants.*;

@Service("com.jsoup.crawling.service.implementation.JsoupCrawlingServiceImpl")
@Slf4j
public class JsoupCrawlingServiceImpl implements JsoupCrawlingService {

    @Autowired
    @Qualifier("com.jsoup.crawling.service.helper.Notifier")
    private Notifier notifier;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.BingCrawling")
    private BingCrawling bingCrawling;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.GoogleCrawling")
    private GoogleCrawling googleCrawling;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.DuckDuckGoCrawling")
    private DuckDuckGoCrawling duckDuckGoCrawling;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.AolCrawling")
    private AolCrawling aolCrawling;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.StartpageCrawling")
    private StartpageCrawling startpageCrawling;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.SwisscowsCrawling")
    private SwisscowsCrawling swisscowsCrawling;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.SearchEncryptCrawling")
    private SearchEncryptCrawling searchEncryptCrawling;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.LycosCrawling")
    private LycosCrawling lycosCrawling;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.ExciteCrawling")
    private ExciteCrawling exciteCrawling;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.YahooCrawling")
    private YahooCrawling yahooCrawling;


    @Value("${proxy.rotating.api}")
    private String proxyHost;

    @Value("${proxy.rotating.port}")
    private String proxyPort;

    @Value("${proxy.rotating.username}")
    private String proxyUsername;

    @Value("${proxy.rotating.password}")
    private String proxyPassword;

    @Value("${user.agent.path}")
    private String userAgentFilePath;

    @Value("${referrer.file.path}")
    private String referrerFilePath;


    private JSONObject getCrawledInfoFromBing(String searchQuery){
//        JSONObject crawledInfo = new JSONObject();
//        String referrer = getRandomReferrer("", referrerList);
//        String userAgent = getRandomUserAgent("", userAgentList);
//        crawledInfo = getBingDataFromDocument(searchQuery, referrer, userAgent);
        JSONObject crawledInfo = bingCrawling.getBingDataFromDocument(searchQuery);
        return crawledInfo;
    }
    private JSONObject getCrawledInfoFromGoogle(String searchQuery){
        JSONObject crawledInfo = googleCrawling.getGoogleDataFromDocument(searchQuery);
        return crawledInfo;
    }
    private JSONObject getCrawledInfoFromDuckDuckGo(String searchQuery){
        JSONObject crawledInfo = duckDuckGoCrawling.getDuckDuckGoDataFromDocument(searchQuery);
        return crawledInfo;
    }
    private JSONObject getCrawledInfoFromAOL(String searchQuery){
        JSONObject crawledInfo = aolCrawling.getAOLDataFromDocument(searchQuery);
        return crawledInfo;
    }
    private JSONObject getCrawledInfoFromSwisscows(String searchQuery){
        JSONObject crawledInfo = swisscowsCrawling.getSwisscowsDataFromDocument(searchQuery);
        return crawledInfo;
    }

    private JSONObject getCrawledInfoFromStartpage(String searchQuery){
        JSONObject crawledInfo = startpageCrawling.getStartpageDataFromDocument(searchQuery);
        return crawledInfo;
    }

    private JSONObject getCrawledInfoFromSearchEncrypt(String searchQuery){
        JSONObject crawledInfo = searchEncryptCrawling.getSearchEncryptDataFromDocument(searchQuery);
        return crawledInfo;
    }

    private JSONObject getCrawledInfoFromYahoo(String searchQuery){
        JSONObject crawledInfo = yahooCrawling.getYahooDataFromDocument(searchQuery);
        return crawledInfo;
    }

    private JSONObject getCrawledInfoFromExcite(String searchQuery){
        JSONObject crawledInfo = exciteCrawling.getExciteDataFromDocument(searchQuery);
        return crawledInfo;
    }

    private JSONObject getCrawledInfoFromLycos(String searchQuery){
        JSONObject crawledInfo = lycosCrawling.getLycosDataFromDocument(searchQuery);
        return crawledInfo;
    }



    @Override
    public JSONObject getCrawledInformation(String searchQuery, String searchEngine) {

        if (searchEngine.equalsIgnoreCase(BING)){
            return getCrawledInfoFromBing(searchQuery);
        }
        else if(searchEngine.equalsIgnoreCase(GOOGLE)){
            searchQuery = searchQuery.replace(BING_QUERY_PREFIX, GOOGLE_QUERY_PREFIX);
            return getCrawledInfoFromGoogle(searchQuery);
        }
        else if(searchEngine.equalsIgnoreCase(DUCKDUCKGO)){
            searchQuery = searchQuery.replace(BING_QUERY_PREFIX, DUCKDUCKGO_QUERY_PREFIX);
            return getCrawledInfoFromDuckDuckGo(searchQuery);
        }
        else if(searchEngine.equalsIgnoreCase(AOL)){
            searchQuery = searchQuery.replace(BING_QUERY_PREFIX, AOL_QUERY_PREFIX);
            return getCrawledInfoFromAOL(searchQuery);
        }
        else if(searchEngine.equalsIgnoreCase(SWISSCOWS)){
            searchQuery = searchQuery.replace(BING_QUERY_PREFIX, SWISSCOWS_QUERY_PREFIX);
            return getCrawledInfoFromSwisscows(searchQuery);
        }
        else if(searchEngine.equalsIgnoreCase(STARTPAGE)){
            searchQuery = searchQuery.replace(BING_QUERY_PREFIX, STARTPAGE_QUERY_PREFIX);
            return getCrawledInfoFromStartpage(searchQuery);
        }
        else if(searchEngine.equalsIgnoreCase(SEARCHENCRYPT)){
            searchQuery = searchQuery.replace(BING_QUERY_PREFIX, SEARCHENCRYPT_QUERY_PREFIX);
            return getCrawledInfoFromSearchEncrypt(searchQuery);
        }
        else if(searchEngine.equalsIgnoreCase(YAHOO)){
            searchQuery = searchQuery.replace(BING_QUERY_PREFIX, YAHOO_QUERY_PREFIX);
            return getCrawledInfoFromYahoo(searchQuery);
        }
        else if(searchEngine.equalsIgnoreCase(EXCITE)){
            searchQuery = searchQuery.replace(BING_QUERY_PREFIX, EXCITE_QUERY_PREFIX);
            return getCrawledInfoFromExcite(searchQuery);
        }
        else if(searchEngine.equalsIgnoreCase(LYCOS)){
            searchQuery = searchQuery.replace(BING_QUERY_PREFIX, LYCOS_QUERY_PREFIX);
            return getCrawledInfoFromLycos(searchQuery);
        }

        else return null;
    }
}
