package com.example.leadcompanycrawler.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyNames {
  @JsonProperty(value = "REQUEST_ID")
  private long requestId;
  @JsonProperty(value = "COMPANY_NAMES")
  private List<String> companyNameList;
}
