package com.crawling.loadbalancer.service.implementation;

import com.crawling.loadbalancer.model.SearchEngines;
import com.crawling.loadbalancer.service.LoadBalancerService;
import com.crawling.loadbalancer.service.helper.JsoupCrawlConnection;
import com.crawling.loadbalancer.service.helper.WebshareIntegration;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.crawling.loadbalancer.constants.Constants.*;


@Service("com.crawling.loadbalancer.service.implementation.LoadBalancerServiceImpl")
@Slf4j
public class LoadBalancerServiceImpl implements LoadBalancerService {

    @Autowired
    @Qualifier("com.crawling.loadbalancer.service.helper.JsoupCrawlConnection")
    private JsoupCrawlConnection jsoupCrawlConnection;

    @Autowired
    @Qualifier("com.crawling.loadbalancer.service.helper.WebshareIntegration")
    private WebshareIntegration webshareIntegration;

    private SearchEngines bing;
    private SearchEngines aol;
    private SearchEngines google;
    private SearchEngines duckduckgo;
    private SearchEngines searchEncrypt;
    private SearchEngines startPage;
    private SearchEngines lycos;
    private SearchEngines yahoo;
    private SearchEngines excite;
    private SearchEngines swisscows;


    @Value("${search.engines.list}")
    private String[] searchEngineArray;

    @Value("${continous.nodata.count.limit}")
    private int nodataLimit;

    private List<SearchEngines> searchEnginesList;
    private int searchEngineCounter;
    private Instant timeToRefresh;


    @PostConstruct
    public void init(){
        searchEnginesList = new ArrayList<>();
        searchEngineCounter=0;
        initializeSearchEngines();
        JSONObject jsonObject = webshareIntegration.getWebshareIpRotationData();
        String time =(String) jsonObject.get("next_rotate_at");
        time = time.substring(0,23)+"Z";
        Instant rotationTime = Instant.parse(time);
        rotationTime = rotationTime.plus(7, ChronoUnit.HOURS);
        timeToRefresh = rotationTime;
    }

    @Scheduled(fixedDelayString = "${poll.frequency.millis}")
    private void refreshAllSearchEngines(){

        JSONObject jsonObject = webshareIntegration.getWebshareIpRotationData();
        String time =(String) jsonObject.get("next_rotate_at");
        time = time.substring(0,23)+"Z";
        Instant rotationTime = Instant.parse(time);
        rotationTime = rotationTime.plus(7, ChronoUnit.HOURS);
        log.info("next rotation time: "+rotationTime.toString());

        Instant now = Instant.now();
        if (now.isAfter(timeToRefresh)){
            log.info("Refreshing the Blocked Search Engines");
            for (SearchEngines i:searchEnginesList) {
                i.setInSleep(false);
                i.setBlocked(false);
                i.setContinousNoDataCount(0);
                i.setTimeToRefresh(rotationTime);
                i.setPreviousResultNoData(false);
            }
            timeToRefresh=rotationTime;
        }
    }

    public String getNextSearchEngineForCrawl(){

        int count=0;
        String searchEngineName = getNextSearchEngineFromArray(searchEngineCounter++);
        int index = getSearchEngineIndex(searchEngineName);
        SearchEngines searchEnginesObject = searchEnginesList.get(index);
        if (searchEnginesObject.isInSleep()) {
            searchEngineName=null;
            while(searchEnginesObject.isInSleep() && (searchEngineCounter%searchEngineArray.length)!=searchEngineArray.length){
                count++;
                searchEngineName = getNextSearchEngineFromArray(searchEngineCounter++);
                index = getSearchEngineIndex(searchEngineName);
                searchEnginesObject = searchEnginesList.get(index);

                if (count==searchEngineArray.length)
                {
                    searchEngineName = ALL_SEARCH_ENGINES_IN_SLEEP;
                    return searchEngineName;
                }
            }
            searchEngineCounter++;
        }

        return searchEngineName;
    }

    private String getNextSearchEngineFromArray(int index){
        index = index % searchEngineArray.length;
        String searchEngine = searchEngineArray[index];
        return searchEngine;
    }

    /**
     * check if search engine in sleep
     */
    private boolean checkIfSearchEngineInSleep(){
        boolean status = false;

        return status;
    }


    /**
     * add search engine the sleep status
     */
    private void makeSearchEngineSleep(SearchEngines searchEnginesObject){
        JSONObject webshareData = webshareIntegration.getWebshareIpRotationData();
        String time =(String) webshareData.get("next_rotate_at");
        time = time.substring(0,23)+"Z";
//        OffsetDateTime now = OffsetDateTime.now( ZoneOffset.UTC );
        Instant instant = Instant.now();
        log.info("offset: " + instant);
        Instant rotationTime = Instant.parse(time);
        rotationTime = rotationTime.plus(7, ChronoUnit.HOURS);
        log.info("rotation time: "+rotationTime.toString());

        searchEnginesObject.setTimeToRefresh(rotationTime);
        searchEnginesObject.setBlocked(true);
        searchEnginesObject.setInSleep(true);
        searchEnginesObject.setTotalSleepCount(searchEnginesObject.getTotalSleepCount()+1);
        log.info("Search Engine: {} going to sleep.", searchEnginesObject.getName());
        if (rotationTime.isAfter(timeToRefresh))
            timeToRefresh = rotationTime;
    }

    private int getSearchEngineIndex(String searchEngine){
        searchEngine = searchEngine.toLowerCase();
        switch (searchEngine){
            case "bing":
                return 0;

            case "aol":
                return 1;

            case "google":
                return 2;

            case "duckduckgo":
                return 3;

            case "searchencrypt":
                return 4;

            case "startpage":
                return 5;

            case "lycos":
                return 6;

            case "yahoo":
                return 7;

            case "excite":
                return 8;

            case "swisscows":
                return 9;

            default:
                return -1;
        }
    }

    public JSONObject getCrawledJsonData(String searchQuery){
        log.info("finding results for: {}",searchQuery);
        String searchEngine = getNextSearchEngineForCrawl();
        JSONObject resultObject = new JSONObject();

        if (searchEngine.equalsIgnoreCase(ALL_SEARCH_ENGINES_IN_SLEEP)){
            resultObject.put("results", new JSONArray());
            resultObject.put(ALL_SEARCH_ENGINES_IN_SLEEP,true);
            log.info("All search Engines in Sleep...");
        }
        else {
            resultObject = jsoupCrawlConnection.getCrawledData(searchQuery, searchEngine);
            JSONArray jsonArray = (JSONArray) resultObject.get("results");
            resultObject.put(ALL_SEARCH_ENGINES_IN_SLEEP, false);
            if (jsonArray.size() == 0) {
                log.info("NODATA for {}, for searchEngine: {}", searchQuery, searchEngine);
                int index = getSearchEngineIndex(searchEngine);
                SearchEngines searchEnginesObject = searchEnginesList.get(index);
                if (searchEnginesObject.isPreviousResultNoData()){
                    searchEnginesObject.setContinousNoDataCount(searchEnginesObject.getContinousNoDataCount() + 1);
                }
                else {
                    searchEnginesObject.setContinousNoDataCount(1);
                }

                searchEnginesObject.setPreviousResultNoData(true);
                searchEnginesObject.setUsedCount(searchEnginesObject.getUsedCount()+1);
                searchEnginesObject.setTotalNoDataCount(searchEnginesObject.getTotalNoDataCount()+1);

                if (searchEnginesObject.getContinousNoDataCount() >= nodataLimit)
                    makeSearchEngineSleep(searchEnginesObject);
            }
            else{
                int index = getSearchEngineIndex(searchEngine);
                SearchEngines searchEnginesObject = searchEnginesList.get(index);
                searchEnginesObject.setUsedCount(searchEnginesObject.getUsedCount()+1);
                searchEnginesObject.setPreviousResultNoData(false);
                searchEnginesObject.setContinousNoDataCount(0);
            }
        }
        return resultObject;
    }

    private void initializeSearchEngines(){

        bing = SearchEngines.builder().inSleep(false).isBlocked(false).continousNoDataCount(0).name(BING.toLowerCase()).isPreviousResultNoData(false).usedCount(0).totalNoDataCount(0).totalSleepCount(0).timeToRefresh(Instant.now()).build();
        searchEnginesList.add(bing);

        aol = SearchEngines.builder().inSleep(false).isBlocked(false).continousNoDataCount(0).name(AOL.toLowerCase()).isPreviousResultNoData(false).usedCount(0).totalNoDataCount(0).totalSleepCount(0).timeToRefresh(Instant.now()).build();
        searchEnginesList.add(aol);

        google = SearchEngines.builder().inSleep(false).isBlocked(false).continousNoDataCount(0).name(GOOGLE.toLowerCase()).isPreviousResultNoData(false).usedCount(0).totalNoDataCount(0).totalSleepCount(0).timeToRefresh(Instant.now()).build();
        searchEnginesList.add(google);

        duckduckgo = SearchEngines.builder().inSleep(false).isBlocked(false).continousNoDataCount(0).name(DUCKDUCKGO.toLowerCase()).isPreviousResultNoData(false).usedCount(0).totalNoDataCount(0).totalSleepCount(0).timeToRefresh(Instant.now()).build();
        searchEnginesList.add(duckduckgo);

        searchEncrypt = SearchEngines.builder().inSleep(false).isBlocked(false).continousNoDataCount(0).name(SEARCHENCRYPT.toLowerCase()).isPreviousResultNoData(false).usedCount(0).totalNoDataCount(0).totalSleepCount(0).timeToRefresh(Instant.now()).build();
        searchEnginesList.add(searchEncrypt);

        startPage = SearchEngines.builder().inSleep(false).isBlocked(false).continousNoDataCount(0).name(STARTPAGE.toLowerCase()).isPreviousResultNoData(false).usedCount(0).totalNoDataCount(0).totalSleepCount(0).timeToRefresh(Instant.now()).build();
        searchEnginesList.add(startPage);

        lycos = SearchEngines.builder().inSleep(false).isBlocked(false).continousNoDataCount(0).name(LYCOS.toLowerCase()).isPreviousResultNoData(false).usedCount(0).totalNoDataCount(0).totalSleepCount(0).timeToRefresh(Instant.now()).build();
        searchEnginesList.add(lycos);

        yahoo = SearchEngines.builder().inSleep(false).isBlocked(false).continousNoDataCount(0).name(YAHOO.toLowerCase()).isPreviousResultNoData(false).usedCount(0).totalNoDataCount(0).totalSleepCount(0).timeToRefresh(Instant.now()).build();
        searchEnginesList.add(yahoo);

        excite = SearchEngines.builder().inSleep(false).isBlocked(false).continousNoDataCount(0).name(EXCITE.toLowerCase()).isPreviousResultNoData(false).usedCount(0).totalNoDataCount(0).totalSleepCount(0).timeToRefresh(Instant.now()).build();
        searchEnginesList.add(excite);

        swisscows = SearchEngines.builder().inSleep(false).isBlocked(false).continousNoDataCount(0).name(SWISSCOWS.toLowerCase()).isPreviousResultNoData(false).usedCount(0).totalNoDataCount(0).totalSleepCount(0).timeToRefresh(Instant.now()).build();
        searchEnginesList.add(swisscows);
    }

    public static boolean isValid(String url)
    {

        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con =
                    (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public JSONObject getUsageDetails(){
        JSONObject jsonObject = new JSONObject();

        List<Map<String,List<Map<String,Integer>>>> usageDetails = new ArrayList<>();
        for (SearchEngines i: searchEnginesList){
            Map<String,Integer> totalNoDataCount = new HashMap<>();
            Map<String,Integer> totalSleepCount = new HashMap<>();
            Map<String,List<Map<String,Integer>>> searchEngineData = new HashMap<>();

            totalSleepCount.put("totalSleepCount", i.getTotalSleepCount());
            totalNoDataCount.put("totalNoDataCount", i.getTotalNoDataCount());
            List<Map<String,Integer>> list = new ArrayList<>();
            list.add(totalNoDataCount);
            list.add(totalSleepCount);
            searchEngineData.put(i.getName(), list);

            usageDetails.add(searchEngineData);
        }
        jsonObject.put("data", usageDetails);

        return jsonObject;
    }

}
