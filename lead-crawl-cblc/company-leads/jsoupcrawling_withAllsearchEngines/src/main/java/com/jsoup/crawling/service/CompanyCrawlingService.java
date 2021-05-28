package com.jsoup.crawling.service;

import org.json.simple.JSONObject;

public interface CompanyCrawlingService {

    JSONObject getSearchPageLinks(String searchQuery, String searchEngine, int limit);

}
