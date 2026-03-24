package com.example.English.teaching.center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EnglishTeachingCenterApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnglishTeachingCenterApplication.class, args);
	}
}
