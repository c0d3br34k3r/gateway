package com.catascopic.gateway.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.google.common.io.ByteStreams;

public class MessageBuilder {

	protected byte buf[];

	protected int count;

	public MessageBuilder() {
		this(1024);
	}

	public MessageBuilder(int size) {
		buf = new byte[size];
	}

	public void read(InputStream in, int len, byte[] masks) throws IOException {
		ensureCapacity(count + len);
		ByteStreams.readFully(in, buf, count, len);
		for (int i = 0; i < len; i++) {
			buf[count + i] ^= masks[i % 4];
		}
	}

	private void ensureCapacity(int minCapacity) {
		if (minCapacity - buf.length > 0) {
			grow(minCapacity);
		}
	}

	private void grow(int minCapacity) {
		int oldCapacity = buf.length;
		int newCapacity = oldCapacity * 2;
		if (newCapacity - minCapacity < 0) {
			newCapacity = minCapacity;
		}
		if (newCapacity < 0) {
			if (minCapacity < 0) {
				throw new OutOfMemoryError();
			}
			newCapacity = Integer.MAX_VALUE;
		}
		buf = Arrays.copyOf(buf, newCapacity);
	}

	public void reset() {
		count = 0;
	}

	public byte[] toByteArray() {
		return Arrays.copyOf(buf, count);
	}

	@Override public String toString() {
		return new String(buf, 0, count, StandardCharsets.UTF_8);
	}

}
