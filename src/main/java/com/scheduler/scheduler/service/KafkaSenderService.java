package com.scheduler.scheduler.service;

import java.time.ZoneOffset;
import java.util.List;
import java.util.TimeZone;

import com.scheduler.scheduler.job.NewJobClass;
import com.scheduler.scheduler.model.dao.SchedulerJobModelRequest;
import com.scheduler.scheduler.repository.CustomSchedulerRepository;
import org.quartz.CronScheduleBuilder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scheduler.scheduler.util.Constants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@DisallowConcurrentExecution
public class KafkaSenderService {

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private CustomSchedulerRepository schedulerRepository;

	
	//@Scheduled(cron="0 */1 * * * *")
	public void send() throws Exception {
		List<SchedulerJobModelRequest> list = schedulerRepository.findAll();
		log.info("schedule : " + list);
		for (SchedulerJobModelRequest sr : list) {
			this.schedule(sr);
		}

	}

	public void pause() {
		List<SchedulerJobModelRequest> list = schedulerRepository.findAll();
		log.info("pause : " + list);
		for (SchedulerJobModelRequest sr : list) {
			this.pauseJob(sr);
		}
	}

	public void resume() {
		List<SchedulerJobModelRequest> list = schedulerRepository.findAll();
		log.info("resume : " + list);
		for (SchedulerJobModelRequest sr : list) {
			this.resumeJob(sr);
		}

	}

	private void schedule(SchedulerJobModelRequest model) throws SchedulerException {
		try {	
			JobDetail jobDetail = createJobDetails(model);
			Trigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(model.getJobName())
					.withIdentity(model.getJobName(), model.getJobGroup()).withSchedule(CronScheduleBuilder
					.cronSchedule(model.getCronExpression()).inTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC))).build();
		      if (Boolean.TRUE.equals(this.checkJobExist(model.getJobName(), model.getJobGroup()))) {
		    	  TriggerKey triggerkey = TriggerKey.triggerKey(model.getJobName(), model.getJobGroup());
		    	  Trigger cronTrigger = scheduler.getTrigger(triggerkey);
					if (!checkJobRunning(model)) {
						scheduler.rescheduleJob(TriggerKey.triggerKey(model.getJobName(), model.getJobGroup()),
								cronTrigger);
					}
				} else {
					if (!checkJobRunning(model)) {
						scheduler.scheduleJob(jobDetail, trigger);
					}
				}
		} catch (Exception e) {
			log.info(e.toString());
		}
	}
	
	public boolean checkJobRunning(SchedulerJobModelRequest model) {
		boolean jobChecker = false;
		try {	
		JobDetail existingJobDetail = scheduler.getJobDetail(new JobKey(model.getJobName(), model.getJobGroup()));
		if (existingJobDetail != null) {
		    List<JobExecutionContext> currentlyExecutingJobs = scheduler.getCurrentlyExecutingJobs();
		    for (JobExecutionContext jec : currentlyExecutingJobs) {
		        if (existingJobDetail.equals(jec.getJobDetail())) {
		             String message = model.getJobName() + Constants.MSG_JOB_ALREADY_RUNNING;
		             log.info(message);
		             jobChecker = true;
		             return jobChecker;
		        }
		    }
		}
		}
		catch(Exception e) {
			log.error(e.getMessage());
		}
		return jobChecker;
	}
	
	public Boolean checkJobExist(String jobId, String gameGroup) throws SchedulerException {
        Boolean flag = false;
        log.info(Constants.MSG_INSIDE_CHECK_JOB_EXIST);
        TriggerKey triggerkey = TriggerKey.triggerKey(jobId, gameGroup);
        JobKey jobkey = new JobKey(jobId, gameGroup);
        if (scheduler.checkExists(jobkey) && scheduler.checkExists(triggerkey)) {
            flag = true;
        }
        return flag;
    }

	private JobDetail createJobDetails(SchedulerJobModelRequest model) {
		JobDetail jobDetail = null;
		JobDataMap jobDataMap = createJobDataMap(model.getJobName(), model.getInvokeParam());
		jobDetail = JobBuilder.newJob(new NewJobClass().getNewJobClass()).withIdentity(model.getJobName(), model.getJobGroup()).setJobData(jobDataMap)
				.requestRecovery().build();
		return jobDetail;
	}

	private JobDataMap createJobDataMap(String jobName, String invokeParam) {
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(Constants.KEY_JOB_NAME, jobName);
		if (null != invokeParam) {
			jobDataMap.put(Constants.KEY_INVOKE_PARAM, invokeParam);
		} else {
			jobDataMap.put(Constants.KEY_INVOKE_PARAM, "");
		}
		return jobDataMap;
	}

	private void pauseJob(SchedulerJobModelRequest model) {
		try {
			JobKey jobKey = new JobKey(model.getJobName(), model.getJobGroup());
			log.info("Pausing Job :" + model);
			scheduler.pauseJob(jobKey);
		} catch (Exception e) {
			log.info(e.toString());
		}
	}

	private void resumeJob(SchedulerJobModelRequest model) {
		try {
			JobKey jobKey = new JobKey(model.getJobName(), model.getJobGroup());
			log.info("Resuming Job :" + model);
			scheduler.resumeJob(jobKey);
		} catch (Exception e) {
			log.info(e.toString());
		}
	}

}
