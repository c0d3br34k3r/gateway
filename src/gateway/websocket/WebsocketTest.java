package gateway.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

public class WebsocketTest {

	public static void main(String[] args) throws Exception {
		WebsocketServer server = new WebsocketServer(3637) {

			@Override
			protected Websocket createWebsocket(final Socket socket, String uri,
					Map<String, String> headers) {
				System.out.println("opened");
				final Websocket websocket = new Websocket() {

					@Override
					protected OutputStream getOutputStream() throws IOException {
						return socket.getOutputStream();
					}

					@Override
					protected InputStream getInputStream() throws IOException {
						return socket.getInputStream();
					}

					@Override
					protected void onClose(int code, String message) {
						System.err.println(code + ": " + message);
					}

					@Override
					protected void closeConnection() throws IOException {
						// socket.close();
					}

					@Override
					protected void onMessage(String text) {
						System.out.println(text);
						try {
							send(new StringBuilder(text).reverse().toString());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				return websocket;
			}
		};
		Websocket accept = server.accept();
		accept.run();
	}

}
