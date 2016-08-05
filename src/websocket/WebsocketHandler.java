package websocket;

import java.io.IOException;

import server.HttpRequest;

public abstract class WebsocketHandler {

	protected void onOpen(HttpRequest request) {}
	
	protected void onMessage(String text) {}

	protected void onMessage(byte[] bytes) {}
	
	protected void onClose(int code, String message) {}
	
	protected void onError(IOException e) {}

}
