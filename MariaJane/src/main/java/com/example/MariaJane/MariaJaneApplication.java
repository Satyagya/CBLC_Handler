package com.example.MariaJane;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@Slf4j
@ComponentScan(basePackages = {"com.example.MariaJane"})
public class MariaJaneApplication {

	public static void main(String[] args) {
		SpringApplication.run(MariaJaneApplication.class, args);
	}

}
