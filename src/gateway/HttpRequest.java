package gateway;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.google.common.base.Splitter;
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
	private Map<String, String> headers = new LinkedHashMap<>();
	private Map<String, String> cookies = Collections.emptyMap();
	private InputStream content;

	private HttpRequest() {}

	static HttpRequest read(InputStream in) throws IOException {
		HttpRequest req = new HttpRequest();
		req.read(new HttpReader(in));
		return req;
	}

	void read(HttpReader input) throws IOException {
		parseRequestLine(input.readLine());
		for (;;) {
			String line = input.readLine();
			if (line.isEmpty()) {
				break;
			}
			TokenParser header = new TokenParser(line);
			String key = header.getNext(':').trim();
			String value = header.remainder().trim();

			if (key.equals(HttpHeaders.COOKIE)) {
				cookies = parseCookies(value);
			} else {
				headers.put(key, value);
			}
		}
		Integer contentLength = contentLength();
		if (contentLength != null) {
			content = input.streamContent(contentLength);
		} else if (CHUNKED.equals(headers.get(HttpHeaders.TRANSFER_ENCODING))) {
			content = input.streamChunked();
		}
	}

	private void parseRequestLine(String requestLine) {
		Iterator<String> requestParts = Splitter.on(' ').split(requestLine).iterator();
		method = HttpMethod.valueOf(requestParts.next());
		requestUri = requestParts.next();
		httpVersion = requestParts.next();

		int queryIndex = requestUri.indexOf('?');
		if (queryIndex == -1) {
			path = requestUri;
			query = "";
		} else {
			path = requestUri.substring(0, queryIndex);
			query = requestLine.substring(queryIndex + 1);
		}
	}

	private Map<String, String> parseCookies(String value) {
		Map<String, String> cookies = new LinkedHashMap<>();
		TokenParser parser = new TokenParser(value);
		while (parser.hasNext()) {
			cookies.put(parser.getNext('='), parser.getNext(';'));
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
		return query;
	}
	
	public List<String> parsePath() {
		return PathParser.parse(path);
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
	
	public DateTime getHeaderAsDateTime(String key) {
		String value = headers.get(key);
		return value == null ? null : HttpDateTimeFormat.parse(value);
	}

	public Map<String, String> headers() {
		return headers;
	}

	public Map<String, String> cookies() {
		return cookies;
	}

	public InputStream payload() {
		return content;
	}

	public String readPayload(Charset charset) throws IOException {
		Integer length = contentLength();
		if (length == null) {
			throw new IllegalStateException();
		}
		byte[] payload = new byte[length];
		ByteStreams.readFully(content, payload);
		return new String(payload, charset);
	}

	public String readPayload() throws IOException {
		return readPayload(StandardCharsets.UTF_8);
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
		return method + " " + requestUri + " " + httpVersion;
	}
	
	

}
