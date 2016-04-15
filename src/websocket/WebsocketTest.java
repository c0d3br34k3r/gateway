package websocket;

import java.io.IOException;

import server.HttpStatus;

public class WebsocketTest {

	public static void main(String[] args) throws IOException {
		WebsocketServer server = new WebsocketServer(3637);
		Websocket websocket = server.accept();
		for (;;) {
			System.out.println(websocket.read().text().length());
			websocket.send(HttpStatus._418_IM_A_TEAPOT.toString());
		}
	}

}
