package com.example.LeadCrawl.services.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import static com.example.LeadCrawl.constants.Constants.*;
import static com.example.LeadCrawl.constants.Constants.DOUBLE_SLASH;

@Component("com.example.LeadCrawl.services.helpers.Helper")
@Slf4j
public class Helper {

  public void navigateToUrl(WebDriver driver, String sourceUrl) {
    sourceUrl = decodeUrl(sourceUrl);
    try {
      driver.manage().timeouts().pageLoadTimeout(THREE, TimeUnit.MINUTES);
      driver.get(sourceUrl);
      log.debug("Navigation successfull for sourceUrl: {}", sourceUrl);
    } catch (Exception e) {
      log.error("Error while navigating to sourceUrl: {}", sourceUrl, e);
    }
  }

  public String decodeUrl(String url) {
    String decodedUrl = null;
    try {
      decodedUrl = URLDecoder.decode(url, UTF_8);
    } catch (UnsupportedEncodingException e) {
      log.error("UnsupportedEncodingException while decoding url: {}", url, e);
    }
    return decodedUrl;
  }

  public void sleepInSeconds(Long seconds) {
    try {
      log.debug("Going to sleep for {} sec", seconds);
      Thread.sleep(seconds * 1000);
    } catch (Exception e) {
      log.error("Exception while sleeping for {} sec", seconds);
    }
  }

  public List<WebElement> applyFluentWaitByXpathForList(WebDriver driver, String xpath) {
    FluentWait<WebDriver> wait = getFluentWait(driver);
    return wait.until(webDriver -> webDriver.findElements(By.xpath(xpath)));
  }

  private FluentWait<WebDriver> getFluentWait(WebDriver driver) {
    return new FluentWait<>(driver).withTimeout(Duration.ofMinutes(THREE))
        .pollingEvery(Duration.ofSeconds(FIVE)).ignoring(NoSuchElementException.class);
  }

  public String getHashCode(String profileUrl) {
    return String.valueOf(profileUrl.hashCode());
  }

  public WebElement applyFluentWaitByXpath(WebDriver driver, String xpath) {
    FluentWait<WebDriver> wait = getFluentWait(driver);
    return wait.until(webDriver -> webDriver.findElement(By.xpath(xpath)));
  }
  /**
   * return hostname for a websiteUrl eg: www.stackify.com -> stackify.com
   * by converting url into required format and then removing uneccessary word from url eg: www.
   *
   * @param websiteUrl
   *
   * @return
   */
  public String getHostName(String websiteUrl) {
    String hostName = null;
    if (StringUtils.isNotBlank(websiteUrl)) {
      websiteUrl = websiteUrl.toLowerCase().trim();
      String fullUrl = getFullUrl(websiteUrl);
      try {
        hostName = getHostNameIfUrlIsNotBlank(fullUrl);
      } catch (URISyntaxException e) {
        log.error("URISyntaxException while getting host name for url: {}", websiteUrl, e);
      }
    }
    return hostName;
  }

  /**
   * remove WWW in case of WWW is present as a prefix in hostname
   *
   * @param hostName
   *
   * @return
   */
  private String removeWWW(String hostName) {
    String refactoredHostName = hostName;
    if (StringUtils.isNotBlank(hostName) && hostName.contains(WWW)) {
      refactoredHostName = hostName.replaceAll(WWW, EMPTY_STRING).trim();
    }
    return refactoredHostName;
  }

  /**
   * return hostname is null in case of empty or null url otherwise return correct hostname
   *
   * @param websiteUrl
   *
   * @return
   *
   * @throws URISyntaxException
   */
  private String getHostNameIfUrlIsNotBlank(String websiteUrl) throws URISyntaxException {
    String hostName = null;
    URI url;
    if (StringUtils.isNotBlank(websiteUrl)) {
      url = new URI(websiteUrl);
      hostName = url.getHost();
      hostName = removeWWW(hostName);
    } else {
      log.debug("WebsiteUrl passed is null for companyUrl: {}", websiteUrl);
    }
    return hostName;
  }

  /**
   * return full website URL in case of missing slash to convert
   * into acceptable case for URI eg: www.coviam.com -> //www.coviam.com
   *
   * @param url
   *
   * @return
   */
  private String getFullUrl(String url) {
    String fullUrl = url;
    if (StringUtils.isNotBlank(url) && !url.contains(DOUBLE_SLASH)) {
      fullUrl = String.format("%s%s", DOUBLE_SLASH, url);
    }
    return fullUrl;
  }
}
