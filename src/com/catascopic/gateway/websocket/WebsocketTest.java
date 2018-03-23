package com.catascopic.gateway.websocket;

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
					Map<String, String> headers) throws IOException {
				System.out.println("opened");
				final Websocket websocket =
						new Websocket(socket.getInputStream(), socket.getOutputStream()) {

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
									sendClose(1008, "what the fuck");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							@Override
							protected void onPong(byte[] payload) {
								System.out.println("PONG:" + new String(payload));
							}

							@Override
							protected boolean isClosed() {
								return socket.isClosed();
							}

							@Override
							protected void handleException(IOException e) {
								e.printStackTrace();
							}

							@Override
							protected void handleException(WebsocketProtocolException e) {
								e.printStackTrace();
							}

						};
				return websocket;
			}
		};
		Websocket accept = server.accept();
		accept.run();
	}

}
