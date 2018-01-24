package gateway.websocket;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.common.net.HttpHeaders;

import gateway.HttpReader;
import gateway.HttpStatus;

public abstract class WebsocketServer implements Closeable {

	private final ServerSocket server;

	public WebsocketServer(int port) throws IOException {
		server = new ServerSocket(port);
	}

	private static final BaseEncoding BASE64 = BaseEncoding.base64();
	private static final String WEBSOCKET_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	private static final String CRLF = "\r\n";
	private static final String WEBSOCKET_KEY = "Sec-WebSocket-Key";

	public final AbstractWebsocket accept() throws IOException {
		Socket socket = server.accept();
		HttpReader in = new HttpReader(socket.getInputStream());
		String requestLine = in.readLine();
		Map<String, String> headers = readHeaders(in);
		String requestUri = Splitter.on(' ').splitToList(requestLine).get(1);
		String key = headers.get(WEBSOCKET_KEY);
		if (key == null) {
			throw new IOException("No websocket accept key found.");
		}
		String acceptKey =
				BASE64.encode(Hashing.sha1().hashString(key + WEBSOCKET_GUID, US_ASCII).asBytes());
		String reply = "HTTP/1.1 " + HttpStatus._101_SWITCHING_PROTOCOLS + CRLF
				+ HttpHeaders.UPGRADE + ": websocket" + CRLF
				+ HttpHeaders.CONNECTION + ": Upgrade" + CRLF
				+ "Sec-WebSocket-Accept: " + acceptKey + CRLF
				+ CRLF;
		OutputStream out = socket.getOutputStream();
		out.write(reply.getBytes(US_ASCII));
		out.flush();
		return createWebsocket(socket, requestUri, headers);
	}

	protected abstract AbstractWebsocket createWebsocket(Socket socket, String uri,
			Map<String, String> headers) throws IOException;

	@Override
	public void close() throws IOException {
		server.close();
	}

	public boolean isClosed() {
		return server.isClosed();
	}

	private static Map<String, String> readHeaders(HttpReader in) throws IOException {
		Map<String, String> headers = new LinkedHashMap<>();
		Splitter headerSplitter = Splitter.on(':').trimResults();
		for (;;) {
			String line = in.readLine();
			if (line.isEmpty()) {
				break;
			}
			List<String> header = headerSplitter.splitToList(line);
			headers.put(header.get(0), header.get(1));
		}
		return headers;
	}

}
