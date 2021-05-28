package com.jsoup.crawling.service;

import org.json.simple.JSONObject;

public interface JsoupCrawlingService {

    JSONObject getCrawledInformation(String searchQuery, String searchEngine);

}
