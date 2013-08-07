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
