package com.scheduler.scheduler;

import org.quartz.SchedulerException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@SpringBootApplication(scanBasePackageClasses = { SchedulerServiceApplication.class })
@PropertySource({ "classpath:application.properties" })
@EnableEurekaClient
public class SchedulerServiceApplication {
	
	public static void main(String[] args) throws JsonMappingException, JsonProcessingException, SchedulerException {
		SpringApplication.run(SchedulerServiceApplication.class, args);
		
	}
}
