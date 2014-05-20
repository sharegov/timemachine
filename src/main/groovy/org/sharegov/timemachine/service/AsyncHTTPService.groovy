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



import groovyx.net.http.ContentType
import groovyx.net.http.URIBuilder
import groovyx.net.http.Method

	
import net.sf.json.JSONObject
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLContext
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManager
import java.security.SecureRandom
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.scheme.SchemeRegistry

import org.apache.http.impl.cookie.BasicClientCookie


class AsyncHTTPService implements HTTPService {

	private static Logger _log = LoggerFactory.getLogger(AsyncHTTPService.class);

	def http
	String cookieName
	String cookieValue

	public void init(){
		
		// accept ssl self signed certificates (peer not authenticated - SSLPeerUnverifiedException
		def sslContext = SSLContext.getInstance("SSL")
		sslContext.init(null, [ new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {null }
			public void checkClientTrusted(X509Certificate[] certs, String authType) { }
			public void checkServerTrusted(X509Certificate[] certs, String authType) { }
		} ] as TrustManager[], new SecureRandom())

		def sf = new SSLSocketFactory(sslContext)
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
		def httpsScheme = new Scheme("https", sf, 443)
		http.client.connectionManager.schemeRegistry.register( httpsScheme )
	}
	
		
	Object request(String url, def query, ContentType contentType=JSON){
		Map result = request ([url], query, contentType)
		return result[url]
	}
	
	Map request(List urls, def query, ContentType contentType=JSON){
		
		def done = [:]

		// Fire each one of the requests.
		urls.each {url ->
			// make the http request to the given url.
			done[url] =
				http.request(url, GET, contentType, processRequest(query, GET))
		}

		// Wait for all requests to comeback
		while ( true ) {
			if ( done.every{ it.value.done} )
				break
			Thread.sleep(100)
		}

		// Processs the request data. Convert the resuts to a map. return result.
		// When calling get an exception may be returned, if exception happened in the
		// thread, like connection refused: ConnectException.
		Boolean exceptionsFound = false
		String concatenatedExceptionsMessage = ""
		Map result = done.collectEntries {key, value ->
			try{
				[key, value.get()]
			}catch(Exception e){
				String message = "request() - Exception at the thread level for url ${key} | message: ${e.getMessage()} | query: ${query}"
				_log.error message
				exceptionsFound = true
				concatenatedExceptionsMessage += " ++ ${message} ++ "

				[key, null]
			}
		}
		
		// If exceptions happens in any of the threads rethrow an exception
		if(exceptionsFound)
			throw new RetrievalOfDataException(concatenatedExceptionsMessage)
		
		
		return result
		
	}

	Object requestPost(String url, def query, ContentType contentType=JSON){

		def result = http.request(url, POST, contentType, processRequest(query, POST))

		// Wait for call to come back
		while ( true ) {
			if (result.done)
				break
			Thread.sleep(100)
		}

		// return result. when calling get an exception may be returned, if exception happened in the
		// thread, like connection refused: ConnectException
		try{
			return  result.get()

		}catch (Exception e) {
			String message = "requestPost() - Exception at the thread level for url ${url} | message: ${e.getMessage()} | query: ${query}"
			_log.error message
			throw new RetrievalOfDataException(message)
		}

	}
	
	Closure processRequest(def query, Method httpMethod) {
		Closure cls = {
			
			Date startDate = new Date()
			headers.'Cookie'="${cookieName}=${cookieValue}"
			
			if(httpMethod == POST)
				body = query?:[:]
				//send URLENC, query?:[:]
			else
				uri.query = query
			
			response.success = {resp, result ->
				Date endDate = new Date()
				_log.info "processRequest() - ${endDate.time-startDate.time} - Successful request for uri ${uri} | query: ${query} | result: ${result}"
				
				return result }

			response.failure = { resp ->
				Date endDate = new Date()
				String message = "${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
				_log.error "processRequest() - ${endDate.time-startDate.time} - Unexpected error for uri ${uri} | message: ${message} | query: ${query}"
				_log.error "processRequest() - response ${resp}"
	
				throw new RetrievalOfDataException("Unexpected error for uri ${uri} | message: ${message} | query: ${query}")
			}

		}
	}
	

}
