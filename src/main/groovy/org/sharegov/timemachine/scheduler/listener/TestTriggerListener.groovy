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

import org.sharegov.timemachine.scheduler.QuartzTaskFacade
import org.sharegov.timemachine.scheduler.Task
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;

class TestTriggerListener implements TriggerListener{

	QuartzTaskFacade taskFacade
	
	public String getName() {
		// TODO Auto-generated method stub
		return "testTriggerListener";
	}

	public void triggerFired(Trigger trigger, JobExecutionContext context) {
		// TODO Auto-generated method stub
		
	}

	public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
		// TODO Auto-generated method stub
		return false;
	}

	public void triggerMisfired(Trigger trigger) {
		// TODO Auto-generated method stub
		
	}

	public void triggerComplete(Trigger trigger, JobExecutionContext context,
			CompletedExecutionInstruction triggerInstructionCode) {
	
		Task task = taskFacade.retrieve(context.jobDetail.name, context.jobDetail.group)
		println "triggered completed"
	}

}
