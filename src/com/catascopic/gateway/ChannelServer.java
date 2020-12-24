package com.catascopic.gateway;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public abstract class ChannelServer<T> {

	public void run() throws IOException {
		Selector selector = Selector.open();
		ServerSocketChannel server = ServerSocketChannel.open();
		server.socket().bind(new InetSocketAddress("localhost", 3637));
		server.register(selector, SelectionKey.OP_ACCEPT);
		server.configureBlocking(false);
		for (;;) {
			selector.select();
			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
			while (keys.hasNext()) {
				SelectionKey key = keys.next();
				if (key.isValid()) {
					if (key.isAcceptable()) {
						accept(server, selector);
					}
					if (key.isReadable()) {
						@SuppressWarnings("unchecked")
						T attachment = (T) key.attachment();
						read(key, attachment);
					}
				}
				keys.remove();
			}
		}
	}

	private void accept(ServerSocketChannel server, Selector selector)
			throws IOException {
		SocketChannel client = server.accept();
		client.configureBlocking(false);
		client.register(selector, SelectionKey.OP_READ, get());
		send(client, "Hello!");
	}

	private static void send(SocketChannel client, String string) throws IOException {
		client.write(ByteBuffer.wrap(string.getBytes(StandardCharsets.UTF_8)));
	}

	private ByteBuffer buf = ByteBuffer.allocate(256);

	protected void read(SelectionKey key, T connection) {
		SocketChannel ch = (SocketChannel) key.channel();
		StringBuilder sb = new StringBuilder();
		
		buf.clear();
		int read = 0;
		while ((read = ch.read(buf)) > 0) {
			ch.read
			buf.flip();
			byte[] bytes = new byte[buf.limit()];
			buf.get(bytes);
			sb.append(new String(bytes));
			buf.clear();
		}
		String msg;
		if (read < 0) {
			msg = key.attachment() + " left the chat.\n";
			ch.close();
		} else {
			msg = key.attachment() + ": " + sb.toString();
		}

		System.out.println(msg);
		broadcast(msg);
	}

	protected abstract T get();

}
