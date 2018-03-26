package com.catascopic.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;

public class HttpRequest {

	private static final String CHUNKED = "Chunked";

	private HttpMethod method;
	private String requestUri;
	private String path;
	private String query;
	private String httpVersion;
	private Map<String, String> headers;
	private Map<String, String> cookies; // = null
	private Map<String, String> trailers; // = null
	private InputStream content;

	private HttpReader reader;

	private HttpRequest(HttpReader reader) throws IOException,
			HttpSyntaxException {
		parseRequestLine(reader.readLine());
		headers = reader.parseHeaders();
		Integer contentLength = contentLength();
		if (contentLength != null) {
			content = reader.streamContent(contentLength);
		} else if (CHUNKED.equals(headers.get(HttpHeaders.TRANSFER_ENCODING))) {
			content = reader.streamChunked();
		}
	}

	private void parseRequestLine(String requestLine)
			throws HttpSyntaxException {
		List<String> requestParts = Splitter.on(' ').splitToList(requestLine);
		method = HttpMethod.valueOf(requestParts.get(0));
		requestUri = requestParts.get(1);
		if (requestParts.get(2).equals(HTTP_1_1)) {
			throw new HttpSyntaxException();
		}
		int queryIndex = requestUri.indexOf('?');
		if (queryIndex == -1) {
			path = requestUri;
			query = "";
		} else {
			path = requestUri.substring(0, queryIndex);
			query = requestLine.substring(queryIndex + 1);
		}
	}

	public HttpMethod method() {
		return method;
	}

	public String requestUri() {
		return requestUri;
	}

	public String path() {
		return path;
	}

	public String query() {
		return query;
	}

	public List<String> parsePath() {
		return UriParser.parse(path);
	}

	public Map<String, String> parseQuery() {
		return UriParser.toMap(query);
	}

	public Multimap<String, String> parseQueryAsMultimap() {
		return UriParser.toMultimap(query);
	}

	public String httpVersion() {
		// TODO: ???
		return HTTP_1_1;
	}

	public String getHeader(String key) {
		return headers.get(key);
	}

	public DateTime getHeaderAsDateTime(String key) {
		String value = headers.get(key);
		return value == null ? null : HttpDateTimeFormat.parse(value);
	}

	public Map<String, String> headers() {
		return headers;
	}

	public Map<String, String> cookies() {
		if (cookies == null) {
			String cookie = getHeader(HttpHeaders.COOKIE);
			cookies = cookie == null
					? Collections.emptyMap()
					: Splitter.on(';').trimResults()
							.withKeyValueSeparator('=').split(cookie);
		}
		return cookies;
	}

	public Map<String, String> trailers() {

	}

	public boolean hasContent() {
		return content != null;
	}

	public InputStream content() {
		return content;
	}

	public String readContent(Charset charset) throws IOException {
		Integer length = contentLength();
		if (length == null) {
			throw new IllegalStateException();
		}
		byte[] payload = new byte[length];
		ByteStreams.readFully(content, payload);
		return new String(payload, charset);
	}

	public String readContent() throws IOException {
		return readContent(StandardCharsets.UTF_8);
	}

	public Integer contentLength() {
		return getAsInteger(HttpHeaders.CONTENT_LENGTH);
	}

	private Integer getAsInteger(String header) {
		String value = headers.get(header);
		return value == null ? null : Integer.parseInt(value);
	}

	@Override
	public String toString() {
		return String.format("%s %s %s", method, requestUri, httpVersion);
	}

}
