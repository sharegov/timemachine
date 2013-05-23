package gov.sharegov.timemachine.scheduler

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*
import static org.quartz.TriggerKey.*
import static org.quartz.DateBuilder.*
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static java.util.Calendar.*

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.util.List;
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
					data:result.data]
	}

	static String toJson(List tasks){

		def listOfMaps = tasks.collect{Task task->
			TaskConverter.covertToMap(task)
		}

		def json = new JsonBuilder()
		json(listOfMaps)
		json.toString()
	}

	static Object covertToMap(Task task){
		
		Map formattedScheduleData = task.scheduleData.inject([:]){acc, key, value -> 
			if(value instanceof Date)
			  acc << [(key):value.format('yyyy/MM/dd HH:mm:ss')]
			else acc << [(key):value]  
			return acc
		}
		
		[name:task.name,
			group:task.group,
			scheduleType:task.scheduleType,
			scheduleData:formattedScheduleData,
			startTime:task.startTime?.format('yyyy/MM/dd HH:mm:ss'),
			endTime:null,
			state:task.state,
			description:task.description,
			data:task.data]

	}
}
