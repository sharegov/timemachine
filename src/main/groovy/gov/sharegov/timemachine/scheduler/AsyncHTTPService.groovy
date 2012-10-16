package gov.sharegov.timemachine.scheduler




import groovyx.net.http.ContentType
import groovyx.net.http.URIBuilder
import groovyx.net.http.Method


import net.sf.json.JSONObject
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*


class AsyncHTTPService implements HTTPService {

	private static Logger _log = LoggerFactory.getLogger(AsyncHTTPService.class);

	def http

	Object request(String url, def query, ContentType contentType=JSON){

		def startDate = new Date().time


		def	result = http.request(url, GET, contentType) {
			
			// only add query params when they are passed in the query map, otherwise the params on the
			// query string will be lost
			if(query) 
			  uri.query = query
			  
			response.success = {resp, data ->
				_log.info "request() - request for url ${url} : ${data}"
				return data
			}

			response.'404' = {resp ->
				String message = "${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
				_log.error "request() - 404 for url ${url} : ${message}"
				//def result = [error:true, message:message] as JSONObject
				return null
			}

			response.failure = { resp ->
				String message = "${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
				_log.error "request() - Unexpected error: request for url ${url} : ${message}"
				//def result = [error:true, message:message] as JSONObject
				return null
			}
		}


		// Wait for call to come back
		while ( true ) {
			if (result.done)
				break
		}

		def endDate = new Date().time

		_log.info "time for url ${url} is ${endDate-startDate}"

		// return result. when calling get an exception may be returned, if exception happened in the
		// thread, like connection refused: ConnectException
		try{
			return  result.get()

		}catch (Exception e) {
			_log.error "request() - Exception at the thread level. url: ${url} - ${e.getMessage()}"
			return null
		}


	}


	Object requestPost(String url, def query, ContentType contentType=JSON){

		def startDate = new Date().time

		def result = http.request(url, POST, contentType) {

			send URLENC, query

			response.success = {resp, data ->
				_log.info "requestPost() - request for url ${url} : ${data}"
				return data
			}

			response.'404' = {resp ->
				String message = "${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
				_log.error "requestPost() - 404 for url ${url} : ${message}"
				//def result = [error:true, message:message] as JSONObject
				return null
			}

			response.failure = { resp ->
				String message = "${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
				_log.error "requestPost() - Unexpected error: request for url ${url} : ${message}"
				//def result = [error:true, message:message] as JSONObject
				return null
			}
		}

		// Wait for call to come back
		while ( true ) {
			if (result.done)
				break
		}

		def endDate = new Date().time

		_log.info "requestPost() - time for url ${url} is ${endDate-startDate}"

		// return result. when calling get an exception may be returned, if exception happened in the
		// thread, like connection refused: ConnectException
		try{
			return  result.get()

		}catch (Exception e) {
			_log.error "requestPost() - Exception at the thread level. url: ${url} - ${e.getMessage()}"
			return null
		}

	}

	Map request(List urls, def query, ContentType contentType=JSON){

		def done = [:]

		urls.each {url ->
			done[url] =
					http.request(url, GET, contentType) {
						uri.query = query
						def startDate = new Date().time
						response.success = {resp, data ->
							_log.info "requestMultiple() - request for url ${url} : ${data}"
							def endDate = new Date().time
							_log.info "time for url ${url} is ${endDate-startDate}"
							return data }

						response.'404' = {resp ->
							String message = "${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
							_log.error "requestMultiple() - 404 for url ${url} : ${message}"
							//def result = [error:true, message:message] as JSONObject
							return null
						}

						response.failure = { resp ->
							String message = "${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
							_log.error "requestMultiple() - Unexpected error: request for url ${url} : ${message}"
							//def result = [error:true, message:message] as JSONObject
							return null
						}
					}

		}


		while ( true ) {

			if ( done.every{ it.value.done} )
				break
		}

		// convert the resuts to a map. return result. when calling get an exception may be returned, if exception happened in the
		// thread, like connection refused: ConnectException
		return done.collectEntries {key, value ->
			try{
				[key, value.get()]
			}catch(Exception e){
				_log.error "requestMultiple() - Exception at the thread level. ${key} ${e.getMessage()}"
				[key, null]
			}

		}
	}


}
