package gov.sharegov.timemachine.scheduler;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

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
		
		//Obtain the url.
		String url = context.mergedJobDataMap.get('url')
		_log.info "About to fire request to url: ${url}"
		
		// Fire the request to the url
		ApplicationContext ctx = AppContext.getApplicationContext();
		HTTPService httpService = (AsyncHTTPService) ctx
				.getBean("ASYNC_HTTP_SERVICE");
	    httpService.request(url, null);  
		
		_log.info("End job execution group/name ${context.jobDetail.group}/${context.jobDetail.name}");
	}

}
