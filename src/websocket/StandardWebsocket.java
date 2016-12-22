package websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public abstract class StandardWebsocket extends Websocket implements Runnable {

	private Socket socket;

	protected StandardWebsocket(Socket socket) {
		this.socket = socket;
	}

	@Override protected final OutputStream getOutputStream() throws IOException {
		return socket.getOutputStream();
	}

	@Override protected final InputStream getInputStream() throws IOException {
		return socket.getInputStream();
	}

	@Override protected void closeConnection() throws IOException {
		socket.close();
	}

	@Override public void run() {
		try {
			while (!isClosed()) {
				handleNextFrame();
			}
		} catch (IOException e) {
			synchronized (this) {
				if (!isClosed()) {
					try {
						closeConnection();
					} catch (IOException e1) {

					}
				}
			}
		}
	}

}
