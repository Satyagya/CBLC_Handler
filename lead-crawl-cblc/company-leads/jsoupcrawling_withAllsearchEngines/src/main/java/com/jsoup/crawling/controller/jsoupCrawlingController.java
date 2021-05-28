package com.jsoup.crawling.controller;

import com.jsoup.crawling.service.CompanyCrawlingService;
import com.jsoup.crawling.service.JsoupCrawlingService;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/jsoupCrawling")
@Slf4j
public class jsoupCrawlingController {

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.JsoupCrawlingServiceImpl")
    private JsoupCrawlingService jsoupCrawlingService;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.CompanyCrawlingServiceImpl")
    private CompanyCrawlingService companyCrawlingService;

    @PostMapping(value = "/getJson")
    public JSONObject getCrawledJSON(@RequestBody JSONObject requestParameters){
        String searchQuery = (String) requestParameters.get("searchQuery");
        String searchEngine = (String) requestParameters.get("searchEngine");
        return jsoupCrawlingService.getCrawledInformation(searchQuery, searchEngine);
    }

    @PostMapping(value = "/getCompanyLinks")
    public JSONObject getCrawledLinksJSON(@RequestBody JSONObject requestParameters){
        String searchQuery = (String) requestParameters.get("searchQuery");
        String searchEngine = (String) requestParameters.get("searchEngine");
        int limit = Integer.parseInt((String) requestParameters.get("limit"));
        return companyCrawlingService.getSearchPageLinks(searchQuery, searchEngine, limit);
    }
}
