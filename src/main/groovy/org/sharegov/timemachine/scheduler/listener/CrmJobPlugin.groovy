package org.sharegov.timemachine.scheduler.listener


import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.SchedulerPlugin;

class CrmJobPlugin extends CrmJobListener implements SchedulerPlugin{

	public void initialize(String name, Scheduler scheduler)
			throws SchedulerException {

		scheduler.listenerManager.addJobListener(this)
	}

	public void start() {
		// TODO Auto-generated method stub
	}

	public void shutdown() {
		// TODO Auto-generated method stub
	}

}
