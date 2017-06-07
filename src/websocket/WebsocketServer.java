package websocket;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.common.net.HttpHeaders;

import server.HttpInput;
import server.HttpStatus;

public abstract class WebsocketServer implements Closeable {

	private final ServerSocket server;

	public WebsocketServer(int port) throws IOException {
		server = new ServerSocket(port);
	}

	private static final BaseEncoding BASE64 = BaseEncoding.base64();
	private static final String WEBSOCKET_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	private static final String CRLF = "\r\n";
	private static final String WEBSOCKET_KEY = "Sec-WebSocket-Key";

	public final Websocket4 accept() throws IOException {
		Socket socket = server.accept();
		HttpInput in = new HttpInput(socket.getInputStream());
		String requestLine = in.readLine();
		Map<String, String> headers = in.readHeaders();
		String requestUri = Splitter.on(' ').splitToList(requestLine).get(1);
		String key = headers.get(WEBSOCKET_KEY);
		if (key == null) {
			throw new IOException("No websocket accept key found.");
		}
		String acceptKey = BASE64.encode(Hashing.sha1().hashString(key + WEBSOCKET_GUID, US_ASCII).asBytes());
		String reply = HttpStatus._101_SWITCHING_PROTOCOLS + CRLF
				+ HttpHeaders.UPGRADE + ": websocket" + CRLF
				+ HttpHeaders.CONNECTION + ": Upgrade" + CRLF
				+ "Sec-WebSocket-Accept: " + acceptKey + CRLF
				+ CRLF;
		OutputStream out = socket.getOutputStream();
		out.write(reply.getBytes(US_ASCII));
		out.flush();
		return createWebsocket(socket, requestUri, headers);
	}

	protected abstract Websocket4 createWebsocket(Socket socket, String uri,
			Map<String, String> headers);

	@Override public void close() throws IOException {
		server.close();
	}

	public boolean isClosed() {
		return server.isClosed();
	}

}
