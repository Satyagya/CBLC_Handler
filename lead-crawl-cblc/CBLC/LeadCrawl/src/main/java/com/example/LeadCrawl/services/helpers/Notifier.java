package com.example.LeadCrawl.services.helpers;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Component("com.example.LeadCrawl.services.helpers.Notifier")
@Slf4j
public class Notifier {

  @Value("${slack.webhook.url}")
  private String slackWebHookURL;

  @Autowired
  private WebClient webClient;
  /**
   * Notify slack in case if of any error messages during crawling
   */
  public void notifySlack(String message) {
    SlackPostBody postBody = new SlackPostBody();
    postBody.setText(message);
    try {
      webClient.post().uri(slackWebHookURL).body(BodyInserters.fromObject(postBody)).exchange()
          .block();
      log.debug("Slack notification send successfully");
    } catch (Exception e) {
      log.error("Exception while notifying in slack", e);
    }
  }
  /**
   * Class for notifying slack primarily for error messages
   */
  @Data
  private class SlackPostBody {
    //Don't change the name. Slack requires the name "text"
    private String text;
  }
}
