package gateway.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BlockingWebsocket extends AbstractWebsocket {

	private final Socket socket;
	private BlockingQueue<String> queue = new LinkedBlockingQueue<>();

	public BlockingWebsocket(Socket socket) throws IOException {
		super(socket.getInputStream(), socket.getOutputStream());
		this.socket = socket;
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
