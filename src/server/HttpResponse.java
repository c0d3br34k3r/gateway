package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;

public class HttpResponse {

	private HttpStatus status;
	private Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private ContentStrategy strategy = NO_CONTENT;
	private final OutputStream out;

	HttpResponse(OutputStream out) {
		this.out = out;
	}

	public HttpResponse setStatus(HttpStatus status) {
		this.status = status;
		return this;
	}

	public HttpResponse setHeader(String key, Object value) {
		setHeader(key, value.toString());
		return this;
	}

	public HttpResponse setHeader(String key, String value) {
		headers.put(key, value);
		return this;
	}

	public HttpResponse setContent(String content) {
		return setContent(content, StandardCharsets.UTF_8);
	}

	public HttpResponse setContent(String content, Charset charset) {
		return setContent(content.getBytes(charset));
	}

	public HttpResponse setContent(final File file) {
		setHeader(HttpHeaders.CONTENT_LENGTH, file.length());
		strategy = new ContentStrategy() {

			@Override public void writeTo(OutputStream out) throws IOException {
				try (InputStream content = new FileInputStream(file)) {
					ByteStreams.copy(content, out);
				}
			}
		};
		return this;
	}

	public HttpResponse setContent(final byte[] content) {
		setHeader(HttpHeaders.CONTENT_LENGTH, content.length);
		strategy = new ContentStrategy() {

			@Override public void writeTo(OutputStream out) throws IOException {
				out.write(content);
			}
		};
		return this;
	}

	private static final String CRLF = "\r\n";

	public void send() throws IOException {
		StringBuilder response = new StringBuilder();
		response.append(status).append(CRLF);
		for (Entry<String, String> header : headers.entrySet()) {
			response.append(header.getKey())
					.append(": ")
					.append(header.getValue())
					.append(CRLF);
		}
		response.append(CRLF);
		out.write(response.toString().getBytes(StandardCharsets.US_ASCII));
		strategy.writeTo(out);
		out.flush();
	}

	public void send404() throws IOException {
		status = HttpStatus._404_NOT_FOUND;
		send();
	}

	private static final ContentStrategy NO_CONTENT = new ContentStrategy() {

		@Override public void writeTo(OutputStream out) {}
	};

	private interface ContentStrategy {

		void writeTo(OutputStream out) throws IOException;
	}

}
