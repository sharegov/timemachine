package gov.sharegov.timemachine.scheduler

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean

class HttpPropertiesFactoryBean extends AbstractFactoryBean<Properties>{
	
	private static Logger _log = LoggerFactory.getLogger(HttpPropertiesFactoryBean.class);

	HTTPService httpService
	String url
	
	@Override
	public Class<?> getObjectType() {
		return Properties.class;
	}

	@Override
	protected Properties createInstance() throws Exception {
		Properties props = new Properties();
		
		_log.info "Getting db connection information from ${url}"
		def query = null
		def result = httpService.request(url, query)

		// properties for database connection.
		props.put('database.url', result['OperationsDatabaseConfig']['hasUrl'])
		props.put('database.username', result['OperationsDatabaseConfig']['hasUsername'])
		props.put('database.password', result['OperationsDatabaseConfig']['hasPassword'])
		
		_log.info(props.toString())
		
		return props
	}
	
}
