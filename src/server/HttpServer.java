package server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteSource;
import com.google.common.net.HttpHeaders;

public abstract class HttpServer {

	private ServerSocket server;

	protected static final DateTimeFormatter HTTP_DATE_FORMAT =
			DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
					.withZoneUTC().withLocale(Locale.US);

	public HttpServer(int port) throws IOException {
		this.server = new ServerSocket(port);
	}

	public void start() throws IOException {
		for (;;) {
			new Thread(new Connection(server.accept())).start();
		}
	}

	private static final Splitter REQUEST = Splitter.on(' ');
	private static final byte[] NO_CONTENT = new byte[0];
	private static final Splitter QUERY_PARAMETERS = Splitter.on('&');

	private class Connection implements Runnable {

		private final HttpInput in;
		private final OutputStream out;

		Connection(Socket socket) throws IOException {
			this.in = new HttpInput(socket.getInputStream());
			this.out = new BufferedOutputStream(socket.getOutputStream());
		}

		@Override public void run() {
			System.out.println("CONNECTION OPENED");
			Stopwatch stopwatch = Stopwatch.createStarted();
			try {
				for (;;) {
					String requestLine = in.readLine();
					if (requestLine == null) {
						break;
					}
					System.out.println(requestLine);
					List<String> parts = REQUEST.splitToList(requestLine);
					String requestUri = parts.get(1);
					int query = requestUri.lastIndexOf('?');
					Multimap<String, String> queryParameters;
					if (query == -1) {
						queryParameters = ImmutableMultimap.of();
					} else {
						queryParameters = parseQueryParameters(requestUri.substring(query + 1));
						requestUri = requestUri.substring(0, query);
					}

					Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
					readHeaders(headers);
					byte[] content = readContent(headers);
					handle(new HttpRequest(HttpMethod.valueOf(parts.get(0)),
							requestUri,
							queryParameters,
							parts.get(2),
							Collections.unmodifiableMap(headers),
							ByteSource.wrap(content)), new HttpResponse(out));
					System.out.println();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("CONNECTION CLOSED: " + stopwatch);
		}

		private Multimap<String, String> parseQueryParameters(String params) {
			Builder<String, String> builder = ImmutableMultimap.builder();
			for (String part : QUERY_PARAMETERS.split(params)) {
				int separator = part.indexOf('=');
				if (separator != -1) {
					builder.put(part.substring(0, separator), part.substring(separator + 1));
				}
			}
			return builder.build();
		}

		private void readHeaders(Map<String, String> headers) throws IOException {
			String line;
			for (;;) {
				line = in.readLine();
				if (line.isEmpty()) {
					break;
				}
				System.out.println(line);
				int separator = line.indexOf(':');
				headers.put(line.substring(0, separator).trim(),
						line.substring(separator + 1).trim());
			}
		}

		private byte[] readContent(Map<String, String> headers) throws IOException {
			String contentLength = headers.get(HttpHeaders.CONTENT_LENGTH);
			if (contentLength != null) {
				return in.readContent(Integer.parseInt(contentLength));
			}
			String encoding = headers.get(HttpHeaders.TRANSFER_ENCODING);
			if (encoding != null && encoding.equalsIgnoreCase("chunked")) {
				byte[] content = in.readChunked();
				readHeaders(headers);
				return content;
			}
			return NO_CONTENT;
		}
	}

	private static String decode(String requestUri) {
		int index = requestUri.indexOf('%');
		if (index == -1) {
			return requestUri;
		}
		StringBuilder builder = new StringBuilder();
		int lastIndex = 0;
		while (index != -1) {
			builder.append(requestUri.substring(lastIndex, index))
					.append((char) Integer.parseInt(
							requestUri.substring(index + 1, index + 3), 16));
			lastIndex = index + 3;
			index = requestUri.indexOf('%', lastIndex);
		}
		return builder.append(requestUri.substring(lastIndex)).toString();
	}

	protected abstract void handle(HttpRequest request, HttpResponse response) throws IOException;

}
