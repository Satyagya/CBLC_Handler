package com.crawling.loadbalancer.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchEngines {
    String name;
    boolean inSleep;
    boolean isBlocked;
    boolean isPreviousResultNoData;
    Instant timeToRefresh;
    int continousNoDataCount;
    int usedCount;
    //added for checking efficiency
    int totalNoDataCount;
    int totalSleepCount;
}
