package com.crawling.loadbalancer.controller;

import com.crawling.loadbalancer.service.LoadBalancerService;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping(value = "/loadbalancer")
public class LoadBalancerController {

    @Autowired
    @Qualifier("com.crawling.loadbalancer.service.implementation.LoadBalancerServiceImpl")
    LoadBalancerService loadBalancerService;

    @GetMapping(value = "getSearchEngine")
    public String getNextSearchEngineToCrawl(){
        return loadBalancerService.getNextSearchEngineForCrawl();
    }

    @PostMapping(value = "getCrawledJson")
    public JSONObject getCrawledJson(@RequestBody JSONObject jsonObject){
        String searchQuery = (String) jsonObject.get("searchQuery");
        return loadBalancerService.getCrawledJsonData(searchQuery);
    }

    @PostMapping(value = "getUsageDetails")
    public JSONObject getUsageDetails(){
        return loadBalancerService.getUsageDetails();
    }
}
