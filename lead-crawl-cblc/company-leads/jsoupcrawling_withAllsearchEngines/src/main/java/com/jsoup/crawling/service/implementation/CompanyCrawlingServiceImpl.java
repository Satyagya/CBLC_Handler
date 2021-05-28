package com.jsoup.crawling.service.implementation;


import com.jsoup.crawling.service.CompanyCrawlingService;
import com.jsoup.crawling.service.helper.Notifier;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import static com.jsoup.crawling.contants.Constants.*;


@Service("com.jsoup.crawling.service.implementation.CompanyCrawlingServiceImpl")
@Slf4j
public class CompanyCrawlingServiceImpl implements CompanyCrawlingService {


    @Autowired
    @Qualifier("com.jsoup.crawling.service.helper.Notifier")
    private Notifier notifier;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.BingCrawling")
    private BingCrawling bingCrawling;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.GoogleCrawling")
    private GoogleCrawling googleCrawling;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.DuckDuckGoCrawling")
    private DuckDuckGoCrawling duckDuckGoCrawling;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.AolCrawling")
    private AolCrawling aolCrawling;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.StartpageCrawling")
    private StartpageCrawling startpageCrawling;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.SwisscowsCrawling")
    private SwisscowsCrawling swisscowsCrawling;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.SearchEncryptCrawling")
    private SearchEncryptCrawling searchEncryptCrawling;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.LycosCrawling")
    private LycosCrawling lycosCrawling;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.ExciteCrawling")
    private ExciteCrawling exciteCrawling;

    @Autowired
    @Qualifier("com.jsoup.crawling.service.implementation.YahooCrawling")
    private YahooCrawling yahooCrawling;


    @Override
    public JSONObject getSearchPageLinks(String searchQuery, String searchEngine, int limit) {
        if (searchEngine.equalsIgnoreCase(GOOGLE))
            return googleCrawling.getSearchPageLinks(searchQuery, limit);
        else if(searchEngine.equalsIgnoreCase(LYCOS))
            return lycosCrawling.getSearchPageLinks(searchQuery, limit);
        else if(searchEngine.equalsIgnoreCase(YAHOO))
            return yahooCrawling.getSearchPageLinks(searchQuery, limit);
        else return null;
    }
}
