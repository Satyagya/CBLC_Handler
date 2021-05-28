package com.crawling.loadbalancer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {"com.crawling.loadbalancer"})
@Slf4j
public class LoadbalancerApplication {

	public static void main(String[] args) {
		try {
			SpringApplication.run(LoadbalancerApplication.class, args);
		}
		catch (Exception e){
			e.printStackTrace();
			log.error(e.toString());
		}
	}

}
