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



import groovyx.net.http.ContentType;


import net.sf.json.JSONObject
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovyx.net.http.ContentType
import groovyx.net.http.Method

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

class SyncHTTPService implements HTTPService{

	private static Logger _log = LoggerFactory.getLogger(SyncHTTPService.class);

	def http
	
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

		def startDate = new Date().time

		http.request(url, GET, contentType) {
			uri.query = query

			response.success = {resp, data ->
				_log.info "syncRequest() - request for url ${url} : ${data}"
				_log.info "syncRequest() - uri ${uri}"
				_log.info "syncRequest() - response ${resp}"
				_log.info "syncRequest() - query params ${query}"
				_log.info "syncRequest() - conetentType ${contentType}"
				return data
			}

			response.'404' = {resp ->
				String message = "${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
				_log.error "syncrequest() - 404 for url ${url} : ${message}"
				_log.error "syncRequest() - uri ${uri}"
				_log.error "syncRequest() - response ${resp}"
				_log.error "syncRequest() - query params ${query}"
				_log.error "syncRequest() - conetentType ${contentType}"

				def errorResult = [error:[code:resp.statusLine.statusCode, message:resp.statusLine.reasonPhrase, details:[""]]] as JSONObject
				return errorResult
			}

			response.failure = { resp ->
				String message = "${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
				_log.error "syncRequest() - Unexpected error: request for url ${url} : ${message}"
				_log.error "syncRequest() - uri ${uri}"
				_log.error "syncRequest() - response ${resp}"
				_log.error "syncRequest() - query params ${query}"
				_log.error "syncRequest() - conetentType ${contentType}"

				def errorResult = [error:[code:resp.statusLine.statusCode, message:resp.statusLine.reasonPhrase, details:[""]]] as JSONObject
				return errorResult

			}
		}
		
		def endDate = new Date().time
		_log.info "time for url ${url} is ${endDate-startDate}"
	}

	Object requestPost(String url, def query, ContentType contentType=JSON){

		def startDate = new Date().time
		
		http.request(url, POST, contentType) {

			send URLENC, query

			response.success = {resp, data ->
				_log.info "syncRequestPost() - request for url ${url} : ${data}"
				_log.info "syncRequestPost() - response ${resp}"
				_log.info "syncRequestPost() - query params ${query}"
				_log.info "syncRequestPost() - conetentType ${contentType}"
				return data
			}

			response.'404' = {resp ->
				String message = "${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
				_log.error "syncRequestPost() - 404 for url ${url} : ${message}"
				_log.error "syncRequestPost() - response ${resp}"
				_log.error "syncRequestPost() - query params ${query}"
				_log.error "syncRequestPost() - conetentType ${contentType}"

				def errorResult = [error:[code:resp.statusLine.statusCode, message:resp.statusLine.reasonPhrase, details:[""]]] as JSONObject
				return errorResult
			}

			response.failure = { resp ->
				String message = "${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
				_log.error "syncRequestPost() - Unexpected error: request for url ${url} : ${message}"
				_log.error "syncRequestPost() - response ${resp}"
				_log.error "syncRequestPost() - query params ${query}"
				_log.error "syncRequestPost() - conetentType ${contentType}"

				def errorResult = [error:[code:resp.statusLine.statusCode, message:resp.statusLine.reasonPhrase, details:[""]]] as JSONObject
				return errorResult

			}
		}
		
		def endDate = new Date().time
		_log.info "syncRequestPost() - time for url ${url} is ${endDate-startDate}"
	}

	Map request(List urls, def query, ContentType contentType){
		_log.error "requestMultiple - request() - Method not implemented. It returns null."
		return null
	}
}


