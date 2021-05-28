package com.example.LeadCrawl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {"com.example.LeadCrawl", "org.redisson.api"})
@Slf4j
public class LeadCrawlApplication {

  public static void main(String[] args) {
    try {
      SpringApplication.run(LeadCrawlApplication.class, args);
    } catch (Exception e) {
      e.printStackTrace();
    }


  }

  /**
   * WebClient bean
   *
   * @return
   */
  @Bean
  public WebClient webClient() {
    return WebClient.create();
  }

}
