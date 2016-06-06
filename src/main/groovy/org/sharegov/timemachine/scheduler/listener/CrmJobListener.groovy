/*******************************************************************************
 * Copyright 2014 Miami-Dade County
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.sharegov.timemachine.scheduler.listener

import org.sharegov.timemachine.IHistoryTaskDAO
import org.sharegov.timemachine.scheduler.QuartzTaskFacade
import org.sharegov.timemachine.scheduler.Task
import org.sharegov.timemachine.scheduler.TaskConverter
import groovy.time.TimeCategory;

import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobListener
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CrmJobListener implements JobListener {
	
	IHistoryTaskDAO historyTaskDAO
	QuartzTaskFacade taskFacade
	
	private static Logger _log = LoggerFactory.getLogger(CrmJobListener.class);

    public String getName() {
        return "crmJobListener";
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
				else if (context.data.retry && context.data.retryMany)
					retryMany(context)
				else {
					//Task task = taskFacade.retrieve(context.jobDetail.name, context.jobDetail.group)
					//httpService.requestPost("http://127.0.0.1:5984/tm", TaskConverter.convertToMap(task) 	
				}
					
			}
			
			// persist to DB
			Task task = taskFacade.retrieve(context.jobDetail.name, context.jobDetail.group)
			historyTaskDAO.saveHistory(task, context.getFireTime(), false, context.data);
			
		} else {

			// reset the retries back to 1.
		
			Map retry = context.jobDetail.jobDataMap.retry
			
			retry = buildRetry(retry)
			retry.retryCount = 1
			
			context.jobDetail.jobDataMap.retry = retry
			
			_log.info("${context.jobDetail.group}/${context.jobDetail.name}: Job was executed successfully.")
			
			// persist to DB
			Task task = taskFacade.retrieve(context.jobDetail.name, context.jobDetail.group)
			historyTaskDAO.saveHistory(task, context.getFireTime(), true, context.data);
		
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
