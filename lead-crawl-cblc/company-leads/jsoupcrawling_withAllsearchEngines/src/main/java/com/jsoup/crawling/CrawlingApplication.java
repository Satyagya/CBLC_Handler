package com.jsoup.crawling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class CrawlingApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrawlingApplication.class, args);
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
