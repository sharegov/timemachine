package gov.sharegov.timemachine.scheduler;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	 * the <code>Job</code>.
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
		String url = context.mergedJobDataMap.get('url')
		_log.info "About to fire request to url: ${url}"

		//Build the http request
		def http = new HTTPBuilder(url)
		http.request(Method.GET, ContentType.JSON) {
			response.success = {resp, json ->
				_log.info "JSON OUTPUT :  ${json}"
			}

			response.'404' = {resp ->
				_log.warn "weather not found. ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
				null
			}

			response.failure = { resp -> _log.error "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase} " }
		}

		_log.info("End job execution group/name ${context.jobDetail.group}/${context.jobDetail.name}");
	}

}
