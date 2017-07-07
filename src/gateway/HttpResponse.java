package gateway;

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

import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;

public class HttpResponse {

	private HttpStatus status;
	private Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private Content content = NO_CONTENT;
	private List<String> cookies = new ArrayList<>();
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
		setLastModified(new DateTime(Files.getLastModifiedTime(file).toMillis()));
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

	public HttpResponse setLastModified(DateTime time) {
		setHeader(HttpHeaders.LAST_MODIFIED, HttpDateTimeFormat.toString(time));
		return this;
	}

	public HttpResponse setChunkedContent(InputStream stream) {
		setHeader(HttpHeaders.TRANSFER_ENCODING, "Chunked");
		content = new ChunkedContent(stream, 8192);
		return this;
	}

	public void addCookie(CookieBuilder cookie) {
		cookies.add(cookie.toString());
	}

	private static final String CRLF = "\r\n";

	public void send() throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append(status.toString()).append(CRLF);
		for (Entry<String, String> header : headers.entrySet()) {
			builder.append(header.getKey())
					.append(": ")
					.append(header.getValue())
					.append(CRLF);
		}
		for (String cookie : cookies) {
			builder.append(HttpHeaders.SET_COOKIE).append(": ").append(cookie).append(CRLF);
		}
		builder.append(CRLF);
		writeAscii(out, builder.toString());
		content.write(out);
		out.flush();
	}

	private static void writeAscii(OutputStream out, String text) throws IOException {
		out.write(text.getBytes(StandardCharsets.US_ASCII));
	}

	private interface Content {

		void write(OutputStream out) throws IOException;
	}

	private static class ChunkedContent implements Content {

		private InputStream input;
		private int bufferSize;

		public ChunkedContent(InputStream input, int bufferSize) {
			this.input = input;
			this.bufferSize = bufferSize;
		}

		@Override
		public void write(OutputStream out) throws IOException {
			try (InputStream in = input) {
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
