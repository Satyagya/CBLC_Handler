package com.crawling.loadbalancer.service;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

@Service("com.crawling.loadbalancer.service.LoadBalancerService")
public interface LoadBalancerService {

    String getNextSearchEngineForCrawl();
    JSONObject getCrawledJsonData(String searchQuery);
    JSONObject getUsageDetails();
}
