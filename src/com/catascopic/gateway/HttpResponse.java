package com.catascopic.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.joda.time.DateTime;

import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;

public class HttpResponse {

	private HttpStatus status;
	private Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private Content content = NO_CONTENT;
	private List<String> cookies = new ArrayList<>();
	private static final String VERSION = "HTTP/1.1";

	HttpResponse() {
		setHeader(HttpHeaders.DATE, HttpDateTimeFormat.print(DateTime.now()));
	}

	public HttpResponse setStatus(HttpStatus status) {
		this.status = status;
		return this;
	}

	public HttpResponse setHeader(String key, String value) {
		headers.put(key, value);
		return this;
	}

	public HttpResponse setExpires(DateTime expires) {
		return setHeader(HttpHeaders.EXPIRES, HttpDateTimeFormat.print(expires));
	}

	public HttpResponse setContentType(MediaType contentType) {
		return setHeader(HttpHeaders.CONTENT_TYPE, contentType.toString());
	}

	public HttpResponse setLastModified(DateTime time) {
		return setHeader(HttpHeaders.LAST_MODIFIED, HttpDateTimeFormat.print(time));
	}

	public HttpResponse setContent(String content) {
		return setContent(content, StandardCharsets.UTF_8);
	}

	public HttpResponse setContent(String content, Charset charset) {
		return setContent(content.getBytes(charset));
	}

	public HttpResponse setContent(final byte[] bytes) {
		setHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(bytes.length));
		content = new Content() {

			@Override
			public void write(OutputStream out) throws IOException {
				out.write(bytes);
			}
		};
		return this;
	}

	public HttpResponse setContent(final Path file) throws IOException {
		setHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(Files.size(file)));
		content = new Content() {

			@Override
			public void write(OutputStream out) throws IOException {
				try (InputStream in = Files.newInputStream(file)) {
					ByteStreams.copy(in, out);
				}
			}
		};
		return this;
	}

	public HttpResponse setContentAndLastModified(Path file) throws IOException {
		return setLastModified(new DateTime(Files.getLastModifiedTime(file).toMillis()))
				.setContent(file);
	}

	public HttpResponse setChunkedContent(ByteSource byteSource) {
		setHeader(HttpHeaders.TRANSFER_ENCODING, "Chunked");
		content = new ChunkedContent(byteSource, 8192);
		return this;
	}

	public HttpResponse addCookie(CookieBuilder cookie) {
		cookies.add(cookie.toString());
		return this;
	}

	private static final String CRLF = "\r\n";

	void send(OutputStream out) throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append(VERSION).append(' ')
				.append(status.code()).append(' ')
				.append(status.title()).append(CRLF);
		for (Entry<String, String> header : headers.entrySet()) {
			appendHeader(builder, header.getKey(), header.getValue());
		}
		for (String cookie : cookies) {
			appendHeader(builder, HttpHeaders.SET_COOKIE, cookie);
		}
		builder.append(CRLF);
		writeAscii(out, builder.toString());
		content.write(out);
		out.flush();
	}

	private static void appendHeader(StringBuilder builder, String key, String value) {
		builder.append(key).append(": ").append(value).append(CRLF);
	}

	private static void writeAscii(OutputStream out, String text) throws IOException {
		out.write(text.getBytes(StandardCharsets.US_ASCII));
	}

	@Override
	public String toString() {
		return VERSION + " " + status;
	}

	private interface Content {

		void write(OutputStream out) throws IOException;
	}

	private static class ChunkedContent implements Content {

		private ByteSource byteSource;
		private int bufferSize;

		public ChunkedContent(ByteSource byteSource, int bufferSize) {
			this.byteSource = byteSource;
			this.bufferSize = bufferSize;
		}

		@Override
		public void write(OutputStream out) throws IOException {
			try (InputStream in = byteSource.openStream()) {
				byte[] buf = new byte[bufferSize];
				for (;;) {
					int count = in.read(buf);
					if (count == -1) {
						break;
					}
					writeAscii(out, count + CRLF);
					out.write(buf, 0, count);
					writeAscii(out, CRLF);
				}
				writeAscii(out, "0" + CRLF + CRLF);
			}
		}
	}

	private static final Content NO_CONTENT = new Content() {

		@Override
		public void write(OutputStream out) {
			// Do nothing
		}
	};

}
