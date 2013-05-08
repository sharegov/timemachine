package gov.sharegov.timemachine.scheduler




import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLContext
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManager
import java.security.SecureRandom
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.scheme.SchemeRegistry

import net.sf.json.JSONObject
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import groovyx.net.http.ContentType
import groovyx.net.http.Method

import groovyx.net.http.ContentType;

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

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
