package websocket;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public abstract class BlockingWebsocket extends Websocket {

	public BlockingWebsocket() {
		this(32);
	}

	public BlockingWebsocket(int capacity) {
		this.queue = new ArrayBlockingQueue<>(capacity);
	}

	private BlockingQueue<String> queue;

	@Override protected void onMessage(String text) {
		queue.add(text);
	}

	@Override protected void onMessage(byte[] bytes) {
		try {
			sendClose(1003, "This websocket only accepts text data.");
		} catch (IOException e) {
			// ???
		}
	}

	public String next() throws InterruptedException {
		return queue.take();
	}

	@Override protected void onClose(int code, String message) {
		queue.add(null);
	}
	
	public static abstract class Message {
		
	}

}
