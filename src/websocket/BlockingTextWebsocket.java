package websocket;

import java.io.IOException;
import java.net.Socket;

public class BlockingTextWebsocket extends Websocket4 {

	protected BlockingTextWebsocket(Socket socket) {
		super(socket);
	}

	String message; // = null

	public String nextMessage() throws IOException {
		do {
			handleNextFrame();
		} while (message == null);
		return message;
	}

	@Override protected void onMessage(String text) {
		message = text;
	}

}
