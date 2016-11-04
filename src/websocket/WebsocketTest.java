package websocket;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public class WebsocketTest {

	public static void main(String[] args) throws IOException {
		WebsocketServer server = new WebsocketServer(3637) {

			@Override protected Websocket createWebsocket(Socket socket, String uri,
					Map<String, String> headers) {
				System.out.println("opened");
				final Websocket websocket = new Websocket(socket) {

					@Override protected void onMessage(String text) {
						System.out.println(text.length());
					}

					@Override protected void onClose(int code, String message) {
						System.err.println("Connection closed: [" + code + "]" + (message.isEmpty()
								? "" : (" " + message)));
					}
				};
//				new Thread(new Runnable() {
//
//					@Override public void run() {
//						try {
//							Thread.sleep(5000);
//						} catch (InterruptedException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
//						try {
//							websocket.close();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
//					
//				}).start();
				return websocket;
			}
		};
		Websocket websocket = server.accept();
		while (!websocket.receivedClosed()) {
			websocket.handleNextFrame();
		}
	}

}
