package com.scheduler.scheduler.listener;

import java.text.SimpleDateFormat;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenericJobListener implements JobListener {

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss:SSS");

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {

		String jobName = context.getJobDetail().getJobDataMap().getString("jobName");
		String jobInvokeParam = context.getJobDetail().getJobDataMap().getString("invokeParam");
		log.info("The job is going to be executed: " + jobName + ", jobInvokeParam: " + jobInvokeParam);

	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {

		String jobName = context.getJobDetail().getJobDataMap().getString("jobName");
		log.warn("The job was veoted and not executed: " + jobName);
	}


	private String getJobExecutionExceptionDetails(JobExecutionException jobException) {
		if (null != jobException) {
			return jobException.getMessage();
		} else {
			return " NO";
		}
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		log.info("Job was executed: {} at {}, jobException details: {}", 
				context.getJobDetail().getKey(), 
				sdf.format(System.currentTimeMillis()), 
				getJobExecutionExceptionDetails(jobException));
	}
}
