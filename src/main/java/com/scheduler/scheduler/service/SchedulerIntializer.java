package com.scheduler.scheduler.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.scheduler.scheduler.repository.CustomSchedulerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class SchedulerIntializer {
	
	@Autowired
	private JobService jobService;
	
	@Autowired
	private CustomSchedulerRepository schedulerRepository;
	
	@PostConstruct
	public void schedule() {
		try {
			jobService.schedule();
			jobService.resume();
		}catch (Exception e) {
			log.error(e.toString());
		}
	}
	
	@PreDestroy
	public void pauseScheduler() {
		try {
			jobService.pause();
		}catch (Exception e) {
			log.error(e.toString());
		}
	}
}
