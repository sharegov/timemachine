package gov.sharegov.timemachine.scheduler;

import static org.junit.Assert.*;
import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*
import static org.quartz.DateBuilder.*
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

import org.junit.*;
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.triggers.*
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

class TaskConverterTests {

	@Before
	public void setUp() throws Exception {

		new ClassPathXmlApplicationContext("configtest.xml");

		ApplicationContext ctx = AppContext.getApplicationContext();
		Scheduler scheduler = (Scheduler) ctx
				.getBean("timeMachineTestScheduler");

	}

	@After
	public void tearDown() throws Exception {
		ApplicationContext ctx = AppContext.getApplicationContext();
		Scheduler scheduler = (Scheduler) ctx
				.getBean("timeMachineTestScheduler");

		scheduler.shutdown()
	}

	@Test
	public void testToJson_SimpleFireOnce(){
		
		// Create a task Object
		Date startTime = new Date().parse('yyyy/MM/dd HH:mm:ss', '2020/11/01 14:0:0')
		Task task = [name:'newcrmjob',
			group:'newcrmgroup',
			scheduleType:'SIMPLE',
			scheduleData:[:],
			startTime:startTime,
			endTime:null,
			state:'NORMAL',
			description:"this is a new insert newcrmjob",
			data:[url:'http://localhost:9192/timemachine-0.1/task/']]
		
		assert TaskConverter.toJson(task) == '{"name":"newcrmjob","group":"newcrmgroup","scheduleType":"SIMPLE","scheduleData":{},"startTime":"2020/11/01 14:00:00","endTime":null,"state":"NORMAL","description":"this is a new insert newcrmjob","data":{"url":"http://localhost:9192/timemachine-0.1/task/"}}'
		
	}
	
	@Test
	public void testToTask_SimpleFireOnce(){
	    String json = '''{"name":"myJob",
						  "group":"myGroup",
						  "scheduleType":"SIMPLE",
						  "scheduleData":{},
						  "startTime":{"second":"0",
										"minute":"0",
						        		"hour":"14",
						  				"day_of_month":"1",
										"month":"11",	 
										"year":"2020"},
						  "endTime":{},
						  "state":"PAUSED",
						  "description":"this is the description",
						  "data":{"url":"http://localhost:9192/timemachine-0.1/task/"}}''' 

		Task task = TaskConverter.toTask(json)
		Date startTime = new Date().parse('yyyy/MM/dd HH:mm:ss', '2020/11/01 14:0:0')
		assert task
		assert task.name ==  'myJob'
		assert task.group == 'myGroup'
		assert task.scheduleType == 'SIMPLE'
		assert task.scheduleData == [:]
		assert task.startTime == startTime
		assert task.endTime == null
		assert task.state == 'PAUSED'
		assert task.description == "this is the description"
		assert task.data.url == "http://localhost:9192/timemachine-0.1/task/"
	}
	
	@Test
	public void testToJsonList(){
		
		Date startTime = new Date().parse('yyyy/MM/dd HH:mm:ss', '2020/11/01 14:0:0')
		Task task = [name:'newcrmjob',
			group:'newcrmgroup',
			scheduleType:'SIMPLE',
			scheduleData:[:],
			startTime:startTime,
			endTime:null,
			state:'NORMAL',
			description:"this is a new insert newcrmjob",
			data:[url:'http://localhost:9192/timemachine-0.1/task/']]
		
		assert TaskConverter.toJson([task]) == '[{"name":"newcrmjob","group":"newcrmgroup","scheduleType":"SIMPLE","scheduleData":{},"startTime":"2020/11/01 14:00:00","endTime":null,"state":"NORMAL","description":"this is a new insert newcrmjob","data":{"url":"http://localhost:9192/timemachine-0.1/task/"}}]'

	}
	
	
}
