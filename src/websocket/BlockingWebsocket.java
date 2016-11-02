package websocket;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import websocket.Websocket.Message;

public class BlockingWebsocket extends Websocket2 {

	public BlockingWebsocket(Socket socket) throws IOException {
		this(socket, 32);
	}
	
	public BlockingWebsocket(Socket socket, int queueCapacity) throws IOException {
		super(socket);
		this.messages = new ArrayBlockingQueue<>(queueCapacity);
	}

	private BlockingQueue<Message> messages;

	@Override protected void onMessage(String text) {
		messages.put(new TextMessage(text));
	}

	@Override protected void onMessage(byte[] bytes) {
		messages.put(new BinaryMessage(bytes));
	}

	@Override protected void onClose(int code, String message) {
		messages.put(new CloseMessage(code, message));
	}

	@Override protected void onError(IOException e) {
		messages.put(new ErrorMessage(e));
	}
	
	public Message nextMessage() throws IOException {
		Message message = messages.take();
		
		return message;
	}
	
	private static class Message

}
