package com.scheduler.scheduler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JobService {
	
	@Autowired
	private KafkaSenderService kafkaSenderService;
	
	
	public void schedule() throws Exception{
		log.info("Scheduling up");
		kafkaSenderService.send();
	}
	
	public void pause(){
		log.info("Pausing Job");
		kafkaSenderService.pause();
	}

	public void resume(){
		log.info("Resuming Job");
		kafkaSenderService.resume();
	}
	
}
