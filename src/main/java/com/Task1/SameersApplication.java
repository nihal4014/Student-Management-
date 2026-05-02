package com.Task1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SameersApplication {

	public static void main(String[] args) {
		SpringApplication.run(SameersApplication.class, args);
	}

}
