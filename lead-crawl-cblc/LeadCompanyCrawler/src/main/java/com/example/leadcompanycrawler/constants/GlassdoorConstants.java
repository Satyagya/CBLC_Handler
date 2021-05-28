package com.example.leadcompanycrawler.constants;

public final class GlassdoorConstants {
  private GlassdoorConstants() {

  }

  public static final String COMPANY_URL_XPATH = "//*[@id='EmpBasicInfo']/div/div/div[1]/span";
  public static final String OVERVIEW = "Overview";
  public static final String BLANK = "";
  public static final String LABEL = "label";
  public static final String SPAN = "span";
  public static final String SRC = "src";
  public static final String WEBSITE_ATTR = "Website";
  public static final String HEADQUARTERS_ATTR = "Headquarters";
  public static final String SIZE_ATTR = "Size";
  public static final String FOUNDED_ATTR = "Founded";
  public static final String TYPE_ATTR = "Type";
  public static final String INDUSTRY_ATTR = "Industry";
  public static final String REVENUE_ATTR = "Revenue";
  public static final String HTTPS = "https://";
  public static final String HTTP = "http://";
  public static final String WEBSITE_REPLACE = "Website:";
  public static final String EMPLOYEES_REPLACE = "Employees";
  public static final String COMPANY_REQUEST_ID_LOWERCASE = "companyRequestId";
  public static final String COMPANY_REQUEST_TRACKER_ID_LOWERCASE = "companyRequestTrackerId";
  public static final String COMPANY_DETAILS_TSV = "tmp/companyDetails.tsv";
  public static final String REAL_COMPANY_DETAILS_TSV = "tmp/realtimeCompanyDetails.tsv";
}
