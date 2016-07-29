package server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	private InputStream content = null;
	private final OutputStream out;

	HttpResponse(OutputStream out) {
		this.out = out;
	}

	public HttpResponse setStatus(HttpStatus status) {
		this.status = status;
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

	public HttpResponse setContent(File file) {
		setHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(file.length()));
		try {
			content = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
		return this;
	}

	public HttpResponse setContent(byte[] bytes) {
		setHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(bytes.length));
		content = new ByteArrayInputStream(bytes);
		return this;
	}

	public HttpResponse setContent(InputStream stream, long length) {
		setHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(length));
		content = ByteStreams.limit(stream, length);
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
		if (content != null) {
			ByteStreams.copy(content, out);
		}
		out.flush();
	}

}
