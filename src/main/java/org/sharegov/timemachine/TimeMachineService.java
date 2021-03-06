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
package org.sharegov.timemachine;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;




import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.restlet.Request;
import org.restlet.data.Form;
import org.sharegov.timemachine.HistoryTaskDAO;
import org.sharegov.timemachine.scheduler.QuartzTaskFacade;
import org.sharegov.timemachine.scheduler.Task;
import org.sharegov.timemachine.scheduler.TaskConverter;
import org.sharegov.timemachine.scheduler.samples.HelloJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import mjson.Json;
import static mjson.Json.*;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.DateBuilder.*;

/**
 * The TimeMachineService handles the requests for "The Time Machine" returning json objects 
 * when necessary.
 * So far It handles:
 * 
 * 1. CRUD of tasks.
 * @author fiallega
 *
 */

@Path("timemachine-0.1")
@Produces("application/json")
public class TimeMachineService {

	
	
	/**
	 * Get a task with the identifiers group/name
	 * @param group: the group name of the task
	 * @param name: the name of the task, which needs to be unique within the group. 
	 * @return Json -
	 * value returned when task exists
     *
	 *	{ "name":"crmsimplejob",
	 *	 "group":"crmgroup"
	 *	 "startTime":"2020/01/31 00:00:00",
	 *	 "endTime":null,
	 *	 "scheduleType":"SIMPLE",
	 *	 "scheduleData":{"repeatInterval":0,"repeatCount":0},
	 *	 "description":"this is the crm simple job",
	 *	 "data":{"url":"http://localhost:9192/timemachine-0.1/task/"},
	 *	 "state":"NORMAL"}
	 *	
	 *	value returned when task does not exist
	 *	{"error":"task does not exist","name":"crmsimplejob2","ko":true,"group":"crmgroup"}
	 */
	@GET
	@Path("/task/{group}/{name}")
	public Json getTask(@PathParam("group") String group, @PathParam("name") String name) {
		
		getLog().info("Starting getTask() for name/group " + name + "/" + group);
		ApplicationContext ctx = AppContext.getApplicationContext();
		QuartzTaskFacade taskFacade = (QuartzTaskFacade) ctx.getBean("taskFacade");		
		
		Task task = taskFacade.retrieve(name,group);
		if (task == null) {
			getLog().debug("No task can be retrieved for name/group " + name + "/" + group);
			return object("ok", false, "message", "task not found", "name", name, "group", group);
		}
		else {
			getLog().debug("About to return task " + TaskConverter.toJson(task));
			return read(TaskConverter.toJson(task));
		}
	}

	/**
	 * Gets all available tasks.
	 * @return Json - array of json objects. It returns an empty json array if no tasks are found.
	 * [
	 *  {"startTime":"2020/01/31 00:00:00","scheduleType":"SIMPLE","scheduleData":{"repeatInterval":0,"repeatCount":0},"description":"this is the crm simple job","name":"crmsimplejob","data":{"url":"http://localhost:9192/timemachine-0.1/task/"},"state":"NORMAL","endTime":null,"group":"crmgroup"},
     *  {"startTime":"2012/01/17 20:53:23","scheduleType":"CRON","scheduleData":{},"description":"this is the crm cron job","name":"crmjob","data":{"url":"http://localhost:9192/timemachine-0.1/task/"},"state":"NORMAL","endTime":null,"group":"crmgroup"}
     *  ]
	 */
	@GET
	@Path("/task")
	public Json getAllTasks() {

		getLog().info("Starting getAllTasks()");
		ApplicationContext ctx = AppContext.getApplicationContext();
		QuartzTaskFacade taskFacade = (QuartzTaskFacade) ctx
				.getBean("taskFacade");
		
		List tasks = taskFacade.retrieveAll();
		
		getLog().debug(TaskConverter.toJson(tasks));
		return read(TaskConverter.toJson(tasks));
	}
	
	
	/**
	 * 
	 * @param group
	 * @return
	 */
	@GET
	@Path("/task/{group}")
	public Json getAllTasksByGroup(@PathParam("group") String group) {

		getLog().info("Starting getAllTasksByGroup()");
		ApplicationContext ctx = AppContext.getApplicationContext();
		QuartzTaskFacade taskFacade = (QuartzTaskFacade) ctx
				.getBean("taskFacade");
		
		List tasks = taskFacade.retrieveAllByGroup(group);
		
		getLog().debug(TaskConverter.toJson(tasks));
		return read(TaskConverter.toJson(tasks));
	}
	

	/**
	 * Insert a new task
	 * @param json - The expected json object is of the form
	 * {"name":"myJob",
	 *	  "group":"myGroup",
	 *	  "scheduleType":"SIMPLE",
	 *	  "scheduleData":{},
	 *	  "startTime":{"second":"0",
	 *			"minute":"0",
	 *	        	"hour":"2",
	 *	  		"day_of_month":"1",
	 *			"month":"11",	 
	 *			"year":"2020"},
	 *	  "endTime":"",
	 *	  "state":"PAUSED",
	 *	  "description":"this is the description",
	 *	  "data":{"url":"http://localhost:9192/timemachine-0.1/task/"}}
	 * @return Json - returns back the task inserted in json format.
	 */
	@POST
	@Path("/task")
	@Consumes("application/json")
	public Json insertTask(Json json) {

		getLog().info("Starting insertTask() " + json.toString());
		ApplicationContext ctx = AppContext.getApplicationContext();
		QuartzTaskFacade taskFacade = (QuartzTaskFacade) ctx
				.getBean("taskFacade");
		
		Task task = taskFacade.insert(TaskConverter.toTask(json.toString()));
		
		if (task == null) {
			getLog().info("Finish insertTask() . No Task to be inserted.");
			return read(TaskConverter.toJson(new Task()));
		}
		else {
			getLog().info("Finish insertTask() " +  TaskConverter.toJson(task));
			return read(TaskConverter.toJson(task));			
		}
						
	}

	/**
	 * Updates a task with name/group identifiers contained in the json object
	 * @param json - the json object to be updated with the new values contained in the object.
	 * {"name":"crmsimplejob",
	 *	  "group":"crmgroup",
	 *	  "scheduleType":"SIMPLE",
	 *	  "scheduleData":{},
	 *	  "startTime":{"second":"10",
	 *	               "minute":"10",
	 *	               "hour":"2",
	 *	               "day_of_month":"1",
	 *	               "month":"11",
	 *	               "year":"2030"},
	 *	  "endTime":"",
	 *	  "state":"PAUSED",
	 *	  "description":"this is the description updated",
	 *	  "data":{"url":"http://localhost:9192/timemachine-0.1/task/"}}
	 * @return Json - the updated task in json format.
	 * if no task exists {"error":"task does not exist","ko":true}
	 */
	@PUT
	@Path("/task")
	@Consumes("application/json")
	public Json updateTask(Json json) {
		getLog().info("Starting updateTask() " + json.toString());
		
		ApplicationContext ctx = AppContext.getApplicationContext();
		QuartzTaskFacade taskFacade = (QuartzTaskFacade) ctx
				.getBean("taskFacade");

		Task task = taskFacade.update(TaskConverter.toTask(json.toString()));
		if (task == null){
			getLog().debug("No task can be updated for " + json.toString());
			return object("ko", true, "error", "task does not exist");
		}
		else{
			getLog().debug("About to return updated task " + TaskConverter.toJson(task));
			return read(TaskConverter.toJson(task));
		}
	}
	
	@PUT
	@Path("/task/{group}/{name}")
	@Consumes("application/json")
	public Json updateTask(Json json, @PathParam("group") String group, @PathParam("name") String name) {

		ApplicationContext ctx = AppContext.getApplicationContext();
		QuartzTaskFacade taskFacade = (QuartzTaskFacade) ctx
				.getBean("taskFacade");
		Map map = json.asMap();
		Task task = taskFacade.update(map, name, group);
		if (task == null)
			return object("ko", true, "error", "task does not exist", "name", name, "group", group);
		else		
			return read(TaskConverter.toJson(task));
		
	}
	
	/**
	 * deletes task with identifier group/name
	 * @param group: the group name of the task
	 * @param name: the name of the task, which needs to be unique within the group. 
	 * @return Json - 
	 * if deleted returns {"ok", true, "name", name, "group", group}
	 * if fails to delete because task does not exists  {"ko", true, "error", "task does not exist", "name", name, "group", group}
	 */
	@DELETE
	@Path("/task/{group}/{name}")
	public Json deleteTask(@PathParam("group") String group, @PathParam("name") String name) {

		getLog().info("Starting deleteTask() for name/group " + name + "/" + group);
				
		ApplicationContext ctx = AppContext.getApplicationContext();
		QuartzTaskFacade taskFacade = (QuartzTaskFacade) ctx
				.getBean("taskFacade");
		
		Boolean answer = taskFacade.delete(name,group);
		if (answer){
			getLog().debug("Deleted task with  group/name " + group + "/" + name);
			return object("ok", true, "name", name, "group", group);
		}
		else{
			getLog().debug("No task to delete with  group/name " + group + "/" + name);
			return object("ko", true, "error", "task does not exist", "name", name, "group", group);
		}

	}
	
	
	@GET 
	@Path("/time")
	public Json time() {
		Date date = new Date();
		//Calendar calendar = new GregorianCalendar();
		//CalendarFo
		
		String formattedDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date);
		
		return object("time", date.getTime(), "formattedTime", formattedDate);
	}
	
	@GET
	@Path("/inserthistory")
	public Json testInsertHistory(){
		
		ApplicationContext ctx = AppContext.getApplicationContext();
		IHistoryTaskDAO taskFacade = (IHistoryTaskDAO) ctx
				.getBean("HISTORY_TASK_DAO");
		
		taskFacade.doNothing();
		
		Task task = new Task();
		task.setName("Joe");
		
		//taskFacade.saveHistory(task);
		return object("time", "Please Check");
	}
	
	@GET
	@Path("/testquartz")
	public String testQuartz() {

		Request request = Request.getCurrent();
		Form queryParams = Request.getCurrent().getResourceRef()
				.getQueryAsForm();
		
		ApplicationContext ctx = AppContext.getApplicationContext();
		Scheduler scheduler = (Scheduler) ctx
				.getBean("timeMachineScheduler");
		try {
			System.out.println("THE SCHEDULER" + scheduler.getSchedulerName());
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // computer a time that is on the next round minute
        Date runTime = evenMinuteDate(new Date());

		
		// define the job and tie it to our HelloJob class
		JobDetail job = newJob(HelloJob.class).withIdentity("job1", "group1")
				.build();

		// Trigger the job to run on the next round minute
		Trigger trigger = newTrigger().withIdentity("trigger1", "group1")
				.startAt(runTime).build();

		// Tell quartz to schedule the job using our trigger
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getLog().info(job.getKey() + " will run at: " + runTime);

		return "ok";
	}
	
	
	@POST
	@Path("/testtask")
	@Consumes("application/json")
	public Json testTask(Json json) {

		return null;
						
	}
	
	@POST
	@Path("/testfalse")
	@Consumes("application/json")
	public Json testFalse(Json json) {

		return object("ok", false, "retryAfter", 10);
		
						
	}
	
	public static Logger getLog() 
	{
		return LoggerFactory.getLogger(TimeMachineService.class);
        
    }
	
}
