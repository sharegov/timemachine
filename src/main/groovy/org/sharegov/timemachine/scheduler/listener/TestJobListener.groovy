package org.sharegov.timemachine.scheduler.listener

import java.util.Map;

import org.sharegov.timemachine.scheduler.QuartzTaskFacade
import org.sharegov.timemachine.scheduler.TaskConverter
import org.sharegov.timemachine.scheduler.Task
import org.sharegov.timemachine.service.HTTPService
import groovy.time.TimeCategory;

import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobListener
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




class TestJobListener implements JobListener {
	
	HTTPService httpService
	QuartzTaskFacade taskFacade
		
	private static Logger _log = LoggerFactory.getLogger(CrmJobListener.class);

    public String getName() {
        return "testJobListener";
    }

    public void jobToBeExecuted(JobExecutionContext context) {
        _log.info "${context.jobDetail.group}/${context.jobDetail.name} is about to be executed."
    }

    public void jobExecutionVetoed(JobExecutionContext context) {
        _log.info "${context.jobDetail.group}/${context.jobDetail.name} was vetoed."
    }

    public void jobWasExecuted(JobExecutionContext context,
            JobExecutionException jee) {	

		
		// job executed with errors
		if(jee) {			
			_log.info("${context.jobDetail.group}/${context.jobDetail.name}: was NOT executed successfully.");
			
			// retry only for simple triggers
			if (context.trigger.class == SimpleTriggerImpl.class){
				
				if(context.data.retry && context.data.retryAfter)
					retryOnce(context, context.data.retryAfter)
				else if (context.data.retry)
					retryMany(context)
			}
			
			Task task = taskFacade.retrieve(context.jobDetail.name, context.jobDetail.group)
			Map data = TaskConverter.	covertToMap(task)
			data.ok = false
			httpService.requestPost("http://s0141668:5984/tm", data)
			
		} else {

			// reset the retries back to 1.
		
			Map retry = context.jobDetail.jobDataMap.retry
			
			retry = buildRetry(retry)
			retry.retryCount = 1
			
			context.jobDetail.jobDataMap.retry = retry
			
			_log.info("${context.jobDetail.group}/${context.jobDetail.name}: Job was executed successfully.")

			Task task = taskFacade.retrieve(context.jobDetail.name, context.jobDetail.group)
			Map data = TaskConverter.covertToMap(task)
			data.ok = true
			httpService.requestPost("http://s0141668:5984/tm", data)					
		}
		

    }
			
	
	private void retryOnce(JobExecutionContext context, Integer retryAfter){
		Trigger retrievedTrigger = context.trigger
		use(TimeCategory){
			retrievedTrigger.startTime = new Date() + retryAfter.seconds
		}
		
		context.scheduler.rescheduleJob(retrievedTrigger.key, retrievedTrigger)
		context.jobDetail.jobDataMap.retry = [retryAfter:retryAfter]
	}
			
	private void retryMany(JobExecutionContext context) {
		
		// collect retry data
		Map retry = context.jobDetail.jobDataMap.retry
		
		retry = buildRetry(retry)

		// still retries left. Reset trigger
		if(retry.retryCount <= retry.retryMaxCount) {
			_log.info("${context.jobDetail.group}/${context.jobDetail.name}: retryCount: ${retry.retryCount} of ${retry.retryMaxCount}")
			Trigger retrievedTrigger = context.trigger
			Date startTime = retrievedTrigger.startTime
			Integer retryInterval = retry.retryInterval
			use(TimeCategory){
				retrievedTrigger.startTime = new Date() + retryInterval.minutes
			}
			
			context.scheduler.rescheduleJob(retrievedTrigger.key, retrievedTrigger)
			retry.retryCount += 1
		}
		// Too many retries. No more. Reset count.
		else {
			_log.error("${context.jobDetail.group}/${context.jobDetail.name}: will not be retried any more. retryCount: ${retry.retryCount} exceeds ${retry.retryMaxCount}")
			retry.retryCount = 1
		}
		
		// keep the new retry state.
		context.jobDetail.jobDataMap.retry = retry
		
	}		
			

	private Map buildRetry(retry){
		
		Map newRetry = [:]
		if(!retry)
			newRetry = [retryMaxCount: 20, retryCount: 1, retryInterval: 2]
		else {
			newRetry.retryMaxCount =  retry.retryMaxCount ?: 20
			newRetry.retryCount = retry.retryCount ?: 1
			newRetry.retryInterval = retry.retryInterval ?: 2
		}
		
		return newRetry
				
			
	}
}	