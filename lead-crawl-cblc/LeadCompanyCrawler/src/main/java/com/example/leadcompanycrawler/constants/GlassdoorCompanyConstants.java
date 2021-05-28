package com.example.leadcompanycrawler.constants;

public final class GlassdoorCompanyConstants {
  private GlassdoorCompanyConstants() {
  }

  public static final String USER_AGENT =
      "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782"
          + ".112 Safari/535.1";
  public static final String GLASSDOOR_BASE_URL = "https://www.glassdoor.com";
  public static final String GLASSDOOR_COMPANY_DIV_CSSQUERY = "div.header.cell.info";
  public static final String COMPANY_URL_CSSQUERY = "div.margBotXs>a";
  public static final String COMPANY_DETAILS_DIV_CSSQUERY = "div.infoEntity";
  public static final String COMPANY_NAME_CSSQUERY = "#DivisionsDropdownComponent";
  public static final String COMPANY_RATING_CSSQUERY = "span.hidden.rating";
  public static final String COMPANY_LOGO_CSSQUERY =
      "span.sqLogo.tighten.medSqLogo.logoOverlay>img";
  public static final String JSON_EXTENSION = ".json";
  public static final String BING_QUERY = "https://www.bing.com/search?q=site:glassdoor.co.in/Overview AND ";
  public static final String FIRST_RESULT_CLASS="b_algo";
  public static final String H2="h2";
  public static final String ANCHOR_TAG="a";
  public static final String HREF="href";
}
