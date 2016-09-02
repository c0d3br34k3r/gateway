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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;

public class HttpResponse {

	private HttpStatus status;
	private Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private Content content = NO_CONTENT;
	private List<String> cookies = new ArrayList<>(4);
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
			content = new RegularContent(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
		return this;
	}

	public HttpResponse setContent(byte[] bytes) {
		setHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(bytes.length));
		content = new RegularContent(new ByteArrayInputStream(bytes));
		return this;
	}

	public HttpResponse setChunkedContent(InputStream stream) {
		setHeader(HttpHeaders.TRANSFER_ENCODING, "Chunked");
		content = new ChunkedContent(stream, 8192);
		return this;
	}

	public void addCookie(Cookie cookie) {
		cookies.add(cookie.toString());
	}

	private static final String CRLF = "\r\n";

	public void send() throws IOException {
		// OutputStreamWriter writer = new OutputStreamWriter(out,
		// StandardCharsets.UTF_8);
		StringBuilder response = new StringBuilder();
		response.append(status).append(CRLF);
		for (Entry<String, String> header : headers.entrySet()) {
			response.append(header.getKey())
					.append(": ")
					.append(header.getValue())
					.append(CRLF);
		}
		for (String cookie : cookies) {
			response.append(HttpHeaders.SET_COOKIE).append(": ").append(cookie);
		}
		response.append(CRLF);
		out.write(response.toString().getBytes(StandardCharsets.US_ASCII));
		content.write(out);
		out.flush();
	}

	private interface Content {

		void write(OutputStream out) throws IOException;
	}

	private static class RegularContent implements Content {

		private InputStream input;

		public RegularContent(InputStream input) {
			this.input = input;
		}

		@Override public void write(OutputStream out) throws IOException {
			ByteStreams.copy(input, out);
		}
	}

	private static class ChunkedContent implements Content {

		private InputStream input;
		private int bufferSize;

		public ChunkedContent(InputStream input, int bufferSize) {
			this.input = input;
			this.bufferSize = bufferSize;
		}

		@Override public void write(OutputStream out) throws IOException {
			byte[] buf = new byte[bufferSize];
			for(;;) {
				int count = input.read(buf);
				if (count == -1) {
					break;
				}
				out.write((count + CRLF).getBytes(StandardCharsets.US_ASCII));
				out.write(buf, 0, count);
				out.write(CRLF.getBytes(StandardCharsets.US_ASCII));
			}
			out.write((0 + CRLF + CRLF).getBytes(StandardCharsets.US_ASCII));
		}
	}

	private static final Content NO_CONTENT = new Content() {

		@Override public void write(OutputStream out) {
			// Do nothing
		}
	};

}
