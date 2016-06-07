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
package org.sharegov.timemachine.scheduler;

import static org.junit.Assert.*;
import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*
import static org.quartz.DateBuilder.*
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

import org.junit.*;
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.JobKey
import org.quartz.Trigger
import org.sharegov.timemachine.AppContext;
import org.sharegov.timemachine.scheduler.QuartzTaskFacade;
import org.sharegov.timemachine.scheduler.Task;
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext




class QuartzTaskFacadeTests {

	QuartzTaskFacade taskFacade

	@Before
	public void setUp() throws Exception {

		new ClassPathXmlApplicationContext("configtest.xml");

		ApplicationContext ctx = AppContext.getApplicationContext();
		Scheduler scheduler = (Scheduler) ctx
				.getBean("timeMachineTestScheduler");

		taskFacade = new QuartzTaskFacade()
		taskFacade.scheduler = scheduler
	}

	@After
	public void tearDown() throws Exception {
		ApplicationContext ctx = AppContext.getApplicationContext();
		Scheduler scheduler = (Scheduler) ctx
				.getBean("timeMachineTestScheduler");

		scheduler.shutdown()
	}

	@Test
	public void testRetrieve_WithSimpleTrigger() throws Exception {
		Date startTime = new Date().parse('yyyy/MM/dd', '2020/01/31')
				
		def task = taskFacade.retrieve('crmsimplejob','crmgroup')
		assert task
		assert task.name == 'crmsimplejob'
		assert task.group == 'crmgroup'
		assert task.scheduleType == 'SIMPLE'
		assert task.scheduleData == [repeatCount:0, repeatInterval:0]
		assert task.startTime == startTime
		assert task.endTime == null
		assert task.state == 'NORMAL'
		assert task.description == "this is the crm simple job"
		assert task.data.url == 'http://localhost:9192/timemachine-0.1/task/'
	}
	
	@Test
	public void testRetrieve_Cron() throws Exception {

		def task = taskFacade.retrieve('crmjob','crmgroup')
		assert task
		assert task.name == 'crmjob'
		assert task.group == 'crmgroup'
		assert task.scheduleType == 'CRON'
		assert task.scheduleData == [:]
		//assert task.startTime == new Date()
		assert task.endTime == null
		assert task.state == 'NORMAL'
		assert task.description == "this is the crmjob"
		assert task.data.url == 'http://localhost:9192/timemachine-0.1/task/'
	}

	@Test
	public void testRetrieve_JobDetailNotExists() throws Exception {

		Task task = taskFacade.retrieve('crmNojob','crmNogroup')
		assert task == null
	}

	@Test
	public void testRetrieve_JobDetailWithNoTrigger() throws Exception {

		def task = taskFacade.retrieve('crmjobtwo','crmgroup')
		assert task
		assert task.name == 'crmjobtwo'
		assert task.group == 'crmgroup'
		assert task.scheduleType == null
		assert task.scheduleData == [:]
		assert task.startTime == null
		assert task.endTime == null
		assert task.state == 'NONE'
		assert task.description == "this is the crmjobtwo"
		assert task.data.url == 'http://localhost:9192/timemachine-0.1/task/'
	}

	@Test
	public void testInsert_WithSimpleTrigger() throws Exception {

		Date startTime = new Date() +  5
		Task task = [name:'newcrmjob',
			group:'newcrmgroup',
			scheduleType:'SIMPLE',
			scheduleData:[:],
			startTime:startTime,
			endTime:null,
			state:'NORMAL',
			description:"this is a new insert newcrmjob",
			data:[url:'http://localhost:9192/timemachine-0.1/task/']]
		
		task = taskFacade.insert(task)

		assert task
		assert task.name ==  'newcrmjob'
		assert task.group == 'newcrmgroup'
		assert task.scheduleType == 'SIMPLE'
		assert task.scheduleData == [repeatCount:0, repeatInterval:0]
		assert task.startTime == startTime
		assert task.endTime == null
		assert task.state == 'NORMAL'
		assert task.description == "this is a new insert newcrmjob"
		assert task.data.url == 'http://localhost:9192/timemachine-0.1/task/'

	}
	
	@Test
	public void testInsert_WithStatePause() throws Exception {

		Date startTime = new Date().parse('yyyy/MM/dd', '2020/01/31')
		Task task = [name:'newcrmjob',
			group:'newcrmgroup',
			scheduleType:'SIMPLE',
			scheduleData:[:],
			startTime:startTime,
			endTime:null,
			state:'PAUSED',
			description:"this is a new insert newcrmjob with pause state",
			data:[url:'http://localhost:9192/timemachine-0.1/task/']]
		
		task = taskFacade.insert(task)

		assert task
		assert task.name ==  'newcrmjob'
		assert task.group == 'newcrmgroup'
		assert task.scheduleType == 'SIMPLE'
		assert task.scheduleData == [repeatCount:0, repeatInterval:0]
		assert task.startTime == startTime
		assert task.endTime == null
		assert task.state == 'PAUSED'
		assert task.description == "this is a new insert newcrmjob with pause state"
		assert task.data.url == 'http://localhost:9192/timemachine-0.1/task/'

	}

	@Test
	public void testInsert_JobAlreadyExistsDoNotInsert() throws Exception {
		
		Date startTime = new Date().parse('yyyy/MM/dd', '2020/01/31')
		Task task = [name:'crmjob',
			group:'crmgroup',
			scheduleType:'SIMPLE',
			scheduleData:[:],
			startTime:startTime,
			endTime:null,
			state:'NORMAL',
			description:"this is a new insert newcrmjob",
			data:[url:'http://localhost:9192/timemachine-0.1/task/']]
		
		assert taskFacade.insert(task) == null
	}

	@Test
	public void testRetrieveAll(){
		assert taskFacade.retrieveAll().size == 4
	}

	@Test
	public void testRetrieveAllByGroup(){
		assert taskFacade.retrieveAllByGroup("crmgroup").size == 3		
	}
	
	@Test
	public void testRetrieveAllByGroup_GroupNotExits(){
		List tasks = taskFacade.retrieveAllByGroup("nogroup")
		assert tasks.size == 0
		assert !tasks
		assert tasks == []
	}
	
	@Test
	public void testDelete(){
		assert taskFacade.retrieveAll().size == 4
		def task = taskFacade.retrieve("crmjob", "crmgroup")
		taskFacade.delete(task)
		assert taskFacade.retrieveAll().size == 3
	}

	@Test
	public void testDelete_ByNameGroup(){
		assert taskFacade.retrieveAll().size == 4
		def task = taskFacade.retrieve("crmjob", "crmgroup")
		taskFacade.delete(task)
		assert taskFacade.retrieveAll().size == 3
	}
	
	@Test
	public void testDelete_JobDetailNotExists(){
		//create new task not persisted in the scheduler
		Date startTime = new Date().parse('yyyy/MM/dd', '2020/01/31')
		Task task = [name:'myjob',
			group:'mygroup',
			scheduleType:'SIMPLE',
			scheduleData:[:],
			startTime:startTime,
			endTime:null,
			state:'NORMAL',
			description:"",
			data:[url:'http://localhost:9192/timemachine-0.1/task/']]

		assert taskFacade.delete(task) == false

	}
	
	@Test
	public void testUpdate_SimpleTrigger(){
		
		Date startTime = new Date().parse('yyyy/MM/dd', '2022/03/20')
		def values = [startTime:startTime, 
			          state:'PAUSED',
					  description:"updated description",
			          data:[url:'http://localhost:9192/timemachine-0.1/task/group/name']]
		def task = taskFacade.update(values, "crmsimplejob", "crmgroup")
		
		assert task.description == "updated description"
		assert task.state == "PAUSED"
		assert task.startTime == startTime
		assert task.data.url == 'http://localhost:9192/timemachine-0.1/task/group/name'
	}
	
	@Test
	public void testUpdate_SimpleTrigger_TaskParam(){
		
		// First check the values of the stored task.
		Date startTime = new Date().parse('yyyy/MM/dd', '2020/01/31')
		def task = taskFacade.retrieve('crmsimplejob','crmgroup')
		assert task
		assert task.name == 'crmsimplejob'
		assert task.group == 'crmgroup'
		assert task.scheduleType == 'SIMPLE'
		assert task.scheduleData == [repeatCount:0, repeatInterval:0]
		assert task.startTime == startTime
		assert task.endTime == null
		assert task.state == 'NORMAL'
		assert task.description == "this is the crm simple job"
		assert task.data.url == 'http://localhost:9192/timemachine-0.1/task/'
		
		// Create a new task with the same group/name
		Date updatedStartTime = new Date().parse('yyyy/MM/dd', '2015/04/15')
		Task updatedTask = [
			name:'crmsimplejob',
			group:'crmgroup',
			scheduleType:'SIMPLE',
			scheduleData:[:],
			startTime:updatedStartTime,
			endTime:null,
			state:'PAUSED',
			description:"this is an updated the crm simple job",
			data:[url:'http://localhost:9192/timemachine-0.1/task/group/name']]

		// update the task 
		Task myTask = taskFacade.update(updatedTask)
		assert myTask.description == "this is an updated the crm simple job"
		assert myTask.state == "PAUSED"
		assert myTask.startTime == updatedStartTime
		assert myTask.data.url == 'http://localhost:9192/timemachine-0.1/task/group/name'

	}
	
	@Test
	public void testUpdate_TaskParam_NotExixts(){
		
		// Create a new non existent task with the same group/name
		Date updatedStartTime = new Date().parse('yyyy/MM/dd', '2015/04/15')
		Task nonExistentTask = [
			name:'notexistjob',
			group:'notexistgroup',
			scheduleType:'SIMPLE',
			scheduleData:[:],
			startTime:updatedStartTime,
			endTime:null,
			state:'PAUSED',
			description:"this is a non existent crm simple job",
			data:[url:'http://localhost:9192/timemachine-0.1/task/group/name']]

		// update the task
		Task myTask = taskFacade.update(nonExistentTask)
		assert myTask == null
	}
	
	@Test
	public void testUpdate_NotExixts(){
		
		Date startTime = new Date().parse('yyyy/MM/dd', '2022/03/20')
		def values = [startTime:startTime,
					  state:'PAUSED',
					  description:"updated description",
					  data:[url:'http://localhost:9192/timemachine-0.1/task/group/name']]
		def task = taskFacade.update(values, "notexistsjob", "notexistsgroup")
		assert task == null
	}
	
	
}
