package websocket;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.google.common.io.BaseEncoding;
import com.google.common.net.HttpHeaders;

import server.HttpInput;
import server.HttpStatus;

public class WebsocketServer implements Closeable {

	private final ServerSocket server;

	public WebsocketServer(int port) throws IOException {
		server = new ServerSocket(port);
	}

	private static final BaseEncoding BASE64 = BaseEncoding.base64();
	private static final MessageDigest SHA1;
	private static final String WEBSOCKET_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	private static final String CRLF = "\r\n";
	private static final String WEBSOCKET_KEY = "Sec-WebSocket-Key: ";

	static {
		try {
			SHA1 = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError(e);
		}
	}

	public Websocket accept() throws IOException {
		return accept(NULL_DEVICE);
	}

	public Websocket accept(List<String> headers) throws IOException {
		return accept(new ListWriter(headers));
	}

	private Websocket accept(LineConsumer consumer) throws IOException {
		Socket socket = server.accept();
		HttpInput in = new HttpInput(socket.getInputStream());
		String key = null;
		for (;;) {
			String line = in.readLine();
			if (line.isEmpty()) {
				break;
			}
			consumer.consume(line);
			if (line.startsWith(WEBSOCKET_KEY)) {
				key = line.substring(WEBSOCKET_KEY.length());
			}
		}
		if (key == null) {
			throw new IOException("No websocket accept key found.");
		}

		String acceptKey = BASE64.encode(SHA1.digest((key + WEBSOCKET_GUID).getBytes(US_ASCII)));
		String reply = HttpStatus._101_SWITCHING_PROTOCOLS + CRLF
				+ HttpHeaders.UPGRADE + ": websocket" + CRLF
				+ HttpHeaders.CONNECTION + ": Upgrade" + CRLF
				+ "Sec-WebSocket-Accept: " + acceptKey + CRLF
				+ CRLF;
		OutputStream out = socket.getOutputStream();
		out.write(reply.getBytes(US_ASCII));
		out.flush();
		return new Websocket(socket);
	}

	private interface LineConsumer {

		void consume(String line);
	}

	private static final LineConsumer NULL_DEVICE = new LineConsumer() {

		@Override public void consume(String line) {}
	};

	private static class ListWriter implements LineConsumer {

		final List<String> lines;

		ListWriter(List<String> lines) {
			this.lines = lines;
		}

		@Override public void consume(String line) {
			lines.add(line);
		}
	}

	@Override public void close() throws IOException {
		server.close();
	}

	public boolean isClosed() {
		return server.isClosed();
	}

}
