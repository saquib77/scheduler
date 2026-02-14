package com.scheduler.scheduler.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class NewJobClass implements Job {
	
	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;
	
	private Class<? extends Job> newJobClass = NewJobClass.class;
	
	public NewJobClass() {}
	
	public Class<? extends Job> getNewJobClass(){
		return newJobClass;
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("Sending Kafka Topic : "+ context.getJobDetail().getKey().getGroup()); 
		kafkaTemplate.send(context.getJobDetail().getKey().getGroup(),1);
	}

}
