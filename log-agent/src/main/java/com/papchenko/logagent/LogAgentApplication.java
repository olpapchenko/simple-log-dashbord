package com.papchenko.logagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LogAgentApplication {
	public static void main(String[] args) {
		SpringApplication.run(LogAgentApplication.class, args);
	}
}
