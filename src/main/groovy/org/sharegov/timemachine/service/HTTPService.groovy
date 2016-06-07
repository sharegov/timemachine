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
package org.sharegov.timemachine.service

import java.util.List;
import java.util.Map;

import groovyx.net.http.ContentType;

interface HTTPService {

	/**
	 * Makes an http GET request to url with query parameters and specific contentType
	 * @param url - the url to make http request to
	 * @param query - the query parameters to be used in the given http request
	 * @param contentType - The content type like JSON or XML
	 * @return - data returned from the http request or null if error of some sort occurs.
	 */
	Object request(String url, def query, ContentType contentType);
	
	/**
	* Makes a series of http GET requests to all urls with query parameters and specific contentType
	* @param urls - the urls to make http request to
	* @param query - the query parameters to be used in the given http request
	* @param contentType - The content type like JSON or XML
	* @return - data returned from the http requests as a map. The key will be the url and the value
	* the result of that http call to the key url. The value will be null if an error occurs for the
	* specific http request.
	*/
	Map request(List urls, def query, ContentType contentType);
		
	/**
	* Makes an http POST request to url with query parameters and specific contentType
	* @param url - the url to make http request to
	* @param query - the query parameters to be used in the given http request
	* @param contentType - The content type like JSON or XML
	* @return - data returned from the http request or null if error of some sort occurs.
	*/
	Object requestPost(String url, def query, ContentType contentType);
}
