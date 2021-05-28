package com.jsoup.crawling.contants;

public final class Constants {
    private Constants() {
    }

    public static final String EMPTY_SPACE = " ";
    public static final String COMMA_PATTERN = "\\,";
    public static final String EMPTY_STRING = "";
    public static final String NEW_LINE = "\n";
    public static final String DASH = "–";
    public static final String COMMA_SPACE = ", ";
    public static final String HREF = "href";
    public static final String SRC = "src";
    public static final long MIN_POLITENESS_IN_MILLI_SEC = 3000;
    public static final int MIN_LIMIT_POLITENESS_IN_MILLI_SEC = 10000;
    public static final int MAX_LIMIT_POLITENESS_IN_MILLI_SEC = 15000;
    public static final String NUMBER_PATTERN = "[\\d,]+";
    public static final String SOURCE_NAME = "SourceName";
    public static final String SOURCE_ID = "SourceId";
    public static final String ATTRIBUTE_INFO = "AttributeInfo";
    public static final String ENTITIES = "Entities";
    public static final String ATTRIBUTE_VALIDATION_TYPE = "attributeValidationType";
    public static final String ATTRIBUTE_VALUE_REGEX = "attributeValueRegex";
    public static final String ATTRIBUTE_PROCESSOR = "attributeProcessor";
    public static final String ATTRIBUTE_DEFAULT_VALUE = "defaultValue";
    public static final String UTF_8 = "UTF-8";
    public static final int THREE = 3;
    public static final int FIVE = 5;
    public static final long ZERO = 0L;
    public static final String NUMBER_IN_LINE_PATTERN = ".*[\\d].*";
    public static final String OF = " of ";
    public static final String TO = " to ";
    public static final String MIDDLE_DOT = " · ";
    public static final String ISSUE_RESOLVING_STEPS =
            "https://docs.google.com/document/d/1rO2ghg2XtuYd94tqiCiIDPrCF3VIk96FcN2NJIpgDQs/edit";
    public static final String ANY_NUMBER_PATTERN = ".*\\d.*";
    public static final String REQUEST_TYPE = "RequestType";
    public static final String EXCEPT_NUMBER_PATTERN = "[^0-9]";
    public static final String DATA_ID = "data-id";
    public static final String ARIA_LABEL = "aria-label";
    public static final String META = "meta";
    public static final String NAME = "name";
    public static final String CONTENT = "content";
    public static final String PROPERTY = "property";
    public static final String HASH_TAG = "#";
    public static final String SPAN_TAG = "</span>";
    public static final String LIST = "</li>";
    public static final String DOT_SPACE = "· ";
    public static final String POST_DESCRIPTION = "description";
    public static final String POST_TITLE = "og:title";
    public static final String POST_IMAGE = "og:image";
    public static final String POST_VIDEO = "og:video";
    public static final String BUTTON_CLICK_SCRIPT = "arguments[0].click();";
    public static final String WWW = "www.";
    public static final String DOUBLE_SLASH = "//";
    public static final Long THIRTY = 30L;
    public static final String ALPHABET_BRACKET = "[a-zA-Z()]";
    public static final String EXCEPT_NUMBER = "[^0-9]";
    public static final String YEAR = "yr";
    public static final String DATE_FORMAT = "d MMMM yyyy";
    public static final String SLASH = "/";
    public static final String COMMA = ",";
    public static final String LINKEDIN_SN_USERNAME = "rustom.suresh@gmail.com";
    public static final String LINKEDIN_SN_PASSWORD = "Wizworks@90";
    public static final String LINKEDIN_USERNAME = "dragoon1729@gmail.com";
    public static final String LINKEDIN_PASSWORD = "dragoon1729";
    public static final String FACEBOOK_USERNAME = "mikitaverma3@gmail.com";
    public static final String FACEBOOK_PASSWORD = "Leadmi@123";
    public static final String UNDERSCORE = "_";
    public static final String REQUEST = "REQUEST";
    public static final String SIMPLE_DATE_PATTERN = "yyyy.MM.dd.HH.mm.ss";
    public static final String LOCAL = "LOCAL";
    public static final Long TEN = 10L;
    public static final String AND = " AND ";
    public static final String BING_QUERY_POSTFIX =
            "'full profile' AND -https://www.linkedin.com/jobs AND -https://www"
                    + ".linkedin.com/company AND -https://www.linkedin.com/salary&first=";
    public static final String BING_QUERY_PREFIX =
            "https://www.bing.com/search?q=site:linkedin.com/in/ AND ";
    public static final String PROFILE_URL_XPATH = "//*[@class='b_algo']/h2/a";
    public static final String NO_RESULT_FOUND_XPATH = "//*[text()='There are no results for ']";
    public static final String MARIADB_CONFIG = "jdbc:mariadb://127.0.0.1:3306/AUTOMI_CRAWLER";
    public static final String MARIADB_USER_NAME = "automi_crawler_admin";
    public static final String MARIADB_PASSWORD = "automi_crawler_admin";
    public static final String MARIADB_DESG_COLUMN = "DESIGNATIONS";
    public static final String MARIADB_MAIL_COLUMN = "EMAIL_IDS";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String TITILE_XPATH_P1 = "//*[@id=\"b_results\"]/li[";
    public static final String TITILE_XPATH_P2 = "]/div/div[2]/div/ul[1]/li[1]/div";
    public static final String H2="h2";
    public static final String H3="h3";
    public static final String SPAN="span";
    public static final String ANCHOR_TAG="a";
    public static final String NO_EMAIL_PRESENT = "NO_EMAIL_PRESENT";
    public static final String BING_RESULTS_LIST_CLASS="b_algo";
    public static final String BING_NO_RESULT_FOR_SEARCH_CLASS = "b_no";
    public static final String NO_ANSWER_FOR_SEARCH_CLASS = "b_ans";
    public static final String GOOGLE_RESULTS_LIST_CLASS = "r";
    public static final String DUCKDUCKGO_RESULTS_LIST_CLASS = "result__title";
    public static final String SWISSCOWS_RESULTS_LIST_CLASS = "item item--web";
    public static final String AOL_RESULTS_LIST_CLASS = "compTitle options-toggle";
    public static final String STARTPAGE_RESULTS_LIST_CLASS = "w-gl__result-title";
    public static final String SEARCHENCRYPT_RESULTS_LIST_CLASS = "web-result__title";
    public static final String GOOGLE_QUERY_PREFIX =
            "https://www.google.com/search?q=site:linkedin.com/in/ AND ";
    public static final String DUCKDUCKGO_QUERY_PREFIX =
            "https://html.duckduckgo.com/html/?q=site:linkedin.com/in/ AND ";
    public static final String AOL_QUERY_PREFIX =
            "https://search.aol.com/aol/search?q=site:linkedin.com/in/ AND ";
    public static final String SWISSCOWS_QUERY_PREFIX =
            "https://swisscows.com/web?query=site:linkedin.com/in/ AND ";
    public static final String STARTPAGE_QUERY_PREFIX =
            "https://www.startpage.com/sp/search?q=site:linkedin.com/in/ AND ";
    public static final String SEARCHENCRYPT_QUERY_PREFIX =
            "https://www.searchencrypt.com/search?q=site:www.linkedin.com/in/ AND ";
    public static final String LYCOS_QUERY_PREFIX =
            "https://search16.lycos.com/web/?q=site:linkedin.com/in AND ";
    public static final String YAHOO_QUERY_PREFIX =
            "https://in.search.yahoo.com/search?p=site:linkedin.com/in AND ";
    public static final String EXCITE_QUERY_PREFIX =
            "https://results.excite.com/serp?q=linkedin.com/in AND ";
    public static final String BING = "BING";
    public static final String GOOGLE = "GOOGLE";
    public static final String DUCKDUCKGO = "DUCKDUCKGO";
    public static final String AOL = "AOL";
    public static final String SWISSCOWS = "SWISSCOWS";
    public static final String STARTPAGE = "STARTPAGE";
    public static final String SEARCHENCRYPT = "SEARCHENCRYPT";
    public static final String LYCOS = "LYCOS";
    public static final String YAHOO = "YAHOO";
    public static final String EXCITE = "EXCITE";

}


