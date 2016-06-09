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
package org.sharegov.timemachine

import groovy.json.JsonBuilder
import java.util.Map;

import org.hibernate.SessionFactory
import org.sharegov.timemachine.scheduler.Task
import org.sharegov.timemachine.scheduler.TaskConverter
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.annotation.Propagation
import org.springframework.beans.factory.annotation.Autowired

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



class HistoryTaskDAO implements IHistoryTaskDAO{


	SessionFactory sessionFactory
	
	private static Logger _log = LoggerFactory.getLogger(HistoryTaskDAO.class);

	public void doNothing(){
		println "nothing"
	}	

	@Transactional
	public void saveHistory(Task task, Date fireTime, Boolean success, Map result) {

		_log.info "Right before save history of group/name ${task.group}/${task.name}"
		_log.info "Task is ${task.toString()}"
		_log.info "Result is ${result.toString()}"
		
		//result =["placeholder":"placeholder"]
		//result = [server:84, "stats-sum":[["ALL/ALL/ALL":["lastFailureMessage":null,"firstEntryTime":"2016-06-07T11:49:39.518-0400","lastFailureException":null,"successCount":1,"lastSuccessId":"16-10029404","lastFailureTime":null,"lastSuccessTime":"2016-06-07T11:49:39.518-0400","lastFailureId":null,"failureCount":0]]], ok:true]
		Integer dbSuccess = success?1:0
		String dbTask = TaskConverter.toJson(task)
		def json = new JsonBuilder(result)
		String dbAnswer = json?.toString()
		
		_log.info "About to save history of group/name ${task.group}/${task.name}"

		HistoryTask historyTask = [name:task.name, group:task.group, 
								   success:dbSuccess, message:result?.message,
								   task:dbTask, answer:dbAnswer, fireTime:fireTime]
		sessionFactory.currentSession.saveOrUpdate(historyTask);

	}
	
}
