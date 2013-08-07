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