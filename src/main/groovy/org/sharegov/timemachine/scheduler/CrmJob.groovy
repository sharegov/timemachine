package org.sharegov.timemachine.scheduler;

import java.util.Date;

import org.sharegov.timemachine.AppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution
import org.quartz.DisallowConcurrentExecution
import org.quartz.Trigger

import org.sharegov.timemachine.service.AsyncHTTPService;
import org.sharegov.timemachine.service.HTTPService;
import org.sharegov.timemachine.service.RetrievalOfDataException
import groovy.time.*

import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

/**
 * <p>
 * This is just a simple job that says "Hello" to the world.
 * </p>
 * 
 * @author fiallega
 */

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CrmJob implements Job {

	private static Logger _log = LoggerFactory.getLogger(CrmJob.class);

	/**
	 * <p>
	 * Empty constructor for job initilization
	 * </p>
	 * <p>
	 * Quartz requires a public empty constructor so that the
	 * scheduler can instantiate the class whenever it needs.
	 * </p>
	 */
	public CrmJob() {
	}

	/**
	 * <p>
	 * Called by the <code>{@link org.quartz.Scheduler}</code> when a
	 * <code>{@link org.quartz.Trigger}</code> fires that is associated with
	 * the <code>Job</code>. It is going to execute and http request to the
	 * url contained in the jobDataMap with key = url.
	 * </p>
	 * 
	 * @throws JobExecutionException
	 *             if there is an exception while executing the job.
	 */
	public void execute(JobExecutionContext context)
	throws JobExecutionException {

		_log.info("Start job execution group/name ${context.jobDetail.group}/${context.jobDetail.name}");
		context.getJobDetail().getJobDataMap().each {key, value ->
			_log.info "-> ${key}, ${value}"
		}

		// Obtain the url.
		String url = context.mergedJobDataMap.get('url')
		// hack fix for temporary corrupted data in db. Please remove at some point.
		url = url.replaceFirst("https://311hub.miamidade.gov//", "https://311hub.miamidade.gov/")
		_log.info "About to fire request to url: ${url}"

		// Obtain the http method. No method, then by default is a GET
		String httpMethod = context.mergedJobDataMap.get('method')?:"GET"
		
		// obtain task
		ApplicationContext ctx = AppContext.getApplicationContext();
		QuartzTaskFacade taskFacade = (QuartzTaskFacade) ctx
				.getBean("taskFacade");
		Task task = taskFacade.retrieve(context.jobDetail.key.name, context.jobDetail.key.group)
		
		try{
			// Fire the request to the url
			ctx = AppContext.getApplicationContext();
			HTTPService httpService = (AsyncHTTPService) ctx
					.getBean("ASYNC_HTTP_SERVICE");

			//contentIsTask: false should be the default		
			//Map query = [schedule:TaskConverter.covertToMap(task)]
			Map query = task.restCall?.content
			Map result = [:]

			switch(httpMethod){
				case "GET":
					result = httpService.request(url, null)
					_log.info("End job execution group/name ${context.jobDetail.group}/${context.jobDetail.name}. http method ${httpMethod} ");
					break
				
				case "POST":
					result = httpService.requestPost(url, query)
					_log.info("End job execution group/name ${context.jobDetail.group}/${context.jobDetail.name}");
					break
				
				default:
					_log.error("http method ${httpMethod} not supported.  group/name ${context.jobDetail.group}/${context.jobDetail.name} - ${rode.message}" );
			}
			
			
			if(result?.ok == false) {
				context.data = result
				context.data.retry = true
				_log.error("End with ok=false. job execution group/name ${context.jobDetail.group}/${context.jobDetail.name}" );
				throw new JobExecutionException("error occured")
			}
			
		    context.jobDetail.jobDataMap["previousFireTime"] = new Date()
		} catch(RetrievalOfDataException rode){
			context.data = [retry:true]
			_log.error("End with ERROR job execution group/name ${context.jobDetail.group}/${context.jobDetail.name} - ${rode.message}" );			
			throw new JobExecutionException("error occured")
		} 

	}

}
