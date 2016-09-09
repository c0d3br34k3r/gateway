package server;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.collect.Multimap;
import com.google.common.net.HttpHeaders;

public class HttpRequest {

	private HttpMethod method;
	private String path;
	private String query;
	private String httpVersion;
	private Map<String, String> headers = new LinkedHashMap<>();
	private Map<String, String> cookies = Collections.emptyMap();
	private ReadableInputStream content;

	private HttpRequest() {};

	static HttpRequest create(HttpInput input) throws IOException {
		HttpRequest req = new HttpRequest();
		req.read(input);
		return req;
	}

	void read(HttpInput input) throws IOException {
		List<String> requestLine = Splitter.on(' ').splitToList(input.readLine());
		method = HttpMethod.valueOf(requestLine.get(0));
		List<String> requestUri = Splitter.on('?').splitToList(requestLine.get(1));
		httpVersion = requestLine.get(2);
		path = requestUri.get(0);
		query = requestUri.get(1);
		Splitter headerSplitter = Splitter.on(':').trimResults();
		for (;;) {
			String line = input.readLine();
			if (line.isEmpty()) {
				break;
			}
			List<String> header = headerSplitter.splitToList(line);
			String key = header.get(0);
			String value = header.get(1);
			headers.put(key, value);
			if (key.equals(HttpHeaders.COOKIE)) {
				cookies = parseCookies(value);
			}
		}
		Integer contentLength = contentLength();
		if (contentLength != null) {
			content = input.streamContent(contentLength);
		} else if (headers.get(HttpHeaders.TRANSFER_ENCODING).equals("Chunked")) {
			content = input.streamChunked();
		}
	}

	private Map<String, String> parseCookies(String value) {
		Map<String, String> cookies = new LinkedHashMap<>();
		Splitter cookieSplitter = Splitter.on('=').trimResults();
		for (String cookie : Splitter.on(';').split(value)) {
			List<String> parts = cookieSplitter.splitToList(cookie);
			cookies.put(parts.get(0), parts.get(1));
		}
		return cookies;
	}

	public HttpMethod method() {
		return method;
	}

	public String path() {
		return path;
	}

	public String query() {
		return path;
	}

	public Map<String, String> parseQuery() {
		return QueryParser.toMap(query);
	}

	public Multimap<String, String> parseQueryAsMultimap() {
		return QueryParser.toMultimap(query);
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

	public Map<String, String> cookies() {
		return cookies;
	}

	public ReadableInputStream content() {
		return content;
	}

	public Integer contentLength() {
		return getAsInteger(HttpHeaders.CONTENT_LENGTH);
	}

	private Integer getAsInteger(String header) {
		String value = headers.get(header);
		return value == null ? null : Integer.parseInt(value);
	}

}
