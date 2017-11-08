package gateway.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BlockingWebsocket extends Websocket {

	private final Socket socket;
	private BlockingQueue<String> queue = new LinkedBlockingQueue<>();

	public BlockingWebsocket(Socket socket) {
		this.socket = socket;
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		return socket.getOutputStream();
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		return socket.getInputStream();
	}

	@Override
	protected void closeConnection() throws IOException {
		socket.close();
	}

	@Override
	protected void onMessage(String text) {
		queue.add(text);
	}

	@Override
	protected void onMessage(byte[] bytes) {
		try {
			sendClose(1008);
		} catch (IOException e) {

		}
	}

	@Override
	protected void onClose(int code, String message) {
		queue.add(code + ": " + message);
	}

	public String getMessage() throws InterruptedException {
		return queue.take();
	}

}
