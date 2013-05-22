package gov.sharegov.timemachine.scheduler

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*
import static org.quartz.TriggerKey.*
import static org.quartz.DateBuilder.*
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.impl.matchers.GroupMatcher.*

import net.redhogs.cronparser.CronExpressionDescriptor

import java.util.List;

import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.ScheduleBuilder
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.JobKey
import org.quartz.impl.triggers.*

import org.quartz.ObjectAlreadyExistsException

/**
 * The QuartzTaskFacade is the 'interface' to query Tasks where the scheduler
 * @author fiallega
 *
 */
class QuartzTaskFacade {
	Scheduler scheduler

	/**
	 * Build a task from schedule information.
	 * Return null if no task exists with that (name,group)
	 * @param name
	 * @param group
	 * @return Task - returns the task with name -name- and group -group-. It returns null
	 * if no task is found with that (name,group) combination.
	 */
	Task retrieve(String name, String group){

		Task task = new Task()
		JobDetail job = scheduler.getJobDetail(JobKey.jobKey(name, group))

		if (job) {
			task.name = job.key.name
			task.group = job.key.group
			task.data = job.jobDataMap
			task.description = job.description

			Trigger trigger = scheduler.getTriggersOfJob(JobKey.jobKey(name, group))[0]
			task.startTime = trigger?.startTime
			task.endTime = trigger?.endTime
			task.scheduleData = [:]
			if (trigger?.class == SimpleTriggerImpl.class){
				task.scheduleType = 'SIMPLE'
				task.scheduleData = [repeatCount:trigger?.repeatCount,
							repeatInterval:trigger?.repeatInterval]
			}
			else if (trigger?.class == CronTriggerImpl.class){
				task.scheduleType = 'CRON'
				
				task.scheduleData = [cronExpression:trigger?.cronExpression,
					cronExpression:CronExpressionDescriptor.getDescription(trigger?.cronExpression)]
			}

			task.state = scheduler.getTriggerState(trigger?.getKey())

			return task
		} else
			return null
	}

	List retrieveAll() {

		List tasks = new ArrayList();

		// enumerate each job group
		scheduler.jobGroupNames.each {group ->
			// enumerate each job in group
			scheduler.getJobKeys(groupEquals(group)).each {jobKey ->
				//println jobKey.name
				tasks.add(retrieve(jobKey.name, jobKey.group))
			}
		}

		return tasks

	}

	/**
	 * Retrieves all tasks that belong to group 'group'
	 * @param group
	 * @return List - the tasks belonging to group 'group'. Returns empty list if no task
	 * on the group which is the same as saying that the group does not exists.
	 */
	List retrieveAllByGroup(String group) {
		List tasks = new ArrayList();

		// enumerate each job in group
		scheduler.getJobKeys(groupEquals(group)).each {jobKey ->
			//println jobKey.name
			tasks.add(retrieve(jobKey.name, jobKey.group))
		}

		return tasks
	}

	Task insert(Task task){

		// build the jobDetail
		JobDetail job = newJob(CrmJob.class)
				.withIdentity(task.name, task.group)
				.withDescription(task.description)
				.usingJobData(new JobDataMap(task.data))
				.build();

		// build the trigger
		
		// build a cron or a simple schedule
		ScheduleBuilder sb = null
		if(task.scheduleType == "SIMPLE")
			sb = simpleSchedule()
		else if (task.scheduleType == "CRON")
			sb = cronSchedule(task.scheduleData.cronExpression)
				
		Trigger trigger = newTrigger()
				.withIdentity(triggerKey(task.name, task.group))
				.withSchedule(sb)
				.startAt(task.startTime)
				.build();

		try{
			//shedule a task
			scheduler.scheduleJob(job, trigger);

			//Set the state to PAUSE if asked.
			if(task.state == 'PAUSED')
				scheduler.pauseJob(job.key)

			//retrieve task from schedule so that all fields are populated (ie:state)
			return retrieve(task.name, task.group)
		} catch (ObjectAlreadyExistsException oaee) {
			task = null
		}

		return task
	}


	/**
	 * Update either the startTime,state,description and/or data
	 * [startTime:Date(), state:'NORMAL',description:"",
	 data:[url:'http://localhost:9192/timemachine-0.1/task/']]	
	 * @param values
	 * @param name
	 * @param group
	 * @return null if no task existed
	 */
	Task update(values, String name, String group){
		Task task = retrieve(name, group)

		if(!task)
			return null

		values.each {key,value ->
			task."$key" = value
		}

		// update job data
		scheduler.addJob(getJob(task), true);

		// update trigger data
		Trigger trigger = scheduler.getTriggersOfJob(JobKey.jobKey(name, group))[0]
		scheduler.rescheduleJob(trigger.key, getTrigger(task))

		// update state from PAUSE to NORMAL and from NORMAL to PAUSE
		if(task.state == 'PAUSED')
			scheduler.pauseJob(getJob(task).key)
		else if(task.state == 'NORMAL')
			scheduler.resumeJob(getJob(task).key)

		retrieve(name, group)
	}


	Task update (Task task){

		if(!retrieve(task.name, task.group))
			return null

		// update job data
		scheduler.addJob(getJob(task), true);

		// update trigger data
		Trigger trigger = scheduler.getTriggersOfJob(JobKey.jobKey(task.name, task.group))[0]
		scheduler.rescheduleJob(trigger.key, getTrigger(task))

		// update state from PAUSE to NORMAL and from NORMAL to PAUSE
		if(task.state == 'PAUSED')
			scheduler.pauseJob(getJob(task).key)
		else if(task.state == 'NORMAL')
			scheduler.resumeJob(getJob(task).key)

		retrieve(task.name, task.group)
	}

	Boolean delete(Task task){
		delete(task.name, task.group)
	}

	Boolean delete(String name, String group){
		scheduler.deleteJob JobKey.jobKey(name, group)
	}

	private JobDetail getJob(Task task){

		JobDetail job = newJob(CrmJob.class)
				.withIdentity(task.name, task.group)
				.withDescription(task.description)
				.usingJobData(new JobDataMap(task.data))
				.build();
	}

	private Trigger getTrigger(Task task){

		def scheduleType
		if(task.scheduleType == "SIMPLE")
			scheduleType = simpleSchedule()
		else if (task.scheduleType == "CRON"){
			// TODO:Creation of cron expression needs a lot of work
			def cronExpression = "${task.scheduleData.seconds} ${task.scheduleData.minutes} ${task.scheduleData.hours} ${task.scheduleData.day_of_month} ${task.scheduleData.month} ${task.scheduleData.day_of_week} ${task.scheduleData.year}"
			scheduleType = cronSchedule(cronExpression)
		}

		Trigger trigger = newTrigger()
				.withIdentity(triggerKey(task.name, task.group))
				.withSchedule(scheduleType)
				.startAt(task.startTime)
				.build();
	}
}
