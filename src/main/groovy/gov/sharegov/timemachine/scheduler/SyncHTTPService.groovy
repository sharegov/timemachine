package gov.sharegov.timemachine.scheduler


import groovyx.net.http.ContentType;

import net.sf.json.JSONObject
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import groovyx.net.http.ContentType
import groovyx.net.http.Method

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

class SyncHTTPService implements HTTPService{

	private static Logger _log = LoggerFactory.getLogger(SyncHTTPService.class);

	def http

	Object request(String url, def query, ContentType contentType=JSON){


		http.request(url, GET, contentType) {
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
	}

	Object requestPost(String url, def query, ContentType contentType=JSON){

		http.request(url, POST, contentType) {

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
	}

	Map request(List urls, def query, ContentType contentType){
		_log.error "requestMultiple() - Method not implemented. It returns null."
		return null
	}
}
