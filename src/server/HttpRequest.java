package server;

import java.io.InputStream;
import java.util.Map;

import com.google.common.collect.Multimap;

public class HttpRequest {

	// TODO: multimaps

	private final HttpMethod method;
	private final String requestUri;
	private final String httpVersion;
	private final Map<String, String> headers;
	private final InputStream content;

	HttpRequest(HttpMethod method,
			String requestUri,
			Multimap<String, String> queryParameters,
			String httpVersion,
			Map<String, String> headers,
			InputStream content) {
		this.method = method;
		this.requestUri = requestUri;
		this.httpVersion = httpVersion;
		this.headers = headers;
		this.content = content;
	}

	public HttpMethod method() {
		return method;
	}

	public String requestUri() {
		return requestUri;
	}

	public String httpVersion() {
		return httpVersion;
	}

	public String getHeader(String key) {
		return headers.get(key);
	}

	public Map<String, String> headers() {
		return headers;
	}

	public InputStream content() {
		return content;
	}

}
