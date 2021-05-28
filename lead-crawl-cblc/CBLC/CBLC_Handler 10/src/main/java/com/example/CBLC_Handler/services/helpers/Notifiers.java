package com.example.CBLC_Handler.services.helpers;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

//import org.springframework.web.reactive.function.client.WebClient;


@Slf4j
@Service
public class Notifiers {

    @Autowired(required = false)
    private WebClient webClient;

    @Value("${slack.webhook.url}")
    private String slackWebHookURL;

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
