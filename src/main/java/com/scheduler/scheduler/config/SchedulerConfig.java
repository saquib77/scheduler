package com.scheduler.scheduler.config;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.scheduler.scheduler.factory.QuartzJobFactory;
import com.scheduler.scheduler.util.Constants;

@Configuration
public class SchedulerConfig {

	@Autowired
	private QuartzProperties quartzProperties;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private QuartzJobFactory jobFactory;


	@Bean
	public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
		Properties properties = new Properties();
		properties.putAll(quartzProperties.getProperties());
		properties.put(Constants.QUARTZ_DRIVER_DELEGATE, Constants.QUARTZ_POSTGRESQL_DELEGATE);
		SchedulerFactoryBean factory = new SchedulerFactoryBean();
		factory.setOverwriteExistingJobs(true);
		factory.setDataSource(dataSource);
		factory.setQuartzProperties(properties);
		factory.setJobFactory(jobFactory);
		factory.setWaitForJobsToCompleteOnShutdown(true);
		factory.setOverwriteExistingJobs(false);
		factory.setStartupDelay(1);
		return factory;
	}
	
	

	@Bean(name = Constants.BEAN_SCHEDULER)
	public Scheduler getScheduler() throws SchedulerException, IOException {
		return schedulerFactoryBean().getScheduler();
	}
}

