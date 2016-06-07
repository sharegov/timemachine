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
package org.sharegov.timemachine.scheduler

import org.sharegov.timemachine.service.HTTPService;

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
