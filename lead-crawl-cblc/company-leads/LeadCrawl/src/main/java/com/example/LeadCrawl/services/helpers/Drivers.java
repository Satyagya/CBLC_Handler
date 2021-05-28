package com.example.LeadCrawl.services.helpers;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static com.example.LeadCrawl.constants.Constants.MIN_POLITENESS_IN_MILLI_SEC;

@Slf4j
@Component("com.example.LeadCrawl.services.helpers.Drivers")
public class Drivers {
  @Value("${chrome.driver.path}")
  private String chromeDriverPath;

  private static final String NOT_A_ROBOT_FILE = "notaRobot.crx3";

  /**
   * Get chrome options
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  private ChromeOptions getChromeOptions() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("start-maximized");
    options.addArguments("--kiosk");
    options.addArguments("--js-flags=--expose-gc");
    options.addArguments("--enable-precise-memory-info");
    options.addArguments("--disable-popup-blocking");
    options.addArguments("disable-infobars");
    options.setExperimentalOption("useAutomationExtension", false);
    options
        .setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
    options.addArguments("--disable-notifications");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--no-sandbox");

    /**
     * disabling location
     */
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("profile.default_content_settings.geolocation", 2);
    options.setExperimentalOption("prefs", jsonObject);


    /**
     * disabling cookies
     */
    Map<String, Integer> prefs = new HashMap<>();
    prefs.put("profile.default_content_settings.cookies", 2);
    options.setExperimentalOption("prefs", prefs);

    options.addArguments("headless");
    options.addArguments("window-size=1200,1100");
    try {
      InputStream inputStream =
          this.getClass().getClassLoader().getResourceAsStream(NOT_A_ROBOT_FILE);
      File notARobot = new File(NOT_A_ROBOT_FILE);
      if (null != inputStream) {
        Files.copy(inputStream, notARobot.toPath(), StandardCopyOption.REPLACE_EXISTING);
        options.addExtensions(notARobot);
      }
    } catch (Exception e) {
      log.error("Exception while loading notARobot file", e);
    }
    return options;
  }

  /**
   * Get chrome driver service
   *
   * @return
   */
  private ChromeDriverService getChromeDriverService() {
    return new ChromeDriverService.Builder().usingDriverExecutable(new File(chromeDriverPath))
        .usingAnyFreePort().build();
  }

  /**
   * Intialize instance drivers
   *
   * @return
   */
  public WebDriver getWebDriver() {
    log.info("Getting web driver");
    ChromeOptions options = getChromeOptions();
    LoggingPreferences logs = new LoggingPreferences();
    logs.enable(LogType.DRIVER, Level.FINEST);
    DesiredCapabilities cap = new DesiredCapabilities();
    cap.setCapability(CapabilityType.LOGGING_PREFS, logs);
    options.merge(cap);
    return new ChromeDriver(getChromeDriverService(), options);
  }

  /**
   * closing webDriver which was initialized at the time of crawling
   *
   * @param driver
   */
  public void closeWebDriver(WebDriver driver) {
    log.info("Going to close web driver");
    try {
      driver.close();
      driver.quit();
      Thread.sleep(MIN_POLITENESS_IN_MILLI_SEC);
    } catch (Exception e) {
      log.error("Error while closing the driver", e);
    }
  }
}
