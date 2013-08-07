package org.sharegov.timemachine.scheduler

import static java.util.Calendar.*
import static org.quartz.CronScheduleBuilder.cronSchedule
import static org.quartz.DateBuilder.*
import static org.quartz.JobBuilder.*
import static org.quartz.SimpleScheduleBuilder.simpleSchedule
import static org.quartz.TriggerBuilder.*
import static org.quartz.TriggerKey.*
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.Trigger
import org.quartz.impl.triggers.*


class TaskConverter {

	static String toJson(task){
		def json = new JsonBuilder()
		json(TaskConverter.covertToMap(task))
		json.toString()
	}


	static Task toTask(String json){

		def slurper = new JsonSlurper()
		def result = slurper.parseText(json)

		// Convert startTime to Date
		String formattedStartTime = "${result.startTime.year}/${result.startTime.month}/${result.startTime.day_of_month} ${result.startTime.hour}:${result.startTime.minute}:${result.startTime.second}"
		Date startTime = new Date().parse('yyyy/MM/dd HH:mm:ss', formattedStartTime)

		//TODO: change endtime values to a Date

		Task task = [name:result.name,
			group:result.group,
			scheduleType:result.scheduleType,
			scheduleData:result.scheduleData,
			startTime:startTime,
			endTime:null,
			state:result.state,
			description:result.description,
			data:result.data,
			restCall:result.restCall]
	}

	static String toJson(List tasks){
		println "number of tasks is " + tasks.size()
		def listOfMaps = tasks.collect{Task task->
			TaskConverter.covertToMap(task)
		}

		def json = new JsonBuilder()
		json(listOfMaps)
		json.toString()
	}

	static Object covertToMap(Task task){

		Map formattedScheduleData = task.scheduleData?.inject([:]){acc, key, value ->
			if(value instanceof Date)
				acc << [(key):value.format('yyyy/MM/dd HH:mm:ss')]
			else acc << [(key):value]
			return acc
		}

		Map data = task.restCall?:task.data
		
		[name:task.name,
			group:task.group,
			scheduleType:task.scheduleType,
			scheduleData:formattedScheduleData,
			startTime:task.startTime?.format('yyyy/MM/dd HH:mm:ss'),
			endTime:null,
			state:task.state,
			description:task.description,
			//data:task.data,
			restCall:data]
	}

	static JobDetail buildJobFromTask(Task task) {

		// save into data either data or restCall. data will be deprecated.
		//Map data = null
		Map data = task.restCall?:task.data 
		
		JobDetail job = newJob(CrmJob.class)
				.withIdentity(task.name, task.group)
				.withDescription(task.description)
				.usingJobData(new JobDataMap(data))
				.build();
	}


	static Trigger buildTriggerFromTask(Task task){

		Trigger trigger = newTrigger()
				.withIdentity(triggerKey(task.name, task.group))
				.forJob(task.name, task.group)
				.startAt(task.startTime)
				.endAt(task.endTime?:null)
				.build()

		if(task.scheduleType == "SIMPLE") {
			trigger = trigger.triggerBuilder
					.withSchedule(simpleSchedule()
					.withIntervalInMinutes(task.scheduleData?.repeatInterval ?: 0)
					.withRepeatCount(task.scheduleData?.repeatCount ?: 0))
					.build()
		}
		else if (task.scheduleType == "CRON"){
			trigger = trigger.triggerBuilder
					.withSchedule(cronSchedule(task.scheduleData?.cronExpression))
					.build()
		}
		
		return trigger
	}
}
